package io.viva.tv.app;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

public abstract interface NavBarLayoutWrapper {
	public abstract ViewGroup getHomeDisplay();

	public abstract void showHomeBack(boolean paramBoolean);

	public abstract ViewGroup getTabDisplay();

	public abstract void setSettingItem(Drawable paramDrawable, CharSequence paramCharSequence);

	public abstract void setOptionItem(Drawable paramDrawable, CharSequence paramCharSequence);

	public abstract ViewGroup getOptionDisplay();

	public abstract void setOnClickSettingsLisetener(View.OnClickListener paramOnClickListener);

	public abstract void setOnClickOptionListener(View.OnClickListener paramOnClickListener);

	public abstract void setOnClickTabIndexListener(LeftNavBar.OnClickTabIndexListener paramOnClickTabIndexListener);

	public abstract void setOnClickSearchListener(View.OnClickListener paramOnClickListener);

	public abstract void setOnClickHomeListener(View.OnClickListener paramOnClickListener);

	public abstract void setShowHideAnimationEnabled(boolean paramBoolean);

	public abstract void show(boolean paramBoolean);

	public abstract void showNumberIndicator(int paramInt);

	public abstract void showNumberIndicator(int paramInt1, int paramInt2);

	public abstract void setMaxNumberIndicatoer(int paramInt);

	public abstract boolean handleBackPress();

	public abstract void loseFocusOnFirstFocusFinder();

	public abstract void alwaysLostFocus(boolean paramBoolean);
}
