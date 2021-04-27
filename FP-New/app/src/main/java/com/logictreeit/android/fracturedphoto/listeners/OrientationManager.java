package com.logictreeit.android.fracturedphoto.listeners;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;

/**
 * Created by Administrator on 9/25/2016.
 */
public class OrientationManager extends OrientationEventListener {

    private boolean is10InchTablet;

    public enum ScreenOrientation {
        REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT
    }

    public ScreenOrientation screenOrientation;
    private OrientationListener listener;

    public OrientationManager(Context context, int rate, OrientationListener listener, boolean is10InchTablet) {
        super(context, rate);
        this.is10InchTablet = is10InchTablet;
        setListener(listener);
    }

    public OrientationManager(Context context, int rate) {
        super(context, rate);
    }

    public OrientationManager(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == -1) {
            return;
        }
        ScreenOrientation newOrientation;

        if (is10InchTablet) {
            if (orientation >= 60 && orientation <= 140) {
                newOrientation = ScreenOrientation.PORTRAIT;
            } else if (orientation >= 140 && orientation <= 220) {
                newOrientation = ScreenOrientation.REVERSED_LANDSCAPE;
            } else if (orientation >= 220 && orientation <= 300) {
                newOrientation = ScreenOrientation.REVERSED_PORTRAIT;
            } else {
                newOrientation = ScreenOrientation.LANDSCAPE;
            }
        } else {
            if (orientation >= 60 && orientation <= 140) {
                newOrientation = ScreenOrientation.REVERSED_LANDSCAPE;
            } else if (orientation >= 140 && orientation <= 220) {
                newOrientation = ScreenOrientation.REVERSED_PORTRAIT;
            } else if (orientation >= 220 && orientation <= 300) {
                newOrientation = ScreenOrientation.LANDSCAPE;
            } else {
                newOrientation = ScreenOrientation.PORTRAIT;
            }
        }

        if (newOrientation != screenOrientation) {
            screenOrientation = newOrientation;
            if (listener != null) {
                listener.onOrientationChange(screenOrientation);
            }
        }
    }

    public void setListener(OrientationListener listener) {
        this.listener = listener;
    }

    public ScreenOrientation getScreenOrientation() {
        return screenOrientation;
    }

    public interface OrientationListener {
        void onOrientationChange(ScreenOrientation screenOrientation);
    }
}