package io.dcloud.ndtv.wbp;

import org.json.JSONObject;

import com.wasu.paysdk.PaySdk;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
//		PaySdk.initSDK(this, initParam(), false);
		PaySdk.initSDK(this, initParam());
		// PaySdk.initSDK(this, initParam(), true);// ���Ի���
	}

	private String initParam() {
		try {
			JSONObject jsonParma = new JSONObject();
			JSONObject jsonAlipay = new JSONObject();
			jsonAlipay.put("appId", "2016080400163709");
			jsonAlipay.put("pid", "2088102169883491");
			jsonAlipay.put("targetId", "kkkkk091125");
			jsonParma.put("alipay", jsonAlipay);
			return jsonParma.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
