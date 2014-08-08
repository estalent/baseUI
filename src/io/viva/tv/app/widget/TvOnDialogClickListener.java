package io.viva.tv.app.widget;

import android.content.DialogInterface;

public abstract class TvOnDialogClickListener implements DialogInterface.OnClickListener {
	boolean isPalyBtnClick = false;

	public void onClick(DialogInterface paramDialogInterface, int paramInt) {
		synchronized (this) {
			if (this.isPalyBtnClick)
				return;
			this.isPalyBtnClick = true;
		}
		onClicked(paramDialogInterface, paramInt);
		synchronized (this) {
			this.isPalyBtnClick = false;
		}
	}

	public abstract void onClicked(DialogInterface paramDialogInterface, int paramInt);
}
