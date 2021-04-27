package com.logictreeit.android.fracturedphoto.helpers;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager; 

public class VolumeControlHelper {
	
	public static AudioManager getAudioManager(Activity act) {
		return audioManager == null ? audioManager = (AudioManager)act.getSystemService(Context.AUDIO_SERVICE) : audioManager;
	}

	public static int getStreamMaxVolume(Activity act) { 
		if(streamMaxVolume == 0){
			setStreamMaxVolume(getAudioManager(act).getStreamMaxVolume(AudioManager.STREAM_MUSIC));		
		}
		return VolumeControlHelper.streamMaxVolume;
	}

	public static void setStreamMaxVolume(int streamMaxVolume) {
		VolumeControlHelper.streamMaxVolume = streamMaxVolume;
	}

	private static int streamMaxVolume = 0;
	private static AudioManager audioManager;

}
