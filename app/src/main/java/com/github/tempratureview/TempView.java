package com.github.tempratureview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by lcy on 2016/11/8.
 */
public class TempView extends View {

    private String formatNumber;
    private Paint paint;
    private int mHeight;
    private int mWidth;
    private int radus;
    private int lineHeight = 20;
    private int padding = 50;
    private Paint bigTextPaint;
    private Paint smallTextPaint;
    private int lineL;//线段长度；
    private String TAG = "TempView";
    private int[] doughnutColors = {Color.RED, Color.GREEN};
    private int[] colors;
    private float scale;
    private int duration;
    private String unit="%";

    public TempView(Context context) {
        this(context, null);
    }


    public TempView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TempView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TempView);
        colors = new int[]{
                typedArray.getColor(R.styleable.TempView_color0, Color.parseColor("#536dfe")),
                typedArray.getColor(R.styleable.TempView_color1, Color.parseColor("#00bcd4")),
                typedArray.getColor(R.styleable.TempView_color2, Color.parseColor("#259b24")),
                typedArray.getColor(R.styleable.TempView_color3, Color.parseColor("#f57c00")),
                typedArray.getColor(R.styleable.TempView_color3, Color.parseColor("#e91e63"))};
        f = new float[colors.length][3];
        result = new float[3];
        paint = new Paint();
        paint.setStrokeWidth(4);
        paint.setAntiAlias(true);

        bigTextPaint = new Paint();
        bigTextPaint.setStrokeWidth(4);
        bigTextPaint.setAntiAlias(true);
        bigTextPaint.setTextSize(typedArray.getDimension(R.styleable.TempView_textSizeBig,72));

        smallTextPaint = new Paint();
        smallTextPaint.setStrokeWidth(4);
        smallTextPaint.setAntiAlias(true);
        smallTextPaint.setTextSize(typedArray.getDimension(R.styleable.TempView_textSizeSmall,22));
        scale = typedArray.getFloat(R.styleable.TempView_scale,0.8f);
        duration = typedArray.getInt(R.styleable.TempView_duration,1000);
        unit = typedArray.getString(R.styleable.TempView_unit);
        formatNumber=typedArray.getString(R.styleable.TempView_formatNumber);
        if (unit==null)
            unit="";
        if (formatNumber==null){
            formatNumber="%.0f";
        }
        typedArray.recycle();
        bounds1 = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getHeight();
        mWidth = getWidth();
        radus = (int) (Math.min(mWidth, mHeight) * scale / 2);
        lineL = (int) (radus * 0.16);
    }

    int endDegrees = 308;//总角度；
    double endDegreesR = Math.PI * 2 * endDegrees / 360;//总弧度；
    //    int offset = 0;
    String showText2;
    int currentDegree = 0;

    int mRadius = 140;

    float currPer = 0.02f;
    double currDegree = currPer * Math.PI * 2 * endDegrees / 360;//当前转过的弧度；
    double per_end = 0.66;

    Rect bounds1;
    @Override
    protected void onDraw(Canvas canvas) {
        currDegree = currPer * Math.PI * 2 * endDegrees / 360;
//        Log.e(TAG, "当前转过的角度=" + currDegree / Math.PI / 2 * 360);
        String result = String.format(formatNumber, currPer * 100);
        showText2 = result + unit;
        bigTextPaint.getTextBounds(showText2, 0, showText2.length(), bounds1);
        Paint.FontMetricsInt fontMetrics = bigTextPaint.getFontMetricsInt();
        int baseline = (mHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        int currColor = getCurrentColor(currPer, colors);
        bigTextPaint.setColor(currColor);
        canvas.drawText(showText2, 0, showText2.length(), (mWidth - bounds1.width()) / 2, baseline, bigTextPaint);
        canvas.translate(mWidth / 2, mHeight / 2);
        float offsetX = getOffsetX(showText2, smallTextPaint);
        float offsetY = getOffsetY(showText2, smallTextPaint);
        smallTextPaint.setColor(currColor);
        canvas.drawText(showText2, (float) (0 + offsetX + radus * Math.sin(currDegree - endDegreesR / 2)), (float) (-radus + offsetY + radus * (1 - Math.cos(currDegree - endDegreesR / 2))), smallTextPaint);
        canvas.drawCircle((float) (0 + (radus - 1.5 * lineL) * Math.sin(currDegree - endDegreesR / 2)), (float) (-(radus - 1.5 * lineL) + (radus - 1.5 * lineL) * (1 - Math.cos(currDegree - endDegreesR / 2))), lineL / 4, smallTextPaint);
        canvas.rotate(-endDegrees / 2, 0, 0);
//        paint.setShader(new SweepGradient(0, 0, new int[]{Color.RED,Color.GREEN}, null));
//        canvas.drawCircle(0,0,80,paint);
        for (int i = 0; i < endDegrees / 4 + 1; i++) {
            paint.setColor(getCurrentColor(i * 1.0f / (endDegrees / 4 + 1), colors));
            if (i == 0 || i == endDegrees / 4)
                canvas.drawLine(0, -radus - 0.5f * lineL, 0, -radus + lineL, paint);
            else
                canvas.drawLine(0, -radus, 0, -radus + lineL, paint);
            canvas.rotate(4, 0, 0);
        }
    }

    double PI2 = 2 * Math.PI;

    private float getOffsetX(String showString, Paint paint) {
        paint.getTextBounds(showString, 0, showString.length(), bounds1);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int w = bounds1.width();
        int h = bounds1.height();
        double l = Math.sqrt(w * w + h * h) / 2;

        return (float) (-l * Math.sin(currDegree + (360 - endDegrees) * 0.5 / 360 * PI2) - w / 2);

    }

    private float getOffsetY(String showString, Paint paint) {
        paint.getTextBounds(showString, 0, showString.length(), bounds1);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int w = bounds1.width();
        int h = bounds1.height();
        double l = Math.sqrt(w * w + h * h) / 2;
        return (float) (l * Math.cos(currDegree + (360 - endDegrees) * 0.5 / 360 * PI2) + h / 2);
    }

    public void startAni() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currPer = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    /**
     * 颜色渐变算法
     * 获取某个百分比下的渐变颜色值
     *
     * @param percent
     * @param colors
     * @return
     */
    float[][] f;
    float[] result;
    public int getCurrentColor(float percent, int[] colors) {
        for (int i = 0; i < colors.length; i++) {
            f[i][0] = (colors[i] & 0xff0000) >> 16;
            f[i][1] = (colors[i] & 0x00ff00) >> 8;
            f[i][2] = (colors[i] & 0x0000ff);
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < f.length; j++) {
                if (f.length == 1 || percent == j / (f.length - 1f)) {
                    result = f[j];
                } else {
                    if (percent > j / (f.length - 1f) && percent < (j + 1f) / (f.length - 1)) {
                        result[i] = f[j][i] - (f[j][i] - f[j + 1][i]) * (percent - j / (f.length - 1f)) * (f.length - 1f);
                    }
                }
            }
        }
        return Color.rgb((int) result[0], (int) result[1], (int) result[2]);
    }
}
