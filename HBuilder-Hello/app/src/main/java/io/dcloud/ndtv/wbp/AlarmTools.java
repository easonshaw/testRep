package io.dcloud.ndtv.wbp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class AlarmTools {
	public void addAlarm(Context context, String strTime, String cust, String desc) {
		Long alarmMills = dataOne(strTime);
		int intFlag = (int) (Math.random() * 10000);
		Intent intent = new Intent("ALARM_CLOCK");
		intent.putExtra("intFlag", intFlag);
		intent.putExtra("cust", cust);
		intent.putExtra("desc", desc);
		PendingIntent pi = PendingIntent.getBroadcast(context, intFlag, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, alarmMills, pi);
		System.out.println("===========" + alarmMills);
	}

	/**
	 * 调此方法输入所要转换的时间输入例如（"yyyy-MM-dd HH:mm:ss"）返回时间戳
	 */
	public static Long dataOne(String time) {
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
