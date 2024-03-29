package net.rusnet.sb.fingerdrawapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiDrawView extends View {

    public static final int FIRST_POINT_INDEX = 0;
    public static final int SECOND_POINT_INDEX = 1;

    private Brush mCurrentBrush;
    private int mCurrentColor;
    private Paint mCurrentPaint  = new Paint();
    private Drawing mCurrentDrawing;

    private List<PointF> mCurrentCoordinates = new ArrayList<>();
    private Path mCurrentPath = new Path();

    private List<Drawing> mDrawings = new ArrayList<>();

    private boolean mScrollMode;

    private GestureDetector mDetector;

    public void setScrollMode(boolean scrollMode) {
        mScrollMode = scrollMode;
    }

    public MultiDrawView(Context context) {
        this(context, null);
    }

    public MultiDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void clear() {
        mDrawings = new ArrayList<>();
        invalidate();
    }

    public void setParams(Brush brush, int color) {
        mCurrentBrush = brush;
        mCurrentColor = color;

        mCurrentPaint.setColor(mCurrentColor);
        mCurrentPaint.setAntiAlias(true);
        mCurrentPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.default_stroke_width));

        switch (brush) {
            case PATH:
            case LINE:
            case FIGURE:
                mCurrentPaint.setStyle(Paint.Style.STROKE);
                break;
            case RECTANGLE:
                mCurrentPaint.setStyle(Paint.Style.FILL);
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Drawing drawing : mDrawings) {
            mCurrentCoordinates = drawing.getCoordinates();
            switch (drawing.getDrawingType()) {
                case PATH:
                    for (int i = 0; i < mCurrentCoordinates.size(); i++) {
                        if (i == 0) {
                            mCurrentPath.moveTo(
                                    mCurrentCoordinates.get(i).x,
                                    mCurrentCoordinates.get(i).y);
                        } else {
                            mCurrentPath.lineTo(
                                    mCurrentCoordinates.get(i).x,
                                    mCurrentCoordinates.get(i).y);
                        }
                    }
                    canvas.drawPath(mCurrentPath, drawing.getDrawingPaint());
                    mCurrentPath.reset();
                    break;
                case LINE:
                    canvas.drawLine(mCurrentCoordinates.get(FIRST_POINT_INDEX).x,
                            mCurrentCoordinates.get(FIRST_POINT_INDEX).y,
                            mCurrentCoordinates.get(SECOND_POINT_INDEX).x,
                            mCurrentCoordinates.get(SECOND_POINT_INDEX).y,
                            drawing.getDrawingPaint());
                    break;
                case RECTANGLE:
                    float left = Math.min(mCurrentCoordinates.get(FIRST_POINT_INDEX).x,
                            mCurrentCoordinates.get(SECOND_POINT_INDEX).x);
                    float right = Math.max(mCurrentCoordinates.get(FIRST_POINT_INDEX).x,
                            mCurrentCoordinates.get(SECOND_POINT_INDEX).x);
                    float top = Math.min(mCurrentCoordinates.get(FIRST_POINT_INDEX).y,
                            mCurrentCoordinates.get(SECOND_POINT_INDEX).y);
                    float bottom = Math.max(mCurrentCoordinates.get(FIRST_POINT_INDEX).y,
                            mCurrentCoordinates.get(SECOND_POINT_INDEX).y);
                    canvas.drawRect(left, top, right, bottom, drawing.getDrawingPaint());
                    break;
                case FIGURE:
                    if (mCurrentCoordinates.isEmpty()) return;

                    if (mCurrentCoordinates.size() == 1) {
                        canvas.drawPoint(mCurrentCoordinates.get(FIRST_POINT_INDEX).x,
                                mCurrentCoordinates.get(FIRST_POINT_INDEX).y,
                                drawing.getDrawingPaint());
                    } else {
                        for (int i = 1; i < mCurrentCoordinates.size(); i++) {
                            PointF one = mCurrentCoordinates.get(i-1);
                            PointF two = mCurrentCoordinates.get(i);

                            canvas.drawLine(one.x,
                                    one.y,
                                    two.x,
                                    two.y,
                                    drawing.getDrawingPaint());

                            if (i == mCurrentCoordinates.size() - 1) {
                                one = mCurrentCoordinates.get(FIRST_POINT_INDEX);
                                canvas.drawLine(one.x,
                                        one.y,
                                        two.x,
                                        two.y,
                                        drawing.getDrawingPaint());
                            }
                        }
                    }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mScrollMode) {
            return mDetector.onTouchEvent(event);
        }

        PointF currentPoint = new PointF(event.getX(), event.getY());
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mCurrentDrawing = new Drawing(mCurrentBrush, mCurrentPaint);
                mDrawings.add(mCurrentDrawing);
                mCurrentDrawing.addPoint(currentPoint);
                if (mCurrentDrawing.getDrawingType() == Brush.LINE ||
                        mCurrentDrawing.getDrawingType() == Brush.RECTANGLE) {
                    mCurrentDrawing.addPoint(currentPoint);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //событие опускания доп. пальца
                int actionIndex = event.getActionIndex();
                int pointerId = event.getPointerId(actionIndex);
                if (mCurrentDrawing.getCoordinates().size() == pointerId) {
                    mCurrentDrawing.addPoint(new PointF(
                            event.getX(actionIndex),
                            event.getY(actionIndex)
                    ));
                } else {
                    mCurrentDrawing.setPoint(new PointF(
                            event.getX(actionIndex),
                            event.getY(actionIndex)), pointerId);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mCurrentDrawing.getDrawingType()) {
                    case PATH:
                        mCurrentDrawing.addPoint(currentPoint);
                        break;
                    case LINE:
                    case RECTANGLE:
                        mCurrentDrawing.setPoint(currentPoint, SECOND_POINT_INDEX);
                        break;
                    case FIGURE:
                        for (int i = 0; i < event.getPointerCount(); i++) {
                            int id = event.getPointerId(i);
                            mCurrentDrawing.setPoint(new PointF(
                                    event.getX(i),
                                    event.getY(i)), id);
                        }
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return false;
        }

        invalidate();
        return true;
    }

    private void init() {
        mDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                for (Drawing drawing : mDrawings) {
                    for (PointF coordinate : drawing.getCoordinates()) {
                        coordinate.x -= distanceX;
                        coordinate.y -= distanceY;
                    }
                }

                invalidate();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

}
