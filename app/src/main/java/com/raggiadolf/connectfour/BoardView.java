package com.raggiadolf.connectfour;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ragnaradolf on 22/09/15.
 */
public class BoardView extends View {

    private int m_cellWidth  = 0;
    private int m_cellHeight = 0;

    private char[][] m_board = new char[6][7];
    private Paint m_paint = new Paint();
    private OnMoveEventHandler m_moveHandler = null;

    ShapeDrawable m_shape = new ShapeDrawable(new OvalShape());
    Rect m_rect = new Rect();

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_paint.setColor(Color.WHITE);
        m_paint.setStyle(Paint.Style.STROKE);
        m_paint.setStrokeWidth(2.0f);
    }

    public void setBoard(String string) {
        for(int index = 0, r = 6; r >= 0; --r) {
            for(int c = 0; c < 6; ++c, ++index) {
                m_board[c][r] = string.charAt(index);
            }
        }

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        m_cellWidth = xNew  / 7;
        m_cellHeight = yNew / 6;
    }

    public void onDraw(Canvas canvas) {
        for(int index = 0, r = 6; r >= 0; --r) {
            for(int c = 0; c < 6; ++c, ++index) {
                m_rect.set(c * m_cellWidth, r * m_cellHeight,
                        c * m_cellWidth + m_cellWidth, r * m_cellHeight + m_cellHeight);
                canvas.drawRect(m_rect, m_paint);
                m_rect.inset((int)(m_rect.width() * 0.1), (int)(m_rect.height() * 0.1));
                m_shape.setBounds(m_rect);

                switch(m_board[c][r]) {
                    case 'r':
                        m_shape.getPaint().setColor(Color.RED);
                        m_shape.draw(canvas);
                        break;
                    case 'o':
                        m_shape.getPaint().setColor(Color.BLACK);
                        m_shape.draw(canvas);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return true;
    }

    private int xToCol(int x) {
        return x / m_cellWidth;
    }

    private int yToRow(int y) {
        return y / m_cellHeight;
    }

    public void setMoveEventHandler(OnMoveEventHandler handler) {
        m_moveHandler = handler;
    }
}
