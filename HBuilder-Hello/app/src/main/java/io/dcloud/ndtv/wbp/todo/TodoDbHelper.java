package io.dcloud.ndtv.wbp.todo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ��������SQLite���ݿ����
 */
public class TodoDbHelper extends SQLiteOpenHelper {
	public TodoDbHelper(Context context) {
		// context, name, factory, version
		super(context, "todoDB", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE todo(_id INTEGER PRIMARY KEY AUTOINCREMENT," // ��������id
				+ " title TEXT DEFAULT \"\"," // ����
				+ " content TEXT DEFAULT \"\"," // ����
				+ " custname TEXT DEFAULT \"\"," // �����ͻ�����
				+ " custno TEXT DEFAULT \"\"," // �����ͻ��ͱ�
				+ " createtime TEXT DEFAULT \"\"," // ����ʱ��
				+ " alarmtime TEXT DEFAULT \"\"," // ����ʱ��
				+ " priority TEXT DEFAULT \"0\", " // ���ȼ� 1,2,3
				+ " isdone TEXT DEFAULT \"0\" " // �Ƿ���� 0,1
				+ ")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

}
