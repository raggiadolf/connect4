package com.raggiadolf.connectfour;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ragnaradolf on 22/09/15.
 */
public class BoardView extends View {

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {

    }

    public void onDraw(Canvas canvas) {

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return true;
    }
}
