package io.dcloud.ndtv.wbp.todo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * 开机监听, 将未过期的定时添加到系统定时器中
 */
public class TodoBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		TodoUtils todoUtils = new TodoUtils(context);
		String undoneTodo = todoUtils.undoneTodo(); // 所有未完成的
		try {
			JSONArray jsonArr = new JSONArray(undoneTodo);
			if (jsonArr.length() > 0) {
				for (int i = 0; i < jsonArr.length(); i++) {
					JSONObject jsonObj = jsonArr.getJSONObject(i);
					int autoid = jsonObj.getInt("autoid");
					String alarmtime = jsonObj.getString("alarmtime");
					if (!TextUtils.isEmpty(alarmtime)) { // 有提醒的
						Long alarmMills = dataOne(alarmtime);
						if (alarmMills > System.currentTimeMillis()) { // 日期大于当前
							addAlarm(context, alarmMills, autoid);
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 添加提醒
	private void addAlarm(Context context, Long alarmMills, int flag) {
		Intent intent = new Intent("TODO_ALARM_CLOCK");
		intent.putExtra("intFlag", flag);
		PendingIntent pi = PendingIntent.getBroadcast(context, flag, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, alarmMills, pi);
	}

	// 调此方法输入所要转换的时间输入例如（"yyyy-MM-dd HH:mm:ss"）返回时间戳
	private static Long dataOne(String time) {
		SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		Date date;
		long l = 0l;
		try {
			date = sdr.parse(time);
			l = date.getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}

}
