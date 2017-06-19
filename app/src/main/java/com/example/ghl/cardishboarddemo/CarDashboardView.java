package com.example.ghl.cardishboarddemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by ghl on 2017/6/5.
 */

public class CarDashboardView extends View implements Runnable {

    private int mWidth, mHeight;  //View 的宽和高
    private Paint outerArcPaint;
    private Paint outerSmallArcPaint;
    private Paint speedPaint;
    private Paint scaleLinePaint;
    private Paint scaleTextPaint;
    private Paint unitTextPaint;
    private Paint pointerPaint;
    private Paint insideCirclePaint;

    private int mStartAngle = 150; // 起始角度
    private int mSweepAngle = 240; // 绘制角度
    private int offset = 50;  //图形与边界的距离
    private int mMin = 0; // 速度最小值
    private int mMax = 180; // 速度最大值
    private int mSection = 9; // 值域（mMax-mMin）等分份数
    private int mVelocity = mMin; // 实时速度
    private float mCenterX, mCenterY; // 圆心坐标
    private int mRadius; // 扇形半径
    private int mLength1; // 长刻度的相对圆弧的长度
    private int mLength2; // 刻度读数顶部的相对圆弧的长度
    private int mStrokeWidth; // 画笔宽度
    private String mHeaderText = "km/h"; // 表头
    private String[] mTexts;
    private Rect mRectText;
    private int[] mColors;//渐变线颜色
    private int type; //速度控制模式  0 自然减速 1 油门  2 刹车
    private boolean start = true; //开始重绘


    public CarDashboardView(Context context) {
        this(context, null);
    }

    public CarDashboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarDashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mStrokeWidth = dp2px(3);
        mLength1 = dp2px(8) + mStrokeWidth;
        mLength2 = mLength1 + dp2px(4);

        mColors = new int[]{
                Color.GREEN, Color.YELLOW, Color.RED,
                Color.GREEN, Color.YELLOW, Color.RED,
                Color.GREEN, Color.YELLOW, Color.RED,
                Color.GREEN, Color.YELLOW, Color.RED,
                Color.GREEN, Color.YELLOW, Color.RED,
                Color.GREEN, Color.YELLOW, Color.RED,
        };

        mTexts = new String[mSection + 1]; // 需要显示mSection + 1个刻度读数
        for (int i = 0; i < mTexts.length; i++) {
            int n = (mMax - mMin) / mSection;
            mTexts[i] = String.valueOf(mMin + i * n);
        }

        //最外层大圆弧的画笔
        outerArcPaint = new Paint();
        outerArcPaint.setStyle(Paint.Style.STROKE);
        outerArcPaint.setStrokeWidth(mStrokeWidth);
        outerArcPaint.setColor(Color.parseColor("#5555aa"));
        outerArcPaint.setAntiAlias(true);//防止边缘锯齿

        //最外层小圆弧的画笔
        outerSmallArcPaint = new Paint();
        outerSmallArcPaint.setAntiAlias(true);//防止边缘锯齿
        outerSmallArcPaint.setStyle(Paint.Style.STROKE);
        outerSmallArcPaint.setStrokeCap(Paint.Cap.ROUND);//设置线冒样式，取值有Cap.ROUND(圆形线冒)、Cap.SQUARE(方形线冒)、Paint.Cap.BUTT(无线冒)
        outerSmallArcPaint.setStrokeWidth(dp2px(3));
        SweepGradient sweepGradient = new SweepGradient(mCenterX, mCenterY, mColors, null);
        outerSmallArcPaint.setShader(sweepGradient);//填充颜色


        //内部圆的画笔
        insideCirclePaint = new Paint();
        insideCirclePaint.setColor(Color.parseColor("#27408B"));
        insideCirclePaint.setStyle(Paint.Style.FILL);
        insideCirclePaint.setAntiAlias(true);//防止边缘锯齿

        //绘制速度值的画笔
        speedPaint = new Paint();
        speedPaint.setColor(Color.parseColor("#ff0000"));
        speedPaint.setAntiAlias(true);//防止边缘锯齿
        speedPaint.setStrokeWidth(4);

        //刻度线的画笔
        scaleLinePaint = new Paint();
        scaleLinePaint.setStrokeWidth(4);
        scaleLinePaint.setAntiAlias(true);//防止边缘锯齿
        scaleLinePaint.setColor(Color.parseColor("#ECEFF1"));

        //刻度数字的画笔
        scaleTextPaint = new Paint();
        scaleTextPaint.setTextSize(sp2px(16));
        scaleTextPaint.setTypeface(Typeface.DEFAULT);
        scaleTextPaint.setColor(Color.parseColor("#ECEFF1"));
        scaleTextPaint.setAntiAlias(true);//防止边缘锯齿
        mRectText = new Rect();

        //绘制单位km/h的画笔
        unitTextPaint = new Paint();
        unitTextPaint.setColor(Color.parseColor("#ECEFF1"));
        unitTextPaint.setAntiAlias(true);//防止边缘锯齿
        unitTextPaint.setTypeface(Typeface.DEFAULT);
        unitTextPaint.setTextSize(sp2px(16));
        unitTextPaint.setTextAlign(Paint.Align.CENTER);

        //指针的画笔
        pointerPaint = new Paint();
        pointerPaint.setStrokeCap(Paint.Cap.ROUND);//设置线的两端为圆弧
        pointerPaint.setColor(Color.parseColor("#55aaaa"));
        pointerPaint.setStrokeWidth(dp2px(3));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
        mCenterX = mWidth / 2f;
        mCenterY = mHeight / 2f;
        mRadius = (mHeight - offset * 2) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawArc(canvas);
        drawText(canvas);
        drawScale(canvas);
        drawPointer(canvas);
    }

    /**
     * 画指针
     *
     * @param canvas
     */
    private void drawPointer(Canvas canvas) {
        float θ = mStartAngle + mSweepAngle * (mVelocity - mMin) / (mMax - mMin); // 指针与水平线夹角
        float[] p1 = getCoordinatePoint(mRadius - mStrokeWidth, θ);
        float[] p2 = getCoordinatePoint(mRadius / 10 * 6, θ);
        canvas.drawLine(p1[0], p1[1], p2[0], p2[1], pointerPaint);
    }

    /**
     * 画刻度
     *
     * @param canvas
     */
    private void drawScale(Canvas canvas) {

        /**
         * 画长刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        double cos = Math.cos(Math.toRadians(mStartAngle - 180));
        double sin = Math.sin(Math.toRadians(mStartAngle - 180));
        int xOff = (mWidth - mHeight) / 2 + offset + mStrokeWidth / 2;
        int yOff = offset - mStrokeWidth / 2;

        float x0 = (float) (xOff + mRadius * (1 - cos));
        float y0 = (float) (yOff + mRadius * (1 - sin));
        float x1 = (float) (xOff + mRadius - (mRadius - mLength1) * cos);
        float y1 = (float) (yOff + mRadius - (mRadius - mLength1) * sin);

        canvas.save();
        canvas.drawLine(x0, y0, x1, y1, scaleLinePaint);
        float angle = mSweepAngle * 1f / mSection;
        for (int i = 0; i < mSection; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            canvas.drawLine(x0, y0, x1, y1, scaleLinePaint);
        }
        canvas.restore();


        //画刻度读数
        float α;
        float[] p;
        angle = mSweepAngle * 1f / mSection;
        for (int i = 0; i <= mSection; i++) {
            α = mStartAngle + angle * i;
            p = getCoordinatePoint(mRadius - mLength2, α);
            if (α % 360 > 135 && α % 360 < 225) {
                scaleTextPaint.setTextAlign(Paint.Align.LEFT);
            } else if ((α % 360 >= 0 && α % 360 < 45) || (α % 360 > 315 && α % 360 <= 360)) {
                scaleTextPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                scaleTextPaint.setTextAlign(Paint.Align.CENTER);
            }
            scaleTextPaint.getTextBounds(mHeaderText, 0, mTexts[i].length(), mRectText);
            int txtH = mRectText.height();
            if (i <= 1 || i >= mSection - 1) {
                canvas.drawText(mTexts[i], p[0], p[1] + txtH / 2, scaleTextPaint);
            } else if (i == 3) {
                canvas.drawText(mTexts[i], p[0] + txtH / 2, p[1] + txtH, scaleTextPaint);
            } else if (i == mSection - 3) {
                canvas.drawText(mTexts[i], p[0] - txtH / 2, p[1] + txtH, scaleTextPaint);
            } else {
                canvas.drawText(mTexts[i], p[0], p[1] + txtH, scaleTextPaint);
            }
        }
    }

    /**
     * 绘制文本
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {

        //绘制速度值
        int xOffset = dp2px(22);
        if (mVelocity >= 100) {
            drawDigitalTube(canvas, mVelocity / 100, -xOffset);
            drawDigitalTube(canvas, (mVelocity - 100) / 10, 0);
            drawDigitalTube(canvas, mVelocity % 100 % 10, xOffset);
        } else if (mVelocity >= 10) {
            drawDigitalTube(canvas, -1, -xOffset);
            drawDigitalTube(canvas, mVelocity / 10, 0);
            drawDigitalTube(canvas, mVelocity % 10, xOffset);
        } else {
            drawDigitalTube(canvas, -1, -xOffset);
            drawDigitalTube(canvas, -1, 0);
            drawDigitalTube(canvas, mVelocity, xOffset);
        }

        //绘制单位km/h
        canvas.drawText(mHeaderText, mWidth / 2, mHeight / 2 + unitTextPaint.getTextSize() + offset, unitTextPaint);

    }

    /**
     * 画圆弧
     *
     * @param canvas
     */
    private void drawArc(Canvas canvas) {

        //画外部大圆弧
        RectF mRectFArc = new RectF((mWidth - mHeight) / 2 + offset, offset, mWidth - (mWidth - mHeight) / 2 - offset, mHeight - offset);
        canvas.drawArc(mRectFArc, mStartAngle - 15, mSweepAngle + 30, false, outerArcPaint);

        //画外部小圆弧
        RectF mSmallRectFArc = new RectF((mWidth - mHeight) / 2 + offset + mLength1, offset + mLength1, mWidth - (mWidth - mHeight) / 2 - offset - mLength1, mHeight - offset - mLength1);
        canvas.drawArc(mSmallRectFArc, mStartAngle - 15 + mSweepAngle + 30 - 360, 360 - (mSweepAngle + 30), false, outerSmallArcPaint);

        //画内部中心圆
        canvas.drawCircle(mCenterX, mCenterY, mRadius / 2, insideCirclePaint);

    }

    /**
     * 数码管样式
     */
    //      1
    //      ——
    //   2 |  | 3
    //      —— 4
    //   5 |  | 6
    //      ——
    //       7
    private void drawDigitalTube(Canvas canvas, int num, int xOffset) {

        int lx = dp2px(5);
        int ly = dp2px(10);
        int gap = dp2px(2);
        float x = mCenterX + xOffset;
        float y = mCenterY - (gap * 4 + ly * 2);

        // 1
        speedPaint.setAlpha(num == -1 || num == 1 || num == 4 ? 100 : 255);
        canvas.drawLine(x - lx, y, x + lx, y, speedPaint);
        // 2
        speedPaint.setAlpha(num == -1 || num == 1 || num == 2 || num == 3 || num == 7 ? 100 : 255);
        canvas.drawLine(x - lx - gap, y + gap, x - lx - gap, y + gap + ly, speedPaint);
        // 3
        speedPaint.setAlpha(num == -1 || num == 5 || num == 6 ? 100 : 255);
        canvas.drawLine(x + lx + gap, y + gap, x + lx + gap, y + gap + ly, speedPaint);
        // 4
        speedPaint.setAlpha(num == -1 || num == 0 || num == 1 || num == 7 ? 100 : 255);
        canvas.drawLine(x - lx, y + gap * 2 + ly, x + lx, y + gap * 2 + ly, speedPaint);
        // 5
        speedPaint.setAlpha(num == -1 || num == 1 || num == 3 || num == 4 || num == 5 || num == 7
                || num == 9 ? 100 : 255);
        canvas.drawLine(x - lx - gap, y + gap * 3 + ly,
                x - lx - gap, y + gap * 3 + ly * 2, speedPaint);
        // 6
        speedPaint.setAlpha(num == -1 || num == 2 ? 100 : 255);
        canvas.drawLine(x + lx + gap, y + gap * 3 + ly,
                x + lx + gap, y + gap * 3 + ly * 2, speedPaint);
        // 7
        speedPaint.setAlpha(num == -1 || num == 1 || num == 4 || num == 7 ? 100 : 255);
        canvas.drawLine(x - lx, y + gap * 4 + ly * 2, x + lx, y + gap * 4 + ly * 2, speedPaint);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

    public float[] getCoordinatePoint(int radius, float angle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    public int getVelocity() {
        return mVelocity;
    }

    public void setVelocity(int velocity) {

        mVelocity = velocity;
        postInvalidate();
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    //设置速度控制模式
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void run() {
        int speedChange;
        while (start) {
            switch (type) {
                case 0://自然减速
                    speedChange = -1;
                    break;
                case 1://油门
                    speedChange = 3;
                    break;
                case 2://刹车
                    speedChange = -5;
                    break;
                default://自然减速
                    speedChange = -1;
                    break;
            }
            mVelocity += speedChange;
            if (mVelocity < mMin) {
                mVelocity = mMin;
            }else if(mVelocity>mMax) {
                mVelocity = mMax;
            }
            try {
                Thread.sleep(50);
                setVelocity(mVelocity);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
