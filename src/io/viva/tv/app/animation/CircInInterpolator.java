package io.viva.tv.app.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

public class CircInInterpolator implements Interpolator {
	public CircInInterpolator() {
	}

	public CircInInterpolator(Context context, AttributeSet attrs) {
	}

	public float getInterpolation(float input) {
		return (float) (1.0D - Math.abs(Math.sqrt(1.0D - Math.pow(input, 2.0D))));
	}
}
