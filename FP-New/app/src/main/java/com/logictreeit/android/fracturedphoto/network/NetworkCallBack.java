package com.logictreeit.android.fracturedphoto.network;

public interface NetworkCallBack {

	
	public void onSuccess(int requestCode,Object object);

	public void onFailure(int requestCode,String message);
	
	public void onCancelled();

}
