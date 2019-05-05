package io.dcloud.ndtv.wbp.todo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 待办事项SQLite数据库操作
 */
public class TodoDbHelper extends SQLiteOpenHelper {
	public TodoDbHelper(Context context) {
		// context, name, factory, version
		super(context, "todoDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE todo(_id INTEGER PRIMARY KEY AUTOINCREMENT," // 待办事项id
				+ " title TEXT DEFAULT \"\"," // 标题
				+ " content TEXT DEFAULT \"\"," // 内容
				+ " custname TEXT DEFAULT \"\"," // 关联客户名称
				+ " custno TEXT DEFAULT \"\"," // 关联客户客编
				+ " createtime TEXT DEFAULT \"\"," // 创建时间
				+ " alarmtime TEXT DEFAULT \"\"," // 提醒时间
				+ " priority TEXT DEFAULT \"0\", " // 优先级 1,2,3
				+ " isdone TEXT DEFAULT \"0\" " // 是否完成 0,1
				+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

}
