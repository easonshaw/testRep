package io.dcloud.ndtv.wbp.location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ddpush.client.demo.udp.Params;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

public class LocationService extends Service {
	private final Timer timer = new Timer();
	LocationManager locationManager = null;
	private SharedPreferences sp;
	private String sessionId;

	// 定义并初始化定时器任务
	private TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			// 时间判断：早8点-晚9点之间才定位
			Calendar calendar = Calendar.getInstance();
			int i = calendar.get(Calendar.HOUR_OF_DAY);
			if (i < 8 || i > 21)
				return;

			String lngAndLat = "0.0,0.0";
			try {
				lngAndLat = getLngAndLat(LocationService.this);
			} catch (Exception e1) {
			}
			// lngAndLat = "1,1";
			if (lngAndLat != null) { // 上传定位
				NameValuePair pair1 = new BasicNameValuePair("func", "addClientuserLocation");
				NameValuePair pair2 = new BasicNameValuePair("args", "{\"sessionId\":\"" + sessionId + "\",\"location\":\"" + lngAndLat
						+ "\"}");
				List<NameValuePair> pairList = new ArrayList<NameValuePair>();
				pairList.add(pair1);
				pairList.add(pair2);
				try {
					HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
					HttpPost httpPost = new HttpPost(Params.LOCATION_URL);
					httpPost.setEntity(requestHttpEntity);
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(httpPost);
					// showResponseResult(response);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("============" + System.currentTimeMillis());
			System.out.println("============" + lngAndLat);
		}
	};

	@Override
	public void onCreate() {// 初始化
		super.onCreate();
		sp = getSharedPreferences(Params.DEFAULT_PRE_NAME, MODE_PRIVATE);
		sessionId = sp.getString(Params.USER_SESSIONID, "");
		if (!TextUtils.isEmpty(sessionId)) {
			// 定时任务：延迟0秒执行,周期为30分钟
			timer.schedule(timerTask, 0, 30 * 60 * 1000);
		}
	}

	// 获取经纬度
	private String getLngAndLat(Context context) throws Exception {
		double latitude = 0.0;
		double longitude = 0.0;
		if (locationManager == null)
			locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null
				|| locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
			if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { // 默认用网络定位
				if (Looper.myLooper() == null) {
					Looper.prepare();
				}
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, locationListener);
				Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				// Looper.loop();

				if (location != null) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				} else
					return null;

			} else {
				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				} else
					return null;
			}

		} else {// 无法定位：1、提示用户打开定位服务；2、跳转到设置界面
			Toast.makeText(this, "定位失败，请设置-打开定位服务", Toast.LENGTH_SHORT).show();
			Intent i = new Intent();
			i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(i);
		}
		if (longitude != 0.0 || latitude != 0.0) {
			double[] gps84_To_bd09 = GPSUtil.gps84_To_bd09(latitude, longitude);
			latitude = gps84_To_bd09[0];
			longitude = gps84_To_bd09[1];
		}
		return longitude + "," + latitude;
	}

	LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		timer.cancel();

		super.onDestroy();
	}
}
