package io.viva.tv.app;

import io.viva.baseui.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

class OptionsDisplay {
	static final String TAG = "Options";
	final Context mContext;
	ViewGroup mView;
	boolean mExpanded;
	View mMenuOption;
	View mSettingsOption;
	View mSearchOption;
	TabDisplay mTabDisplay;

	OptionsDisplay(Context context, ViewGroup parent, TypedArray attributes) {
		this.mContext = context;
		createView(parent, attributes);
	}

	View getView() {
		return this.mView;
	}

	OptionsDisplay setVisible(boolean visible) {
		this.mView.setVisibility(visible ? View.VISIBLE : View.GONE);
		return this;
	}

	boolean isVisible() {
		return this.mView.getVisibility() == View.VISIBLE;
	}

	OptionsDisplay setExpanded(boolean expanded) {
		this.mExpanded = expanded;
		refreshExpandedState();
		return this;
	}

	private void refreshExpandedState() {
		setOptionExpanded(this.mMenuOption, this.mExpanded);

		ViewGroup optionsContainer = getOptionsContainer();
		for (int i = 0; i < optionsContainer.getChildCount(); i++) {
			setOptionExpanded(optionsContainer.getChildAt(i), this.mExpanded);
		}
	}

	void createView(ViewGroup parent, TypedArray attributes) {
		this.mView = ((ViewGroup) parent.findViewById(R.id.optionDisplay));

		if (StyleFetcher.getStyle() == 1) {
			this.mSearchOption = this.mView.findViewById(R.id.search);
			this.mSearchOption.setClickable(true);
			this.mSearchOption.setFocusable(true);
			this.mSearchOption.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					OptionsDisplay.this.handleFocusChange(hasFocus);
				}
			});
			configureOption(this.mSearchOption, null, true);
		}

		this.mMenuOption = this.mView.findViewById(R.id.menu);
		configureOption(this.mMenuOption, null, true);

		this.mMenuOption.setClickable(true);
		this.mMenuOption.setFocusable(true);
		this.mMenuOption.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				OptionsDisplay.this.handleFocusChange(hasFocus);
			}
		});
		this.mSettingsOption = this.mView.findViewById(R.id.settings);
		configureOption(this.mSettingsOption, null, true);

		this.mSettingsOption.setClickable(true);
		this.mSettingsOption.setFocusable(true);

		this.mSettingsOption.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				OptionsDisplay.this.handleFocusChange(hasFocus);
			}
		});
	}

	void handleFocusChange(boolean hasFocus) {
		if ((hasFocus) && (this.mTabDisplay != null)) {
			this.mTabDisplay.lostFocusOnTabListView();
		} else if ((!hasFocus) && (this.mTabDisplay != null)) {
			this.mTabDisplay.resetFocusOnTabListView();
		}
	}

	public void setTabDisplay(TabDisplay tabDisplay) {
		this.mTabDisplay = tabDisplay;
	}

	public void setOnClickMenuOptionListener(View.OnClickListener listener) {
		this.mMenuOption.setOnClickListener(listener);
	}

	public void setOnClickSettingsOptionListener(View.OnClickListener listener) {
		this.mSettingsOption.setOnClickListener(listener);
	}

	public void setOnClickSearchOptionListener(View.OnClickListener listener) {
		if (this.mSearchOption != null)
			this.mSearchOption.setOnClickListener(listener);
	}

	private void setDuplicateParentState(View view) {
		view.setDuplicateParentStateEnabled(true);
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent == null) {
			return;
		}
		int index = parent.indexOfChild(view);
		parent.removeViewAt(index);
		parent.addView(view, index);
	}

	private View configureOption(View option, CharSequence title, boolean active) {
		ImageView iconView = getOptionIcon(option);
		iconView.setEnabled(active);
		if (title != null) {
			getOptionTitle(option).setText(title);
		}
		return option;
	}

	private static void setOptionExpanded(View option, boolean expanded) {
		getOptionTitle(option).setVisibility(expanded ? View.VISIBLE : View.GONE);
	}

	private static ImageView getOptionIcon(View option) {
		return (ImageView) option.findViewById(R.id.icon);
	}

	private static TextView getOptionTitle(View option) {
		return (TextView) option.findViewById(R.id.title);
	}

	private ViewGroup getOptionsContainer() {
		return (ViewGroup) this.mView.findViewById(R.id.shown_options);
	}

	public void setLogo(Drawable logo) {
	}
}
