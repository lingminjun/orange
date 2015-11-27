package com.juzistar.m.view.com;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.juzistar.m.R;

/**
 * roundImageView
 *
 * @author junxian.bian
 * @2012-12-13
 */
public class RoundAngleImageView extends ImageView {

    private static final int default_radius = 5;

    private boolean hiddenStroke;
    private int strokeColor;


    private int roundWidth;
    private int roundHeight;

    private float width_stroke_paint = 1.0f;
    private RectF mCircleOval;
    private Paint strokePaint;
    private Paint paint;
    private Paint paint2;
    private Path path = null;

    public RoundAngleImageView(Context context) {
        super(context);
        init(context, null);
    }

    public RoundAngleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundAngleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundAngleImageView);
            roundHeight = roundWidth = a.getDimensionPixelSize(R.styleable.RoundAngleImageView_radius, default_radius);
            hiddenStroke = a.getBoolean(R.styleable.RoundAngleImageView_hiddenStroke, false);
            width_stroke_paint = a.getFloat(R.styleable.RoundAngleImageView_strokeWidth, width_stroke_paint);
            strokeColor = a.getColor(R.styleable.RoundAngleImageView_strokeColor, Color.WHITE);
            a.recycle();
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            roundWidth = (int) (roundWidth * density);
            roundHeight = (int) (roundHeight * density);
            strokeColor = Color.WHITE;
        }

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        paint2 = new Paint();
        paint2.setXfermode(null);

        strokePaint = new Paint();
        strokePaint.setColor(strokeColor);
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(width_stroke_paint);

        path = new Path();
        mCircleOval = new RectF(0, 0, 0, 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int sl = getPaddingLeft();
        int sr = getPaddingRight();
        int st = getPaddingTop();
        int sb = getPaddingBottom();

        mCircleOval.set(sl + width_stroke_paint / 2, st + width_stroke_paint / 2,
                w - sr - width_stroke_paint / 2, h - sb - width_stroke_paint / 2);

    }

    @Override
    public void draw(Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap);
        super.draw(canvas2);

        drawLiftUp(canvas2);
        drawRightUp(canvas2);
        drawLiftDown(canvas2);
        drawRightDown(canvas2);
        canvas.drawBitmap(bitmap, 0, 0, paint2);
        bitmap.recycle();

        if (!hiddenStroke) {

            float max = Math.max(this.getWidth(), this.getHeight());
            if (max <= 2*roundWidth) {
                canvas.drawCircle(mCircleOval.centerX(), mCircleOval.centerY(), max/2-1, strokePaint);
            }
            else {
                RectF rect = new RectF(0f, 0f, this.getWidth(), this.getHeight());
                canvas.drawRoundRect(rect, roundWidth, roundHeight, strokePaint);
            }
        }
    }

    private void drawLiftUp(Canvas canvas) {
        path.reset();
        path.moveTo(0, roundHeight);
        path.lineTo(0, 0);
        path.lineTo(roundWidth, 0);
        path.arcTo(new RectF(0, 0, roundWidth * 2, roundHeight * 2), -90, -90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawLiftDown(Canvas canvas) {
        path.reset();
        path.moveTo(0, getHeight() - roundHeight);
        path.lineTo(0, getHeight());
        path.lineTo(roundWidth, getHeight());
        path.arcTo(new RectF(0, getHeight() - roundHeight * 2, 0 + roundWidth * 2, getWidth()), 90, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightDown(Canvas canvas) {
        path.reset();
        path.moveTo(getWidth() - roundWidth, getHeight());
        path.lineTo(getWidth(), getHeight());
        path.lineTo(getWidth(), getHeight() - roundHeight);
        path.arcTo(new RectF(getWidth() - roundWidth * 2, getHeight() - roundHeight * 2, getWidth(), getHeight()), 0,
                90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightUp(Canvas canvas) {
        path.reset();
        path.moveTo(getWidth(), roundHeight);
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth() - roundWidth, 0);
        path.arcTo(new RectF(getWidth() - roundWidth * 2, 0, getWidth(), 0 + roundHeight * 2), -90, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

}
