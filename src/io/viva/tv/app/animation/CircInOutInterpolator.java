package io.viva.tv.app.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

public class CircInOutInterpolator implements Interpolator {
	public CircInOutInterpolator() {
	}

	public CircInOutInterpolator(Context context, AttributeSet attrs) {
	}

	public float getInterpolation(float input) {
		if (input < 0.5D) {
			return (float) (0.5D - 0.5D * Math.abs(Math.sqrt(1.0D - 4.0D * Math.pow(input, 2.0D))));
		}
		return (float) (0.5D + 0.5D * Math.abs(Math.sqrt(1.0D - 4.0D * Math.pow(input - 1.0F, 2.0D))));
	}
}
