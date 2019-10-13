package net.rusnet.sb.fingerdrawapp;

import android.graphics.Paint;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class Drawing {

    private Brush mDrawingType;
    private Paint mDrawingPaint;
    private List<PointF> mCoordinates = new ArrayList<>();

    public Drawing(Brush drawingType, Paint drawingPaint) {
        mDrawingType = drawingType;
        mDrawingPaint = new Paint(drawingPaint);
    }

    public void addPoint(PointF point) {
        mCoordinates.add(new PointF(point.x, point.y));
    }

    public Brush getDrawingType() {
        return mDrawingType;
    }

    public Paint getDrawingPaint() {
        return mDrawingPaint;
    }

    public List<PointF> getCoordinates() {
        return mCoordinates;
    }

    public void setPoint(PointF point, int index) {
        mCoordinates.get(index).x = point.x;
        mCoordinates.get(index).y = point.y;
    }
}
