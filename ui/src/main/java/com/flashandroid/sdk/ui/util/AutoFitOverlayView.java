package com.flashandroid.sdk.ui.util;

import static android.view.View.MeasureSpec.getSize;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.flashandroid.sdk.ui.CameraParameters;

/**
 * A {@link androidx.appcompat.widget.AppCompatImageView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitOverlayView extends androidx.appcompat.widget.AppCompatImageView {
    private static final String TAG = "AUTO_FIT_IMAGE_VIEW";
    private int ratioWidth = 0;
    private int ratioHeight = 0;

    private int currentWidth;
    private int currentHeight;

    public AutoFitOverlayView(Context context) {
        this(context, null);
    }

    public AutoFitOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitOverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters.
     * The RATIO_1X1 takes both width and height as 1.
     * The RATIO_3X4 takes width as 3 and height as 4.
     *
     * @param ratioMode Ratio mode - Either 1:1 or 3:4
     */
    public void setAspectRatio(CameraParameters.CameraRatioMode ratioMode) {
        this.ratioWidth = ratioMode == CameraParameters.CameraRatioMode.RATIO_1X1 ? 1 : 3;
        this.ratioHeight = ratioMode == CameraParameters.CameraRatioMode.RATIO_1X1 ? 1 : 4;
        requestLayout();
    }


    public int getCurrentWidth() {
        return this.currentWidth;
    }

    public int getCurrentHeight() {
        return this.currentHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getSize(widthMeasureSpec);
        int height = getSize(heightMeasureSpec);

        if (0 == this.ratioWidth || 0 == this.ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * this.ratioWidth / this.ratioHeight) {
                this.currentHeight = width * this.ratioHeight / this.ratioWidth;
                this.currentWidth = width;
                setMeasuredDimension(width, this.currentHeight);
            } else {
                this.currentHeight = height;
                this.currentWidth = height * this.ratioWidth / this.ratioHeight;
                setMeasuredDimension(this.currentWidth, height);
            }
        }
        Log.d(TAG + "_WIDTH", String.valueOf(getMeasuredWidth()));
        Log.d(TAG + "_HEIGHT", String.valueOf(getMeasuredHeight()));
    }
}
