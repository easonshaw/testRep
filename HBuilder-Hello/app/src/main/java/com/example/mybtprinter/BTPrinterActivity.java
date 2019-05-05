package com.example.mybtprinter;

import io.dcloud.ndtv.wbp.R;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current session.
 */
public class BTPrinterActivity extends Activity {
	// Debugging
	private static final String TAG = "BTPrinter";

	// Message types sent from the BluetoothService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// Key names received from the BluetoothService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	private TextView mOutTextView;
	private TextView mConnectState;
	private Button mPrintButton;
	private Button mPrintScan;
	private Button mPrintCancel;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the services
	private BluetoothService mService = null;

	// ��ӡ�������
	// private String startLine;
	// private String endLine;
	// private int flag = 0;// ������ʾ��ӡ���ݻ����˼��Σ�����ֽ����ȥflag��

	private String printContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// getActionBar().setDisplayHomeAsUpEnabled(true);// ���÷��ؼ�
		// getActionBar().setDisplayUseLogoEnabled(false);
		// getActionBar().setDisplayShowHomeEnabled(false);
		// setTitle("��ӡ����");
		// if (!MyGlobal.getInstance().getCurrentConf().getEnableLandscape())
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// ��ʾ����
		setContentView(R.layout.printer_main);
		// ��ȡ���ص�����������
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// ���������ã� ��ʾ
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "����������", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// �����Ƿ񼤻��û�����ϵͳ��������
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			// ����������г�ʼ��
			if (mService == null)
				init();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (mService != null) {
			if (mService.getState() == BluetoothService.STATE_NONE) {
				mService.start();
			}
		}
	}

	/**
	 * ��ʼ��
	 */
	private void init() {
		Log.d(TAG, "setupChat()");

		mConnectState = (TextView) findViewById(R.id.tv_connect_state);// ��������״̬��ʾTextView
		mOutTextView = (TextView) findViewById(R.id.tv_text_out);// ��ȡ��Ҫ��ӡ����Ϣ

		printContent = getIntent().getStringExtra("printContent");// ��������
		if (TextUtils.isEmpty(printContent)) {
			Toast.makeText(BTPrinterActivity.this, "����������������ԣ�", 0).show();
			finish();
		}
		mOutTextView.setText(printContent);

		/**
		 * ��ӡ
		 */
		mPrintButton = (Button) findViewById(R.id.button_print);
		mPrintButton.setOnClickListener(new OnClickListener() {
			private byte[] blankLines;

			@Override
			public void onClick(View v) {// ��ʼ��ӡ

				blankLines = new byte[3];
				int int_blankLine = 0;
				// int int_blankLine = 0 - (flag * 14);// �������Ŀհ׳��ȣ������14��ʾһ�����ֵĸ߶�ԼΪ14����λ
				blankLines[0] = 0x1b;
				blankLines[1] = 0x4a;
				blankLines[2] = (byte) int_blankLine;

				// �������δ���ӣ���ʾ����
				if (mService.getState() != BluetoothService.STATE_CONNECTED) {
					Toast.makeText(BTPrinterActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
					return;
				}

				String message = ".\r\n\r\n\r\n\r\n             ��������ƾ֤\r\n" + printContent;
				// ����Ƿ����˴�ӡ���ݣ���Ϊ��ת��byte�ͣ����
				if (message.length() > 0) {

					byte[] send;
					try {
						send = message.getBytes("GBK");
					} catch (UnsupportedEncodingException e) {
						send = message.getBytes();
					}
					// ��2��byte����ϲ������д�ӡ
					mService.write(byteMerger(send, blankLines));
				}
			}
		});

		/**
		 * ��������
		 */
		mPrintScan = (Button) findViewById(R.id.scan);
		mPrintScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent serverIntent = new Intent(BTPrinterActivity.this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
		});

		/**
		 * ���أ�ȡ����ӡ��
		 */
		mPrintCancel = (Button) findViewById(R.id.cancel);
		mPrintCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(BTPrinterActivity.this).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				}).setNegativeButton("ȡ��", null).setTitle("�˳���ӡ").setMessage("�Ƿ��˳���ӡ��").show();
			}
		});

		// Initialize the BluetoothService to perform bluetooth connections
		mService = new BluetoothService(this, mHandler);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth services
		if (mService != null)
			mService.stop();
	}

	// The Handler that gets information back from the BluetoothService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mConnectState.setText(R.string.title_connected_to);
					mConnectState.append(mConnectedDeviceName);
					break;
				case BluetoothService.STATE_CONNECTING:
					mConnectState.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					mConnectState.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				// byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				// String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ:
				// byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				// String readMessage = new String(readBuf, 0, msg.arg1);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "�����Ӵ�ӡ����" + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				if (BluetoothAdapter.checkBluetoothAddress(address)) {
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
					// Attempt to connect to the device
					mService.connect(device);
				}
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a session
				init();
			} else {
				// User did not enable Bluetooth or an error occured
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	// // ������
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main2, menu);
	// return true;
	// }

	// @Override
	// public boolean onMenuItemSelected(int featureId, MenuItem item) {
	// // TODO Auto-generated method stub
	//
	// if (item.getOrder() == 1) {
	// Intent intent = new Intent();
	// intent.setClass(BTPrinterDemo.this, MainActivity.class);
	// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// BTPrinterDemo.this.startActivity(intent);
	// }
	// return super.onMenuItemSelected(featureId, item);
	// }

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// �ϲ�2��byte����ķ���
	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	/**
	 * ���÷��ؼ���������ֱ���˳�
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(BTPrinterActivity.this).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
			}
		}).setNegativeButton("ȡ��", null).setTitle("�˳���ӡ").setMessage("�Ƿ��˳���ӡ��").show();
		// super.onBackPressed();
	}

}