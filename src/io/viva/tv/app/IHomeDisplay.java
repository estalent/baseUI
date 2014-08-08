package io.viva.tv.app;

import android.view.View;

public abstract interface IHomeDisplay {
	public abstract View getView();

	public abstract IHomeDisplay setVisible(boolean paramBoolean);

	public abstract IHomeDisplay setAsUp(boolean paramBoolean);

	public abstract boolean isVisible();

	public abstract IHomeDisplay setImageMode(Mode paramMode);

	public abstract void setOnClickHomeListener(View.OnClickListener paramOnClickListener);

	public abstract IHomeDisplay setExpanded(boolean paramBoolean);

	public static enum Mode {
		ICON, LOGO, BOTH;
	}
}
