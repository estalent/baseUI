package io.viva.tv.app;

import io.viva.baseui.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

class TopOptionsDisplay extends OptionsDisplay {
	ViewGroup mSearchLayout;
	ImageView mLogoView;

	TopOptionsDisplay(Context context, ViewGroup parent, TypedArray attributes) {
		super(context, parent, attributes);
	}

	public void setOnClickMenuOptionListener(View.OnClickListener listener) {
	}

	public void setOnClickSettingsOptionListener(View.OnClickListener listener) {
	}

	OptionsDisplay setExpanded(boolean expanded) {
		this.mExpanded = expanded;
		return this;
	}

	void createView(ViewGroup parent, TypedArray attributes) {
		this.mView = ((ViewGroup) parent.findViewById(R.id.optionDisplay));
		this.mSearchLayout = ((ViewGroup) this.mView.findViewById(R.id.top_search));
		this.mLogoView = ((ImageView) this.mView.findViewById(R.id.icon_logo));
	}

	public void setLogoVisibility(int visibility) {
		this.mLogoView.setVisibility(visibility);
	}

	public void setSearchLayoutVisibility(int visibility) {
		this.mSearchLayout.setVisibility(visibility);
	}

	public void setLogo(Drawable logo) {
		if (this.mLogoView.getVisibility() == View.GONE) {
			this.mLogoView.setVisibility(View.VISIBLE);
		}
		this.mLogoView.setImageDrawable(logo);
	}
}
