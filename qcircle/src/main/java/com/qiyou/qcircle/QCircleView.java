package com.qiyou.qcircle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by QiYou
 * on 2019/7/8
 */
public class QCircleView extends View {

    private final float CIRCLE_ANGLE = 360.0f;
    private final int ARC_NUMBER = 8;
    private Paint outpaint;
    private int centerX;
    private int centerY;
    private float radius;
    private Paint textpaint;
    private Paint linepaint;
    private Path linePath;
    private int btnCenterX;
    private int btnCenterY;
    private float btnRadius = 50.0f;
    private Paint btnpaint;
    private float x;
    private float y;
    private boolean isDrawOnInCircle = false;
    private BTN_TYPE btn_type = BTN_TYPE.BTN_DOWN;
    private float btn_down_x;
    private float btn_down_y;
    private double btnMoveRadius;
    private double centerBtnMoveRadius;
    private double radians;
    private double degrees;
    private float j_x_1;
    private float j_y_1;
    private float j_x_2;
    private float j_y_2;
    private RectF arcRectf = new RectF(0, 0, 0, 0);
    private Paint arcpaint;
    private float cX;


    enum BTN_TYPE {
        BTN_DOWN, BTN_MOVE, BTN_UP
    }

    public QCircleView(Context context) {
        this(context, null);
    }

    public QCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        outpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outpaint.setStyle(Paint.Style.FILL);
        outpaint.setColor(Color.GRAY);
        outpaint.setStrokeWidth(10);

        textpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textpaint.setStyle(Paint.Style.FILL);
        textpaint.setColor(Color.WHITE);
        textpaint.setStrokeWidth(10);

        linepaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linepaint.setStyle(Paint.Style.STROKE);
        linepaint.setColor(Color.WHITE);
        linepaint.setStrokeWidth(10);

        btnpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnpaint.setStyle(Paint.Style.FILL);
        btnpaint.setColor(Color.RED);
        btnpaint.setStrokeWidth(10);

        arcpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcpaint.setStyle(Paint.Style.FILL);
        arcpaint.setColor(Color.YELLOW);
        arcpaint.setStrokeWidth(10);
        arcpaint.setAlpha(100);


        linePath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2;
        centerY = h / 2;
        radius = Math.min(w, h) / 2.0f;
        btnCenterX = w / 4 * 3;
        btnCenterY = h / 8;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBtn(canvas);
        if (isDrawOnInCircle) {
            drawCircleView(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                btn_down_x = x;
                btn_down_y = y;
                if (onDownBtn(x, y)) {
                    btn_type = BTN_TYPE.BTN_DOWN;
                    isDrawOnInCircle = true;
                    postInvalidate();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                btnMoveRadius =
                        Math.sqrt((btn_down_x - x) * (btn_down_x - x) + (btn_down_y - y) * (btn_down_y - y));
                if (btnMoveRadius <= (radius / 2)) {
                    btn_type = BTN_TYPE.BTN_MOVE;
                    isDrawOnInCircle = true;
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                x = 0;
                y = 0;
                isDrawOnInCircle = false;
                postInvalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 判断手指点击位置是否在btn内
     *
     * @param eventX
     * @param eventY
     * @return
     */
    private boolean onDownBtn(float eventX, float eventY) {
        double eventDiatance =
                Math.sqrt((btnCenterX - eventX) * (btnCenterX - eventX) + (centerY - eventY) * (centerY - eventY));
        if (eventDiatance <= btnRadius) {
            return true;
        }
        return false;
    }

    /**
     * 手指点击位置画出按钮，并跟随手指移动而移动
     * 1、计算手指在点击按钮移动的距离
     * 2、计算园中中心按钮的移动距离
     * 3、中心按钮随手指移动距离 = 中心按钮所在内圆半径 / 按钮可移动距离半径（默认100）* 点击按钮随手指移动的距离
     * 4、点击按钮可移动的圆半径为100，中心按钮可移动的距离为内圆半径，当中心按钮移动过到扇形所在区域时，即为选中扇形
     * 5、如果中心按钮在两个扇形之间，中心按钮与扇形重叠区域超过中心按钮半径即为选中该扇形
     *
     * @param canvas
     */
    private void drawOnEventBtn(Canvas canvas) {
        switch (btn_type) {
            case BTN_DOWN:
                canvas.drawCircle(centerX, centerY, btnRadius, btnpaint);
                break;
            case BTN_MOVE:
                //点击按钮移动的点到按下的点X Y轴的坐标差
                float moveX = x - btn_down_x;
                float moveY = y - btn_down_y;
                //内圆圆心和点击按钮的坐标比例
                float bX = centerX / btnCenterY;
                float bY = centerY / btnCenterY;
                //中心按钮相对与点击按钮的圆心坐标
                float mX = centerX + moveX * bX;
                float mY = centerY + moveY * bY;
                //中心按钮移动的圆心坐标到内圆圆心坐标的距离
                centerBtnMoveRadius =
                        Math.sqrt((mX - centerX) * (mX - centerX) + (mY - centerY) * (mY - centerY));
                if (centerBtnMoveRadius < (radius / 2)) {
                    //在内圆内显示
                    canvas.drawCircle(mX, mY, btnRadius, btnpaint);
                } else {
                    /**
                     * 计算 A(mX,mY) 与 O(centerX,centerY) 形成直线与X轴的夹角
                     * 一、在X轴之下
                     * 二、在X轴之上
                     */
                    if ((mX > centerX && mY > centerY) || (mX < centerX && mY > centerY)) {
                        radians = Math.atan2(mY - centerY, mX - centerX);
                        degrees = radians * (180 / Math.PI);
                    }
                    if ((mX > centerX && mY < centerY) || (mX < centerX && mY < centerY)) {
                        radians = Math.atan2(centerY - mY, mX - centerX);
                        degrees = radians * (180 / Math.PI);
                    }
                    measurePonit(centerX, centerY, mX, mY, radius / 2, centerX, centerY);
                    if (degrees > 90 && degrees < 180) {
                        //Y轴左
                        canvas.drawCircle(j_x_2, j_y_2, btnRadius, btnpaint);
                        if (j_y_2 > centerY) {
                            cX = (float) (centerX + Math.cos(Math.toRadians(getArcAngle() * 3)) * (radius / 2));
                            if (j_x_2 > cX) {
                                arcpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                                canvas.drawArc(arcRectf, getArcAngle() * 2, getArcAngle(), true,
                                        arcpaint);
                            } else {
                                arcpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                                canvas.drawArc(arcRectf, getArcAngle() * 3, getArcAngle(), true,
                                        arcpaint);
                            }
                        } else {
                            cX = (float) (centerX + Math.cos(Math.toRadians(-(getArcAngle() * 3))) * (radius / 2));
                            if (j_x_2 > cX) {
                                arcpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                                canvas.drawArc(arcRectf, getArcAngle() * 5, getArcAngle(), true,
                                        arcpaint);
                            } else {
                                arcpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                                canvas.drawArc(arcRectf, getArcAngle() * 4, getArcAngle(), true,
                                        arcpaint);
                            }
                        }
                    } else {
                        //Y轴右
                        canvas.drawCircle(j_x_1, j_y_1, btnRadius, btnpaint);
                        if (j_y_1 > centerY) {
                            cX = (float) (centerX + Math.cos(Math.toRadians(getArcAngle())) * (radius / 2));
                            if (j_x_1 < cX) {
                                arcpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                                canvas.drawArc(arcRectf, getArcAngle(), getArcAngle(), true,
                                        arcpaint);
                            } else {
                                arcpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                                canvas.drawArc(arcRectf, 0, getArcAngle(), true, arcpaint);
                            }
                        } else {
                            cX = (float) (centerX + Math.cos(Math.toRadians(-getArcAngle())) * (radius / 2));
                            if (j_x_1 < cX) {
                                arcpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                                canvas.drawArc(arcRectf, -90, getArcAngle(), true, arcpaint);
                            } else {
                                arcpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                                canvas.drawArc(arcRectf, -getArcAngle(), getArcAngle(), true,
                                        arcpaint);
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * 计算直线与圆相交的两点坐标
     *
     * @param x1      起点直线的X轴坐标
     * @param y1      起点直线的轴坐标
     * @param x2      终点直线的X轴坐标
     * @param y2      终点直线的Y轴坐标
     * @param r       圆半径
     * @param centerx 圆X轴坐标
     * @param centery 圆y轴坐标
     */
    private void measurePonit(float x1, float y1, float x2, float y2, float r, float centerx,
                              float centery) {
        //直线斜率不存在，垂直与X轴
        if ((x1 == x2) && (y1 != y2)) {
            if (Math.abs(centerx - x1) < r) {
                double y = Math.sqrt(r * r - ((x1 - centerx) * (x1 - centerx)));
                j_x_1 = x1;
                j_y_1 = (float) (centery + y);
                j_x_2 = x1;
                j_y_2 = (float) (centery - y);
            }
        }
        //两点重合
        else if ((x1 == x2) && (y1 == y2)) {
            j_x_1 = x1;
            j_y_1 = y1;
            j_x_2 = x2;
            j_y_2 = y2;
        }
        //直线斜率为0，平行于X轴
        else if ((y1 == y2) && (x1 != x2)) {
            double area = Math.abs(centery - y1);
            if (area <= r) {
                double x = Math.sqrt(r * r - ((y1 - centery) * (y1 - centery)));
                j_x_1 = (float) (centerx + x);
                j_y_1 = y1;
                j_x_2 = (float) (centerx - x);
                j_y_2 = y1;
            }
        } else {
            double k = (y2 - y1) / (x2 - x1);
            double b = y2 - k * (x2);
            double del =
                    4 * Math.pow((k * b - centerx - k * (centery)), 2) - 4 * (1 + k * k) *
                            (Math.pow((centerx), 2) + Math.pow((b - centery), 2) - r * r);
            if (del > 0) {
                double tmp = 2 * (k * b - centerx - k * centery);
                j_x_1 = (float) ((-tmp + Math.sqrt(del)) / (2 * (1 + k * k)));
                j_y_1 = (float) (k * (j_x_1) + b);
                j_x_2 = (float) ((-tmp - Math.sqrt(del)) / (2 * (1 + k * k)));
                j_y_2 = (float) (k * (j_x_2) + b);
            }
        }
    }


    /**
     * 利用混合模式，画两个圆，形成圆环
     *
     * @param canvas
     */
    private void drawCircleView(Canvas canvas) {
        arcRectf.left = centerX - radius;
        arcRectf.top = centerY - radius;
        arcRectf.right = centerX + radius;
        arcRectf.bottom = centerY + radius;
        int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        canvas.drawCircle(centerX, centerY, radius, outpaint);
        drawBitmapAndText(canvas);
        outpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        drawLines(canvas);
        canvas.drawCircle(centerX, centerY, radius / 2, outpaint);
        outpaint.setXfermode(null);
        drawOnEventBtn(canvas);
        canvas.restoreToCount(layerId);
    }

    /**
     * 把圆平均分成8份
     *
     * @param canvas
     */
    private void drawLines(Canvas canvas) {
        for (int i = 0; i < ARC_NUMBER; i++) {
            linePath.moveTo(centerX, centerY);
            linePath.lineTo((float) Math.cos(Math.toRadians(i * getArcAngle())) * radius + centerX,
                    (float) Math.sin(Math.toRadians(i * getArcAngle())) * radius + centerY);
        }
        canvas.drawPath(linePath, linepaint);
    }

    /**
     * 画图片和文字描述
     *
     * @param canvas
     */
    private void drawBitmapAndText(Canvas canvas) {
        Map<Integer, String> msgMap = getMsgMap();
        Map<Integer, Integer> iconMap = getIconMap();
        float imgWidth = radius / 4;
        for (int i = 1; i <= 15; i += 2) {
            float bitmapCenterX =
                    (float) (centerX + Math.cos(Math.toRadians((getArcAngle() / 2) * i - 90)) * (radius / 4) * 3);
            float bitmapCenterY =
                    (float) (centerY + Math.sin(Math.toRadians((getArcAngle() / 2) * i - 90)) * (radius / 4 * 3));

            RectF rectF = new RectF(
                    bitmapCenterX - imgWidth / 2,
                    bitmapCenterY - imgWidth / 2 + (imgWidth / 4),
                    bitmapCenterX + imgWidth / 2,
                    bitmapCenterY + imgWidth / 2 + (imgWidth / 4)
            );

            Matrix matrix = new Matrix();
            matrix.setRotate((getArcAngle() / 2) * i, bitmapCenterX, bitmapCenterY);
            canvas.setMatrix(matrix);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconMap.get(i));
            canvas.drawBitmap(bitmap, null, rectF, null);
            String text = msgMap.get(i);
            textpaint.setTextSize(30);
            textpaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetricsInt fontMetricsInt = textpaint.getFontMetricsInt();
            float baseLine =
                    (rectF.bottom + rectF.top - fontMetricsInt.bottom - fontMetricsInt.top) / 2;
            canvas.drawText(text,
                    rectF.centerX(),
                    baseLine - radius / 5,
                    textpaint);
            matrix.reset();
            canvas.setMatrix(matrix);
        }
    }

    /**
     * 点击按钮
     *
     * @param canvas
     */
    private void drawBtn(Canvas canvas) {
        float btnLeftX = btnCenterX - btnRadius;
        float btnRightX = btnCenterX + btnRadius;
        float btnTopY = btnCenterY - btnRadius;
        float btnBottomY = btnCenterY + btnRadius;
        RectF rectF = new RectF(btnLeftX, btnTopY, btnRightX, btnBottomY);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_btn);
        canvas.drawBitmap(bitmap, null, rectF, null);
    }

    /**
     * 获取每个扇形角度
     *
     * @return
     */
    private float getArcAngle() {
        return CIRCLE_ANGLE / ARC_NUMBER;
    }

    /**
     * 获取文字描述map 防守、集合、需要子弹、需要配件、快上车、需要药品、有人来过、进攻
     *
     * @return
     */
    private Map<Integer, String> getMsgMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "防守");
        map.put(3, "集合");
        map.put(5, "需要子弹");
        map.put(7, "需要配件");
        map.put(9, "快上车");
        map.put(11, "需要药品");
        map.put(13, "有人来过");
        map.put(15, "进攻");
        return map;
    }

    /**
     * 获取图片
     *
     * @return
     */
    private Map<Integer, Integer> getIconMap() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, R.drawable.ic_fangshou);
        map.put(3, R.drawable.ic_jihe);
        map.put(5, R.drawable.ic_zidan);
        map.put(7, R.drawable.ic_peijian);
        map.put(9, R.drawable.ic_car);
        map.put(11, R.drawable.ic_yaopin);
        map.put(13, R.drawable.ic_jiaoyin);
        map.put(15, R.drawable.ic_jingong);
        return map;
    }
}
