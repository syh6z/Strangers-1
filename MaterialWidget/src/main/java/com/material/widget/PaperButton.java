package com.material.widget;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by IntelliJ IDEA.
 * User: keith.
 * Date: 14-10-9.
 * Time: 17:04.
 */
public class PaperButton extends View {

    private static final String TAG = PaperButton.class.getSimpleName();
    private static final long ANIMATION_DURATION = 1000;
	private static final long ANIMATION_OUT_DURATION = 200;
    private static final int StateNormal = 1;
    private static final int StateTouchDown = 2;
    private static final int StateTouchUp = 3;
    private static final float SHADOW_RADIUS = 8.0f;
    private static final float SHADOW_OFFSET_X = 0.0f;
    private static final float SHADOW_OFFSET_Y = 4.0f;
    private static final float MIN_SHADOW_COLOR_ALPHA = 0.1f;
    private static final float MAX_SHADOW_COLOR_ALPHA = 0.6f;
	private static final int DEFAULT_DIAMETER_DP = 35;

    private int mState = StateNormal;
    private long mStartTime;
    private int mColor;
    private int mShadowColor;
    private int mCornerRadius;
    private int mPadding;
    private int mTextSize;
    private int mTextColor;
    private float mShadowRadius;
    private float mShadowOffsetX;
    private float mShadowOffsetY;
    private CharSequence mText;
    private RectF backgroundRectF;
    private Rect mFingerRect;
    private Path rippleClipPath;
    private boolean mMoveOutside;
    private Point mTouchPoint = new Point();

	private int mCurrentRadius;
	private boolean mIsLightRipple;
	private int mRippleDiameter;

    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    public PaperButton(Context context) {
        this(context, null);
    }

    public PaperButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaperButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPadding = getResources().getDimensionPixelSize(R.dimen.paper_padding);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PaperButton);
        mColor = attributes.getColor(R.styleable.PaperButton_paper_color,
                getResources().getColor(R.color.paper_button_color));
        mShadowColor = attributes.getColor(R.styleable.PaperButton_paper_shadow_color,
                getResources().getColor(R.color.paper_button_shadow_color));
        mCornerRadius = attributes.getDimensionPixelSize(R.styleable.PaperButton_paper_corner_radius,
                getResources().getDimensionPixelSize(R.dimen.paper_button_corner_radius));
        mText = attributes.getText(R.styleable.PaperButton_paper_text);
        mTextSize = attributes.getDimensionPixelSize(R.styleable.PaperButton_paper_text_size,
                getResources().getDimensionPixelSize(R.dimen.paper_text_size));
        mTextColor = attributes.getColor(R.styleable.PaperButton_paper_text_color,
                getResources().getColor(R.color.paper_text_color));
        final String assetPath = attributes.getString(R.styleable.PaperButton_paper_font);
        if (assetPath != null) {
            AssetManager assets = context.getAssets();
            Typeface typeface = Typeface.createFromAsset(assets, assetPath);
            textPaint.setTypeface(typeface);
        }
        mShadowRadius = attributes.getFloat(R.styleable.PaperButton_paper_shadow_radius, SHADOW_RADIUS);
        mShadowOffsetX = attributes.getFloat(R.styleable.PaperButton_paper_shadow_offset_x, SHADOW_OFFSET_X);
        mShadowOffsetY = attributes.getFloat(R.styleable.PaperButton_paper_shadow_offset_y, SHADOW_OFFSET_Y);

	    mIsLightRipple = attributes.getBoolean(R.styleable.PaperButton_paper_is_light_ripple, false);
	    mRippleDiameter = attributes.getDimensionPixelSize(
			    R.styleable.PaperButton_paper_dimension,
			    (int) dpToPx(getResources(), DEFAULT_DIAMETER_DP)
	    );
        attributes.recycle();

        backgroundPaint.setColor(mColor);
        backgroundPaint.setStyle(Paint.Style.FILL);
        int shadowColor = changeColorAlpha(mShadowColor, MIN_SHADOW_COLOR_ALPHA);
        backgroundPaint.setShadowLayer(mShadowRadius, mShadowOffsetX, mShadowOffsetY, shadowColor);

        textPaint.setColor(mTextColor);
        textPaint.setTextSize(mTextSize);
        textPaint.setTextAlign(TextPaint.Align.CENTER);

	    if(mIsLightRipple){
		    ripplePaint.setColor(lightenColor(mColor));
	    }else {
		    ripplePaint.setColor(darkenColor(mColor));
	    }

        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private int changeColorAlpha(int color, float value) {
        int alpha = Math.round(Color.alpha(color) * value);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f;
        return Color.HSVToColor(hsv);
    }

	private int lightenColor(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 1.2f;
		return Color.HSVToColor(hsv);
	}

    public void setColor(int color) {
        mColor = color;
        backgroundPaint.setColor(mColor);
        invalidate();
    }

    public void setShadowColor(int color) {
        mShadowColor = color;
        backgroundPaint.setShadowLayer(mShadowRadius, mShadowOffsetX, mShadowOffsetY, mShadowColor);
        invalidate();
    }

    public void setTextSize(int pixel) {
        mTextSize = pixel;
        textPaint.setTextSize(mTextSize);
        invalidate();
    }

    public void setTextColor(int color) {
        mTextColor = color;
        textPaint.setColor(mTextColor);
        invalidate();
    }
    
    public void setText(String text){
        mText = text;
        invalidate();
    }

    private RectF getRectF() {
        if (backgroundRectF == null) {
            backgroundRectF = new RectF();
            backgroundRectF.left = mPadding;
            backgroundRectF.top = mPadding;
            backgroundRectF.right = getWidth() - mPadding;
            backgroundRectF.bottom = getHeight() - mPadding;
        }
        return backgroundRectF;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMoveOutside = false;
                mFingerRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
                mTouchPoint.set(Math.round(event.getX()), Math.round(event.getY()));
                mState = StateTouchDown;
                mStartTime = System.currentTimeMillis();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mFingerRect.contains(getLeft() + (int) event.getX(),
                        getTop() + (int) event.getY())) {
                    mMoveOutside = true;
	                if(mState == StateTouchDown){
		                mState = StateTouchUp;
		                mStartTime = System.currentTimeMillis();
	                }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mMoveOutside) {
                    mState = StateTouchUp;
                    mStartTime = System.currentTimeMillis();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mState = StateNormal;
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int radius = 0;
        int shadowColor = changeColorAlpha(mShadowColor, MIN_SHADOW_COLOR_ALPHA);
        long elapsed = System.currentTimeMillis() - mStartTime;
        switch (mState) {
            case StateNormal:
                shadowColor = changeColorAlpha(mShadowColor, MIN_SHADOW_COLOR_ALPHA);
                break;
            case StateTouchDown:
                ripplePaint.setAlpha(255);
                if (elapsed < ANIMATION_DURATION) {
                    radius = mRippleDiameter + Math.round(elapsed * getWidth() * 1.5f / 2 / ANIMATION_DURATION);
	                mCurrentRadius = radius;
	                float shadowAlpha;
	                if(elapsed < ANIMATION_OUT_DURATION){
		                shadowAlpha = (MAX_SHADOW_COLOR_ALPHA - MIN_SHADOW_COLOR_ALPHA)
				                * elapsed
				                / ANIMATION_OUT_DURATION
				                + MIN_SHADOW_COLOR_ALPHA;
	                }else{
		                shadowAlpha = (MAX_SHADOW_COLOR_ALPHA - MIN_SHADOW_COLOR_ALPHA) + MIN_SHADOW_COLOR_ALPHA;
	                }
	                shadowColor = changeColorAlpha(mShadowColor, shadowAlpha);
                } else {
                    radius = getWidth() * 3 / 4;
                    shadowColor = changeColorAlpha(mShadowColor, MAX_SHADOW_COLOR_ALPHA);
                }
                postInvalidate();
                break;
            case StateTouchUp:
                if (elapsed < ANIMATION_OUT_DURATION) {
                    int alpha = Math.round((ANIMATION_OUT_DURATION - elapsed) * 255 / ANIMATION_OUT_DURATION);
                    ripplePaint.setAlpha(alpha);
                    radius = mCurrentRadius + Math.round(elapsed * getWidth() * 1.5f / 2 / ANIMATION_OUT_DURATION);
                    float shadowAlpha = (MAX_SHADOW_COLOR_ALPHA - MIN_SHADOW_COLOR_ALPHA)
                            * (ANIMATION_OUT_DURATION - elapsed)
                            / ANIMATION_OUT_DURATION
                            + MIN_SHADOW_COLOR_ALPHA;
                    shadowColor = changeColorAlpha(mShadowColor, shadowAlpha);
                } else {
                    mState = StateNormal;
                    radius = 0;
	                mCurrentRadius = mRippleDiameter;
                    ripplePaint.setAlpha(0);
                    shadowColor = changeColorAlpha(mShadowColor, MIN_SHADOW_COLOR_ALPHA);
	                if(!mMoveOutside){
		                performClick();
	                }
                }
                postInvalidate();
                break;
        }
        backgroundPaint.setShadowLayer(mShadowRadius, mShadowOffsetX, mShadowOffsetY, shadowColor);
        canvas.drawRoundRect(getRectF(), mCornerRadius, mCornerRadius, backgroundPaint);
        canvas.save();
        if (mState == StateTouchDown || mState == StateTouchUp) {
            if (rippleClipPath == null) {
                rippleClipPath = new Path();
                rippleClipPath.addRoundRect(getRectF(), mCornerRadius, mCornerRadius, Path.Direction.CW);
            }
            canvas.clipPath(rippleClipPath);
        }
        canvas.drawCircle(mTouchPoint.x, mTouchPoint.y, radius, ripplePaint);
        canvas.restore();
        if (mText != null && mText.length() > 0) {
            int y = (int) (getHeight() / 2 - ((textPaint.descent() + textPaint.ascent()) / 2));
            canvas.drawText(mText.toString(), getWidth() / 2, y, textPaint);
        }
    }

	static float dpToPx(Resources resources, float dp) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
	}
}
