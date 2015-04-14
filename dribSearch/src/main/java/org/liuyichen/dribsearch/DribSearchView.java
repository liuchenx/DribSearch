package org.liuyichen.dribsearch;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.util.Property;

import static org.liuyichen.dribsearch.DribSearchView.State.LINE;
import static org.liuyichen.dribsearch.DribSearchView.State.RUNNING;
import static org.liuyichen.dribsearch.DribSearchView.State.SEARCH;

/**
 *  Copyright 2015 liuyichen
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
public class DribSearchView extends View implements ValueAnimator.AnimatorUpdateListener {

    private static final float DEFAULT_ALPHA = 0.8f;
    private static final int DEFAULT_COLOR = Color.WHITE;

    private static final float DEGREE_360 = 360f;

    private static final float DEFAULT_JOIN_ANGLE = 45;

    private static final double sin45 = Math.sin(2 * Math.PI / 360 * 45);

    private Paint mSearchPaint = new Paint();
    private Path mSearchPath = new Path();

    private int mBreadth = 2;
    private int mSearchColor = DEFAULT_COLOR;

    public DribSearchView(Context context) {
        this(context, null);
    }

    public DribSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DribSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DribSearchView);
        mBreadth = a.getInteger(R.styleable.DribSearchView_breadth, 2);
        mSearchColor = a.getColor(R.styleable.DribSearchView_search_color, Color.WHITE);
        a.recycle();
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DribSearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();

    }

    void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        // attrs to do

        mSearchPaint.setStyle(Paint.Style.STROKE);
        mSearchPaint.setStrokeWidth(mBreadth);
        mSearchPaint.setColor(mSearchColor);
        mSearchPaint.setAntiAlias(true);
        mSearchPaint.setAlpha((int)(255 * DEFAULT_ALPHA));
        mSearchPaint.setStrokeCap(Paint.Cap.ROUND);
        mSearchPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resetLayout(left, top, right, bottom);
    }

    int drawLeft = 0;
    int drawTop = 0;
    int drawRight = 0;
    int drawBottom = 9;

   // float r = 0;
    float rx = 0;
    float ry = 0;
    RectF mRoundRect = new RectF();
    float defaultJoinx = 0;
    float defaultJoiny = 0;

    RectF mTouchRect = new RectF();
    protected void resetLayout(int left, int top, int right, int bottom) {

        drawLeft = left + getPaddingLeft() + 1;
        drawTop = top + getPaddingTop() + 1;
        drawRight = right - getPaddingRight() - 1;
        drawBottom = bottom - getPaddingBottom() - 1;

        int h = drawBottom - drawTop;
        float r = 0;
        mTouchRect.set(drawRight - h, drawTop, drawRight, drawBottom);
        // sin45 * 2R + R = height
        r = (float)(h / (sin45 * 2 + 1));
        rx = (float)(drawRight - 2 * r * sin45);
        ry = r + drawTop;
        mRoundRect.set(rx - r, ry - r, rx + r, ry + r);
        defaultJoinx = (float)(rx + sin45 * r);
        defaultJoiny = (float)(ry + sin45 * r);

        if (mState == SEARCH) {
            joinAngle = DEFAULT_JOIN_ANGLE;
            joinx = 0;
            joiny = 0;
            lineDelx = drawRight - drawLeft;
        } else if (mState == SEARCH) {
            joinAngle  = DEGREE_360 + DEFAULT_JOIN_ANGLE;
            joinx = drawRight - defaultJoinx;
            joiny = drawBottom - defaultJoiny;
            lineDelx = 0;
        }
        mSearchTouchListener.setOther(mTmpOnTouchListener, mTouchRect);
        super.setOnTouchListener(mSearchTouchListener);
    }

    private float joinAngle = 0;
    private float joinx = 0;
    private float joiny = 0;
    private float lineDelx = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mSearchPath.reset();
        mSearchPaint.setStrokeWidth(mBreadth);
        mSearchPath.addArc(mRoundRect, joinAngle, DEGREE_360 + DEFAULT_JOIN_ANGLE - joinAngle);

        mSearchPath.moveTo(defaultJoinx + joinx, defaultJoiny + joiny);
        mSearchPath.lineTo(drawRight, drawBottom);

        canvas.drawPath(mSearchPath, mSearchPaint);

        mSearchPaint.setStrokeWidth(2);
        mSearchPath.moveTo(drawRight, drawBottom);
        mSearchPath.lineTo(drawLeft + lineDelx, drawBottom);

        canvas.drawPath(mSearchPath, mSearchPaint);
    }

    public static enum State {

        RUNNING, SEARCH, LINE;
    }

    State mState = SEARCH;

    public void toggle() {

        switch (mState) {
            case RUNNING:
                break;
            case SEARCH:
                changeLine();
                break;
            case LINE:
                changeSearch();
                break;
        }
    }

    private class SearchTouchListener implements  OnTouchListener {

        private OnTouchListener other;
        private RectF rect;

        private SearchTouchListener() {
        }

        public void setOther(OnTouchListener other,RectF rect ) {
            this.other = other;
            this.rect = rect;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float mx = event.getX();
                float my = event.getY();
                if (rect.contains(mx, my) && mClickSearchListener != null && mState == SEARCH) {
                    mClickSearchListener.onClickSearch();
                }
            }
            if (other != null) {
                return other.onTouch(v, event);
            } else {
                return false;
            }
        }
    }

    private SearchTouchListener mSearchTouchListener = new SearchTouchListener();

    private OnTouchListener mTmpOnTouchListener = null;
    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mTmpOnTouchListener = l;
        mSearchTouchListener.setOther(mTmpOnTouchListener, mTouchRect);
        super.setOnTouchListener(mSearchTouchListener);
    }

    public void changeLine() {
        if (mState == LINE) return;
        ObjectAnimator round = ObjectAnimator.ofFloat(this, joinAngleProperty, DEFAULT_JOIN_ANGLE, DEGREE_360 + DEFAULT_JOIN_ANGLE);
        round.setDuration(450);
        round.addUpdateListener(this);

        PropertyValuesHolder pvJx = PropertyValuesHolder.ofFloat(joinXProperty, 0f, drawRight - defaultJoinx);
        PropertyValuesHolder pvJy = PropertyValuesHolder.ofFloat(joinYProperty, 0f, drawBottom - defaultJoiny);
        ObjectAnimator bar = ObjectAnimator.ofPropertyValuesHolder(this, pvJx, pvJy);
        bar.setDuration(300);
        bar.addUpdateListener(this);

        ObjectAnimator line = ObjectAnimator.ofFloat(this, lineDelxProperty, drawRight - drawLeft, 0);
        line.setDuration(700);
        line.addUpdateListener(this);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(round).before(bar).with(line);
        animatorSet.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animator) {
                mState = LINE;
                mListener.onChange(mState);
            }

            @Override
            public void onAnimationStart(Animator animator) {
                mState = RUNNING;
            }
        });
        animatorSet.start();
    }

    public void changeSearch() {
        if (mState == SEARCH) return;
        ObjectAnimator round = ObjectAnimator.ofFloat(this, joinAngleProperty, DEGREE_360 + DEFAULT_JOIN_ANGLE, DEFAULT_JOIN_ANGLE);
        round.setDuration(450);
        round.addUpdateListener(this);

        PropertyValuesHolder pvJx = PropertyValuesHolder.ofFloat(joinXProperty, drawRight - defaultJoinx, 0f);
        PropertyValuesHolder pvJy = PropertyValuesHolder.ofFloat(joinYProperty, drawBottom - defaultJoiny, 0f);
        ObjectAnimator bar = ObjectAnimator.ofPropertyValuesHolder(this, pvJx, pvJy);
        bar.setDuration(300);
        bar.addUpdateListener(this);

        ObjectAnimator line = ObjectAnimator.ofFloat(this, lineDelxProperty, 0, drawRight - drawLeft);
        line.setDuration(700);
        line.addUpdateListener(this);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(bar).with(line);
        animatorSet.play(bar).before(round);
        animatorSet.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animator) {
                mState = SEARCH;
                mListener.onChange(mState);
            }

            @Override
            public void onAnimationStart(Animator animator) {
                mState = RUNNING;
            }
        });
        animatorSet.start();
    }

    private Property<DribSearchView, Float> joinAngleProperty
            = new Property<DribSearchView, Float>(Float.class, "joinAngle") {
        @Override
        public Float get(DribSearchView object) {
            return object.getJoinAngle();
        }

        @Override
        public void set(DribSearchView object, Float value) {
            object.setJoinAngle(value);
        }
    };

    private Property<DribSearchView, Float> joinXProperty
            = new Property<DribSearchView, Float>(Float.class, "joinx") {
        @Override
        public Float get(DribSearchView object) {
            return object.getJoinx();
        }

        @Override
        public void set(DribSearchView object, Float value) {
            object.setJoinx(value);
        }
    };

    private Property<DribSearchView, Float> joinYProperty
            = new Property<DribSearchView, Float>(Float.class, "joiny") {
        @Override
        public Float get(DribSearchView object) {
            return object.getJoiny();
        }

        @Override
        public void set(DribSearchView object, Float value) {
            object.setJoiny(value);
        }
    };

    private Property<DribSearchView, Float> lineDelxProperty
            = new Property<DribSearchView, Float>(Float.class, "lineDelx") {
        @Override
        public Float get(DribSearchView object) {
            return object.getLineDelx();
        }

        @Override
        public void set(DribSearchView object, Float value) {
            object.setLineDelx(value);
        }
    };

    private float getJoinAngle() {
        return joinAngle;
    }

    private void setJoinAngle(float joinAngle) {
        this.joinAngle = joinAngle;
    }


    private float getJoinx() {
        return joinx;
    }

    private void setJoinx(float joinx) {
        this.joinx = joinx;
    }

    private float getJoiny() {
        return joiny;
    }

    private void setJoiny(float joiny) {
        this.joiny = joiny;
    }

    private float getLineDelx() {
        return lineDelx;
    }

    private void setLineDelx(float lineDelx) {
        this.lineDelx = lineDelx;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        invalidate();
    }

    private class SimpleAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {

        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

    public interface OnChangeListener {

        void onChange(State state);
    }

    private OnChangeListener mListener;

    public void setOnChangeListener(OnChangeListener listener) {
        mListener = listener;
    }

    public interface OnClickSearchListener {
        void onClickSearch();
    }

    private OnClickSearchListener mClickSearchListener;

    public void setOnClickSearchListener(OnClickSearchListener l) {
        mClickSearchListener = l;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.joinAngle = this.joinAngle;
        savedState.joinx = this.joinx;
        savedState.joiny = this.joiny;
        savedState.lineDelx = this.lineDelx;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.joinAngle = savedState.joinAngle;
        this.joinx = savedState.joinx;
        this.joiny = savedState.joiny;
        this.lineDelx = savedState.lineDelx;
        invalidate();
    }

    private static class SavedState extends BaseSavedState {

        protected float joinAngle;
        protected float joinx;
        protected float joiny;
        protected float lineDelx;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(this.joinAngle);
            dest.writeFloat(this.joinx);
            dest.writeFloat(this.joiny);
            dest.writeFloat(this.lineDelx);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
