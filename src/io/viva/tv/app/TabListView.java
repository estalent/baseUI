package io.viva.tv.app;

import io.viva.baseui.R;
import io.viva.tv.app.animation.MultiAnimator;
import io.viva.tv.app.animation.PosInfo;
import io.viva.tv.app.animation.TransInfo;
import io.viva.tv.app.animation.TransInfoKey;

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
import android.view.animation.AnimationUtils;
import android.widget.ListView;

public class TabListView extends ListView {
	private static final String TAG = "TabListView";
	private int mHighlighted;
	private boolean mClearingFocus;
	private Rect mCurrentSelectRect;
	private Rect mNextSelectRect;
	private Rect mLastSelectRect;
	private Drawable mSelectedDrawable;
	private Drawable mLastSelectedDrawable;
	private int mSelectedDrawableWidth;
	private int mSelectedDrawableHeight;
	private int mLastSelectedDrawableWidth;
	private int mLastSelectedDrawableHeight;
	boolean mIsAttached;
	boolean mAnimating;
	int mDisplayStyle;
	TabDisplay mTabDisplay;
	int focusLostResult = 0;
	static final int NORMAL_STATUS = 0;
	static final int KEYEVENT_DOWN = 20;
	static final int FOCUS_LOST_DOWN = 10001;
	static final int FOCUS_LOST_RIGHT = 10002;
	Rect viewOfRect = new Rect();

	Rect tmpRect = new Rect();
	private TransInfo mSelectorTrans;
	private Rect mSelectorCurRect;
	private int selectCurWidth;
	private int selectCurHeight;
	private int mCachedDuration = -1;

	public TabListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setSelector(2114191362);
		this.mSelectedDrawable = getSelectedDrawable();
		this.mSelectedDrawableWidth = this.mSelectedDrawable.getIntrinsicWidth();
		this.mSelectedDrawableHeight = this.mSelectedDrawable.getIntrinsicHeight();

		this.mLastSelectedDrawable = getContext().getResources().getDrawable(2114191546);

		this.mLastSelectedDrawableWidth = this.mLastSelectedDrawable.getIntrinsicWidth();
		this.mLastSelectedDrawableHeight = this.mLastSelectedDrawable.getIntrinsicHeight();

		this.mLastSelectRect = new Rect();
		setFocusableInTouchMode(true);
	}

	private Drawable getSelectedDrawable() {
		int d = 0;
		switch (this.mDisplayStyle) {
		case 1:
			d = R.drawable.tui_ic_mangosteen_leftnavbar_bg_focus;
			break;
		default:
			d = R.drawable.tui_leftnavbar_bg_focus;
		}
		return getContext().getResources().getDrawable(d);
	}

	public void setHighlighted(int index) {
		this.mHighlighted = index;
	}

	public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
		int selectedIndex = this.mHighlighted - getFirstVisiblePosition();
		if ((!hasFocus()) && (selectedIndex >= 0) && (selectedIndex < getChildCount()) && (direction == 17)) {
			views.add(this);
		} else {
			super.addFocusables(views, direction, focusableMode);
		}
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.mIsAttached = true;
	}

	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		this.mIsAttached = false;
	}

	public void clearFocus() {
		this.mClearingFocus = true;
		super.clearFocus();
		this.mClearingFocus = false;
	}

	public void setTabDisplay(TabDisplay tabDisplay) {
		this.mTabDisplay = tabDisplay;
	}

	public void clearChildFocus(View child) {
		if (this.mClearingFocus) {
			super.clearChildFocus(child);
		} else {
			post(new Runnable() {
				public void run() {
					TabListView.this.setSelection(TabListView.this.mHighlighted);
				}
			});
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (this.mAnimating) {
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			prepareSelectorMovingAnimator();
		}

		boolean handler = super.onKeyDown(keyCode, event);

		if ((this.mCurrentSelectRect != null) && (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP)) {
			startSelectorMovingAnimator();
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			this.focusLostResult = KEYEVENT_DOWN;
		} else {
			this.focusLostResult = NORMAL_STATUS;
		}

		return handler;
	}

	public void lostFocus() {
		this.focusLostResult = FOCUS_LOST_DOWN;
		invalidate();
	}

	public void resetFocus() {
		Log.d(TAG, "-------resetFocus-----------");
		this.focusLostResult = NORMAL_STATUS;
		invalidate();
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

	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		if (gainFocus) {
			this.focusLostResult = NORMAL_STATUS;
			this.mTabDisplay.recoverSelectionState();
			this.mTabDisplay.setFakeSelected(false);
			requestLayout();
		} else {
			if (this.mTabDisplay != null) {
				this.mTabDisplay.cleartSelectionState();
				this.mTabDisplay.setFakeSelected(true);
				requestLayout();
			}
			switch (this.focusLostResult) {
			case KEYEVENT_DOWN:
				this.focusLostResult = FOCUS_LOST_DOWN;
			}
		}
	}

	private void mergeSelectorRect(Rect r, int intrinsicWidth, int intrinsicHeight) {
		int w1 = intrinsicWidth >> 1;
		int rw1 = r.right - r.left >> 1;

		r.right = (r.left + intrinsicWidth);

		int h1 = intrinsicHeight >> 1;
		int rh1 = r.bottom - r.top >> 1;
		r.top += rh1 - h1;
		r.bottom = (r.top + intrinsicHeight);
	}

	private Rect getRectOfView(int position) {
		this.viewOfRect.setEmpty();
		View indexOfView = getChildAt(position - getFirstVisiblePosition());
		if (indexOfView != null) {
			this.viewOfRect.left = indexOfView.getLeft();
			this.viewOfRect.right = indexOfView.getRight();
			this.viewOfRect.top = indexOfView.getTop();
			this.viewOfRect.bottom = indexOfView.getBottom();
		}
		return this.viewOfRect;
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
		if (this.tmpRect == null) {
			this.tmpRect = new Rect();
		}
		if (this.mAnimating) {
			this.tmpRect.left = ((int) this.mSelectorTrans.x - (this.selectCurWidth >> 1));
			this.tmpRect.right = ((int) this.mSelectorTrans.x + (this.selectCurWidth >> 1));
			this.tmpRect.top = ((int) this.mSelectorTrans.y - (this.selectCurHeight >> 1));
			this.tmpRect.bottom = ((int) this.mSelectorTrans.y + (this.selectCurHeight >> 1));

			mergeSelectorRect(this.tmpRect, this.mSelectedDrawableWidth, this.mSelectedDrawableHeight);
			drawSelectorDrawable(this.tmpRect, canvas);
		} else {
			if (this.mCurrentSelectRect == null) {
				this.mCurrentSelectRect = new Rect();
				getFocusedRect(this.mCurrentSelectRect);
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
		case FOCUS_LOST_DOWN:
			break;
		default:
			if (isFocused()) {
				this.mSelectedDrawable.setBounds(rect);
				this.mSelectedDrawable.draw(canvas);
			} else if (this.mDisplayStyle == 0) {
				this.mLastSelectedDrawable.setBounds(rect);
				this.mLastSelectedDrawable.draw(canvas);
			}
			break;
		}
	}

	public void setDisplayStyle(int style) {
		this.mDisplayStyle = style;
	}

	protected void dispatchDraw(Canvas canvas) {
		drawSelector(canvas);
		super.dispatchDraw(canvas);
	}

	void startSelectorShiftAni() {
		Rect rectStart = this.mCurrentSelectRect;
		Rect rectEnd = this.mNextSelectRect;

		int xStart = rectStart.left + rectStart.right >> 1;
		int yStart = rectStart.top + rectStart.bottom >> 1;

		int xEnd = rectEnd.left + rectEnd.right >> 1;
		int yEnd = rectEnd.top + rectEnd.bottom >> 1;

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

				if (-1 == TabListView.this.mCachedDuration) {
					TabListView.this.mCachedDuration = TabListView.this.getContext().getResources().getInteger(2114387968);
				}
				int duration = TabListView.this.mCachedDuration;

				this.mMultiAnimator.setDuration(duration);
				this.mMultiAnimator.setInterpolator(AnimationUtils.loadInterpolator(TabListView.this.getContext(), 2114322434));

				this.mMultiAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					public void onAnimationUpdate(ValueAnimator animation) {
						TabListView.TabListSelectBinder.this.mCurTrans = TabListView.TabListSelectBinder.this.mMultiAnimator.getCurTrans();
						TabListView.this.mSelectorTrans = TabListView.TabListSelectBinder.this.mCurTrans;
						TabListView.this.postInvalidate();
					}
				});
				this.mMultiAnimator.addListener(new Animator.AnimatorListener() {
					public void onAnimationEnd(Animator animation) {
						TabListView.this.mAnimating = false;
						if (TabListView.this.mTabDisplay.getunFocus()) {
							TabListView.this.mTabDisplay.setUnFocus(false);
							TabListView.this.getRootView().requestFocus();
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
			TabListView.this.mAnimating = true;
			return this;
		}
	}
}
