package com.logictreeit.android.fracturedphoto.custom_ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

/**
 * Created by Administrator on 12/20/2017.
 */

public class CustomCheckedTextView extends CheckedTextView{
    
    public CustomCheckedTextView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }
    
    public CustomCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CustomCheckedTextView(Context context) {
        super(context);
        init();
    }
    
    /**Font is set based on the activity the user is in
     * so using try catch block to verify the view and setting the font
     * by this way font can be easily changed*/
    private void init() {
        if (!isInEditMode()) {
            Typeface myFonts = Typeface.createFromAsset(getContext().getAssets(), "fonts/AmaticSC-Bold-webfont.ttf");
            setTypeface(myFonts);
        }
    }
    
    @Override
    public void setEllipsize(TextUtils.TruncateAt where) {
        super.setEllipsize(where);
    }
}
