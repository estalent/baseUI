package io.viva.tv.app;

import android.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class TabFrame extends LinearLayout {
	private boolean mConfigured;
	private boolean mIsCustom;
	private boolean mIsFakeSelected;
	private final int[] FAKE_SELECTED = { 16842908 };
	private final int[] FAKE_SELECTED_STUB = { 16842915 };

	public TabFrame(Context context, AttributeSet attributes) {
		super(context, attributes);
	}

	public void setSelected(boolean selected) {
	}

	public void select(boolean selected) {
		super.setSelected(selected);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void expand(boolean expanded) {
		if (!this.mIsCustom) {
			getTitle().setVisibility(expanded ? View.VISIBLE : View.GONE);
		}
		setActivated(expanded);
	}

	private ImageView getIcon() {
		return (ImageView) findViewById(R.id.icon);
	}

	private TextView getTitle() {
		return (TextView) findViewById(R.id.title);
	}

	public void configureNormal(Drawable icon, CharSequence text) {
		markConfigured(false);
		getIcon().setImageDrawable(icon);
		getTitle().setText(text);
	}

	public void configureCustom(View content) {
		markConfigured(true);

		setBackgroundDrawable(null);

		content.setDuplicateParentStateEnabled(true);

		removeAllViews();
		addView(content);
	}

	public void setFakeSelected(boolean isFakeSelected) {
		this.mIsFakeSelected = isFakeSelected;
	}

	private boolean isFakeSelected() {
		return this.mIsFakeSelected;
	}

	protected int[] onCreateDrawableState(int extraSpace) {
		int[] drawableState = super.onCreateDrawableState(extraSpace + this.FAKE_SELECTED.length);
		if (isFakeSelected()) {
			mergeDrawableStates(drawableState, this.FAKE_SELECTED);
		} else {
			mergeDrawableStates(drawableState, this.FAKE_SELECTED_STUB);
		}
		return drawableState;
	}

	private void markConfigured(boolean isCustom) {
		if (this.mConfigured) {
			throw new IllegalStateException("Frame already configured.");
		}
		this.mConfigured = true;
		this.mIsCustom = isCustom;
	}
}
