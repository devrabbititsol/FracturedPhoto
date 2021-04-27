package com.logictreeit.android.fracturedphoto.helpers;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;

/**
 * Created by santhoshannam on 9/28/2015.
 */
public class ShatteredImageDragShadowBuilder extends View.DragShadowBuilder {

    private Point mScaleFactor;

    // Defines the constructor for myDragShadowBuilder
    public ShatteredImageDragShadowBuilder(View v) {
        // Stores the View parameter passed to myDragShadowBuilder.
        super(v);
    }

    // Defines a callback that sends the drag shadow dimensions and touch point back to the
    // system.
    @Override
    public void onProvideShadowMetrics(Point size, Point touch) {
        // Defines local variables
        float width;
        float height;

        // Sets the width of the shadow to triple the width of the original View
        width = getView().getWidth() * 2.5f;

        // Sets the height of the shadow to triple the height of the original View
        height = getView().getHeight() * 2.5f;

        // Sets the size parameter's width and height values. These get back to the system
        // through the size parameter.
        size.set((int)width, (int)height);
        // Sets size parameter to member that will be used for scaling shadow image.
        mScaleFactor = size;
        // Sets the touch point's position to be in the middle of the drag shadow
        touch.set((int)width / 2, (int)height / 2);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        // Draws the ColorDrawable in the Canvas passed in from the system.
        canvas.scale(mScaleFactor.x / (float) getView().getWidth(), mScaleFactor.y / (float) getView().getHeight());
        getView().draw(canvas);
    }

}
