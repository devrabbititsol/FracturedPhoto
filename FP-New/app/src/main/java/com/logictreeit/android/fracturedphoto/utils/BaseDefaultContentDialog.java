package com.logictreeit.android.fracturedphoto.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Created by shangzheng on 2017/9/11
 * ☃☃☃ 09:15.
 * <p>
 */

public class BaseDefaultContentDialog extends BaseDialogFragment {
    private View mContent;
    private int mBeginDialogWidth;
    private int mBeginDialogHeight;
    protected boolean isFirstCreateDialog = true; // 表示第一次初始化本DialogFragment

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                Window window = getDialog().getWindow();
                if (window != null) {
                    mContent = window.findViewById(android.R.id.content);
                    mBeginDialogWidth = mContent.getWidth();
                    mBeginDialogHeight = mContent.getHeight() + dp2px(24);

                    /*
                     * 由于showListener的调用时间比onResume还晚,所以需要在显示的时候,手动调用一次旋转.
                     */
                    setRotation(mRotation);
                }
            }
        });

    }

    @Override
    public void setRotation(int rotation) {

        int windowSize[] = getWindowSize();
        if (getDialog() == null) {
            return;
        }
        Window window = getDialog().getWindow();
        if (window == null) {
            Log.e("TAG", "setRotation: window = null");
            return;
        }

        if (mContent == null) {
            return;
        }

        int w, h;
        int tranX, tranY;
        if (rotation == 1 || rotation == 3) {//横屏
            w = (int) (windowSize[1] * 0.70 + 0.5f);
            h = mBeginDialogHeight - 20;
            tranX = (h - w) / 2;
            tranY = (w - h) / 2;
            window.setLayout(h + 80, w + 100);
        } else {
            w = mBeginDialogWidth;
            h = mBeginDialogHeight - dp2px(24);
            tranX = 0;
            tranY = 0;
            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        mContent.getLayoutParams().width = w;
        mContent.getLayoutParams().height = h;
        mContent.setLayoutParams(mContent.getLayoutParams());

        int duration = isFirstCreateDialog ? 0 : 200;

        mContent.animate()
                .rotation(90 * (rotation))
                .translationX(tranX)
                .translationY(tranY)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isFirstCreateDialog = false;
                    }
                });
    }

    private int dp2px(int dp) {
        return dip2px(getActivity(), dp);
    }

    public static int[] getWindowSize(){
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}