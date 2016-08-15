package com.example.android.sunshine.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Arrow that shows wind direction.
 * Use {@link #setAngle(float)} to change direction.
 */
public class WindArrowView extends View {

    private Paint arrowPaint;
    private Bitmap arrowBitmap;
    private Matrix rotationMatrix;
    private float angle = 45;

    /**
     * See {@link View class documentation for details.}
     */
    public WindArrowView(Context context) {
        super(context);
        init();
    }

    /**
     * See {@link View class documentation for details.}
     */
    public WindArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * See {@link View class documentation for details.}
     */
    public WindArrowView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        init();
    }

    private void init() {
        setupArrowPaint();
        setRotationMatrix();
        createArrowBitmap();
    }

    private void setRotationMatrix() {
        rotationMatrix = new Matrix();
        rotationMatrix.setRotate(angle, 11, 11);
    }

    private void setupArrowPaint() {
        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(3);
        arrowPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int minimumWidth = getPaddingLeft() + getPaddingRight() + arrowBitmap.getWidth() + 1;
        int minimumHeight = getPaddingTop() + getPaddingBottom() + arrowBitmap.getHeight() + 1;

        setMeasuredDimension(
            resolveSize(minimumWidth, widthMeasureSpec),
            resolveSize(minimumHeight, heightMeasureSpec));
        Log.d("WindArrowView.java", "onMeasure() called");
    }

    public static int manageSize(int childSize, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                // Just return the child's size.
                return childSize;

            case MeasureSpec.AT_MOST:
                // Return the smallest of childSize and specSize
                return (specSize < childSize)? specSize : childSize;

            case MeasureSpec.EXACTLY:
                // Should honor parent-View's request for the given size.
                // If not desired, don't set the child's layout_width/layout_height to a fixed
                // value (nor fill_parent in certain cases).
                return specSize;
        }

        return childSize;
    }


    private void createArrowBitmap() {
        // Holds entire arrow drawn on canvas
        arrowBitmap = Bitmap.createBitmap(21, 21, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(arrowBitmap);

        // Draw arrow's shaft
        canvas.drawLine(11, 0, 11, 21, arrowPaint);

        // Draw arrow head
        canvas.drawPath(getArrowHeadPath(), arrowPaint);
    }

    @NonNull
    private Path getArrowHeadPath() {
        // Path for drawing arrow head
        final Path arrowHeadPath = new Path();

        // Draw left head
        arrowHeadPath.moveTo(11, 1);
        arrowHeadPath.lineTo(3, 8);

        // Draw right head
        arrowHeadPath.moveTo(11, 1);
        arrowHeadPath.lineTo(19, 8);
        return arrowHeadPath;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(arrowBitmap, rotationMatrix, null);
    }

    /**
     * Returns the current arrow rotation angle.
     * @return Returns angle in degrees.
     */
    public float getAngle() {
        return angle;
    }

    /**
     * Specifies the angle for arrow position.
     * @param angle to be set for arrow position in degrees. For example 0 - 360.
     */
    public void setAngle(final float angle) {
        this.angle = angle;
        rotationMatrix.setRotate(angle, 11, 11);
        invalidate();
    }
}
