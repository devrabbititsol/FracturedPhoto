package com.logictreeit.android.fracturedphoto.network;

import android.os.Handler;
import android.os.Message;

public class RequestHandler extends Handler {

	public static final int SUCCESS = 200;
	public static final int FAILURE = 400;
	private int requestCode;
	private NetworkCallBack networkCallBack;

	public RequestHandler(int code, NetworkCallBack networkCallBack) {
		this.requestCode = code;
		this.networkCallBack = networkCallBack;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (msg.what == SUCCESS) {
			Object object = msg.obj;
			networkCallBack.onSuccess(requestCode, object);
		} else if (msg.what == FAILURE) {
			String object = (String) msg.obj;
			networkCallBack.onFailure(requestCode, object);
		}
	}
}