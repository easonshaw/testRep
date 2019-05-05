package io.dcloud.ndtv.wbp.todo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 待办事项-数据库工具类
 */
public class TodoUtils {
	// public static String TODO_AUTOID = "autoid"; // 待办事项id
	// public static String TODO_TITLE = "title"; // 标题
	// public static String TODO_CONTENT = "content"; // 内容
	// public static String TODO_CUSTNAME = "custname"; // 关联客户名称
	// public static String TODO_CUSTNO = "custno"; // 关联客户客编
	// public static String TODO_CREATETIME = "createtime"; // 创建时间
	// public static String TODO_ALARMTIME = "alarmtime"; // 提醒时间
	// public static String TODO_PRIORITY = "priority"; // 优先级 1,2,3
	// public static String TODO_ISDONE = "isdone"; // 是否完成 0,1
	private TodoDbHelper db;
	private SQLiteDatabase dbRead, dbWrite;

	public TodoUtils(Context context) {
		super();
		// TODO Auto-generated constructor stub
		db = new TodoDbHelper(context);
		dbRead = db.getReadableDatabase();
		dbWrite = db.getWritableDatabase();
	}

	// 读取所有, 返回json数组
	public String allTodo() {
		Cursor c = dbRead.query("todo", null, null, null, null, null, "_id desc");
		JSONArray jsonArr = new JSONArray();
		while (c.moveToNext()) {
			String autoid = c.getString(c.getColumnIndex("_id"));
			String title = c.getString(c.getColumnIndex("title"));
			String content = c.getString(c.getColumnIndex("content"));
			String custname = c.getString(c.getColumnIndex("custname"));
			String custno = c.getString(c.getColumnIndex("custno"));
			String createtime = c.getString(c.getColumnIndex("createtime"));
			String alarmtime = c.getString(c.getColumnIndex("alarmtime"));
			String priority = c.getString(c.getColumnIndex("priority"));
			String isdone = c.getString(c.getColumnIndex("isdone"));
			try {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("autoid", autoid);
				jsonObj.put("title", title);
				jsonObj.put("content", content);
				jsonObj.put("custname", custname);
				jsonObj.put("custno", custno);
				jsonObj.put("createtime", createtime);
				jsonObj.put("alarmtime", alarmtime);
				jsonObj.put("priority", priority);
				jsonObj.put("isdone", isdone);
				jsonArr.put(jsonObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jsonArr.toString();
	}

	// 读取所有未完成, 返回json数组
	public String undoneTodo() {
		Cursor c = dbRead.query("todo", null, "isdone=0", null, null, null, "_id");
		JSONArray jsonArr = new JSONArray();
		while (c.moveToNext()) {
			String autoid = c.getString(c.getColumnIndex("_id"));
			String title = c.getString(c.getColumnIndex("title"));
			String content = c.getString(c.getColumnIndex("content"));
			String custname = c.getString(c.getColumnIndex("custname"));
			String custno = c.getString(c.getColumnIndex("custno"));
			String createtime = c.getString(c.getColumnIndex("createtime"));
			String alarmtime = c.getString(c.getColumnIndex("alarmtime"));
			String priority = c.getString(c.getColumnIndex("priority"));
			String isdone = c.getString(c.getColumnIndex("isdone"));
			try {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("autoid", autoid);
				jsonObj.put("title", title);
				jsonObj.put("content", content);
				jsonObj.put("custname", custname);
				jsonObj.put("custno", custno);
				jsonObj.put("createtime", createtime);
				jsonObj.put("alarmtime", alarmtime);
				jsonObj.put("priority", priority);
				jsonObj.put("isdone", isdone);
				jsonArr.put(jsonObj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jsonArr.toString();
	}

	// 读取指定(autoid)
	public String searchTodo(int id) {
		String title = "";
		Cursor c = dbRead.query("todo", null, "_id=" + id, null, null, null, "_id");
		// JSONObject jsonObj = new JSONObject();
		if (c.moveToFirst() != false) { // 查询不为空
			title = c.getString(c.getColumnIndex("title"));
			// String autoid = c.getString(c.getColumnIndex("_id"));
			// String content = c.getString(c.getColumnIndex("content"));
			// String custname = c.getString(c.getColumnIndex("custname"));
			// String custno = c.getString(c.getColumnIndex("custno"));
			// String createtime = c.getString(c.getColumnIndex("createtime"));
			// String alarmtime = c.getString(c.getColumnIndex("alarmtime"));
			// String priority = c.getString(c.getColumnIndex("priority"));
			// String isdone = c.getString(c.getColumnIndex("isdone"));
			// try {
			// jsonObj.put("autoid", autoid);
			// jsonObj.put("title", title);
			// jsonObj.put("content", content);
			// jsonObj.put("custname", custname);
			// jsonObj.put("custno", custno);
			// jsonObj.put("createtime", createtime);
			// jsonObj.put("alarmtime", alarmtime);
			// jsonObj.put("priority", priority);
			// jsonObj.put("isdone", isdone);
			// } catch (JSONException e) {
			// e.printStackTrace();
			// }
			// System.out.println("==========" + jsonObj.toString());
		}
		return title;
	}

	// 新增, 返回autoid
	public String addTodo(Context context, String title, String content, String custname, String custno, String createtime,
			String alarmtime, String priority, String isdone) {
		ContentValues cv = new ContentValues();
		cv.put("title", title);
		cv.put("content", content);
		cv.put("custname", custname);
		cv.put("custno", custno);
		cv.put("createtime", createtime);
		cv.put("alarmtime", alarmtime);
		cv.put("priority", priority);
		cv.put("isdone", isdone);
		long insert = dbWrite.insert("todo", null, cv);

		if (insert != -1 && !alarmtime.equals(""))
			addAlarm(context, alarmtime, (int) insert);
		return String.valueOf(insert);
	}

	// 删除
	public boolean deleteTodo(Context context, String id) {
		cancelAlarm(context, Integer.parseInt(id)); // 取消提醒
		int delete = dbWrite.delete("todo", "_id=" + id, null);
		if (delete > 0)
			return true;
		else
			return false;
	}

	// 编辑
	public boolean updateTodo(Context context, String id, String title, String content, String custname, String custno, String createtime,
			String alarmtime, String priority, String isdone) {
		if (alarmtime.equals(""))
			cancelAlarm(context, Integer.parseInt(id));
		else
			addAlarm(context, alarmtime, Integer.parseInt(id));

		ContentValues cv = new ContentValues();
		cv.put("title", title);
		cv.put("content", content);
		cv.put("custname", custname);
		cv.put("custno", custno);
		cv.put("createtime", createtime);
		cv.put("alarmtime", alarmtime);
		cv.put("priority", priority);
		cv.put("isdone", isdone);
		int update = dbWrite.update("todo", cv, "_id=" + id, null);
		if (update > 0)
			return true;
		else
			return false;
	}

	// 设置成已完成
	public boolean doneTodo(Context context, String id) {
		ContentValues cv = new ContentValues();
		cv.put("isdone", 1);
		cancelAlarm(context, Integer.parseInt(id)); // 取消提醒
		int update = dbWrite.update("todo", cv, "_id=" + id, null);
		if (update > 0)
			return true;
		else
			return false;
	}

	// 添加提醒
	public void addAlarm(Context context, String strTime, int flag) {
		Long alarmMills = dataOne(strTime);
		Intent intent = new Intent("TODO_ALARM_CLOCK");
		intent.putExtra("intFlag", flag);
		PendingIntent pi = PendingIntent.getBroadcast(context, flag, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, alarmMills, pi);
	}

	// 取消提醒
	public void cancelAlarm(Context context, int flag) {
		Intent intent = new Intent("TODO_ALARM_CLOCK");
		intent.putExtra("intFlag", flag);
		PendingIntent pi = PendingIntent.getBroadcast(context, flag, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		am.cancel(pi);
	}

	// 调此方法输入所要转换的时间输入例如（"yyyy-MM-dd HH:mm:ss"）返回时间戳
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
