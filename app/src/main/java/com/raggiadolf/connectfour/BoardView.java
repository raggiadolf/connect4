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
import android.util.Log;
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

        m_paint.setColor(Color.BLACK);
        m_paint.setStyle(Paint.Style.STROKE);
        m_paint.setStrokeWidth(2.0f);
    }

    public void setBoard(String string) {
        Log.i("board", string);
        for(int row = 0, index = 0; row < 6; row++) {
            for(int col = 0; col < 7; col++, index++) {
                m_board[row][col] = string.charAt(index);
            }
        }

        m_board = transpose(m_board);
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
        for(int row = 0; row < 7; row++) {
            for(int col = 0; col < 6; col++) {
                m_rect.set(row * m_cellWidth, col * m_cellHeight,
                        row * m_cellWidth + m_cellWidth, col * m_cellHeight + m_cellHeight);
                canvas.drawRect(m_rect, m_paint);
                m_rect.inset((int)(m_rect.width() * 0.1), (int)(m_rect.height() * 0.1));
                m_shape.setBounds(m_rect);

                switch(m_board[row][col]) {
                    case 'r':
                        m_shape.getPaint().setColor(Color.RED);
                        m_shape.draw(canvas);
                        break;
                    case 'w':
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

    /**
     * Helper function to transpose the matrix, since it does not display
     * the same way our state sees it internally
     * @param array the array to transpose
     * @return 'array' transposed
     */
    private char[][] transpose (char[][] array) {
        if (array == null || array.length == 0)//empty or unset array, nothing do to here
            return array;

        int width = array.length;
        int height = array[0].length;

        char[][] array_new = new char[height][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                array_new[y][x] = array[x][y];
            }
        }
        return array_new;
    }
}
