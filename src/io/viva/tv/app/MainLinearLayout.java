package io.viva.tv.app;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class MainLinearLayout extends LinearLayout {
	TabHorizontalListView mTabHorizontalListView;

	public MainLinearLayout(Context context) {
		super(context);
	}

	public MainLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setTabtHorizontalListView(TabHorizontalListView tabHorizontalListView) {
		this.mTabHorizontalListView = tabHorizontalListView;
	}

	protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
		int count = getChildCount();
		int end;
		int index;
		int increment;
		if ((direction == 130) && (this.mTabHorizontalListView != null) && (this.mTabHorizontalListView.getCount() > 0)) {
			index = 1;
			increment = 1;
			end = count;
		} else {
			if ((direction & 0x2) != 0) {
				index = 0;
				increment = 1;
				end = count;
			} else {
				index = count - 1;
				increment = -1;
				end = -1;
			}
		}

		for (int i = index; i != end; i += increment) {
			View child = getChildAt(index);
			if ((child.getVisibility() == 0) && (child.requestFocus(direction, previouslyFocusedRect))) {
				return true;
			}
		}

		return false;
	}
}
