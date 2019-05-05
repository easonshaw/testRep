package io.dcloud.ndtv.wbp.todo;

import io.dcloud.ndtv.wbp.R;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

public class TodoReceiver extends BroadcastReceiver {
	private TodoUtils todoUtils;

	@Override
	public void onReceive(Context context, Intent intent) {
		todoUtils = new TodoUtils(context);
		int intFlag = intent.getIntExtra("intFlag", 0);
		String title = todoUtils.searchTodo(intFlag);
		// System.out.println("===========================" + intFlag);

		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.icon);
		builder.setContentTitle("����Ӫ��-��������");
		builder.setContentText(title);
		Notification notification = builder.build();

		Intent intent2 = new Intent();
		// intent2.setClass(context, io.dcloud.PandoraEntry.class);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(context, intFlag, intent2, PendingIntent.FLAG_ONE_SHOT);
		notification.contentIntent = pi;

		notification.defaults = Notification.DEFAULT_ALL; // ʹ��Ĭ�����ã������������𶯡�����
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notifyManager.notify(intFlag, notification);

		// �����Ի���
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setTitle("����Ӫ��-��������").setMessage(title).setNegativeButton("ȷ��", null);
		AlertDialog dialog = b.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}

}
