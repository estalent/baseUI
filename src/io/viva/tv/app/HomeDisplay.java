package io.viva.tv.app;

import io.viva.baseui.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

class HomeDisplay implements IHomeDisplay {
	private static final String TAG = "LeftNavBar-Home";
	private IHomeDisplay.Mode mMode;
	private final Context mContext;
	private Drawable mLogo;
	private Drawable mIcon;
	private View mView;
	private boolean mExpanded;

	HomeDisplay(Context context, ViewGroup parent, TypedArray attributes) {
		this.mContext = context;
		this.mMode = IHomeDisplay.Mode.ICON;
		ApplicationInfo appInfo = context.getApplicationInfo();
		PackageManager pm = context.getPackageManager();
		loadLogo(attributes, pm, appInfo);
		loadIcon(attributes, pm, appInfo);
		createView(parent, attributes);
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void loadLogo(TypedArray a, PackageManager pm, ApplicationInfo appInfo) {
		if ((this.mContext instanceof Activity)) {
			try {
				this.mLogo = pm.getActivityLogo(((Activity) this.mContext).getComponentName());
			} catch (PackageManager.NameNotFoundException e) {
				Log.e(TAG, "Failed to load app logo.", e);
			}
		}
		if (this.mLogo == null) {
			this.mLogo = appInfo.loadLogo(pm);
		}
	}

	private void loadIcon(TypedArray a, PackageManager pm, ApplicationInfo appInfo) {
		if ((this.mContext instanceof Activity)) {
			try {
				this.mIcon = pm.getActivityIcon(((Activity) this.mContext).getComponentName());
			} catch (PackageManager.NameNotFoundException e) {
				Log.e(TAG, "Failed to load app icon.", e);
			}
		}
		if (this.mIcon == null) {
			this.mIcon = appInfo.loadIcon(pm);
		}
	}

	public void setOnClickHomeListener(View.OnClickListener listener) {
		this.mView.setOnClickListener(listener);
	}

	private void createView(ViewGroup parent, TypedArray attributes) {
		this.mView = parent.findViewById(R.id.homeDisplay);
	}

	private void updateImage() {
		boolean useIcon = (this.mMode == IHomeDisplay.Mode.ICON) || (this.mLogo == null) || ((this.mMode == IHomeDisplay.Mode.BOTH) && (!this.mExpanded));
		((ImageView) this.mView.findViewById(R.id.home)).setImageDrawable(useIcon ? this.mIcon : this.mLogo);
	}

	public View getView() {
		return this.mView;
	}

	public HomeDisplay setVisible(boolean visible) {
		if (this.mView != null) {
			this.mView.findViewById(R.id.home).setVisibility(visible ? View.VISIBLE : View.GONE);
		}
		return this;
	}

	public boolean isVisible() {
		return this.mView.getVisibility() == View.VISIBLE;
	}

	public HomeDisplay setExpanded(boolean expanded) {
		this.mExpanded = expanded;
		updateImage();
		return this;
	}

	public HomeDisplay setImageMode(IHomeDisplay.Mode mode) {
		this.mMode = mode;
		updateImage();
		return this;
	}

	public HomeDisplay setAsUp(boolean asUp) {
		return this;
	}
}
