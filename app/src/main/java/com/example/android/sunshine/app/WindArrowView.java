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
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Arrow that shows wind direction.
 * Use {@link #setAngle(float)} to change direction.
 */
public class WindArrowView extends View {

    private static final String TAG = "WindArrowView.java";
    private Paint arrowPaint;
    private Bitmap arrowBitmap;
    private Matrix rotationMatrix;
    private float angle = 45;
    private String windDirection;

    // Directions in Degrees
    private  final float NORTH = 0.0f;
    private  final float NORTH_EAST = 45.0f;
    private  final float EAST = 90.0f;
    private  final float SOUTH_EAST = 135.0f;
    private  final float SOUTH = 180.0f;
    private  final float SOUTH_WEST = 225.0f;
    private  final float WEST = 270.0f;
    private  final float NORTH_WEST = 315.0f;

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
                return (specSize < childSize) ? specSize : childSize;

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
     * Specifies the angle for arrow position. Round figures the angle for closest direction,
     * for example, if the angle specified is 85, it rounded to 90 for East direction.
     * @param angle to be set for arrow position in degrees. For example 0 - 360.
     */
    public void setAngle(final float angle) {
        this.angle = roundFigureDegrees(angle);
        rotationMatrix.setRotate(this.angle, 11, 11);
        invalidate();
        sendAccessibilityEvent();
    }

    private void sendAccessibilityEvent() {
        AccessibilityManager manager = (AccessibilityManager) getContext()
            .getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager.isEnabled()) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);
        event.getText().add(convertDegreesToStringDirection(angle));
        return true;
    }

    private String convertDegreesToStringDirection(float directionDegrees) {
        if(directionDegrees == NORTH) {
            return getContext().getString(R.string.north);
        } else if (directionDegrees == NORTH_EAST) {
            return getContext().getString(R.string.north_east);
        } else if (directionDegrees == EAST) {
            return getContext().getString(R.string.east);
        } else if (directionDegrees == SOUTH_EAST) {
            return getContext().getString(R.string.south_east);
        } else if (directionDegrees == SOUTH) {
            return getContext().getString(R.string.south);
        } else if (directionDegrees == SOUTH_WEST) {
            return getContext().getString(R.string.south_west);
        } else if (directionDegrees == WEST) {
            return getContext().getString(R.string.west);
        } else if (directionDegrees == NORTH_WEST) {
            return getContext().getString(R.string.north_west);
        } else throw new RuntimeException("Invalid direction in degrees");
    }

    /**
     * Round figures the angle for closest direction, for example, if the angle specified
     * is 85, it rounded to 90 for East direction.
     */
    private float roundFigureDegrees(float degrees) {
        if (degrees >= 337.5 || degrees < 22.5) {
            return NORTH;
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return NORTH_EAST;
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return EAST;
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return SOUTH_EAST;
        } else if (degrees >= 157.5 && degrees < 202.5) {
            return SOUTH;
        } else if (degrees >= 202.5 && degrees < 247.5) {
            return SOUTH_WEST;
        } else if (degrees >= 247.5 && degrees < 292.5) {
            return WEST;
        } else if (degrees >= 292.5 || degrees < 22.5) {
            return NORTH_WEST;
        }
        return 0;
    }

}
