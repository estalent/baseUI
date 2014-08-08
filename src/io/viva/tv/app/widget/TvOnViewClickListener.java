package io.viva.tv.app.widget;

import android.view.View;

public abstract class TvOnViewClickListener implements View.OnClickListener {
	boolean isPalyBtnClick = false;

	public void onClick(View paramView) {
		synchronized (this) {
			if (this.isPalyBtnClick)
				return;
			this.isPalyBtnClick = true;
		}
		onClicked(paramView);
		synchronized (this) {
			this.isPalyBtnClick = false;
		}
	}

	public abstract void onClicked(View paramView);
}
