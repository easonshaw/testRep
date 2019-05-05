package org.ddpush.client.demo.udp;

import io.dcloud.ndtv.wbp.R;

import org.ddpush.client.demo.udp.service.OnlineService;
import org.ddpush.im.v1.client.appserver.Pusher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	private EditText serverIp;
	private EditText serverPort;
	private EditText pushPort;
	private EditText userName;
	private Button startBtn;
	
	private EditText targetUserName;
	private EditText send0x11Data;
	private EditText send0x20Data;
	private Button send0x10Btn;
	private Button send0x11Btn;
	private Button send0x20Btn;
	
	private Handler handler;
	private Runnable refresher;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_push);
		
		refresher = new Runnable(){
			public void run(){
				MainActivity.this.freshCurrentInfo();
			}
		};
		handler = new Handler();
		handler.postDelayed(refresher, 1000);
		
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		serverIp = (EditText)findViewById(R.id.demo_server_ip);
		serverIp.setText(account.getString(Params.SERVER_IP, ""));
		
		serverPort = (EditText)findViewById(R.id.demo_server_port);
		serverPort.setText(account.getString(Params.SERVER_PORT, "9966"));
		
		pushPort = (EditText)findViewById(R.id.demo_push_port);
		pushPort.setText(account.getString(Params.PUSH_PORT, "9999"));
		
		userName = (EditText)findViewById(R.id.demo_user_name);
		userName.setText(account.getString(Params.USER_NAME, ""));
		
		startBtn = (Button)findViewById(R.id.demo_start_button);
		startBtn.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	MainActivity.this.start();
		    	
		    }
		});
		
		targetUserName = (EditText)findViewById(R.id.demo_target_user_name);
		send0x11Data = (EditText)findViewById(R.id.demo_send_0x11_data);
		send0x20Data = (EditText)findViewById(R.id.demo_send_0x20_data);
		targetUserName = (EditText)findViewById(R.id.demo_target_user_name);
		send0x10Btn = (Button)findViewById(R.id.demo_send_0x10_button);
		send0x10Btn.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	MainActivity.this.send0x10();
		    	
		    }
		});
		send0x11Btn = (Button)findViewById(R.id.demo_send_0x11_button);
		send0x11Btn.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	MainActivity.this.send0x11();
		    	
		    }
		});
		send0x20Btn = (Button)findViewById(R.id.demo_send_0x20_button);
		send0x20Btn.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	MainActivity.this.send0x20();
		    	
		    }
		});
		
		Intent startSrv = new Intent(this.getApplicationContext(), OnlineService.class);
		this.getApplicationContext().startService(startSrv);
	}
	
	protected void start(){
		if(serverIp.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "�����������ip", Toast.LENGTH_SHORT).show();
    		serverIp.requestFocus();
    		return;
		}
		if(serverPort.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "������������˿�", Toast.LENGTH_SHORT).show();
			serverPort.requestFocus();
    		return;
		}
		if(pushPort.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "���������Ͷ˿�", Toast.LENGTH_SHORT).show();
			pushPort.requestFocus();
    		return;
		}
		
		if(userName.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "�������û���", Toast.LENGTH_SHORT).show();
			userName.requestFocus();
    		return;
		}
		int intServerPort = 0, intPushPort = 0;
		try{
			intServerPort = Integer.parseInt(serverPort.getText().toString());
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "�˿ڸ�ʽ����", Toast.LENGTH_SHORT).show();
			serverPort.requestFocus();
    		return;
		}
		try{
			intPushPort = Integer.parseInt(pushPort.getText().toString());
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "�˿ڸ�ʽ����", Toast.LENGTH_SHORT).show();
			pushPort.requestFocus();
    		return;
		}
		//Toast.makeText(this.getApplicationContext(), "��ʼ", Toast.LENGTH_SHORT).show();
		saveAccountInfo();
		Intent startSrv = new Intent(this, OnlineService.class);
		startSrv.putExtra("CMD", "RESET");
		this.startService(startSrv);
		freshCurrentInfo();
	}

	protected void send0x10(){
		if(targetUserName.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "������Ŀ���û���", Toast.LENGTH_SHORT).show();
			targetUserName.requestFocus();
			return;
		}
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		String serverIp = account.getString(Params.SERVER_IP, "");
		String pushPort = account.getString(Params.PUSH_PORT, "");
		int port;
		try{
			port = Integer.parseInt(pushPort);
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "���Ͷ˿ڸ�ʽ����"+pushPort, Toast.LENGTH_SHORT).show();
			return;
		}
		byte[] uuid = null;
		try{
			uuid = Util.md5Byte(targetUserName.getText().toString());
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "����"+e.getMessage(), Toast.LENGTH_SHORT).show();
			targetUserName.requestFocus();
			return;
		}
		Thread t = new Thread(new send0x10Task(this,serverIp,port,uuid));
		t.start();
		
	}
	protected void send0x11(){
		if(targetUserName.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "������Ŀ���û���", Toast.LENGTH_SHORT).show();
			targetUserName.requestFocus();
			return;
		}
		
		if(send0x11Data.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "������������Ϊ������Ϣ", Toast.LENGTH_SHORT).show();
			send0x11Data.requestFocus();
			return;
		}
		if("0".equals(send0x11Data.getText().toString().trim())){
			Toast.makeText(this.getApplicationContext(), "���ֱ������", Toast.LENGTH_SHORT).show();
			send0x11Data.requestFocus();
			return;
		}
		long msg;
		try{
			msg = Long.parseLong(send0x11Data.getText().toString().trim());
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "���ָ�ʽ����", Toast.LENGTH_SHORT).show();
			send0x11Data.requestFocus();
			return;
		}
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		String serverIp = account.getString(Params.SERVER_IP, "");
		String pushPort = account.getString(Params.PUSH_PORT, "");
		int port;
		try{
			port = Integer.parseInt(pushPort);
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "���Ͷ˿ڸ�ʽ����"+pushPort, Toast.LENGTH_SHORT).show();
			return;
		}
		byte[] uuid = null;
		try{
			uuid = Util.md5Byte(targetUserName.getText().toString());
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "����"+e.getMessage(), Toast.LENGTH_SHORT).show();
			targetUserName.requestFocus();
			return;
		}
		Thread t = new Thread(new send0x11Task(this,serverIp,port,uuid,msg));
		t.start();
	}
	protected void send0x20(){
		if(targetUserName.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "������Ŀ���û���", Toast.LENGTH_SHORT).show();
			targetUserName.requestFocus();
			return;
		}
		
		if(send0x20Data.getText().toString().length() == 0){
			Toast.makeText(this.getApplicationContext(), "������һ������", Toast.LENGTH_SHORT).show();
			send0x20Data.requestFocus();
			return;
		}

		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		String serverIp = account.getString(Params.SERVER_IP, "");
		String pushPort = account.getString(Params.PUSH_PORT, "");
		int port;
		try{
			port = Integer.parseInt(pushPort);
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "���Ͷ˿ڸ�ʽ����"+pushPort, Toast.LENGTH_SHORT).show();
			return;
		}
		byte[] uuid = null;
		try{
			uuid = Util.md5Byte(targetUserName.getText().toString());
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "����"+e.getMessage(), Toast.LENGTH_SHORT).show();
			targetUserName.requestFocus();
			return;
		}
		byte[] msg = null;
		try{
			msg = send0x20Data.getText().toString().getBytes("UTF-8");
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "����"+e.getMessage(), Toast.LENGTH_SHORT).show();
			send0x20Data.requestFocus();
			return;
		}
		Thread t = new Thread(new send0x20Task(this,serverIp,port,uuid,msg));
		t.start();
	}
	
	protected void saveAccountInfo(){
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = account.edit();
		editor.putString(Params.SERVER_IP, serverIp.getText().toString());
		editor.putString(Params.SERVER_PORT, serverPort.getText().toString());
		editor.putString(Params.PUSH_PORT, pushPort.getText().toString());
		editor.putString(Params.USER_NAME, userName.getText().toString());
		editor.putString(Params.SENT_PKGS, "0");
		editor.putString(Params.RECEIVE_PKGS, "0");
		editor.commit();
		
	}
	
	protected void freshCurrentInfo(){
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		String serverIp = account.getString(Params.SERVER_IP, "");
		String serverPort = account.getString(Params.SERVER_PORT, "");
		String pushPort = account.getString(Params.PUSH_PORT, "");
		String userName = account.getString(Params.USER_NAME, "");
		String sentPkgs = account.getString(Params.SENT_PKGS, "0");
		String receivePkgs = account.getString(Params.RECEIVE_PKGS, "0");
		String uuid = null;
		
		try{
			uuid = Util.md5(userName);
		}catch(Exception e){
			uuid = "";
		}
		if(userName == null || userName.length() == 0){
			uuid="";
		}
		((TextView)findViewById(R.id.demo_cur_server_ip)).setText(serverIp);
		((TextView)findViewById(R.id.demo_cur_server_ip)).postInvalidate();
		
		((TextView)findViewById(R.id.demo_cur_server_port)).setText(serverPort);
		((TextView)findViewById(R.id.demo_cur_server_port)).postInvalidate();
		
		((TextView)findViewById(R.id.demo_cur_push_port)).setText(pushPort);
		((TextView)findViewById(R.id.demo_cur_push_port)).postInvalidate();
		
		((TextView)findViewById(R.id.demo_cur_user_name)).setText(userName);
		((TextView)findViewById(R.id.demo_cur_user_name)).postInvalidate();
		
		((TextView)findViewById(R.id.demo_cur_uuid)).setText(uuid);
		((TextView)findViewById(R.id.demo_cur_uuid)).postInvalidate();
		
		((TextView)findViewById(R.id.demo_cur_sent_pkgs)).setText(sentPkgs);
		((TextView)findViewById(R.id.demo_cur_sent_pkgs)).postInvalidate();
		
		((TextView)findViewById(R.id.demo_cur_receive_pkgs)).setText(receivePkgs);
		((TextView)findViewById(R.id.demo_cur_receive_pkgs)).postInvalidate();
		
		try{
		}catch(Exception e){}
	}

	@Override
	protected void onResume() {
		freshCurrentInfo();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		handler.removeCallbacks(refresher);
		super.onDestroy();
	}

	class send0x10Task implements Runnable{
		private Context context;
		private String serverIp;
		private int port;
		private byte[] uuid;
		
		public send0x10Task(Context context, String serverIp, int port, byte[] uuid){
			this.context = context;
			this.serverIp = serverIp;
			this.port = port;
			this.uuid = uuid;
		}
		
		public void run(){
			Pusher pusher = null;
			Intent startSrv = new Intent(context, OnlineService.class);
			startSrv.putExtra("CMD", "TOAST");
			try{
				boolean result;
				pusher = new Pusher(serverIp,port, 1000*5);
				result = pusher.push0x10Message(uuid);
				if(result){
					startSrv.putExtra("TEXT", "ͨ����Ϣ���ͳɹ�");
				}else{
					startSrv.putExtra("TEXT", "����ʧ�ܣ���ʽ����");
				}
			}catch(Exception e){
				e.printStackTrace();
				startSrv.putExtra("TEXT", "����ʧ�ܣ�"+e.getMessage());
			}finally{
				if(pusher != null){
					try{pusher.close();}catch(Exception e){};
				}
			}
			context.startService(startSrv);
		}
	}
	
	class send0x11Task implements Runnable{
		private Context context;
		private String serverIp;
		private int port;
		private byte[] uuid;
		private long msg;
		
		public send0x11Task(Context context, String serverIp, int port, byte[] uuid, long msg){
			this.context = context;
			this.serverIp = serverIp;
			this.port = port;
			this.uuid = uuid;
			this.msg = msg;
		}
		
		public void run(){
			Pusher pusher = null;
			Intent startSrv = new Intent(context, OnlineService.class);
			startSrv.putExtra("CMD", "TOAST");
			try{
				boolean result;
				pusher = new Pusher(serverIp,port, 1000*5);
				result = pusher.push0x11Message(uuid,msg);
				if(result){
					startSrv.putExtra("TEXT", "������Ϣ���ͳɹ�");
				}else{
					startSrv.putExtra("TEXT", "����ʧ�ܣ���ʽ����");
				}
			}catch(Exception e){
				e.printStackTrace();
				startSrv.putExtra("TEXT", "����ʧ�ܣ�"+e.getMessage());
			}finally{
				if(pusher != null){
					try{pusher.close();}catch(Exception e){};
				}
			}
			context.startService(startSrv);
		}
	}
	
	public class send0x20Task implements Runnable{
		private Context context;
		private String serverIp;
		private int port;
		private byte[] uuid;
		private byte[] msg;
		
		public send0x20Task(Context context, String serverIp, int port, byte[] uuid, byte[] msg){
			this.context = context;
			this.serverIp = serverIp;
			this.port = port;
			this.uuid = uuid;
			this.msg = msg;
		}
		
		public void run(){
			Pusher pusher = null;
			Intent startSrv = new Intent(context, OnlineService.class);
			startSrv.putExtra("CMD", "TOAST");
			try{
				boolean result;
				
				
				pusher = new Pusher(serverIp,port, 1000*5);
				result = pusher.push0x20Message(uuid,msg);
				if(result){
					startSrv.putExtra("TEXT", "�Զ�����Ϣ���ͳɹ�");
				}else{
					startSrv.putExtra("TEXT", "����ʧ�ܣ���ʽ����");
				}
			}catch(Exception e){
				e.printStackTrace();
				startSrv.putExtra("TEXT", "����ʧ�ܣ�"+e.getMessage());
			}finally{
				if(pusher != null){
					try{pusher.close();}catch(Exception e){};
				}
			}
			context.startService(startSrv);
		}
	}
}
