package com.oleg.hubal.animationview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by User on 09.11.2016.
 */

public class DyingLightProgressView extends View {
    private static final int CIRCLE_FORM = 0;
    private static final int RECTANGLE_FORM = 1;
    private static final int IMAGE_FORM = 2;

    private int mBigShapeSize, mSmallShapeSize, mShapeOffset;
    private int redrawDelay = 1000 / 30;
    private int mShapeWidth = 600;
    private int mShapeHeight = 600;

    private float mShapeSize;
    private int mShapeForm;
    private int mShapeColor;
    private int mStrokeColor;
    private int mStrokeSize;
    private int mAnimationDuration;

    private Paint mSmallShapePaint;
    private Paint mBigShapePaint;
    private Paint mStrokePaint;

    private ArrayList<MotionShape> mShapeList;
    private MotionShape mCenterShape;

    private Handler mHandler = new Handler();

    private Bitmap mSmallBitmap, mBigBitmap;

    private boolean isViewNarrowed = false;

    public DyingLightProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupAttributes(attrs);
        setupPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (width > height) {
            width = height;
        } else {
            height = width;
        }

        setMeasuredDimension(mShapeWidth, mShapeHeight);

        mShapeWidth = width;
        mShapeHeight = height;

        mBigShapeSize = (int) (mShapeWidth / 3 * mShapeSize);
        mSmallShapeSize = mBigShapeSize / 4;
        mShapeOffset = mSmallShapeSize;
        createShapes();
        startAnimation();
    }

    private void setupPaint() {
        mSmallShapePaint = new Paint();
        mSmallShapePaint.setColor(mShapeColor);

        mBigShapePaint = new Paint();
        mBigShapePaint.setColor(mShapeColor);

        mStrokePaint = new Paint();
        mStrokePaint.setStrokeWidth(mStrokeSize);
        mStrokePaint.setColor(mStrokeColor);
    }

    private void setupAttributes(AttributeSet attrs) {
        TypedArray attrArray = getContext().getTheme()
                .obtainStyledAttributes(attrs, R.styleable.DyingLightProgressView, 0, 0);
        try {
            mShapeSize = attrArray.getFloat(R.styleable.DyingLightProgressView_shape_size, 1);
            mShapeForm = attrArray.getInt(R.styleable.DyingLightProgressView_shape_form, 0);
            mShapeColor = attrArray.getColor(
                    R.styleable.DyingLightProgressView_shape_color, Color.BLACK);
            mStrokeSize = attrArray.getInt(R.styleable.DyingLightProgressView_stroke_size, 6);
            mStrokeColor = attrArray.getColor(
                    R.styleable.DyingLightProgressView_stroke_color, Color.BLACK);
            mAnimationDuration = attrArray.getInt(
                    R.styleable.DyingLightProgressView_animation_duration, 1200);
        } finally {
            attrArray.recycle();
        }
    }

    private void createShapes() {
        int shapeStart = mShapeOffset;
        int shapeEnd = mShapeWidth - mShapeOffset;
        int shapeCenter = mShapeWidth / 2;

        Point centerPoint = new Point(shapeCenter, shapeCenter);
        Point botCenter = new Point(shapeCenter, shapeEnd);
        Point rightCenter = new Point(shapeEnd, shapeCenter);
        Point topCenter = new Point(shapeCenter, shapeStart);
        Point leftCenter = new Point(shapeStart, shapeCenter);

        mShapeList = new ArrayList<>();

        MotionShape botLeft = new MotionShape(shapeStart, shapeEnd, mSmallShapeSize);
        botLeft.addAnimationPoint(botCenter);
        botLeft.addAnimationPoint(centerPoint);
        botLeft.createAnimators();
        mShapeList.add(botLeft);

        MotionShape botRight = new MotionShape(shapeEnd, shapeEnd, mSmallShapeSize);
        botRight.addAnimationPoint(rightCenter);
        botRight.addAnimationPoint(centerPoint);
        botRight.createAnimators();
        mShapeList.add(botRight);

        MotionShape topRight = new MotionShape(shapeEnd, shapeStart, mSmallShapeSize);
        topRight.addAnimationPoint(topCenter);
        topRight.addAnimationPoint(centerPoint);
        topRight.createAnimators();
        mShapeList.add(topRight);

        MotionShape topLeft = new MotionShape(shapeStart, shapeStart, mSmallShapeSize);
        topLeft.addAnimationPoint(leftCenter);
        topLeft.addAnimationPoint(centerPoint);
        topLeft.createAnimators();
        mShapeList.add(topLeft);

        mCenterShape = new MotionShape(shapeCenter, shapeCenter, mBigShapeSize);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mBigBitmap = Bitmap.createScaledBitmap(bitmap, mBigShapeSize, mBigShapeSize, false);
        mSmallBitmap = Bitmap.createScaledBitmap(bitmap, mSmallShapeSize, mSmallShapeSize, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShapeForm == CIRCLE_FORM) {
            drawCircles(canvas);
        } else if (mShapeForm == RECTANGLE_FORM) {
            drawRectangles(canvas);
        } else if (mShapeForm == IMAGE_FORM) {
            drawImages(canvas);
        }

        for (int firstShape = 0; firstShape < mShapeList.size(); firstShape++) {
            int secondShape = (firstShape + 1) % mShapeList.size();
            drawShapeLine(canvas, mShapeList.get(firstShape), mShapeList.get(secondShape));
        }
        drawShapeLine(canvas, mShapeList.get(0), mShapeList.get(2));
        drawShapeLine(canvas, mShapeList.get(1), mShapeList.get(3));
    }

    private void drawImages(Canvas canvas) {
        for (MotionShape shape : mShapeList) {
            canvas.drawBitmap(mSmallBitmap, shape.getX() - shape.getSize() / 2,
                    shape.getY() - shape.getSize() / 2, mSmallShapePaint);
        }
        canvas.drawBitmap(mBigBitmap, mCenterShape.getX() - mCenterShape.getSize() / 2,
                mCenterShape.getY() - mCenterShape.getSize() / 2, mBigShapePaint);
    }

    private void drawRectangles(Canvas canvas) {
        int halfSize = mCenterShape.getSize() / 2;
        for (MotionShape shape : mShapeList) {
            halfSize = shape.getSize() / 2;
            canvas.drawRect(shape.getX() - halfSize, shape.getY() - halfSize,
                    shape.getX() + halfSize, shape.getY() + halfSize, mSmallShapePaint);
        }
        canvas.drawRect(mCenterShape.getX() - halfSize, mCenterShape.getY() - halfSize,
                mCenterShape.getX() + halfSize, mCenterShape.getY() + halfSize, mBigShapePaint);
    }

    private void drawCircles(Canvas canvas) {
        for (MotionShape shape : mShapeList) {
            canvas.drawCircle(shape.getX(), shape.getY(), shape.getSize() / 2, mSmallShapePaint);
        }
        canvas.drawCircle(mCenterShape.getX(), mCenterShape.getY(),
                mCenterShape.getSize() / 2, mBigShapePaint);
    }

    private void drawShapeLine(Canvas canvas, MotionShape startShape, MotionShape endShape) {
        canvas.drawLine(startShape.getX(), startShape.getY(),
                endShape.getX(), endShape.getY(), mStrokePaint);
    }

    private void startAnimation() {
        startAlphaAnimation();

        Runnable mTick = new Runnable() {
            @Override
            public void run() {
                invalidate();
                mHandler.postDelayed(this, redrawDelay);
            }
        };
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }

    private void startMoveAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        for (int i = 0; i < mShapeList.size(); i++) {
            MotionShape motionShape = mShapeList.get(i);
            ObjectAnimator shapeAnimator;
            if (isViewNarrowed) {
                shapeAnimator = motionShape.getReverseMoveAnimator();
            } else {
                shapeAnimator = motionShape.getMoveAnimator();
            }
            animatorSet.play(shapeAnimator).after(mAnimationDuration / mShapeList.size() * i);
        }
        animatorSet.addListener(new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                isViewNarrowed = !isViewNarrowed;
                startAlphaAnimation();
            }
        });
        animatorSet.start();
    }

    private void startAlphaAnimation() {
        int alphaFrom = (isViewNarrowed) ? 0 : 255;
        int alphaTo = (isViewNarrowed) ? 255 : 0;
        Paint paint = (isViewNarrowed) ? mSmallShapePaint : mBigShapePaint;
        int repeat = 4;

        ObjectAnimator alphaAnimator = ObjectAnimator.ofInt(paint, "alpha", alphaFrom, alphaTo);
        alphaAnimator.setDuration(mAnimationDuration / repeat);
        alphaAnimator.setRepeatCount(repeat);
        alphaAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        alphaAnimator.addListener(new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                startMoveAnimation();
            }
        });
        alphaAnimator.start();
    }

    private abstract class OnAnimationEndListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        abstract public void onAnimationEnd(Animator animator);

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    private class MotionShape {
        private ArrayList<Point> animationPoints;
        private ObjectAnimator moveAnimator, reverseMoveAnimator;

        private int size;
        private int x;
        private int y;

        private MotionShape(int x, int y, int size) {
            this.size = size;
            this.x = x;
            this.y = y;
            animationPoints = new ArrayList<>();
            animationPoints.add(new Point(x, y));
        }

        public int getSize() { return size; }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public ObjectAnimator getMoveAnimator() {
            return moveAnimator;
        }

        public ObjectAnimator getReverseMoveAnimator() {
            return reverseMoveAnimator;
        }

        public void addAnimationPoint(Point point) {
            animationPoints.add(point);
        }

        public void createAnimators() {
            Path movePath = new Path();
            movePath.moveTo(x, y);
            for (Point point : animationPoints) {
                movePath.lineTo(point.x, point.y);
            }
            moveAnimator = ObjectAnimator.ofInt(this, "x", "y", movePath);
            moveAnimator.setDuration(mAnimationDuration);

            Path reverseMovePath = new Path();
            reverseMovePath.moveTo(mShapeWidth / 2, mShapeHeight / 2);
            for (int i = animationPoints.size() - 1; i >= 0; i--) {
                Point point = animationPoints.get(i);
                reverseMovePath.lineTo(point.x, point.y);
            }
            reverseMoveAnimator = ObjectAnimator.ofInt(this, "x", "y", reverseMovePath);
            reverseMoveAnimator.setDuration(mAnimationDuration);
        }
    }
}