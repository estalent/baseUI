package io.viva.tv.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public abstract class TabImpl extends ActionBar.Tab {
	private final Context mContext;
	private ActionBar.TabListener mCallback;
	private Object mTag;
	private Drawable mIcon;
	private CharSequence mText;
	private int mPosition;
	private View mCustomView;

	public TabImpl(Context context) {
		this.mContext = context;
	}

	public Object getTag() {
		return this.mTag;
	}

	public ActionBar.Tab setTag(Object tag) {
		this.mTag = tag;
		return this;
	}

	public ActionBar.Tab setTabListener(ActionBar.TabListener callback) {
		this.mCallback = callback;
		return this;
	}

	public View getCustomView() {
		return this.mCustomView;
	}

	public ActionBar.Tab setCustomView(View view) {
		this.mCustomView = view;
		return this;
	}

	public ActionBar.Tab setCustomView(int layoutResId) {
		return setCustomView(LayoutInflater.from(this.mContext).inflate(layoutResId, null));
	}

	public Drawable getIcon() {
		return this.mIcon;
	}

	public int getPosition() {
		return this.mPosition;
	}

	public void setPosition(int position) {
		this.mPosition = position;
	}

	public CharSequence getText() {
		return this.mText;
	}

	public ActionBar.Tab setIcon(Drawable icon) {
		this.mIcon = icon;
		return this;
	}

	public ActionBar.Tab setIcon(int resId) {
		return setIcon(this.mContext.getResources().getDrawable(resId));
	}

	public ActionBar.Tab setText(CharSequence text) {
		this.mText = text;
		return this;
	}

	public ActionBar.Tab setText(int resId) {
		return setText(this.mContext.getResources().getText(resId));
	}

	public ActionBar.TabListener getCallback() {
		return this.mCallback;
	}

	boolean hasCustomView() {
		return this.mCustomView != null;
	}

	public String toString() {
		Object source = this.mTag != null ? this.mTag : this.mText;
		return "Tab:" + (source != null ? source.toString() : "<no id>");
	}
}
