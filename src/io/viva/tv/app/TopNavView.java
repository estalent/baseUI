package io.viva.tv.app;

import io.viva.baseui.R;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class TopNavView extends LeftNavView {
	private int mHeightCollapsed;
	private int mHeightExpanded;
	private int mApparentHeightCollapsed;
	private int mApparentHeightExpanded;
	boolean isFirst = true;

	public TopNavView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	public View findFocus() {
		if (this.mLoseFocusOnFirstFocusFinder) {
			this.mLoseFocusOnFirstFocusFinder = false;
			return null;
		}

		if (this.mAlwaysLoseFocus) {
			return null;
		}

		if ((this.isFirst) && (this.mTabs != null) && (this.mTabs.getCount() > 0)) {
			this.isFirst = false;
			this.mTabs.mHorizontalList.requestFocus();
			return this.mTabs.mHorizontalList;
		}
		return super.findFocus();
	}

	public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
		return this.mTabs.getunFocus() ? false : super.requestFocus(direction, previouslyFocusedRect);
	}

	public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
		if (this.mAlwaysLoseFocus) {
			return;
		}
		if (!this.isLeftNavView) {
			if (direction == 2) {
				super.addFocusables(views, direction, focusableMode);
				return;
			}
			if ((direction != 33) && (!hasFocus())) {
				return;
			}
			if (!hasFocus()) {
				int initialCount = views.size();
				switch (this.mNavigationMode) {
				case 2:
					this.mTabs.getView().addFocusables(views, direction, focusableMode);
					break;
				case 1:
					this.mSpinner.getView().addFocusables(views, direction, focusableMode);
					break;
				}

				if (views.size() > initialCount) {
					return;
				}
			}
		}
		super.addFocusables(views, direction, focusableMode);
	}

	public View focusSearch(View focused, int direction) {
		if ((!this.isLeftNavView) && (hasFocus()) && (direction != 130)) {
			return FocusFinder.getInstance().findNextFocus(this, focused, direction);
		}
		return super.focusSearch(focused, direction);
	}

	protected void initNavView(Context context) {
		LayoutInflater.from(context).inflate(R.layout.top_nav, this, true);
		setOrientation(HORIZONTAL);

		this.isLeftNavView = false;

		this.mHome = new TopHomeDisplay(context, this, null).setVisible(true);
		this.mTabs = new TabDisplay(context, this, null, this.isLeftNavView).setVisible(false);
		this.mOptions = new TopOptionsDisplay(context, this, null).setVisible(false);
		this.mSpinner = new SpinnerDisplay(context, this, null).setVisible(false);

		Resources res = context.getResources();
		this.mHeightCollapsed = res.getDimensionPixelSize(R.dimen.top_nav_default_height);
		this.mHeightExpanded = res.getDimensionPixelSize(R.dimen.top_nav_default_height);
		this.mApparentHeightCollapsed = res.getDimensionPixelSize(R.dimen.top_nav_default_height);
		this.mApparentHeightExpanded = res.getDimensionPixelSize(R.dimen.top_nav_default_height);
		this.mAnimationDuration = 200;

		this.mNavigationMode = 0;
		setNavigationMode(2);
		this.mMainLayout = ((LinearLayout) findViewById(R.id.main));
		((MainLinearLayout) this.mMainLayout).setTabtHorizontalListView((TabHorizontalListView) this.mTabs.getView());
	}

	public int getApparentWidth(boolean ignoreHiddenState) {
		if ((!isVisible()) && (!ignoreHiddenState)) {
			return 0;
		}
		boolean isCollapsed = (has(this.mDisplayOptions, 64)) || (!has(this.mDisplayOptions, 32));

		return isCollapsed ? this.mApparentHeightCollapsed : this.mApparentHeightExpanded;
	}

	protected void setViewWidth(int width) {
	}

	public void addTab(TabImpl tab, int position, boolean setSelected) {
		super.addTab(tab, position, setSelected);
		if (this.mTabs.getTopListVisibility() == View.GONE) {
			setTopListVisibility(View.VISIBLE);
		}
	}

	public void setTitle(CharSequence title) {
		super.setTitle(title);
		if (this.mTabs.getTopTitleVisibility() == View.GONE) {
			setTopTitleVisibility(View.VISIBLE);
		}

		this.mTabs.setTopTitleName(title);
	}

	public void setTopListVisibility(int visibility) {
		this.mTabs.setTopListVisibility(visibility);
	}

	public void setTopTitleVisibility(int visibility) {
		this.mTabs.setTopTitleVisibility(visibility);
	}

	public void setLogo(Drawable logo) {
		this.mOptions.setLogo(logo);
	}

	public void showNumberIndicator(int number) {
		showNumberIndicator(this.mTabs.getCount() - 1, number);
	}

	public void showNumberIndicator(int tabIndex, int number) {
		this.mTabs.showNumberIndicator(tabIndex, number);
	}

	public void setMaxNumberIndicator(int max) {
		this.mTabs.setMaxNumberIndicator(max);
	}

	public void showHomeBack(boolean visible) {
		((TopHomeDisplay) this.mHome).showHomeBack(visible);
	}
}
