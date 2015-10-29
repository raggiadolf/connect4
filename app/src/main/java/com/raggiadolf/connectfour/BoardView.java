package com.raggiadolf.connectfour;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ragnaradolf on 22/09/15.
 */
public class BoardView extends View {

    private int m_cellWidth  = 0;
    private int m_cellHeight = 0;

    private boolean m_canMove = true;

    public char[][] m_board = new char[6][7];
    private char[][] m_drawingBoard;

    private Paint m_discPaint = new Paint();
    private OnMoveEventHandler m_moveHandler = null;

    private List<ArrayList<RectF>> m_boardDiscs = new ArrayList<>();
    private RectF m_fallingDisc = null;
    private Paint m_fallingDiscPaint = new Paint();

    private float m_discHeight;

    private int m_player1Color = getResources().getColor(R.color.player1);
    private int m_player2Color = getResources().getColor(R.color.player2);
    private int m_emptyDisc = getResources().getColor(R.color.blankSquare);

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_discPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        for(int row = 0; row < 7; row++) {
            m_boardDiscs.add(new ArrayList<RectF>());
            for (int col = 0; col < 6; col++) {
                RectF newRect = new RectF();
                newRect.set(row * m_cellWidth, col * m_cellHeight,
                        row * m_cellWidth + m_cellWidth, col * m_cellHeight + m_cellHeight);
                newRect.inset((int) (newRect.width() * 0.1), (int) (newRect.height() * 0.1));
                m_boardDiscs.get(row).add(col, newRect);
            }
        }
    }

    /**
     * Sets up the board array from a string representation of a game state.
     * @param string the string representing the state
     */
    public void setupBoard(String string) {
        for(int row = 0, index = 0; row < 6; row++) {
            for(int col = 0; col < 7; col++, index++) {
                m_board[row][col] = string.charAt(index);
            }
        }

        invalidate();
    }

    ValueAnimator animator = new ValueAnimator();

    /**
     * Animates the dropped disc from the top of the board down to its final location.
     * When the animation ends it sets the discs position on the board so that it gets
     * drawn there as a normal disc after the animation ends
     * @param col The column to drop the disc into
     * @param row The row the disc ends up in
     * @param token The character representing the color of the disc being dropped
     * @param endcoord the coordinates representing the final location of the disc
     */
    private void dropDisc(final int col, final int row, final char token, final double endcoord) {
        animator.removeAllUpdateListeners();
        animator.setDuration(300);
        animator.setFloatValues(0.0f, 1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float ratio = (float) animation.getAnimatedValue();
                double diffVertical = endcoord * ratio;
                m_fallingDisc.top = (float) diffVertical - m_discHeight;
                m_fallingDisc.bottom = (float) diffVertical;
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                m_board[col][row] = token;
                m_fallingDisc = null;
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * Creates a new disc to drop onto the board, calculates its end position and then starts the
     * animation for that disc.
     * @param row the row the disc ends up in
     * @param col the column to drop the disc into
     * @param token the character representing the color of the disc being dropped
     */
    public void placeDisc(int row, int col, char token) {
        m_fallingDisc = new RectF();
        m_fallingDisc.set(row * m_cellWidth, 0,
                row * m_cellWidth + m_cellWidth, m_cellHeight);
        m_fallingDisc.inset((int) (m_fallingDisc.width() * 0.1), (int) (m_fallingDisc.height() * 0.1));

        switch(token) {
            case 'R':
                m_fallingDiscPaint.setColor(m_player1Color);
                break;
            case 'W':
                m_fallingDiscPaint.setColor(m_player2Color);
                break;
        }

        double endcoord = (getMeasuredHeight() - m_discHeight * 0.1) - col * m_cellHeight;
        dropDisc(col, row, token, endcoord);
}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        m_cellWidth  = xNew / 7;
        m_cellHeight = yNew / 6;

        for(int row = 0; row < 7; row++) {
            for(int col = 0; col < 6; col++) {
                int x = row * m_cellWidth;
                int y = col * m_cellHeight;
                m_boardDiscs.get(row).get(col).set(x, y, x + m_cellWidth, y + m_cellHeight);
                m_boardDiscs.get(row).get(col).offset(getPaddingLeft(), getPaddingTop());
                m_boardDiscs.get(row).get(col).inset(m_cellWidth * 0.1f, m_cellHeight * 0.1f);
            }
        }

        m_discHeight = m_boardDiscs.get(0).get(0).height();
    }

    public void onDraw(Canvas canvas) {
        m_drawingBoard = rotateByPositiveNinety(m_board);


        for(int row = 0; row < 7; row++) {
            for(int col = 0; col < 6; col++) {
                switch (m_drawingBoard[row][col]) {
                    case 'R':
                        m_discPaint.setColor(m_player1Color);
                        canvas.drawOval(m_boardDiscs.get(row).get(col), m_discPaint);
                        break;
                    case 'W':
                        m_discPaint.setColor(m_player2Color);
                        canvas.drawOval(m_boardDiscs.get(row).get(col), m_discPaint);
                        break;
                    default:
                        m_discPaint.setColor(m_emptyDisc);
                        canvas.drawOval(m_boardDiscs.get(row).get(col), m_discPaint);
                        break;
                }
            }
        }

        if(m_fallingDisc != null) {
            m_discPaint.setColor(Color.BLACK);
            canvas.drawOval(m_fallingDisc, m_fallingDiscPaint);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int x = (int) event.getX();

        if(event.getAction() == MotionEvent.ACTION_DOWN) {

        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            if(m_moveHandler != null) {
                if(m_canMove) {
                    m_moveHandler.onMove(xToCol(x));
                }
            }
        }
        return true;
    }

    private int xToCol(int x) {
        return x / m_cellWidth;
    }

    public void setMoveEventHandler(OnMoveEventHandler handler) {
        m_moveHandler = handler;
    }

    /**
     * Helper function to rotate the matrix by 90 degrees,
     * since it does not display the same way our state
     * sees it internally.
     * @param array the array to transpose
     * @return 'array' transposed
     */
    private char[][] rotateByPositiveNinety (char[][] array) {
        if (array == null || array.length == 0) //empty or unset array, should not be needed in our case.
            return array;

        int width = array.length;
        int height = array[0].length;

        char[][] array_new = new char[height][width];

        // Transpose the array
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                array_new[y][x] = array[x][y];
            }
        }

        // Reverse rows
        for(int j = 0; j < array_new.length; j++){
            for(int i = 0; i < array_new[j].length / 2; i++) {
                char temp = array_new[j][i];
                array_new[j][i] = array_new[j][array_new[j].length - i - 1];
                array_new[j][array_new[j].length - i - 1] = temp;
            }
        }

        return array_new;
    }

    public void setCanMove(boolean canMove) {
        this.m_canMove = canMove;
    }
}
