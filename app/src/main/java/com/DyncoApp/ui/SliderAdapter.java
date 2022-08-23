package com.DyncoApp.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.DyncoApp.R;

public class SliderAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    String register = "Select the area of your object surface for which a unique ID shall be created. You can execute the registration by pressing and holding the touchscreen.";
    String verify = "Verify an existing unique ID by selecting the object’s surface area which is already registered." +
            "\rFor every successful verification, you receive the corresponding unique ID and a score. The score shows the percentage match with the registration.\n" +
            " \n" +
            "Note: The score may vary due to differences in the ambient light and camera angles between the registration and verification.";

    public SliderAdapter(Context context) {
        this.context = context;
    }

    public int[] slideImages = {
            R.drawable.dynamicelementlogo, R.drawable.ic_registered_white, R.drawable.ic_verified_white
    };

    public String[] slideHeadings = {
            "Welcome to Dynco!",
            "Registration",
            "Verification"
    };

    public String[] slideDesc = {
            "With the help of Dynco, you are able to create and identify unique IDs of any object based on its surface.",
            register,
            verify
    };

    @Override
    public int getCount() {
        return slideHeadings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view;
        layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.slidelayout, container, false);
        ImageView slideImageView = view.findViewById(R.id.slideImage);
        TextView slideHeading = view.findViewById(R.id.slideHeading);
        TextView slideDescription = view.findViewById(R.id.slideDescription);
        slideImageView.setImageResource(slideImages[position]);
        slideHeading.setText(slideHeadings[position]);
        slideDescription.setText(slideDesc[position]);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
