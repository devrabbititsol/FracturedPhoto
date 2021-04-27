//package com.logictreeit.android.fracturedphoto.utils;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import com.facebook.android.Facebook;
//
//public class FacebookSessionStore {
//
//	public static final String[] PERMISSIONS = new String[] {"publish_actions", "email"};
//	public static final String TOKEN = "access_token";
//	public static final String EXPIRES = "expires_in";
//	public static final String FB_KEY = "facebook_session";
//
//	//public static final String FACEBOOK_APP_ID = "561874070616060";//here copy your app id logictree
//	public static final String FACEBOOK_APP_ID = "544841665618524";//here copy your app id fracturephoto
//
//	public static boolean saveFBSession(Facebook session, Context context) {
//		Editor editor = context.getSharedPreferences(FB_KEY, Context.MODE_PRIVATE).edit();
//		editor.putString(TOKEN, session.getAccessToken());
//		editor.putLong(EXPIRES, session.getAccessExpires());
//		return editor.commit();
//	}
//
//	public static boolean restoreFBSession(Facebook session, Context context) {
//		SharedPreferences savedSession = context.getSharedPreferences(FB_KEY, Context.MODE_PRIVATE);
//		session.setAccessToken(savedSession.getString(TOKEN, null));
//		session.setAccessExpires(savedSession.getLong(EXPIRES, 0));
//		return session.isSessionValid();
//	}
//
//	public static void clearFBSession(Context context) {
//		Editor editor =  context.getSharedPreferences(FB_KEY, Context.MODE_PRIVATE).edit();
//		editor.clear();
//		editor.commit();
//	}
//
//}
