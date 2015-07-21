package com.droidsonroids.awesomeprogressbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class AwesomeProgressBar extends View {
    private static final int DEFAULT_ANIMATION_DURATION = 800;
    private static final int DEFAULT_RADIUS = 0;
    private static final int DEFAULT_STROKE = 0;

    private Paint mBackgroundPaint;
    private Paint mProgressBarPaint;
    private ValueAnimator mProgressAnimation;

    private float mProgressValue;
    private float mPlusLengthValue;
    private float mRadius;
    private float mStroke;
    private float mXCenter;
    private float mYCenter;
    private int mAnimationDuration;
    private int mBackgroundColor;
    private int mProgressBarColor;

    private RectF mRectF;

    private boolean isAnimationInitialized = false;
    private boolean mIsSuccess = false;
    private State mState;
    private IAnimationStateListener mListener;

    public AwesomeProgressBar(Context context) {
        this(context, null);
    }

    public AwesomeProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public void setIAnimationStateListener(IAnimationStateListener listener) {
        mListener = listener;
    }

    private void initialize(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AwesomeProgressBar);
            try {
                mRadius = array.getDimension(R.styleable.AwesomeProgressBar_radius, DEFAULT_RADIUS);
                mStroke = array.getDimension(R.styleable.AwesomeProgressBar_stroke, DEFAULT_STROKE);
                mAnimationDuration = array.getInteger(
                        R.styleable.AwesomeProgressBar_animationDuration, DEFAULT_ANIMATION_DURATION);
                mBackgroundColor = array.getColor(R.styleable.AwesomeProgressBar_backgroundColor,
                        getDefaultBackgroundColor());
                mProgressBarColor = array.getColor(R.styleable.AwesomeProgressBar_progressBarColor, getDefaultProgressBarColor());
            } finally {
                array.recycle();
            }
        }
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mStroke);
        mBackgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mProgressBarPaint = new Paint();
        mProgressBarPaint.setColor(mProgressBarColor);
        mProgressBarPaint.setStyle(Paint.Style.STROKE);
        mProgressBarPaint.setStrokeWidth(mStroke);
        mProgressBarPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mState = State.IDLE_STATE;
    }

    private int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.default_background_color);
    }

    private int getDefaultProgressBarColor() {
        return getResources().getColor(R.color.default_progress_bar_color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mXCenter = w / 2;
        mYCenter = h / 2;
        mRectF = new RectF(mXCenter - mRadius + mStroke / 2, mYCenter - mRadius + mStroke / 2, mXCenter + mRadius - mStroke / 2, mYCenter + mRadius - mStroke / 2);
        if (!isAnimationInitialized) {
            setupAnimations();
            isAnimationInitialized = true;
        }
    }

    private void setupAnimations() {
        final AnimatorSet signAnimatorSet = new AnimatorSet();

        mProgressAnimation = ValueAnimator.ofFloat(0, 360);
        mProgressAnimation.setDuration(mAnimationDuration);
        mProgressAnimation.setInterpolator(new LinearInterpolator());
        mProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgressValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mProgressAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mState = State.RUNNING_STATE;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                signAnimatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ValueAnimator plusLengthAnimation = ValueAnimator.ofFloat(0, mRadius / 2f);
        plusLengthAnimation.setInterpolator(new OvershootInterpolator());
        plusLengthAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPlusLengthValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        plusLengthAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mIsSuccess) {
                    mState = State.SUCCESS_STATE;
                } else {
                    mState = State.FAILURE_STATE;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setClickable(true);
                mListener.finished();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        signAnimatorSet.setDuration(mAnimationDuration);
        signAnimatorSet.playTogether(rotateAnimation, plusLengthAnimation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mXCenter, mYCenter, mRadius - mStroke / 2, mBackgroundPaint);
        if (mState == State.RUNNING_STATE) {
            canvas.drawArc(mRectF, -90, mProgressValue, false, mProgressBarPaint);
        } else if (mState == State.SUCCESS_STATE) {
            canvas.drawCircle(mXCenter, mYCenter, mRadius - mStroke / 2, mProgressBarPaint);
            canvas.drawLine(mXCenter, mYCenter, mXCenter, mYCenter - mPlusLengthValue, mProgressBarPaint);
            canvas.drawLine(mXCenter, mYCenter, mXCenter, mYCenter + mPlusLengthValue, mProgressBarPaint);
            canvas.drawLine(mXCenter, mYCenter, mXCenter - mPlusLengthValue, mYCenter, mProgressBarPaint);
            canvas.drawLine(mXCenter, mYCenter, mXCenter + mPlusLengthValue, mYCenter, mProgressBarPaint);
        } else if (mState == State.FAILURE_STATE) {
            canvas.drawCircle(mXCenter, mYCenter, mRadius - mStroke / 2, mProgressBarPaint);
            canvas.drawLine(mXCenter, mYCenter, mXCenter - mPlusLengthValue, mYCenter + mPlusLengthValue, mProgressBarPaint);
            canvas.drawLine(mXCenter, mYCenter, mXCenter + mPlusLengthValue, mYCenter - mPlusLengthValue, mProgressBarPaint);
            canvas.drawLine(mXCenter, mYCenter, mXCenter - mPlusLengthValue, mYCenter - mPlusLengthValue, mProgressBarPaint);
            canvas.drawLine(mXCenter, mYCenter, mXCenter + mPlusLengthValue, mYCenter + mPlusLengthValue, mProgressBarPaint);
        }
    }

    public void play(Boolean isSuccess) {
        mIsSuccess = isSuccess;
        setClickable(false);
        if (mProgressAnimation.isRunning()) {
            mState = State.IDLE_STATE;
        }
        mProgressAnimation.start();
    }

    private enum State {RUNNING_STATE, IDLE_STATE, SUCCESS_STATE, FAILURE_STATE}
}
