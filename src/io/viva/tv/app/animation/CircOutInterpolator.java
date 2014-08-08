package io.viva.tv.app.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

public class CircOutInterpolator implements Interpolator {
	public CircOutInterpolator() {
	}

	public CircOutInterpolator(Context context, AttributeSet attrs) {
	}

	public float getInterpolation(float input) {
		return (float) Math.abs(Math.sqrt(1.0D - Math.pow(input - 1.0F, 2.0D)));
	}
}
