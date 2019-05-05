package io.dcloud.ndtv.wbp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// int intFlag = intent.getIntExtra("intFlag", 0);
		int intFlag = intent.getIntExtra("intFlag", 0);
		String cust = intent.getStringExtra("cust");
		String desc = intent.getStringExtra("desc");
		System.out.println("===========================" + intFlag);

		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.icon);
		builder.setContentTitle(cust + "��ʱ����");
		builder.setContentText(desc);
		Notification notification = builder.build();

		Intent intent2 = new Intent();
		intent2.setClass(context, io.dcloud.PandoraEntry.class);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(context, intFlag, intent2, PendingIntent.FLAG_ONE_SHOT);
		notification.contentIntent = pi;

		notification.defaults = Notification.DEFAULT_ALL; // ʹ��Ĭ�����ã������������𶯡�����
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// notification.defaults |= Notification.DEFAULT_SOUND; // ����
		// notification.defaults |= Notification.DEFAULT_VIBRATE; // ��
		notifyManager.notify(intFlag, notification);
	}

}
