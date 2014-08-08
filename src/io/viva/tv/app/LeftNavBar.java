package io.viva.tv.app;

import io.viva.baseui.R;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SpinnerAdapter;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LeftNavBar extends ActionBar implements NavBarLayoutWrapper {
	public static final int DISPLAY_ALWAYS_EXPANDED = 32;
	public static final int DISPLAY_USE_LOGO_WHEN_EXPANDED = 128;
	public static final int DISPLAY_SHOW_INDETERMINATE_PROGRESS = 256;
	public static final int DISPLAY_AUTO_EXPAND = 64;
	public static final int DEFAULT_DISPLAY_OPTIONS = 43;
	public static final int DEFAULT_DISPLAY_LEFTNAVBAR = 32;
	private Context mContext;
	private boolean mIsOverlay;
	private TitleBarView mTitleBar;
	private LeftNavView mLeftNav;
	private View mContent;
	private Window mWindow;
	int mNavBarType = 0;
	int mStyle = 0;
	public static final int TYPE_LEFT_NAVBAR = 0;
	public static final int TYPE_TOP_NAVBAR = 1;
	public static final int STYLE_DEFAULT = 0;
	public static final int STYLE_LEFT_MANGOSTEEN = 1;

	public LeftNavBar(Activity activity) {
		this(activity, 0);
	}

	public LeftNavBar(Activity activity, int type) {
		this(activity, type, 0);
	}

	public LeftNavBar(Activity activity, int type, int style) {
		this.mNavBarType = type;
		this.mStyle = style;
		StyleFetcher.setStyle(style);
		initialize(activity.getWindow(), activity);
	}

	private void initialize(Window window, Context context) {
		this.mWindow = window;
		View decor = window.getDecorView();
		ViewGroup group = (ViewGroup) window.getDecorView();
		LayoutInflater inflater = (LayoutInflater) decor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		cleanExistChild(group, R.id.title_container);
		inflater.inflate(R.layout.lib_title_container, group, true);

		this.mContext = decor.getContext();
		this.mIsOverlay = window.hasFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		initWindowLP();

		this.mTitleBar = ((TitleBarView) decor.findViewById(R.id.title_container));

		switch (this.mNavBarType) {
		case TYPE_TOP_NAVBAR:
			cleanExistChild(group, R.id.top_nav);
			inflater.inflate(R.layout.lib_top_nav, group, true);
			this.mLeftNav = ((TopNavView) decor.findViewById(R.id.top_nav));
			break;
		case TYPE_LEFT_NAVBAR:
		default:
			cleanExistChild(group, R.id.left_nav);
			inflater.inflate(R.layout.lib_left_nav, group, true);
			this.mLeftNav = ((LeftNavView) decor.findViewById(R.id.left_nav));
		}

		if (this.mLeftNav != null) {
			this.mLeftNav.setActivity((Activity) context);
		}

		this.mContent = group.getChildAt(0);

		if ((this.mTitleBar == null) || (this.mLeftNav == null)) {
			throw new IllegalStateException(getClass().getSimpleName() + ": incompatible window decor!");
		}

		setDisplayOptions(32);
		showOptionsMenu(true);
	}

	private void cleanExistChild(ViewGroup group, int childId) {
		View child = group.findViewById(childId);
		if (child != null) {
			group.removeView(child);
		}
	}

	private void initWindowLP() {
		if (this.mWindow != null) {
			WindowManager.LayoutParams wmlp = this.mWindow.getAttributes();
			wmlp.flags &= -257;
			this.mWindow.setAttributes(wmlp);
		}
	}

	private void updateWindowLayout(boolean animated) {
		updateTitleBar(animated);
		if (this.mNavBarType == TYPE_LEFT_NAVBAR) {
			if (!this.mIsOverlay) {
				setLeftMargin(this.mContent, this.mLeftNav.getApparentWidth(false));
			}
		} else if ((this.mNavBarType == TYPE_TOP_NAVBAR) && (!this.mIsOverlay)) {
			setTopMargin(this.mContent, this.mLeftNav.getApparentWidth(false));
		}
	}

	private void updateTitleBar(boolean animated) {
		int options = getDisplayOptions();
		boolean titleVisible = has(options, 8);
		boolean progressVisible = has(options, 256);
		boolean horizontalProgressVisible = this.mTitleBar.isHorizontalProgressVisible();
		this.mTitleBar.setVisible((isShowing()) && ((titleVisible) || (progressVisible) || (horizontalProgressVisible)), animated);

		this.mTitleBar.setProgressVisible(progressVisible);
	}

	private void setLeftMargin(View view, int margin) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
		params.leftMargin = margin;
		view.setLayoutParams(params);
	}

	private void setTopMargin(View view, int margin) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
		params.topMargin = margin;
		view.setLayoutParams(params);
	}

	public void show() {
		setVisible(true);
	}

	public void hide() {
		setVisible(false);
	}

	public void show(boolean overlay) {
		setVisible(true, overlay);
	}

	private void setVisible(boolean visible, boolean overlay) {
		boolean shouldAnimate = true;
		if (this.mLeftNav.setVisible(visible, shouldAnimate))
			updateWindowLayout(shouldAnimate);
	}

	private void setVisible(boolean visible) {
		setVisible(visible, false);
	}

	public boolean isShowing() {
		return this.mLeftNav.isVisible();
	}

	public int getNavBarStyle() {
		return this.mStyle;
	}

	public void setTitle(CharSequence title) {
		this.mLeftNav.setTitle(title);
	}

	public void setTitle(int resId) {
		setTitle(this.mContext.getString(resId));
	}

	public CharSequence getTitle() {
		return this.mLeftNav.getTitle();
	}

	public void setSubtitle(CharSequence subtitle) {
		this.mTitleBar.setSubtitle(subtitle);
	}

	public void setSubtitle(int resId) {
		setSubtitle(this.mContext.getString(resId));
	}

	public CharSequence getSubtitle() {
		return this.mTitleBar.getSubtitle();
	}

	public ActionBar.Tab newTab() {
		return new TabImpl(this.mContext) {
			public void select() {
				LeftNavBar.this.selectTab(this);
			}

			public CharSequence getContentDescription() {
				return null;
			}

			public ActionBar.Tab setContentDescription(int resId) {
				return null;
			}

			public ActionBar.Tab setContentDescription(CharSequence contentDesc) {
				return null;
			}
		};
	}

	private TabImpl convertTab(ActionBar.Tab tab) {
		if (tab == null) {
			return null;
		}
		if (!(tab instanceof TabImpl)) {
			throw new IllegalArgumentException("Invalid tab object.");
		}
		return (TabImpl) tab;
	}

	public void addTab(ActionBar.Tab tab) {
		addTab(tab, -2);
	}

	public void addTab(ActionBar.Tab tab, boolean setSelected) {
		addTab(tab, -2, setSelected);
	}

	public void addTab(ActionBar.Tab tab, int position) {
		addTab(tab, position, getTabCount() == 0);
	}

	public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
		this.mLeftNav.addTab(convertTab(tab), position, setSelected);
	}

	public ActionBar.Tab getSelectedTab() {
		return this.mLeftNav.getTabs().getSelected();
	}

	public ActionBar.Tab getTabAt(int index) {
		return this.mLeftNav.getTabs().get(index);
	}

	public int getTabCount() {
		return this.mLeftNav.getTabs().getCount();
	}

	public void removeAllTabs() {
		this.mLeftNav.getTabs().removeAll();
	}

	public void removeTab(ActionBar.Tab tab) {
		this.mLeftNav.getTabs().remove(convertTab(tab));
	}

	public void removeTabAt(int position) {
		this.mLeftNav.getTabs().remove(position);
	}

	public void selectTab(ActionBar.Tab tab) {
		if (tab != null) {
			this.mLeftNav.getTabs().setUnFocus(true);
			this.mLeftNav.getTabs().select(convertTab(tab));
		}
	}

	public int getNavigationItemCount() {
		switch (getNavigationMode()) {
		case 2:
			return getTabCount();
		case 1:
			return this.mLeftNav.getSpinner().getCount();
		}

		throw new IllegalStateException("No count available for mode: " + getNavigationMode());
	}

	public int getNavigationMode() {
		return this.mLeftNav.getNavigationMode();
	}

	public int getSelectedNavigationIndex() {
		switch (getNavigationMode()) {
		case 2:
			ActionBar.Tab selected = getSelectedTab();
			return selected != null ? selected.getPosition() : -1;
		case 1:
			return this.mLeftNav.getSpinner().getSelected();
		}

		throw new IllegalStateException("No selection available for mode: " + getNavigationMode());
	}

	public void setListNavigationCallbacks(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
		this.mLeftNav.getSpinner().setContent(adapter, callback);
	}

	public void setNavigationMode(int mode) {
		this.mLeftNav.setNavigationMode(mode);
	}

	public void setSelectedNavigationItem(int position) {
		switch (getNavigationMode()) {
		case 2:
			selectTab(getTabAt(position));
			break;
		case 1:
			this.mLeftNav.getSpinner().setSelected(position);
			break;
		default:
			throw new IllegalStateException("Cannot set selection on mode: " + getNavigationMode());
		}
	}

	public int getDisplayOptions() {
		return this.mLeftNav.getDisplayOptions();
	}

	private static boolean has(int changes, int option) {
		return (changes & option) != 0;
	}

	public void setDisplayOptions(int options) {
		int changes = this.mLeftNav.setDisplayOptions(options);
		if ((has(changes, 32)) || (has(changes, 64)) || (has(changes, 8)) || (has(changes, 256))) {
			updateWindowLayout(false);
		}
	}

	public void setDisplayOptions(int options, int mask) {
		int current = getDisplayOptions();
		int updated = options & mask | current & (mask ^ 0xFFFFFFFF);
		setDisplayOptions(updated);
	}

	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		setDisplayOptions(showHomeAsUp ? 4 : 0, 4);
	}

	public void setDisplayShowCustomEnabled(boolean showCustom) {
		setDisplayOptions(showCustom ? 16 : 0, 16);
	}

	public void setDisplayShowHomeEnabled(boolean showHome) {
		setDisplayOptions(showHome ? 2 : 0, 2);
	}

	public void setDisplayShowTitleEnabled(boolean showTitle) {
		setDisplayOptions(showTitle ? 8 : 0, 8);
	}

	public void setDisplayUseLogoEnabled(boolean useLogo) {
		setDisplayOptions(useLogo ? 1 : 0, 1);
	}

	public void setShowHorizontalProgress(int value) {
		this.mTitleBar.setHorizontalProgress(value);
		updateWindowLayout(false);
	}

	public View getCustomView() {
		return this.mLeftNav.getCustomView();
	}

	public void setCustomView(View view) {
		this.mLeftNav.setCustomView(view);
	}

	public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
		view.setLayoutParams(layoutParams);
		setCustomView(view);
	}

	public void setCustomView(int resId) {
		setCustomView(LayoutInflater.from(this.mContext).inflate(resId, this.mLeftNav, false));
	}

	public void addOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
	}

	public void removeOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
	}

	public void setBackgroundDrawable(Drawable d) {
		this.mLeftNav.setBackgroundDrawable(d);
	}

	public int getHeight() {
		return this.mLeftNav.getApparentWidth(true);
	}

	public void setShowHideAnimationEnabled(boolean enabled) {
		this.mLeftNav.setAnimationsEnabled(enabled);
		this.mTitleBar.setAnimationsEnabled(enabled);
	}

	@Deprecated
	public void dispatchMenuVisibilityChanged(boolean visible) {
	}

	public ActionMode startActionMode(ActionMode.Callback callback) {
		return null;
	}

	public void showOptionsMenu(boolean show) {
		this.mLeftNav.showOptionsMenu(Boolean.valueOf(show));
	}

	public void setIcon(int resId) {
	}

	public void setIcon(Drawable icon) {
	}

	public void setLogo(int resId) {
		setLogo(this.mContext.getResources().getDrawable(resId));
	}

	public void setLogo(Drawable logo) {
		this.mLeftNav.setLogo(logo);
	}

	public void setOnClickHomeListener(View.OnClickListener listener) {
		this.mLeftNav.setOnClickHomeListener(listener);
	}

	public void setOnClickOptionListener(View.OnClickListener listener) {
		this.mLeftNav.setOnClickOptionListener(listener);
	}

	public void setOnClickTabIndexListener(OnClickTabIndexListener listener) {
		this.mLeftNav.setOnClickTabIndexListener(listener);
	}

	public ViewGroup getHomeDisplay() {
		return this.mLeftNav.getHomeDisplay();
	}

	public ViewGroup getTabDisplay() {
		return this.mLeftNav.getTabDisplay();
	}

	public void setOnClickSettingsLisetener(View.OnClickListener listener) {
		this.mLeftNav.setOnClickSettingListener(listener);
	}

	public void showHomeBack(boolean visible) {
		this.mLeftNav.showHomeBack(visible);
	}

	public void setSettingItem(Drawable icon, CharSequence text) {
	}

	public void setOptionItem(Drawable icon, CharSequence text) {
	}

	public ViewGroup getOptionDisplay() {
		return this.mLeftNav.getOptionDisplay();
	}

	public void showNumberIndicator(int number) {
		this.mLeftNav.showNumberIndicator(number);
	}

	public void showNumberIndicator(int tabIndex, int number) {
		this.mLeftNav.showNumberIndicator(tabIndex, number);
	}

	public boolean handleBackPress() {
		return this.mLeftNav.handleBackPress();
	}

	public void setMaxNumberIndicatoer(int max) {
		this.mLeftNav.setMaxNumberIndicator(max);
	}

	public void loseFocusOnFirstFocusFinder() {
		this.mLeftNav.loseFocusOnFirstFocusFinder();
	}

	public void alwaysLostFocus(boolean always) {
		this.mLeftNav.alwaysLoseFocus(always);
	}

	public void setOnClickSearchListener(View.OnClickListener listener) {
		this.mLeftNav.setOnClickSearchListener(listener);
	}

	public static abstract interface OnClickTabIndexListener {
		public abstract void onClickTabIndex(ActionBar.Tab paramTab, int paramInt);
	}
}
