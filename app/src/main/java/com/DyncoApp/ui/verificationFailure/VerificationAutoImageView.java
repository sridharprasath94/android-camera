package com.DyncoApp.ui.verificationFailure;


import static android.view.View.MeasureSpec.getSize;

import android.content.Context;
import android.util.AttributeSet;

import com.dynamicelement.sdk.android.ui.util.AutoFitImageView;

/**
 * A {@link androidx.appcompat.widget.AppCompatImageView} that can be adjusted to a specified aspect ratio.
 */
public class VerificationAutoImageView extends AutoFitImageView {

    public VerificationAutoImageView(Context context) {
        this(context, null);
    }

    public VerificationAutoImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerificationAutoImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getSize(widthMeasureSpec), getSize(heightMeasureSpec));

        double percentageFactor = (2.75 / 3);

        int modifiedWidth = (int) (getMeasuredWidth() * percentageFactor);
        int modifiedHeight = (int) (getMeasuredWidth() * percentageFactor);

        setMeasuredDimension(modifiedWidth, modifiedHeight);
    }
}
