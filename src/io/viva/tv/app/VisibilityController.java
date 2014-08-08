package io.viva.tv.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

public class VisibilityController {
	private final View mView;
	private final int mAnimationDuration;
	private boolean mVisible;

	VisibilityController(View view) {
		this.mView = view;
		this.mAnimationDuration = 200;

		this.mVisible = (view.getVisibility() == View.VISIBLE);
	}

	boolean isVisible() {
		return this.mVisible;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	boolean setVisible(final boolean visible, boolean animated) {
		if (isVisible() == visible) {
			return false;
		}
		this.mVisible = visible;
		if (animated) {
			float toAlpha = visible ? 1.0F : 0.0F;
			ObjectAnimator mAnimator = ObjectAnimator.ofFloat(this.mView, "Alpha", new float[] { 1.0F - toAlpha, toAlpha });
			mAnimator.setDuration(this.mAnimationDuration).addListener(new AnimatorListenerAdapter() {
				public void onAnimationStart(Animator animator) {
					if (visible) {
						VisibilityController.this.setViewVisible(true);
					}
				}

				public void onAnimationEnd(Animator animator) {
					if (!visible) {
						VisibilityController.this.setViewVisible(false);
					}
				}
			});
			mAnimator.start();
		} else {
			setViewVisible(visible);
		}
		return true;
	}

	private void setViewVisible(boolean visible) {
		this.mView.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
}
