package com.example.huhanghao.sticknessdot_hhh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by huhanghao on 2017/3/27.
 */

public class SicknessDot extends FrameLayout {
    private PointF mStartPoint, mCurPoint;
    private Paint mPaint;
    private Path mPath;
    private boolean mTouch;
    private TextView mTipTextView;
    private float DEFAULT_RADIUS = 50;
    private float mRadius = DEFAULT_RADIUS;
    private ImageView exploredImageView;
    private boolean isAnimStart = false;

    public SicknessDot(Context context) {
        super(context);
        initView();
    }

    public SicknessDot(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SicknessDot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * 初始化画笔和起始的原点
     */
    private void initView() {
        mStartPoint = new PointF(100, 100);
        mCurPoint = new PointF();

        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);

        mPath = new Path();

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTipTextView = new TextView(getContext());
        mTipTextView.setLayoutParams(params);
        mTipTextView.setPadding(10, 10, 10, 10);
        mTipTextView.setBackgroundResource(R.color.colorAccent);
        mTipTextView.setTextColor(Color.WHITE);
        mTipTextView.setText("99+");

        exploredImageView = new ImageView(getContext());
        exploredImageView.setLayoutParams(params);
        exploredImageView.setImageResource(R.drawable.anim);
        exploredImageView.setVisibility(View.INVISIBLE);
        addView(mTipTextView);
        addView(exploredImageView);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(new RectF(0, 0, getWidth(), getHeight()), mPaint, Canvas.ALL_SAVE_FLAG);
        if (!mTouch || isAnimStart) {
            mTipTextView.setX(mStartPoint.x - mTipTextView.getWidth() / 2);
            mTipTextView.setY(mStartPoint.y - mTipTextView.getHeight() / 2);
        } else {
            calculatePath();
            canvas.drawPath(mPath, mPaint);
            canvas.drawCircle(mStartPoint.x, mStartPoint.y, mRadius, mPaint);
            canvas.drawCircle(mCurPoint.x, mCurPoint.y, mRadius, mPaint);

            // 将textview的中心放在当前手指位置
            mTipTextView.setX(mCurPoint.x - mTipTextView.getWidth() / 2);
            mTipTextView.setY(mCurPoint.y - mTipTextView.getHeight() / 2);
        }
        canvas.restore();
        super.dispatchDraw(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {

                // 判断点击是否在rect所包围的区域
                Rect rect = new Rect();
                int[] location = new int[2];
                mTipTextView.getLocationOnScreen(location);  // 获取该view被点点击时的location，并给location
                rect.left = location[0];
                rect.top = location[1];
                rect.right = mTipTextView.getWidth() + location[0];
                rect.bottom = mTipTextView.getHeight() + location[1];
                if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    mTouch = true;
                } else {
                    mTouch = false;
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                mTouch = false;
            }
        }
        mCurPoint.set(event.getX(), event.getY());
        postInvalidate();
        return true;
    }

    /**
     * 计算拖动时的贝塞尔曲线路径
     */
    private void calculatePath() {

        float x = mCurPoint.x;
        float y = mCurPoint.y;
        float startX = mStartPoint.x;
        float startY = mStartPoint.y;
        // 根据角度算出四边形的四个点
        float dx = x - startX;
        float dy = y - startY;
        double a = Math.atan(dy / dx);
        float offsetX = (float) (mRadius * Math.sin(a));
        float offsetY = (float) (mRadius * Math.cos(a));

        // 根据移动距离改变圆的半径大小
        float distance = (float) Math.sqrt(Math.pow(y - startY, 2) + Math.pow(x - startX, 2));
        mRadius = -distance / 15 + DEFAULT_RADIUS;
        if (mRadius < 9) {
            isAnimStart = true;
            exploredImageView.setX(mCurPoint.x - mTipTextView.getWidth() / 2);
            exploredImageView.setY(mCurPoint.y - mTipTextView.getHeight() / 2);
            exploredImageView.setVisibility(View.VISIBLE);
            ((AnimationDrawable) exploredImageView.getDrawable()).start();

            mTipTextView.setVisibility(View.GONE);
        }

        // 根据角度算出四边形的四个点
        float x1 = startX + offsetX;
        float y1 = startY - offsetY;

        float x2 = x + offsetX;
        float y2 = y - offsetY;

        float x3 = x - offsetX;
        float y3 = y + offsetY;

        float x4 = startX - offsetX;
        float y4 = startY + offsetY;

        float anchorX = (startX + x) / 2;
        float anchorY = (startY + y) / 2;

        mPath.reset();
        mPath.moveTo(x1, y1);
        mPath.quadTo(anchorX, anchorY, x2, y2);
        mPath.lineTo(x3, y3);
        mPath.quadTo(anchorX, anchorY, x4, y4);
        mPath.lineTo(x1, y1);
    }
}
