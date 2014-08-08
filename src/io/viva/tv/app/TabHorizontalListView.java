package io.viva.tv.app;

import io.viva.baseui.R;
import io.viva.tv.app.animation.AnimationUtils;
import io.viva.tv.app.animation.MultiAnimator;
import io.viva.tv.app.animation.PosInfo;
import io.viva.tv.app.animation.TransInfo;
import io.viva.tv.app.animation.TransInfoKey;
import io.viva.tv.app.widget.HorizontalListView;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.OverScroller;

public class TabHorizontalListView extends HorizontalListView {
	private static final String TAG = "TabHorizontalListView";
	private static final boolean DEBUG = true;
	private int mHighlighted;
	private boolean mClearingFocus;
	boolean mAnimating;
	private Rect mCurrentSelectRect;
	private Rect mNextSelectRect;
	private Rect mLastSelectRect;
	private Drawable mSelectedDrawable;
	private Drawable mLastSelectedDrawable;
	private int mSelectedDrawableWidth;
	private int mSelectedDrawableHeight;
	private int mLastSelectedDrawableWidth;
	private int mLastSelectedDrawableHeight;
	TabDisplay mTabDisplay;
	int focusLostResult = 0;
	static final int NORMAL_STATUS = 0;
	static final int KEYEVENT_DOWN = 20;
	static final int KEYEVENT_LEFT = 21;
	static final int FOCUS_LOST_DOWN = 10001;
	static final int FOCUS_LOST_LEFT = 10002;
	Rect rectOfView = new Rect();

	Rect tmpRect = new Rect();
	private TransInfo mSelectorTrans;
	private Rect mSelectorCurRect;
	private int selectCurWidth;
	private int selectCurHeight;

	public TabHorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setSelector(null);
		this.mSelectedDrawable = getContext().getResources().getDrawable(R.drawable.tui_topnavbar_bg_focus);

		this.mSelectedDrawableWidth = this.mSelectedDrawable.getIntrinsicWidth();
		this.mSelectedDrawableHeight = this.mSelectedDrawable.getIntrinsicHeight();

		this.mLastSelectedDrawable = getContext().getResources().getDrawable(R.drawable.tui_topnavbar_bg_normal);

		this.mLastSelectedDrawableWidth = this.mLastSelectedDrawable.getIntrinsicWidth();
		this.mLastSelectedDrawableHeight = this.mLastSelectedDrawable.getIntrinsicHeight();

		this.mLastSelectRect = new Rect();
	}

	public void setHighlighted(int index) {
		this.mHighlighted = index;
	}

	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		if (gainFocus) {
			this.focusLostResult = NORMAL_STATUS;
			this.mTabDisplay.setSelectedClean(false);
			this.mTabDisplay.recoverSelectionState();
		} else {
			this.mTabDisplay.setSelectedClean(true);
			this.mTabDisplay.cleartSelectionState();
			if (this.focusLostResult == KEYEVENT_LEFT) {
				this.focusLostResult = FOCUS_LOST_LEFT;
			}
		}
	}

	public void setTabDisplay(TabDisplay tabDisplay) {
		this.mTabDisplay = tabDisplay;
	}

	public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
		int selectedIndex = this.mHighlighted - getFirstVisiblePosition();
		if ((!hasFocus()) && (selectedIndex >= 0) && (selectedIndex < getChildCount()) && (direction == 33)) {
			views.add(this);
		} else {
			super.addFocusables(views, direction, focusableMode);
		}
	}

	public void clearFocus() {
		this.mClearingFocus = true;
		super.clearFocus();
		this.mClearingFocus = false;
	}

	public void clearChildFocus(View child) {
		if (this.mClearingFocus) {
			super.clearChildFocus(child);
		} else {
			post(new Runnable() {
				public void run() {
					TabHorizontalListView.this.setSelection(TabHorizontalListView.this.mHighlighted);
				}
			});
		}
	}

	private void mergeSelectorRect(Rect r, int intrinsicWidth, int intrinsicHeight) {
		int w1 = intrinsicWidth >> 1;
		int rw1 = r.right - r.left >> 1;
		r.left = (r.left + rw1 - w1);
		r.right = (r.left + intrinsicWidth);

		int h1 = intrinsicHeight >> 1;
		int rh1 = r.bottom - r.top >> 1;
		r.top += rh1 - h1;
		r.bottom = (r.top + intrinsicHeight);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (this.mAnimating) {
			return true;
		}

		if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
			prepareSelectorMovingAnimator();
		}

		boolean handler = super.onKeyDown(keyCode, event);

		if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
			startSelectorMovingAnimator();
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			this.focusLostResult = KEYEVENT_LEFT;
		} else {
			this.focusLostResult = 0;
		}

		return handler;
	}

	public void prepareSelectorMovingAnimator() {
		if (this.mCurrentSelectRect != null) {
			this.mCurrentSelectRect = getSelectedRectFromTab();
		}
	}

	public void startSelectorMovingAnimator() {
		if (this.mNextSelectRect == null) {
			this.mNextSelectRect = new Rect();
		}

		int nextSelectedPosition = getSelectedItemPosition();
		View selectedView = getChildAt(nextSelectedPosition - getFirstVisiblePosition());

		Log.d(TAG, "------------nextSelectedPosition = " + nextSelectedPosition + " , getFirstVisiblePosition = " + getFirstVisiblePosition());

		if (selectedView != null) {
			this.mNextSelectRect.setEmpty();
			this.mNextSelectRect.left = selectedView.getLeft();
			this.mNextSelectRect.right = selectedView.getRight();
			this.mNextSelectRect.top = selectedView.getTop();
			this.mNextSelectRect.bottom = selectedView.getBottom();
		}

		if ((this.mCurrentSelectRect != null) && (this.mNextSelectRect != null) && (!this.mNextSelectRect.isEmpty()) && (!this.mNextSelectRect.equals(this.mCurrentSelectRect))) {
			startSelectorShiftAni();
		}
	}

	private Rect getRectOfView(int position) {
		this.rectOfView.setEmpty();
		View indexOfView = getChildAt(position - getFirstVisiblePosition());
		if (indexOfView != null) {
			this.rectOfView.left = indexOfView.getLeft();
			this.rectOfView.right = indexOfView.getRight();
			this.rectOfView.top = indexOfView.getTop();
			this.rectOfView.bottom = indexOfView.getBottom();
		}

		return this.rectOfView;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private Rect getSelectedRectFromTab() {
		if ((this.mTabDisplay != null) && (this.mTabDisplay.getSelected() != null)) {
			int position = this.mTabDisplay.getSelected().getPosition();
			Rect rect = getRectOfView(position);
			return rect;
		}
		return null;
	}

	protected void drawSelector(Canvas canvas) {
		if (this.tmpRect != null) {
			this.tmpRect.setEmpty();
		}
		if ((this.mAnimating) && (this.tmpRect != null)) {
			this.tmpRect.left = ((int) this.mSelectorTrans.x - (this.selectCurWidth >> 1));
			this.tmpRect.right = ((int) this.mSelectorTrans.x + (this.selectCurWidth >> 1));
			this.tmpRect.top = ((int) this.mSelectorTrans.y - (this.selectCurHeight >> 1));
			this.tmpRect.bottom = ((int) this.mSelectorTrans.y + (this.selectCurHeight >> 1));

			mergeSelectorRect(this.tmpRect, this.mSelectedDrawableWidth, this.mSelectedDrawableHeight);
			drawSelectorDrawable(this.tmpRect, canvas);
		} else {
			if (this.mCurrentSelectRect == null) {
				this.mCurrentSelectRect = getSelectedRectFromTab();
			}

			this.tmpRect = getSelectedRectFromTab();
			if ((this.tmpRect != null) && (!this.tmpRect.isEmpty())) {
				mergeSelectorRect(this.tmpRect, this.mSelectedDrawableWidth, this.mSelectedDrawableHeight);
				drawSelectorDrawable(this.tmpRect, canvas);
			}
		}
	}

	private void drawSelectorDrawable(Rect rect, Canvas canvas) {
		switch (this.focusLostResult) {
		case FOCUS_LOST_LEFT:
			this.mLastSelectedDrawable.setBounds(rect);
			this.mLastSelectedDrawable.draw(canvas);
			break;
		default:
			if (isFocused()) {
				this.mSelectedDrawable.setBounds(rect);
				this.mSelectedDrawable.draw(canvas);
			} else {
				this.mLastSelectedDrawable.setBounds(rect);
				this.mLastSelectedDrawable.draw(canvas);
			}
			break;
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	void startSelectorShiftAni() {
		Rect rectStart = this.mCurrentSelectRect;
		Rect rectEnd = this.mNextSelectRect;

		Log.d(TAG, "-------startSelectorShiftAni, mCurrentSelectRect = " + this.mCurrentSelectRect + ", mNextSelectRect = " + this.mNextSelectRect);

		int xStart = rectStart.left + rectStart.right >> 1;
		int yStart = rectStart.top + rectStart.bottom >> 1;

		int xEnd = rectEnd.left + rectEnd.right >> 1;
		int yEnd = rectEnd.top + rectEnd.bottom >> 1;

		OverScroller scroller = getOverScrollerFromFlingRunnable();
		if ((scroller != null) && (!scroller.isFinished())) {
			int startX = scroller.getStartX();
			int finalX = scroller.getFinalX();
			int distance = finalX - startX;

			Log.d(TAG, "---------------xStart = " + xStart + " , xEnd = " + xEnd + ", distance = " + distance);

			xEnd -= distance;
		}

		this.mSelectorCurRect = new Rect(rectStart);
		this.selectCurWidth = (this.mSelectorCurRect.right - this.mSelectorCurRect.left);
		this.selectCurHeight = (this.mSelectorCurRect.bottom - this.mSelectorCurRect.top);

		TransInfoKey transKeyStart = new TransInfoKey(xStart, yStart, 1.0F, 1.0F, 1.0F, 0.0F);

		TransInfoKey transKeyEnd = new TransInfoKey(xEnd, yEnd, 1.0F, 1.0F, 1.0F, 1.0F);

		PosInfo pos = new PosInfo(new TransInfoKey[] { transKeyStart, transKeyEnd });

		new TabListSelectBinder().bindPosInfo(pos);
	}

	class TabListSelectBinder {
		TransInfo mStartTrans;
		TransInfo mEndTrans;
		TransInfo mCurTrans;
		MultiAnimator mMultiAnimator;

		TabListSelectBinder() {
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public TabListSelectBinder bindPosInfo(PosInfo pos) {
			if (this.mMultiAnimator == null) {
				this.mMultiAnimator = new MultiAnimator();
				int duration = TabHorizontalListView.this.getContext().getResources().getInteger(2114387968);

				this.mMultiAnimator.setDuration(duration);
				this.mMultiAnimator.setInterpolator(AnimationUtils.loadInterpolator(TabHorizontalListView.this.getContext(), 2114322434));

				this.mMultiAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					public void onAnimationUpdate(ValueAnimator animation) {
						TabHorizontalListView.TabListSelectBinder.this.mCurTrans = TabHorizontalListView.TabListSelectBinder.this.mMultiAnimator.getCurTrans();
						TabHorizontalListView.this.mSelectorTrans = TabHorizontalListView.TabListSelectBinder.this.mCurTrans;
						TabHorizontalListView.this.postInvalidate();
					}
				});
				this.mMultiAnimator.addListener(new Animator.AnimatorListener() {
					public void onAnimationEnd(Animator animation) {
						TabHorizontalListView.this.mAnimating = false;
						if (TabHorizontalListView.this.mTabDisplay.getunFocus()) {
							TabHorizontalListView.this.mTabDisplay.setUnFocus(false);
							TabHorizontalListView.this.getRootView().requestFocus();
						}
					}

					public void onAnimationStart(Animator animation) {
					}

					public void onAnimationCancel(Animator animation) {
					}

					public void onAnimationRepeat(Animator animation) {
					}
				});
			}
			this.mMultiAnimator.setPosInfo(pos);
			this.mMultiAnimator.start();
			TabHorizontalListView.this.mAnimating = true;
			return this;
		}
	}
}
