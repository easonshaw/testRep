package org.ddpush.client.demo.udp.service;

import io.dcloud.ndtv.wbp.R;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Map;

import org.ddpush.client.demo.udp.DateTimeUtil;
import org.ddpush.client.demo.udp.MainActivity;
import org.ddpush.client.demo.udp.Params;
import org.ddpush.client.demo.udp.Util;
import org.ddpush.client.demo.udp.receiver.TickAlarmReceiver;
import org.ddpush.im.v1.client.appuser.Message;
import org.ddpush.im.v1.client.appuser.UDPClientBase;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class OnlineService extends Service {
	public static final String DB_NAME = "msgdata.db";
	private SQLiteDatabase db;
	protected PendingIntent tickPendIntent;
	protected TickAlarmReceiver tickAlarmReceiver = new TickAlarmReceiver();
	WakeLock wakeLock;
	MyUdpClient myUdpClient;
	Notification n;
	public static MediaPlayer player = null;
	int soundCount; // ���Ŵ���
	
	public class MyUdpClient extends UDPClientBase {
		public MyUdpClient(byte[] uuid, int appid, String serverAddr, int serverPort)
				throws Exception {
			super(uuid, appid, serverAddr, serverPort);
		}

		@Override
		public boolean hasNetworkConnection() {
			return Util.hasNetwork(OnlineService.this);
		}

		@Override
		public void trySystemSleep() {
			tryReleaseWakeLock();
		}

		@Override
		public void onPushMessage(Message message) {
			if(message == null){
				return;
			}
			if(message.getData() == null || message.getData().length == 0){
				return;
			}
			if(message.getCmd() == 16){// 0x10 ͨ��������Ϣ
				notifyUser(16,"DDPushͨ��������Ϣ","ʱ�䣺"+DateTimeUtil.getCurDateTime(),"�յ�ͨ��������Ϣ");
			}
			if(message.getCmd() == 17){// 0x11 ����������Ϣ
				long msg = ByteBuffer.wrap(message.getData(), 5, 8).getLong();
				notifyUser(17,"DDPush����������Ϣ",""+msg,"�յ�ͨ��������Ϣ");
			}
			if(message.getCmd() == 32){// 0x20 �Զ���������Ϣ
				String str = null;
				try {
					str = new String(message.getData(), 5, message.getContentLength(), "UTF-8");
				} catch (Exception e) {
					str = Util.convert(message.getData(), 5, message.getContentLength());
				}
				Gson gson = new Gson();
				Type type = new TypeToken<Map<String, Object>>() {
				}.getType();
				Map<String, Object> map = gson.fromJson(str, type);
				String from = map.get("from").toString();
				String fromname = map.get("fromname").toString();
				String to = map.get("to").toString();
				String toname = map.get("toname").toString();
				String content = map.get("content").toString();
				String time = map.get("time").toString();
				if (!from.toUpperCase().equals("CCPS"))
					InsertData(from, fromname, to, toname, content, time, 0);
				// �Ƿ�ѭ������
				SharedPreferences sp = getSharedPreferences(Params.DEFAULT_PRE_NAME, MODE_PRIVATE);
				boolean isLooping = sp.getBoolean(Params.ALARM_LOOPING, false);
				// ���Ϳ���
				boolean pushable = sp.getBoolean("pushable", true); // Ĭ��(�״ε�½)Ϊ����
				if (pushable) {
					int intFlag = (int) (Math.random() * 10000);
				//	notifyUser(intFlag, map, isLooping);
					notifyUser(intFlag, map, false); // �ر�ѭ����ֻ��һ��
				}
				SendBroadcast(str);
			}
			setPkgsInfo();
		}
	}

	public OnlineService() {
	}

	// ��ʼ��
	@Override
	public void onCreate() {
		super.onCreate();
		this.setTickAlarm();
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OnlineService");
		resetClient();
		//notifyRunning();
		OpenCreateDB();
	}
	
	private void SendBroadcast(String data)
	{
		Intent intent = new Intent();
		intent.putExtra("msg", data);
		intent.setAction("org.ddpush.client.demo.org.ddpush.client.demo.udp.service");
		sendBroadcast(intent);
	}
	
	public void OpenCreateDB(){  
        db = openOrCreateDatabase(DB_NAME, this.MODE_PRIVATE, null);  
        //db.execSQL("DROP TABLE IF EXISTS Msg");    
        db.execSQL("CREATE TABLE IF NOT EXISTS Msg (_id INTEGER PRIMARY KEY AUTOINCREMENT, _from VARCHAR, _fromname VARCHAR, _to VARCHAR, _toname VARCHAR, _content VARCHAR, _time VARCHAR, _isRead INTEGER)");  
    }
	
	 public void InsertData(String from,String fromname,String to,String toname,String content,String time,int isRead){   
	        ContentValues cvOfMsg = new ContentValues();  
	        cvOfMsg.put("_from", from);
	        cvOfMsg.put("_fromname", fromname);
	        cvOfMsg.put("_to", to);
	        cvOfMsg.put("_toname", toname);
	        cvOfMsg.put("_content", content);
	        cvOfMsg.put("_time", time);
	        cvOfMsg.put("_isRead", isRead);
	        db.insert("Msg", null, cvOfMsg);
	    }  
	 
	@Override
	public int onStartCommand(Intent param, int flags, int startId) {
		if(param == null){
			return START_STICKY;
		}
		String cmd = param.getStringExtra("CMD");
		if(cmd == null){
			cmd = "";
		}
		if(cmd.equals("TICK")){
			if(wakeLock != null && wakeLock.isHeld() == false){
				wakeLock.acquire();
			}
		}
		if(cmd.equals("RESET")){
			if(wakeLock != null && wakeLock.isHeld() == false){
				wakeLock.acquire();
			}
			resetClient();
		}
		if(cmd.equals("TOAST")){
			String text = param.getStringExtra("TEXT");
			if(text != null && text.trim().length() != 0){
				Toast.makeText(this, text, Toast.LENGTH_LONG).show();
			}
		}
		
		setPkgsInfo();

		return START_STICKY;
	}
	
	protected void setPkgsInfo(){
		if(this.myUdpClient == null){
			return;
		}
		long sent = myUdpClient.getSentPackets();
		long received = myUdpClient.getReceivedPackets();
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = account.edit();
		editor.putString(Params.SENT_PKGS, ""+sent);
		editor.putString(Params.RECEIVE_PKGS, ""+received);
		editor.commit();
	}
	
	protected void resetClient(){
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		String serverIp = account.getString(Params.SERVER_IP, "");
		String serverPort = account.getString(Params.SERVER_PORT, "");
		String pushPort = account.getString(Params.PUSH_PORT, "");
		String userName = account.getString(Params.USER_NAME, "");
		if(serverIp == null || serverIp.trim().length() == 0
				|| serverPort == null || serverPort.trim().length() == 0
				|| pushPort == null || pushPort.trim().length() == 0
				|| userName == null || userName.trim().length() == 0){
			return;
		}
		if(this.myUdpClient != null){
			try{myUdpClient.stop();}catch(Exception e){}
		}
		try{
			myUdpClient = new MyUdpClient(Util.md5Byte(userName), 1, serverIp, Integer.parseInt(serverPort));
			myUdpClient.setHeartbeatInterval(50);
			myUdpClient.start();
			SharedPreferences.Editor editor = account.edit();
			editor.putString(Params.SENT_PKGS, "0");
			editor.putString(Params.RECEIVE_PKGS, "0");
			editor.commit();
		}catch(Exception e){
			//Toast.makeText(this.getApplicationContext(), "����ʧ�ܣ�"+e.getMessage(), Toast.LENGTH_LONG).show();
		}
		//Toast.makeText(this.getApplicationContext(), "ddpush���ն�����", Toast.LENGTH_LONG).show();
	}
	
	protected void tryReleaseWakeLock(){
		if(wakeLock != null && wakeLock.isHeld() == true){
			wakeLock.release();
		}
	}
	
	protected void setTickAlarm(){
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);  
		Intent intent = new Intent(this,TickAlarmReceiver.class);
		int requestCode = 0;  
		tickPendIntent = PendingIntent.getBroadcast(this,  
		requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
		//С��2s��MIUI����ϵͳ��Ŀǰ��̹㲥���Ϊ5���ӣ�����5���ӵ�alarm��ȵ�5�����ٴ�����2014-04-28
		long triggerAtTime = System.currentTimeMillis();
		int interval = 300 * 1000;  
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
	}
	
	protected void cancelTickAlarm(){
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(tickPendIntent);  
	}
	
	protected void notifyRunning(){
		NotificationManager notificationManager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		n = new Notification();  
		Intent intent = new Intent(this,MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_ONE_SHOT);
		n.contentIntent = pi;
//		n.setLatestEventInfo(this, "DDPushDemoUDP", "��������", pi);
		//n.defaults = Notification.DEFAULT_ALL;
		//n.flags |= Notification.FLAG_SHOW_LIGHTS;  
		//n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		n.flags |= Notification.FLAG_NO_CLEAR;
		//n.iconLevel = 5;
		           
		n.icon = R.drawable.icon;  
		n.when = System.currentTimeMillis();
		n.tickerText = "DDPushDemoUDP��������";
		notificationManager.notify(0, n);
	}
	
	protected void cancelNotifyRunning(){
		NotificationManager notificationManager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}
	public void notifyUser(int id, String title, String content, String tickerText){
		NotificationManager notificationManager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification();  
		Intent intent = new Intent(this,MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_ONE_SHOT);
		n.contentIntent = pi;

//		n.setLatestEventInfo(this, title, content, pi);
		n.defaults = Notification.DEFAULT_ALL;
		n.flags |= Notification.FLAG_SHOW_LIGHTS;  
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.largeIcon=((BitmapDrawable)(getResources().getDrawable(R.drawable.icon))).getBitmap();
		n.icon = R.drawable.icon;  
		n.when = System.currentTimeMillis();
		n.tickerText = tickerText;
		notificationManager.notify(id, n);
	}

	public void notifyUser(int id, Map<String, Object> data , boolean loopingflag) {
		NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification();
		String _from = data.get("from").toString();
		// String _fromname = data.get("fromname").toString();

		Intent intentClick = new Intent(this, NotificationReceiver.class);
		intentClick.setAction("notification_clicked");
		intentClick.putExtra(NotificationReceiver.TYPE, 1);
		intentClick.putExtra("from", _from);
		PendingIntent pendingIntentClick = PendingIntent.getBroadcast(this, 0, intentClick, PendingIntent.FLAG_ONE_SHOT);

		Intent intentCancel = new Intent(this, NotificationReceiver.class);
		intentCancel.setAction("notification_cancelled");
		intentCancel.putExtra(NotificationReceiver.TYPE, 2);
		PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this, 0, intentCancel, PendingIntent.FLAG_ONE_SHOT);

		n.contentIntent = pendingIntentClick;
		n.deleteIntent = pendingIntentCancel;
//		n.setLatestEventInfo(this, data.get("fromname").toString(), data.get("content").toString(), pendingIntentClick);
		n.defaults = Notification.DEFAULT_ALL;
		n.flags |= Notification.FLAG_SHOW_LIGHTS;
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.largeIcon = ((BitmapDrawable) (getResources().getDrawable(R.drawable.icon))).getBitmap();
		n.icon = R.drawable.icon;
		n.when = System.currentTimeMillis();
		n.tickerText = data.get("fromname").toString() + "����һ����Ϣ";
		notificationManager.notify(id, n);
		

// ��Ҫ��������Ĭ�ϵ�
//		try {
//			if (player == null || (player != null && !player.isPlaying())) {
//				System.out.println("===============================================");
//				player = MediaPlayer.create(this, R.raw.notificationalarm);
//				player.setVolume(1f, 1f);// �������ڣ�����������Ϊ���ֵ
//
//				if (loopingflag) { // �Ƿ�ѭ��
//					player.setLooping(true);// ��������ѭ��
//					player.start();
//				} else { // ���������3��
//					soundCount = 2;
//					player.setOnCompletionListener(new OnCompletionListener() {
//						@Override
//						public void onCompletion(MediaPlayer mp) {
//							if (soundCount > 0) {
//								player.start();
//								soundCount--;
//							} else {
//								player.release();
//								player = null;
//							}
//						}
//					});
//					player.start();
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static class NotificationReceiver extends BroadcastReceiver {
		public static final String TYPE = "type"; // ���type��Ϊ��Notification������Ϣ�ģ���������׵����ѿ���ȥ���ѣ��ܶ�

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int type = intent.getIntExtra(TYPE, -1);
			String _from = intent.getStringExtra("from");
			if (type != -1) {
				NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(type);
			}
			// �������¼�
			if (action.equals("notification_clicked")) {
				if (player != null && player.isPlaying()) {
					try {
						//player.stop();
						//player.prepare();
						player.release();
						player = null;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (_from.toUpperCase().equals("CCPS")) {
					Intent intent2 = new Intent();
					intent2.setClass(context, io.dcloud.PandoraEntry.class);
					// intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent2);
				}
			}
			// ����������͵��ɾ���¼�
			if (action.equals("notification_cancelled")) {
				if (player != null && player.isPlaying()) {
					try {
						//player.stop();
						//player.prepare();
						player.release();
						player = null;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//this.cancelTickAlarm();
		cancelNotifyRunning();
		this.tryReleaseWakeLock();
		if (player != null && player.isPlaying()) {
			//player.stop();
			player.release();
			player = null;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
