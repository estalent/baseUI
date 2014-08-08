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
import android.widget.Button;

class TopHomeDisplay implements IHomeDisplay {
	private static final String TAG = "LeftNavBar-Home";
	private IHomeDisplay.Mode mMode;
	private final Context mContext;
	private Drawable mLogo;
	private Drawable mIcon;
	private View mView;
	private boolean mExpanded;
	private Button backBtn;
	AbsNavView mNavView;

	TopHomeDisplay(Context context, ViewGroup parent, TypedArray attributes) {
		this.mContext = context;
		this.mMode = IHomeDisplay.Mode.ICON;

		createView(parent, attributes);
		this.mNavView = ((AbsNavView) parent);
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
		this.backBtn.setOnClickListener(listener);
	}

	public void showHomeBack(boolean visible) {
		this.backBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void createView(ViewGroup parent, TypedArray attributes) {
		this.mView = parent.findViewById(R.id.homeDisplay);
		this.backBtn = ((Button) this.mView.findViewById(R.id.top_nav_btn_back));
		this.backBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Activity activity = TopHomeDisplay.this.mNavView.getAcitivity();
				activity.finish();
			}
		});
	}

	private void updateImage() {
		boolean useIcon = (this.mMode == IHomeDisplay.Mode.ICON) || (this.mLogo == null) || ((this.mMode == IHomeDisplay.Mode.BOTH) && (!this.mExpanded));

		this.backBtn.setBackgroundDrawable(useIcon ? this.mIcon : this.mLogo);
	}

	public View getView() {
		return this.mView;
	}

	public TopHomeDisplay setVisible(boolean visible) {
		this.mView.findViewById(R.id.top_nav_btn_back).setVisibility(visible ? View.VISIBLE : View.GONE);
		return this;
	}

	public boolean isVisible() {
		return this.mView.getVisibility() == View.VISIBLE;
	}

	public TopHomeDisplay setExpanded(boolean expanded) {
		this.mExpanded = expanded;

		return this;
	}

	public TopHomeDisplay setImageMode(IHomeDisplay.Mode mode) {
		this.mMode = mode;

		return this;
	}

	public TopHomeDisplay setAsUp(boolean asUp) {
		return this;
	}
}
