package io.viva.tv.app;

import io.viva.baseui.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TitleBarView extends RelativeLayout {
	private final VisibilityController mVisibilityController;
	private boolean mIsLegacy;
	private boolean mAnimationsEnabled;
	private TextView mTitle;
	private TextView mSubtitle;
	private ImageView mLeftIcon;
	private ImageView mRightIcon;
	private ProgressBar mCircularProgress;
	private ProgressBar mHorizontalProgress;
	private int mTitleResource;
	private int mSubtitleResource;
	Context mContext;

	public TitleBarView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		this.mContext = context;
		this.mVisibilityController = new VisibilityController(this);
		TypedArray a = context.obtainStyledAttributes(attrs, new int[] { 16842843, 16843245 });

		this.mIsLegacy = a.getBoolean(a.getIndex(1), false);
		if (this.mIsLegacy) {
			this.mTitleResource = a.getResourceId(a.getIndex(0), 0);
		} else {
			a.recycle();
			a = context.obtainStyledAttributes(null, new int[] { 16843512, 16843513 }, 16843470, 0);

			this.mTitleResource = a.getResourceId(a.getIndex(0), 0);
			this.mSubtitleResource = a.getResourceId(a.getIndex(1), 0);
		}
		a.recycle();
	}

	protected void onFinishInflate() {
		super.onFinishInflate();
		if (getChildCount() == 0) {
			LayoutInflater.from(mContext).inflate(R.layout.lib_title_bar, this, true);
		}
		this.mTitle = ((TextView) findViewById(R.id.title));
		this.mSubtitle = ((TextView) findViewById(R.id.subtitle));
		this.mLeftIcon = ((ImageView) findViewById(R.id.left_icon));
		this.mRightIcon = ((ImageView) findViewById(R.id.right_icon));
		this.mCircularProgress = ((ProgressBar) findViewById(R.id.progress_circular));
		if (this.mCircularProgress != null) {
			this.mCircularProgress.setIndeterminate(true);
		}
		this.mHorizontalProgress = ((ProgressBar) findViewById(R.id.progress_horizontal));
		if (this.mIsLegacy) {
			setTextStyle(this.mTitle, this.mTitleResource);
			disableSubtitle();
		} else {
			setTextStyle(this.mTitle, this.mTitleResource);
			setTextStyle(this.mSubtitle, this.mSubtitleResource);
			disableLeftIcon();
			disableRightIcon();
		}
	}

	private void setTextStyle(TextView view, int style) {
		if (style != 0) {
			view.setTextAppearance(getContext(), style);
		}
	}

	public void setTitle(CharSequence text) {
		this.mTitle.setText(text);
	}

	public void setTitleColor(int color) {
		this.mTitle.setTextColor(color);
	}

	public void setLeftIcon(Drawable drawable, int alpha) {
		setIcon(this.mLeftIcon, drawable, alpha);
	}

	public void setRightIcon(Drawable drawable, int alpha) {
		setIcon(this.mRightIcon, drawable, alpha);
	}

	private void setIcon(ImageView view, Drawable drawable, int alpha) {
		if (view == null) {
			return;
		}
		if (drawable != null) {
			drawable.setAlpha(alpha);
			view.setImageDrawable(drawable);
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.GONE);
		}
	}

	public void setHorizontalProgress(int value) {
		if (this.mHorizontalProgress == null) {
			return;
		}
		switch (value) {
		case -1:
			this.mHorizontalProgress.setVisibility(View.VISIBLE);
			break;
		case -2:
			this.mHorizontalProgress.setVisibility(View.GONE);
			break;
		case -3:
			this.mHorizontalProgress.setIndeterminate(true);
			break;
		case -4:
			this.mHorizontalProgress.setIndeterminate(false);
			break;
		default:
			if ((0 <= value) && (value <= 10000))
				this.mHorizontalProgress.setProgress(value - 0);
			else if ((20000 <= value) && (value <= 30000)) {
				this.mHorizontalProgress.setSecondaryProgress(value - 20000);
			}
			break;
		}
	}

	public boolean isHorizontalProgressVisible() {
		return (this.mHorizontalProgress != null) && (this.mHorizontalProgress.getVisibility() == 0);
	}

	public void setCircularProgress(int value) {
		if (this.mCircularProgress == null) {
			return;
		}
		switch (value) {
		case -1:
			this.mCircularProgress.setVisibility(0);
			break;
		case -2:
			this.mCircularProgress.setVisibility(8);
			break;
		}
	}

	public void disableLeftIcon() {
		removeFromParent(this.mLeftIcon);
		this.mLeftIcon = null;
	}

	public void disableRightIcon() {
		removeFromParent(this.mRightIcon);
		this.mRightIcon = null;
	}

	public void disableHorizontalProgress() {
		removeFromParent(this.mHorizontalProgress);
		this.mHorizontalProgress = null;
	}

	public void disableCircularProgress() {
		removeFromParent(this.mCircularProgress);
		this.mCircularProgress = null;
	}

	private void disableSubtitle() {
		removeFromParent(this.mSubtitle);
		this.mSubtitle = null;
	}

	private static void removeFromParent(View view) {
		if (view == null) {
			return;
		}
		ViewParent parent = view.getParent();
		if (parent != null)
			((ViewGroup) parent).removeView(view);
	}

	public CharSequence getTitle() {
		return this.mTitle.getText();
	}

	public void setSubtitle(CharSequence text) {
		this.mSubtitle.setText(text);
		this.mSubtitle.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
	}

	public CharSequence getSubtitle() {
		return this.mSubtitle.getText();
	}

	public void setAnimationsEnabled(boolean enabled) {
		this.mAnimationsEnabled = enabled;
	}

	public void setVisible(boolean visible, boolean animated) {
		this.mVisibilityController.setVisible(visible, (animated) && (this.mAnimationsEnabled));
	}

	public boolean isVisible() {
		return this.mVisibilityController.isVisible();
	}

	public int getApparentHeight() {
		return isVisible() ? this.mContext.getResources().getDimensionPixelSize(2114125853) : 0;
	}

	public void setProgressVisible(boolean visible) {
		setCircularProgress(visible ? -1 : -2);
	}
}
