package io.viva.tv.app;

import io.viva.baseui.R;
import io.viva.tv.app.widget.NumberIndicator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public abstract class AbsNavView extends LinearLayout {
	protected IHomeDisplay mHome;
	protected TabDisplay mTabs;
	protected OptionsDisplay mOptions;
	protected SpinnerDisplay mSpinner;
	protected NumberIndicator mNumberIndicator;
	protected LinearLayout mMainLayout;
	private VisibilityController mVisibilityController;
	private Activity mActivity;
	protected int mAnimationDuration;
	protected boolean mAnimationsEnabled = true;
	protected boolean isLeftNavView;
	protected CharSequence mTitle;
	protected Drawable mLogo;
	protected boolean mLoseFocusOnFirstFocusFinder = false;
	protected boolean mAlwaysLoseFocus;

	public ViewGroup getHomeDisplay() {
		return (ViewGroup) this.mHome.getView();
	}

	public Activity getAcitivity() {
		return this.mActivity;
	}

	public void setActivity(Activity activity) {
		this.mActivity = activity;
	}

	public ViewGroup getTabDisplay() {
		return this.mTabs.getTabDisplay();
	}

	public ViewGroup getOptionDisplay() {
		return (ViewGroup) this.mOptions.getView();
	}

	public AbsNavView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mVisibilityController = new VisibilityController(this);
		initNavView(context);
	}

	public boolean isHorizontal() {
		return this.isLeftNavView;
	}

	public void setOnClickHomeListener(View.OnClickListener listener) {
		this.mHome.setOnClickHomeListener(listener);
	}

	public void setOnClickOptionListener(View.OnClickListener listener) {
		this.mOptions.setOnClickMenuOptionListener(listener);
	}

	public void setOnClickSettingListener(View.OnClickListener listener) {
		this.mOptions.setOnClickSettingsOptionListener(listener);
	}

	public void setOnClickTabIndexListener(LeftNavBar.OnClickTabIndexListener listener) {
		this.mTabs.setOnClickTabIndexListener(listener);
	}

	public void setOnClickSearchListener(View.OnClickListener listener) {
		this.mOptions.setOnClickSearchOptionListener(listener);
	}

	protected abstract void initNavView(Context paramContext);

	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	public boolean setVisible(boolean visible, boolean animated) {
		return this.mVisibilityController.setVisible(visible, (animated) && (this.mAnimationsEnabled));
	}

	public boolean isVisible() {
		return this.mVisibilityController.isVisible();
	}

	ViewGroup getMainSection() {
		return (ViewGroup) findViewById(R.id.main);
	}

	public boolean isLeftNavView() {
		return this.isLeftNavView;
	}

	public CharSequence getTitle() {
		return this.mTitle;
	}

	public void setTitle(CharSequence title) {
		this.mTitle = title;
	}

	public Drawable getLogo() {
		return this.mLogo;
	}

	public void setLogo(Drawable logo) {
	}

	public void showHomeBack(boolean visible) {
	}

	public boolean handleBackPress() {
		View focusView = getFocusedChild();
		if ((focusView == null) && (this.mTabs.getCount() > 0)) {
			this.mTabs.getView().requestFocus();
			return true;
		}
		return false;
	}

	public void setBackgroundDrawable(Drawable d) {
		if (this.mMainLayout != null) {
			this.mMainLayout.setBackgroundDrawable(d);
		}
	}

	public void loseFocusOnFirstFocusFinder() {
		this.mLoseFocusOnFirstFocusFinder = true;
	}

	public void alwaysLoseFocus(boolean always) {
		this.mAlwaysLoseFocus = always;
	}

	public abstract void addTab(TabImpl paramTabImpl, int paramInt, boolean paramBoolean);

	public abstract void showNumberIndicator(int paramInt);

	public abstract void showNumberIndicator(int paramInt1, int paramInt2);

	public abstract void setMaxNumberIndicator(int paramInt);
}
