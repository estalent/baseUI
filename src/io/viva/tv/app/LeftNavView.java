package io.viva.tv.app;

import io.viva.baseui.R;
import io.viva.tv.app.widget.NumberIndicator;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class LeftNavView extends AbsNavView {
	private int mWidthCollapsed;
	private int mWidthExpanded;
	private int mApparentWidthCollapsed;
	private int mApparentWidthExpanded;
	int mDisplayOptions;
	int mNavigationMode;
	boolean mExpanded;
	private ValueAnimator mWidthAnimator;
	private int mStyle;
	public static final int STYLE_DEFAULT = 0;
	public static final int STYLE_LEFT_MANGOSTEEN = 1;

	public LeftNavView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void initNavView(Context context) {
		this.mStyle = StyleFetcher.getStyle();

		setOrientation(VERTICAL);
		this.isLeftNavView = true;

		this.mHome = new HomeDisplay(context, this, null).setVisible(false);
		this.mTabs = new TabDisplay(context, this, null, this.isLeftNavView).setVisible(false);
		this.mOptions = new OptionsDisplay(context, this, null).setVisible(false);
		this.mOptions.setTabDisplay(this.mTabs);
		this.mSpinner = new SpinnerDisplay(context, this, null).setVisible(false);
		this.mNumberIndicator = ((NumberIndicator) findViewById(R.id.numberIndicator));

		Resources res = context.getResources();
		this.mWidthCollapsed = res.getDimensionPixelSize(R.dimen.left_nav_default_width);
		this.mWidthExpanded = res.getDimensionPixelSize(R.dimen.left_nav_default_width);
		this.mApparentWidthCollapsed = res.getDimensionPixelSize(R.dimen.left_nav_expanded_width);
		this.mApparentWidthExpanded = res.getDimensionPixelSize(R.dimen.left_nav_default_width);
		this.mAnimationDuration = 100;

		this.mNavigationMode = 0;
		setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		this.mMainLayout = ((LinearLayout) findViewById(R.id.main));

		setDisplayStyle(this.mStyle);
	}

	private int inflateLayout(int layoutStyle) {
		switch (layoutStyle) {
		case 1:
			return R.layout.left_nav_mangosteen;
		case 0:
		}
		return R.layout.left_nav;
	}

	public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
		return this.mTabs.getunFocus() ? false : super.requestFocus(direction, previouslyFocusedRect);
	}

	public View findFocus() {
		if (this.mAlwaysLoseFocus) {
			return null;
		}
		return super.findFocus();
	}

	public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
		if (this.mAlwaysLoseFocus) {
			return;
		}
		if (this.isLeftNavView) {
			if (direction == 2) {
				super.addFocusables(views, direction, focusableMode);
				return;
			}

			if ((direction == 130) && (!hasFocus()) && (this.mTabs != null) && (this.mTabs.lostFocusOnTabListView())) {
				super.addFocusables(views, direction, focusableMode);
				return;
			}
			if ((direction != 17) && (!hasFocus())) {
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
		if ((this.isLeftNavView) && (hasFocus()) && (direction != 66)) {
			return FocusFinder.getInstance().findNextFocus(this, focused, direction);
		}
		return super.focusSearch(focused, direction);
	}

	protected void onDescendantFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (has(this.mDisplayOptions, 64))
			setExpanded(hasFocus);
	}

	public void setDisplayStyle(int style) {
		if (this.mTabs != null)
			this.mTabs.setDisplayStyle(style);
	}

	public int getDisplayOptions() {
		return this.mDisplayOptions;
	}

	public int setDisplayOptions(int options) {
		int changes = options ^ this.mDisplayOptions;
		this.mDisplayOptions = options;
		if (has(changes, 2)) {
			this.mHome.setVisible(has(options, 2));
		}
		if ((has(changes, 1)) || (has(changes, 128))) {
			setHomeMode();
		}
		if (has(changes, 4)) {
			this.mHome.setAsUp(has(options, 4));
		}

		if ((has(changes, 64)) || (has(changes, 32))) {
			setExpandedState();
		}
		return changes;
	}

	private void setHomeMode() {
		IHomeDisplay.Mode mode;
		if (has(this.mDisplayOptions, 128)) {
			mode = IHomeDisplay.Mode.BOTH;
		} else {
			if (has(this.mDisplayOptions, 1))
				mode = IHomeDisplay.Mode.LOGO;
			else
				mode = IHomeDisplay.Mode.ICON;
		}
		this.mHome.setImageMode(mode);
	}

	private void setExpandedState() {
		if (has(this.mDisplayOptions, 64)) {
			setExpanded(hasFocus(), false);
		} else {
			setExpanded(has(this.mDisplayOptions, 32), false);
		}
	}

	private void setExpanded(boolean expanded) {
		setExpanded(expanded, (this.mAnimationsEnabled) && (isVisible()));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setExpanded(final boolean expanded, boolean animated) {
		if (this.mExpanded == expanded) {
			return;
		}
		if (animated) {
			if (this.mWidthAnimator != null) {
				this.mWidthAnimator.cancel();
			}
			this.mWidthAnimator = ValueAnimator.ofInt(new int[] { getLayoutParams().width, expanded ? this.mWidthExpanded : this.mWidthCollapsed });

			this.mWidthAnimator.setDuration(this.mAnimationDuration);
			this.mWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				public void onAnimationUpdate(ValueAnimator animation) {
					LeftNavView.this.setViewWidth(((Integer) animation.getAnimatedValue()).intValue());
				}
			});
			this.mWidthAnimator.addListener(new AnimatorListenerAdapter() {
				public void onAnimationStart(Animator animator) {
					if (!expanded) {
						LeftNavView.this.setContentExpanded(false);
					}
				}

				public void onAnimationEnd(Animator animator) {
					if (expanded) {
						LeftNavView.this.setContentExpanded(true);
					}
				}
			});
			this.mWidthAnimator.start();
		} else {
			setViewWidth(expanded ? this.mWidthExpanded : this.mWidthCollapsed);
			setContentExpanded(expanded);
		}
		this.mExpanded = expanded;
	}

	private void setContentExpanded(boolean expanded) {
		this.mTabs.setExpanded(expanded);
		this.mOptions.setExpanded(expanded);
		this.mHome.setExpanded(expanded);
	}

	protected void setViewWidth(int width) {
		ViewGroup.LayoutParams params = getLayoutParams();
		params.width = width;
		setLayoutParams(params);
	}

	public int getApparentWidth(boolean ignoreHiddenState) {
		if ((!isVisible()) && (!ignoreHiddenState)) {
			return 0;
		}
		boolean isCollapsed = (has(this.mDisplayOptions, 64)) || (!has(this.mDisplayOptions, 32));

		switch (this.mStyle) {
		case 1:
			return this.mApparentWidthCollapsed;
		case 0:
		}
		return isCollapsed ? this.mApparentWidthCollapsed : this.mApparentWidthExpanded;
	}

	public void setAnimationsEnabled(boolean enabled) {
		this.mAnimationsEnabled = enabled;
	}

	static boolean has(int changes, int option) {
		return (changes & option) != 0;
	}

	public TabDisplay getTabs() {
		return this.mTabs;
	}

	public SpinnerDisplay getSpinner() {
		return this.mSpinner;
	}

	public void setNavigationMode(int mode) {
		if (this.mNavigationMode == mode) {
			return;
		}
		setNavigationModeVisibility(this.mNavigationMode, false);
		setNavigationModeVisibility(mode, true);
		this.mNavigationMode = mode;
	}

	private void setNavigationModeVisibility(int mode, boolean visible) {
		switch (mode) {
		case 2:
			this.mTabs.setVisible(visible);
			break;
		case 1:
			this.mSpinner.setVisible(visible);
			break;
		}
	}

	public int getNavigationMode() {
		return this.mNavigationMode;
	}

	public void showOptionsMenu(Boolean show) {
		this.mOptions.setVisible(show.booleanValue());
	}

	public void setCustomView(View view) {
	}

	private boolean hasCustomView() {
		return getCustomViewWrapper() != null;
	}

	private boolean hasVisibleCustomView() {
		return hasCustomView() && getCustomViewWrapper().getVisibility() == View.VISIBLE;
	}

	public View getCustomView() {
		return null;
	}

	private CustomViewWrapper getCustomViewWrapper() {
		ViewGroup main = getMainSection();

		if (main.getChildCount() == 3) {
			return (CustomViewWrapper) main.getChildAt(2);
		}
		return null;
	}

	private void setCustomViewVisibility(boolean visible) {
		View current = getCustomViewWrapper();
		if (current != null) {
			current.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	public void addTab(TabImpl tab, int position, boolean setSelected) {
		this.mTabs.add(tab, position, setSelected);
	}

	public void showNumberIndicator(int number) {
		if (number <= 0) {
			this.mNumberIndicator.hide();
		} else {
			this.mNumberIndicator.setNumber(number);
			this.mNumberIndicator.show();
		}
	}

	public void setMaxNumberIndicator(int max) {
		this.mNumberIndicator.setMaxNum(max);
	}

	public void showNumberIndicator(int tabIndex, int number) {
	}

	private static final class CustomViewWrapper extends ViewGroup {
		private final View mView;

		CustomViewWrapper(Context context, View view) {
			super(context);
			setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

			this.mView = view;
			if (!(view.getLayoutParams() instanceof ActionBar.LayoutParams)) {
				view.setLayoutParams(generateDefaultLayoutParams());
			}
			addView(view);
		}

		View getView() {
			return this.mView;
		}

		void detach() {
			removeView(this.mView);
		}

		private int findTopOfAvailableSpace(LeftNavView parent) {
			int top = parent.getPaddingTop();
			if (parent.mHome.isVisible()) {
				top += parent.mHome.getView().getMeasuredHeight();
			}
			switch (parent.mNavigationMode) {
			case 2:
				top += parent.mTabs.getView().getMeasuredHeight();
				break;
			case 1:
				top += parent.mSpinner.getView().getMeasuredHeight();
				break;
			}

			return top;
		}

		private int findBottomOfAvailableSpace(LeftNavView parent) {
			int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
			if (parent.mOptions.isVisible()) {
				bottom -= parent.mOptions.getView().getMeasuredHeight();
			}
			return bottom;
		}

		private void checkDimensionsConsistency(int value, int expected) {
			if (value != expected) {
				throw new IllegalStateException("Inconsistent dimensions!");
			}
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		void onPostMeasure(LeftNavView parent) {
			int totalWidth = parent.getMeasuredWidth();
			int totalHeight = parent.getMeasuredHeight();

			int topOfAvailableSpace = findTopOfAvailableSpace(parent);
			int bottomOfAvailableSpace = findBottomOfAvailableSpace(parent);

			int availableWidth = totalWidth - parent.getPaddingLeft() - parent.getPaddingRight();
			int availableHeight = bottomOfAvailableSpace - topOfAvailableSpace;

			int availableInTopHalf = totalHeight / 2 - topOfAvailableSpace;
			int availableInBottomHalf = bottomOfAvailableSpace - totalHeight / 2;

			if (getMeasuredWidth() != 0) {
				checkDimensionsConsistency(availableWidth, getMeasuredWidth());
			}
			if (getMeasuredHeight() != 0) {
				checkDimensionsConsistency(availableHeight, getMeasuredHeight());
			}

			ActionBar.LayoutParams params = (ActionBar.LayoutParams) this.mView.getLayoutParams();
			int horizontalMargin = params.leftMargin + params.rightMargin;
			int verticalMargin = params.topMargin + params.bottomMargin;

			int widthMode = params.width != ViewGroup.LayoutParams.WRAP_CONTENT ? MeasureSpec.EXACTLY : MeasureSpec.AT_MOST;

			int widthValue = params.width >= 0 ? Math.min(params.width, availableWidth) : availableWidth;

			widthValue = Math.max(0, widthValue - horizontalMargin);

			int heightMode = params.height != ViewGroup.LayoutParams.WRAP_CONTENT ? MeasureSpec.EXACTLY : MeasureSpec.AT_MOST;

			int heightValue = params.height >= 0 ? Math.min(params.height, availableHeight) : availableHeight;

			heightValue = Math.max(0, heightValue - verticalMargin);

			int vGravity = params.gravity & 0x70;
			if ((vGravity == Gravity.CENTER_VERTICAL) && (params.height == ViewGroup.LayoutParams.MATCH_PARENT) && (availableInTopHalf > 0) && (availableInBottomHalf > 0)) {
				heightValue = Math.min(availableInTopHalf, availableInBottomHalf) * 2;
			}

			this.mView.measure(View.MeasureSpec.makeMeasureSpec(widthValue, widthMode), View.MeasureSpec.makeMeasureSpec(heightValue, heightMode));
		}

		protected void onLayout(boolean changed, int l, int t, int r, int b) {
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		void onPostLayout(LeftNavView parent) {
			int width = this.mView.getMeasuredWidth();
			int height = this.mView.getMeasuredHeight();
			ActionBar.LayoutParams params = (ActionBar.LayoutParams) this.mView.getLayoutParams();

			int xPosition = 0;

			int containerWidth = getRight() - getLeft();
			switch (params.gravity & 0x7) {
			case 1:
				xPosition = (containerWidth - width) / 2;
				break;
			case 3:
				xPosition = params.leftMargin;
				break;
			case 5:
				xPosition = containerWidth - width - params.rightMargin;
			case 2:
			case 4:
			}
			int vGravity = params.gravity & 0x70;

			int superContainerHeight = parent.getBottom() - parent.getTop() - parent.getPaddingTop() - parent.getPaddingBottom();

			int superCenteredTop = (superContainerHeight - height) / 2 + parent.getPaddingTop();

			int top = findTopOfAvailableSpace(parent);
			int bottom = findBottomOfAvailableSpace(parent);

			if (getBottom() - getTop() != 0) {
				checkDimensionsConsistency(bottom - top, getBottom() - getTop());
			}

			if (vGravity == Gravity.CENTER_VERTICAL) {
				if (superCenteredTop < top) {
					vGravity = Gravity.TOP;
				} else if (superCenteredTop + height > bottom) {
					vGravity = Gravity.BOTTOM;
				}

			}

			int yPosition = 0;
			int containerHeight = bottom - top;
			switch (vGravity) {
			case Gravity.CENTER_VERTICAL:
				yPosition = superCenteredTop - top;
				break;
			case Gravity.TOP:
				yPosition = params.topMargin;
				break;
			case Gravity.BOTTOM:
				yPosition = containerHeight - height - params.bottomMargin;
			}

			this.mView.layout(xPosition, yPosition, xPosition + width, yPosition + height);
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
			return new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		}
	}
}
