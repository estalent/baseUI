package io.viva.tv.app.widget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.KeyEventCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class ViewPager extends ViewGroup {
	private static final String TAG = "ViewPager";
	private static final boolean DEBUG = false;
	private static final boolean USE_CACHE = false;
	private static final int DEFAULT_OFFSCREEN_PAGES = 1;
	private static final int MAX_SETTLE_DURATION = 600;
	private static final int MIN_DISTANCE_FOR_FLING = 25;
	private static final int DEFAULT_GUTTER_SIZE = 16;
	private static final int MIN_FLING_VELOCITY = 400;
	private static final int[] LAYOUT_ATTRS = { 16842931 };
	private static final Comparator<ItemInfo> COMPARATOR = new Comparator() {
		public int compare(ViewPager.ItemInfo paramAnonymousItemInfo1, ViewPager.ItemInfo paramAnonymousItemInfo2) {
			return paramAnonymousItemInfo1.position - paramAnonymousItemInfo2.position;
		}

		@Override
		public int compare(Object lhs, Object rhs) {
			return 0;
		}
	};
	private static final Interpolator sInterpolator = new Interpolator() {
		public float getInterpolation(float paramAnonymousFloat) {
			paramAnonymousFloat -= 1.0F;
			return paramAnonymousFloat * paramAnonymousFloat * paramAnonymousFloat * paramAnonymousFloat * paramAnonymousFloat + 1.0F;
		}
	};
	private final ArrayList<ItemInfo> mItems = new ArrayList();
	private final ItemInfo mTempItem = new ItemInfo();
	private final Rect mTempRect = new Rect();
	private PagerAdapter mAdapter;
	private int mCurItem;
	private int mRestoredCurItem = -1;
	private Parcelable mRestoredAdapterState = null;
	private ClassLoader mRestoredClassLoader = null;
	private Scroller mScroller;
	private PagerObserver mObserver;
	private int mPageMargin;
	private Drawable mMarginDrawable;
	private int mTopPageBounds;
	private int mBottomPageBounds;
	private float mFirstOffset = Float.MIN_VALUE;
	private float mLastOffset = Float.MAX_VALUE;
	private int mChildWidthMeasureSpec;
	private int mChildHeightMeasureSpec;
	private boolean mInLayout;
	private boolean mScrollingCacheEnabled;
	private boolean mPopulatePending;
	private int mOffscreenPageLimit = 1;
	private boolean mIsBeingDragged;
	private boolean mIsUnableToDrag;
	private boolean mIgnoreGutter;
	private int mDefaultGutterSize;
	private int mGutterSize;
	private int mTouchSlop;
	private float mLastMotionX;
	private float mLastMotionY;
	private float mInitialMotionX;
	private float mInitialMotionY;
	private int mActivePointerId = -1;
	private static final int INVALID_POINTER = -1;
	private VelocityTracker mVelocityTracker;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private int mFlingDistance;
	private int mCloseEnough;
	private static final int CLOSE_ENOUGH = 2;
	private boolean mFakeDragging;
	private long mFakeDragBeginTime;
	private EdgeEffectCompat mLeftEdge;
	private EdgeEffectCompat mRightEdge;
	private boolean mFirstLayout = true;
	private boolean mNeedCalculatePageOffsets = false;
	private boolean mCalledSuper;
	private int mDecorChildCount;
	private OnPageChangeListener mOnPageChangeListener;
	private OnPageChangeListener mInternalPageChangeListener;
	private OnAdapterChangeListener mAdapterChangeListener;
	private PageTransformer mPageTransformer;
	private Method mSetChildrenDrawingOrderEnabled;
	private static final int DRAW_ORDER_DEFAULT = 0;
	private static final int DRAW_ORDER_FORWARD = 1;
	private static final int DRAW_ORDER_REVERSE = 2;
	private int mDrawingOrder;
	private ArrayList<View> mDrawingOrderedChildren;
	private static final ViewPositionComparator sPositionComparator = new ViewPositionComparator();
	public static final int SCROLL_STATE_IDLE = 0;
	public static final int SCROLL_STATE_DRAGGING = 1;
	public static final int SCROLL_STATE_SETTLING = 2;
	private final Runnable mEndScrollRunnable = new Runnable() {
		public void run() {
			ViewPager.this.setScrollState(0);
			ViewPager.this.populate();
		}
	};
	private int mScrollState = 0;
	private boolean isExpand = false;

	public ViewPager(Context paramContext) {
		super(paramContext);
		initViewPager();
	}

	public ViewPager(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		initViewPager();
	}

	public ViewPager(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		initViewPager();
	}

	void initViewPager() {
		setWillNotDraw(false);
		setDescendantFocusability(262144);
		setFocusable(true);
		Context localContext = getContext();
		this.mScroller = new Scroller(localContext, sInterpolator);
		ViewConfiguration localViewConfiguration = ViewConfiguration.get(localContext);
		float f = localContext.getResources().getDisplayMetrics().density;
		this.mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(localViewConfiguration);
		this.mMinimumVelocity = ((int) (400.0F * f));
		this.mMaximumVelocity = localViewConfiguration.getScaledMaximumFlingVelocity();
		this.mLeftEdge = new EdgeEffectCompat(localContext);
		this.mRightEdge = new EdgeEffectCompat(localContext);
		this.mFlingDistance = ((int) (25.0F * f));
		this.mCloseEnough = ((int) (2.0F * f));
		this.mDefaultGutterSize = ((int) (16.0F * f));
		ViewCompat.setAccessibilityDelegate(this, new MyAccessibilityDelegate());
		if (ViewCompat.getImportantForAccessibility(this) == 0)
			ViewCompat.setImportantForAccessibility(this, 1);
	}

	protected void onDetachedFromWindow() {
		removeCallbacks(this.mEndScrollRunnable);
		super.onDetachedFromWindow();
	}

	private void setScrollState(int paramInt) {
		if (this.mScrollState == paramInt)
			return;
		this.mScrollState = paramInt;
		if (this.mPageTransformer != null)
			enableLayers(paramInt != 0);
		if (this.mOnPageChangeListener != null)
			this.mOnPageChangeListener.onPageScrollStateChanged(paramInt);
	}

	public void setAdapter(PagerAdapter paramPagerAdapter) {
		if (this.mAdapter != null) {
			this.mAdapter.unregisterDataSetObserver(this.mObserver);
			this.mAdapter.startUpdate(this);
			for (int i = 0; i < this.mItems.size(); i++) {
				ItemInfo localItemInfo = (ItemInfo) this.mItems.get(i);
				this.mAdapter.destroyItem(this, localItemInfo.position, localItemInfo.object);
			}
			this.mAdapter.finishUpdate(this);
			this.mItems.clear();
			removeNonDecorViews();
			this.mCurItem = 0;
			scrollTo(0, 0);
		}
		PagerAdapter localPagerAdapter = this.mAdapter;
		this.mAdapter = paramPagerAdapter;
		if (this.mAdapter != null) {
			if (this.mObserver == null)
				this.mObserver = new PagerObserver();
			this.mAdapter.registerDataSetObserver(this.mObserver);
			this.mPopulatePending = false;
			this.mFirstLayout = true;
			if (this.mRestoredCurItem >= 0) {
				this.mAdapter.restoreState(this.mRestoredAdapterState, this.mRestoredClassLoader);
				setCurrentItemInternal(this.mRestoredCurItem, false, true);
				this.mRestoredCurItem = -1;
				this.mRestoredAdapterState = null;
				this.mRestoredClassLoader = null;
			} else {
				populate();
			}
		}
		if ((this.mAdapterChangeListener != null) && (localPagerAdapter != paramPagerAdapter))
			this.mAdapterChangeListener.onAdapterChanged(localPagerAdapter, paramPagerAdapter);
	}

	private void removeNonDecorViews() {
		for (int i = 0; i < getChildCount(); i++) {
			View localView = getChildAt(i);
			LayoutParams localLayoutParams = (LayoutParams) localView.getLayoutParams();
			if (!localLayoutParams.isDecor) {
				removeViewAt(i);
				i--;
			}
		}
	}

	public PagerAdapter getAdapter() {
		return this.mAdapter;
	}

	void setOnAdapterChangeListener(OnAdapterChangeListener paramOnAdapterChangeListener) {
		this.mAdapterChangeListener = paramOnAdapterChangeListener;
	}

	public void setCurrentItem(int paramInt) {
		this.mPopulatePending = false;
		setCurrentItemInternal(paramInt, !this.mFirstLayout, false);
	}

	public void setCurrentItem(int paramInt, boolean paramBoolean) {
		this.mPopulatePending = false;
		setCurrentItemInternal(paramInt, paramBoolean, false);
	}

	public int getCurrentItem() {
		return this.mCurItem;
	}

	void setCurrentItemInternal(int paramInt, boolean paramBoolean1, boolean paramBoolean2) {
		setCurrentItemInternal(paramInt, paramBoolean1, paramBoolean2, 0);
	}

	void setCurrentItemInternal(int paramInt1, boolean paramBoolean1, boolean paramBoolean2, int paramInt2) {
		if ((this.mAdapter == null) || (this.mAdapter.getCount() <= 0)) {
			setScrollingCacheEnabled(false);
			return;
		}
		if ((!paramBoolean2) && (this.mCurItem == paramInt1) && (this.mItems.size() != 0)) {
			setScrollingCacheEnabled(false);
			return;
		}
		if (paramInt1 < 0)
			paramInt1 = 0;
		else if (paramInt1 >= this.mAdapter.getCount())
			paramInt1 = this.mAdapter.getCount() - 1;
		int i = this.mOffscreenPageLimit;
		if ((paramInt1 > this.mCurItem + i) || (paramInt1 < this.mCurItem - i))
			for (int j = 0; j < this.mItems.size(); j++)
				((ItemInfo) this.mItems.get(j)).scrolling = true;
		boolean j = this.mCurItem != paramInt1 ? true : false;
		populate(paramInt1);
		scrollToItem(paramInt1, paramBoolean1, paramInt2, j);
	}

	private void scrollToItem(int paramInt1, boolean paramBoolean1, int paramInt2, boolean paramBoolean2) {
		ItemInfo localItemInfo = infoForPosition(paramInt1);
		int i = 0;
		if (localItemInfo != null) {
			int j = getWidth();
			i = (int) (j * Math.max(this.mFirstOffset, Math.min(localItemInfo.offset, this.mLastOffset)));
		}
		if (paramBoolean1) {
			smoothScrollTo(i, 0, paramInt2);
			if ((paramBoolean2) && (this.mOnPageChangeListener != null))
				this.mOnPageChangeListener.onPageSelected(paramInt1);
			if ((paramBoolean2) && (this.mInternalPageChangeListener != null))
				this.mInternalPageChangeListener.onPageSelected(paramInt1);
		} else {
			if ((paramBoolean2) && (this.mOnPageChangeListener != null))
				this.mOnPageChangeListener.onPageSelected(paramInt1);
			if ((paramBoolean2) && (this.mInternalPageChangeListener != null))
				this.mInternalPageChangeListener.onPageSelected(paramInt1);
			completeScroll(false);
			scrollTo(i, 0);
		}
	}

	public void setOnPageChangeListener(OnPageChangeListener paramOnPageChangeListener) {
		this.mOnPageChangeListener = paramOnPageChangeListener;
	}

	public void setPageTransformer(boolean paramBoolean, PageTransformer paramPageTransformer) {
		if (Build.VERSION.SDK_INT >= 11) {
			boolean bool = paramPageTransformer != null;
			int i = bool != (this.mPageTransformer != null) ? 1 : 0;
			this.mPageTransformer = paramPageTransformer;
			setChildrenDrawingOrderEnabledCompat(bool);
			if (bool)
				this.mDrawingOrder = (paramBoolean ? 2 : 1);
			else
				this.mDrawingOrder = 0;
			if (i != 0)
				populate();
		}
	}

	void setChildrenDrawingOrderEnabledCompat(boolean paramBoolean) {
		if (this.mSetChildrenDrawingOrderEnabled == null)
			try {
				this.mSetChildrenDrawingOrderEnabled = ViewGroup.class.getDeclaredMethod("setChildrenDrawingOrderEnabled", new Class[] { Boolean.TYPE });
			} catch (NoSuchMethodException localNoSuchMethodException) {
				Log.e("ViewPager", "Can't find setChildrenDrawingOrderEnabled", localNoSuchMethodException);
			}
		try {
			this.mSetChildrenDrawingOrderEnabled.invoke(this, new Object[] { Boolean.valueOf(paramBoolean) });
		} catch (Exception localException) {
			Log.e("ViewPager", "Error changing children drawing order", localException);
		}
	}

	protected int getChildDrawingOrder(int paramInt1, int paramInt2) {
		int i = this.mDrawingOrder == 2 ? paramInt1 - 1 - paramInt2 : paramInt2;
		int j = ((LayoutParams) ((View) this.mDrawingOrderedChildren.get(i)).getLayoutParams()).childIndex;
		return j;
	}

	OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener paramOnPageChangeListener) {
		OnPageChangeListener localOnPageChangeListener = this.mInternalPageChangeListener;
		this.mInternalPageChangeListener = paramOnPageChangeListener;
		return localOnPageChangeListener;
	}

	public int getOffscreenPageLimit() {
		return this.mOffscreenPageLimit;
	}

	public void setOffscreenPageLimit(int paramInt) {
		if (paramInt < 1) {
			Log.w("ViewPager", "Requested offscreen page limit " + paramInt + " too small; defaulting to " + 1);
			paramInt = 1;
		}
		if (paramInt != this.mOffscreenPageLimit) {
			this.mOffscreenPageLimit = paramInt;
			populate();
		}
	}

	public void setPageMargin(int paramInt) {
		int i = this.mPageMargin;
		this.mPageMargin = paramInt;
		int j = getWidth();
		recomputeScrollPosition(j, j, paramInt, i);
		requestLayout();
	}

	public int getPageMargin() {
		return this.mPageMargin;
	}

	public void setPageMarginDrawable(Drawable paramDrawable) {
		this.mMarginDrawable = paramDrawable;
		if (paramDrawable != null)
			refreshDrawableState();
		setWillNotDraw(paramDrawable == null);
		invalidate();
	}

	public void setPageMarginDrawable(int paramInt) {
		setPageMarginDrawable(getContext().getResources().getDrawable(paramInt));
	}

	protected boolean verifyDrawable(Drawable paramDrawable) {
		return (super.verifyDrawable(paramDrawable)) || (paramDrawable == this.mMarginDrawable);
	}

	protected void drawableStateChanged() {
		super.drawableStateChanged();
		Drawable localDrawable = this.mMarginDrawable;
		if ((localDrawable != null) && (localDrawable.isStateful()))
			localDrawable.setState(getDrawableState());
	}

	float distanceInfluenceForSnapDuration(float paramFloat) {
		paramFloat -= 0.5F;
		paramFloat = (float) (paramFloat * 0.47123891676382D);
		return (float) Math.sin(paramFloat);
	}

	void smoothScrollTo(int paramInt1, int paramInt2) {
		smoothScrollTo(paramInt1, paramInt2, 0);
	}

	void smoothScrollTo(int paramInt1, int paramInt2, int paramInt3) {
		if (getChildCount() == 0) {
			setScrollingCacheEnabled(false);
			return;
		}
		int i = getScrollX();
		int j = getScrollY();
		int k = paramInt1 - i;
		int m = paramInt2 - j;
		if ((k == 0) && (m == 0)) {
			completeScroll(false);
			populate();
			setScrollState(0);
			return;
		}
		setScrollingCacheEnabled(true);
		setScrollState(2);
		int n = getWidth();
		int i1 = n / 2;
		float f1 = Math.min(1.0F, 1.0F * Math.abs(k) / n);
		float f2 = i1 + i1 * distanceInfluenceForSnapDuration(f1);
		int i2 = 0;
		paramInt3 = Math.abs(paramInt3);
		if (paramInt3 > 0) {
			i2 = 4 * Math.round(1000.0F * Math.abs(f2 / paramInt3));
		} else {
			float f3 = n * this.mAdapter.getPageWidth(this.mCurItem);
			float f4 = Math.abs(k) / (f3 + this.mPageMargin);
			i2 = (int) ((f4 + 1.0F) * 100.0F);
		}
		i2 = Math.min(i2, 600);
		this.mScroller.startScroll(i, j, k, m, i2);
		ViewCompat.postInvalidateOnAnimation(this);
	}

	ItemInfo addNewItem(int paramInt1, int paramInt2) {
		ItemInfo localItemInfo = new ItemInfo();
		localItemInfo.position = paramInt1;
		localItemInfo.object = this.mAdapter.instantiateItem(this, paramInt1);
		localItemInfo.widthFactor = this.mAdapter.getPageWidth(paramInt1);
		if ((paramInt2 < 0) || (paramInt2 >= this.mItems.size()))
			this.mItems.add(localItemInfo);
		else
			this.mItems.add(paramInt2, localItemInfo);
		return localItemInfo;
	}

	void dataSetChanged() {
		int i = (this.mItems.size() < this.mOffscreenPageLimit * 2 + 1) && (this.mItems.size() < this.mAdapter.getCount()) ? 1 : 0;
		int j = this.mCurItem;
		int k = 0;
		for (int m = 0; m < this.mItems.size(); m++) {
			ItemInfo localItemInfo = (ItemInfo) this.mItems.get(m);
			int i1 = this.mAdapter.getItemPosition(localItemInfo.object);
			if (i1 != -1)
				if (i1 == -2) {
					this.mItems.remove(m);
					m--;
					if (k == 0) {
						this.mAdapter.startUpdate(this);
						k = 1;
					}
					this.mAdapter.destroyItem(this, localItemInfo.position, localItemInfo.object);
					i = 1;
					if (this.mCurItem == localItemInfo.position) {
						j = Math.max(0, Math.min(this.mCurItem, this.mAdapter.getCount() - 1));
						i = 1;
					}
				} else if (localItemInfo.position != i1) {
					if (localItemInfo.position == this.mCurItem)
						j = i1;
					localItemInfo.position = i1;
					i = 1;
				}
		}
		if (k != 0)
			this.mAdapter.finishUpdate(this);
		Collections.sort(this.mItems, COMPARATOR);
		if (i != 0) {
			int m = getChildCount();
			for (int n = 0; n < m; n++) {
				View localView = getChildAt(n);
				LayoutParams localLayoutParams = (LayoutParams) localView.getLayoutParams();
				if (!localLayoutParams.isDecor)
					localLayoutParams.widthFactor = 0.0F;
			}
			setCurrentItemInternal(j, false, true);
			requestLayout();
		}
	}

	void populate() {
		populate(this.mCurItem);
	}

	void populate(int paramInt) {
		ItemInfo localItemInfo1 = null;
		if (this.mCurItem != paramInt) {
			localItemInfo1 = infoForPosition(this.mCurItem);
			this.mCurItem = paramInt;
		}
		if (this.mAdapter == null)
			return;
		if (this.mPopulatePending)
			return;
		if (getWindowToken() == null)
			return;
		this.mAdapter.startUpdate(this);
		int i = this.mOffscreenPageLimit;
		int j = Math.max(0, this.mCurItem - i);
		int k = this.mAdapter.getCount();
		int m = Math.min(k - 1, this.mCurItem + i);
		int n = -1;
		Object localObject1 = null;
		for (n = 0; n < this.mItems.size(); n++) {
			ItemInfo localItemInfo2 = (ItemInfo) this.mItems.get(n);
			if (localItemInfo2.position >= this.mCurItem) {
				if (localItemInfo2.position != this.mCurItem)
					break;
				localObject1 = localItemInfo2;
				break;
			}
		}
		if ((localObject1 == null) && (k > 0))
			localObject1 = addNewItem(this.mCurItem, n);
		if (localObject1 != null) {
			float f1 = 0.0F;
			int i2 = n - 1;
			ItemInfo localItemInfo3 = i2 >= 0 ? (ItemInfo) this.mItems.get(i2) : null;
			float f2 = 2.0F - ((ItemInfo) localObject1).widthFactor;
			for (int i4 = this.mCurItem - 1; i4 >= 0; i4--)
				if ((f1 >= f2) && (i4 < j)) {
					if (localItemInfo3 == null)
						break;
					if ((i4 == localItemInfo3.position) && (!localItemInfo3.scrolling)) {
						this.mItems.remove(i2);
						this.mAdapter.destroyItem(this, i4, localItemInfo3.object);
						i2--;
						n--;
						localItemInfo3 = i2 >= 0 ? (ItemInfo) this.mItems.get(i2) : null;
					}
				} else if ((localItemInfo3 != null) && (i4 == localItemInfo3.position)) {
					f1 += localItemInfo3.widthFactor;
					i2--;
					localItemInfo3 = i2 >= 0 ? (ItemInfo) this.mItems.get(i2) : null;
				} else {
					localItemInfo3 = addNewItem(i4, i2 + 1);
					f1 += localItemInfo3.widthFactor;
					n++;
					localItemInfo3 = i2 >= 0 ? (ItemInfo) this.mItems.get(i2) : null;
				}
			float f3 = ((ItemInfo) localObject1).widthFactor;
			i2 = n + 1;
			if (f3 < 2.0F) {
				localItemInfo3 = i2 < this.mItems.size() ? (ItemInfo) this.mItems.get(i2) : null;
				for (int i6 = this.mCurItem + 1; i6 < k; i6++)
					if ((f3 >= 2.0F) && (i6 > m)) {
						if (localItemInfo3 == null)
							break;
						if ((i6 == localItemInfo3.position) && (!localItemInfo3.scrolling)) {
							this.mItems.remove(i2);
							this.mAdapter.destroyItem(this, i6, localItemInfo3.object);
							localItemInfo3 = i2 < this.mItems.size() ? (ItemInfo) this.mItems.get(i2) : null;
						}
					} else if ((localItemInfo3 != null) && (i6 == localItemInfo3.position)) {
						f3 += localItemInfo3.widthFactor;
						i2++;
						localItemInfo3 = i2 < this.mItems.size() ? (ItemInfo) this.mItems.get(i2) : null;
					} else {
						localItemInfo3 = addNewItem(i6, i2);
						i2++;
						f3 += localItemInfo3.widthFactor;
						localItemInfo3 = i2 < this.mItems.size() ? (ItemInfo) this.mItems.get(i2) : null;
					}
			}
			calculatePageOffsets((ItemInfo) localObject1, n, localItemInfo1);
		}
		this.mAdapter.setPrimaryItem(this, this.mCurItem, localObject1 != null ? ((ItemInfo) localObject1).object : null);
		this.mAdapter.finishUpdate(this);
		int i1 = this.mDrawingOrder != 0 ? 1 : 0;
		if (i1 != 0)
			if (this.mDrawingOrderedChildren == null)
				this.mDrawingOrderedChildren = new ArrayList();
			else
				this.mDrawingOrderedChildren.clear();
		int i2 = getChildCount();
		View localObject2;
		Object localObject3;
		for (int i3 = 0; i3 < i2; i3++) {
			localObject2 = getChildAt(i3);
			LayoutParams localLayoutParams = (LayoutParams) ((View) localObject2).getLayoutParams();
			localLayoutParams.childIndex = i3;
			if ((!localLayoutParams.isDecor) && (localLayoutParams.widthFactor == 0.0F)) {
				localObject3 = infoForChild((View) localObject2);
				if (localObject3 != null) {
					localLayoutParams.widthFactor = ((ItemInfo) localObject3).widthFactor;
					localLayoutParams.position = ((ItemInfo) localObject3).position;
				}
			}
			if (i1 != 0)
				this.mDrawingOrderedChildren.add(localObject2);
		}
		if (i1 != 0)
			Collections.sort(this.mDrawingOrderedChildren, sPositionComparator);
		if (hasFocus()) {
			View localView = findFocus();
			ItemInfo localObject4 = localView != null ? infoForAnyChild(localView) : null;
			if ((localObject4 == null) || (((ItemInfo) localObject4).position != this.mCurItem))
				for (int i5 = 0; i5 < getChildCount(); i5++) {
					View localObject5 = getChildAt(i5);
					localObject4 = infoForChild((View) localObject5);
					if ((localObject4 != null) && (((ItemInfo) localObject4).position == this.mCurItem) && (((View) localObject5).requestFocus(2)))
						break;
				}
		}
	}

	private void calculatePageOffsets(ItemInfo paramItemInfo1, int paramInt, ItemInfo paramItemInfo2) {
		int i = this.mAdapter.getCount();
		int j = getWidth();
		float f1 = j > 0 ? this.mPageMargin / j : 0.0F;
		if (paramItemInfo2 != null) {
			int k = paramItemInfo2.position;
			int m;
			ItemInfo localItemInfo1;
			float f3;
			int i2;
			if (k < paramItemInfo1.position) {
				m = 0;
				localItemInfo1 = null;
				f3 = paramItemInfo2.offset + paramItemInfo2.widthFactor + f1;
				for (i2 = k + 1; (i2 <= paramItemInfo1.position) && (m < this.mItems.size()); i2++) {
					for (localItemInfo1 = (ItemInfo) this.mItems.get(m); (i2 > localItemInfo1.position) && (m < this.mItems.size() - 1); localItemInfo1 = (ItemInfo) this.mItems.get(m))
						m++;
					while (i2 < localItemInfo1.position) {
						f3 += this.mAdapter.getPageWidth(i2) + f1;
						i2++;
					}
					localItemInfo1.offset = f3;
					f3 += localItemInfo1.widthFactor + f1;
				}
			} else if (k > paramItemInfo1.position) {
				m = this.mItems.size() - 1;
				localItemInfo1 = null;
				f3 = paramItemInfo2.offset;
				for (i2 = k - 1; (i2 >= paramItemInfo1.position) && (m >= 0); i2--) {
					for (localItemInfo1 = (ItemInfo) this.mItems.get(m); (i2 < localItemInfo1.position) && (m > 0); localItemInfo1 = (ItemInfo) this.mItems.get(m))
						m--;
					while (i2 > localItemInfo1.position) {
						f3 -= this.mAdapter.getPageWidth(i2) + f1;
						i2--;
					}
					f3 -= localItemInfo1.widthFactor + f1;
					localItemInfo1.offset = f3;
				}
			}
		}
		int k = this.mItems.size();
		float f2 = paramItemInfo1.offset;
		int n = paramItemInfo1.position - 1;
		this.mFirstOffset = (paramItemInfo1.position == 0 ? paramItemInfo1.offset : Float.MIN_VALUE);
		this.mLastOffset = (paramItemInfo1.position == i - 1 ? paramItemInfo1.offset + paramItemInfo1.widthFactor - 1.0F : 3.4028235E+38F);
		int i1 = paramInt - 1;
		ItemInfo localItemInfo2;
		while (i1 >= 0) {
			localItemInfo2 = (ItemInfo) this.mItems.get(i1);
			while (n > localItemInfo2.position)
				f2 -= this.mAdapter.getPageWidth(n--) + f1;
			f2 -= localItemInfo2.widthFactor + f1;
			localItemInfo2.offset = f2;
			if (localItemInfo2.position == 0)
				this.mFirstOffset = f2;
			i1--;
			n--;
		}
		f2 = paramItemInfo1.offset + paramItemInfo1.widthFactor + f1;
		n = paramItemInfo1.position + 1;
		i1 = paramInt + 1;
		while (i1 < k) {
			localItemInfo2 = (ItemInfo) this.mItems.get(i1);
			while (n < localItemInfo2.position)
				f2 += this.mAdapter.getPageWidth(n++) + f1;
			if (localItemInfo2.position == i - 1)
				this.mLastOffset = (f2 + localItemInfo2.widthFactor - 1.0F);
			localItemInfo2.offset = f2;
			f2 += localItemInfo2.widthFactor + f1;
			i1++;
			n++;
		}
		this.mNeedCalculatePageOffsets = false;
	}

	public Parcelable onSaveInstanceState() {
		Parcelable localParcelable = super.onSaveInstanceState();
		SavedState localSavedState = new SavedState(localParcelable);
		localSavedState.position = this.mCurItem;
		if (this.mAdapter != null)
			localSavedState.adapterState = this.mAdapter.saveState();
		return localSavedState;
	}

	public void onRestoreInstanceState(Parcelable paramParcelable) {
		if (!(paramParcelable instanceof SavedState)) {
			super.onRestoreInstanceState(paramParcelable);
			return;
		}
		SavedState localSavedState = (SavedState) paramParcelable;
		super.onRestoreInstanceState(localSavedState.getSuperState());
		if (this.mAdapter != null) {
			this.mAdapter.restoreState(localSavedState.adapterState, localSavedState.loader);
			setCurrentItemInternal(localSavedState.position, false, true);
		} else {
			this.mRestoredCurItem = localSavedState.position;
			this.mRestoredAdapterState = localSavedState.adapterState;
			this.mRestoredClassLoader = localSavedState.loader;
		}
	}

	public void addView(View paramView, int paramInt, ViewGroup.LayoutParams paramLayoutParams) {
		if (!checkLayoutParams(paramLayoutParams))
			paramLayoutParams = generateLayoutParams(paramLayoutParams);
		LayoutParams localLayoutParams = (LayoutParams) paramLayoutParams;
		localLayoutParams.isDecor |= paramView instanceof Decor;
		if (this.mInLayout) {
			if ((localLayoutParams != null) && (localLayoutParams.isDecor))
				throw new IllegalStateException("Cannot add pager decor view during layout");
			localLayoutParams.needsMeasure = true;
			addViewInLayout(paramView, paramInt, paramLayoutParams);
		} else {
			super.addView(paramView, paramInt, paramLayoutParams);
		}
	}

	public void removeView(View paramView) {
		if (this.mInLayout)
			removeViewInLayout(paramView);
		else
			super.removeView(paramView);
	}

	ItemInfo infoForChild(View paramView) {
		for (int i = 0; i < this.mItems.size(); i++) {
			ItemInfo localItemInfo = (ItemInfo) this.mItems.get(i);
			if (this.mAdapter.isViewFromObject(paramView, localItemInfo.object))
				return localItemInfo;
		}
		return null;
	}

	ItemInfo infoForAnyChild(View paramView) {
		ViewParent localViewParent;
		while ((localViewParent = paramView.getParent()) != this) {
			if ((localViewParent == null) || (!(localViewParent instanceof View)))
				return null;
			paramView = (View) localViewParent;
		}
		return infoForChild(paramView);
	}

	ItemInfo infoForPosition(int paramInt) {
		for (int i = 0; i < this.mItems.size(); i++) {
			ItemInfo localItemInfo = (ItemInfo) this.mItems.get(i);
			if (localItemInfo.position == paramInt)
				return localItemInfo;
		}
		return null;
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.mFirstLayout = true;
	}

	protected void onMeasure(int paramInt1, int paramInt2) {
		setMeasuredDimension(getDefaultSize(0, paramInt1), getDefaultSize(0, paramInt2));
		int i = getMeasuredWidth();
		int j = i / 10;
		this.mGutterSize = Math.min(j, this.mDefaultGutterSize);
		int k = i - getPaddingLeft() - getPaddingRight();
		int m = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
		int n = getChildCount();
		View localView;
		LayoutParams localLayoutParams;
		int i2;
		for (int i1 = 0; i1 < n; i1++) {
			localView = getChildAt(i1);
			if (localView.getVisibility() != 8) {
				localLayoutParams = (LayoutParams) localView.getLayoutParams();
				if ((localLayoutParams != null) && (localLayoutParams.isDecor)) {
					i2 = localLayoutParams.gravity & 0x7;
					int i3 = localLayoutParams.gravity & 0x70;
					int i4 = -2147483648;
					int i5 = -2147483648;
					int i6 = (i3 == 48) || (i3 == 80) ? 1 : 0;
					int i7 = (i2 == 3) || (i2 == 5) ? 1 : 0;
					if (i6 != 0)
						i4 = 1073741824;
					else if (i7 != 0)
						i5 = 1073741824;
					int i8 = k;
					int i9 = m;
					if (localLayoutParams.width != -2) {
						i4 = 1073741824;
						if (localLayoutParams.width != -1)
							i8 = localLayoutParams.width;
					}
					if (localLayoutParams.height != -2) {
						i5 = 1073741824;
						if (localLayoutParams.height != -1)
							i9 = localLayoutParams.height;
					}
					int i10 = View.MeasureSpec.makeMeasureSpec(i8, i4);
					int i11 = View.MeasureSpec.makeMeasureSpec(i9, i5);
					localView.measure(i10, i11);
					if (i6 != 0)
						m -= localView.getMeasuredHeight();
					else if (i7 != 0)
						k -= localView.getMeasuredWidth();
				}
			}
		}
		this.mChildWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(k, 1073741824);
		this.mChildHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(m, 1073741824);
		this.mInLayout = true;
		populate();
		this.mInLayout = false;
		n = getChildCount();
		for (int i1 = 0; i1 < n; i1++) {
			localView = getChildAt(i1);
			if (localView.getVisibility() != 8) {
				localLayoutParams = (LayoutParams) localView.getLayoutParams();
				if ((localLayoutParams == null) || (!localLayoutParams.isDecor)) {
					i2 = View.MeasureSpec.makeMeasureSpec((int) (k * localLayoutParams.widthFactor), 1073741824);
					localView.measure(i2, this.mChildHeightMeasureSpec);
				}
			}
		}
	}

	protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
		if (paramInt1 != paramInt3)
			recomputeScrollPosition(paramInt1, paramInt3, this.mPageMargin, this.mPageMargin);
	}

	private void recomputeScrollPosition(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		int k;
		if ((paramInt2 > 0) && (!this.mItems.isEmpty())) {
			int i = paramInt1 + paramInt3;
			int j = paramInt2 + paramInt4;
			k = getScrollX();
			float f2 = k / j;
			int m = (int) (f2 * i);
			scrollTo(m, getScrollY());
			if (!this.mScroller.isFinished()) {
				int n = this.mScroller.getDuration() - this.mScroller.timePassed();
				ItemInfo localItemInfo2 = infoForPosition(this.mCurItem);
				this.mScroller.startScroll(m, 0, (int) (localItemInfo2.offset * paramInt1), 0, n);
			}
		} else {
			ItemInfo localItemInfo1 = infoForPosition(this.mCurItem);
			float f1 = localItemInfo1 != null ? Math.min(localItemInfo1.offset, this.mLastOffset) : 0.0F;
			k = (int) (f1 * paramInt1);
			if (k != getScrollX()) {
				completeScroll(false);
				scrollTo(k, getScrollY());
			}
		}
	}

	public void setExpand(boolean paramBoolean) {
		this.isExpand = paramBoolean;
	}

	protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		this.mInLayout = true;
		populate();
		this.mInLayout = false;
		int i = getChildCount();
		int j = paramInt3 - paramInt1;
		int k = paramInt4 - paramInt2;
		int m = getPaddingLeft();
		int n = getPaddingTop();
		int i1 = getPaddingRight();
		int i2 = getPaddingBottom();
		int i3 = getScrollX();
		int i4 = 0;
		View localView;
		LayoutParams localLayoutParams;
		int i7;
		int i8;
		int i9;
		for (int i5 = 0; i5 < i; i5++) {
			localView = getChildAt(i5);
			if (localView.getVisibility() != 8) {
				localLayoutParams = (LayoutParams) localView.getLayoutParams();
				int i6 = 0;
				i7 = 0;
				if (localLayoutParams.isDecor) {
					i8 = localLayoutParams.gravity & 0x7;
					i9 = localLayoutParams.gravity & 0x70;
					switch (i8) {
					case 2:
					case 4:
					default:
						i6 = m;
						break;
					case 3:
						i6 = m;
						m += localView.getMeasuredWidth();
						break;
					case 1:
						i6 = Math.max((j - localView.getMeasuredWidth()) / 2, m);
						break;
					case 5:
						i6 = j - i1 - localView.getMeasuredWidth();
						i1 += localView.getMeasuredWidth();
					}
					switch (i9) {
					default:
						i7 = n;
						break;
					case 48:
						i7 = n;
						n += localView.getMeasuredHeight();
						break;
					case 16:
						i7 = Math.max((k - localView.getMeasuredHeight()) / 2, n);
						break;
					case 80:
						i7 = k - i2 - localView.getMeasuredHeight();
						i2 += localView.getMeasuredHeight();
					}
					i6 += i3;
					localView.layout(i6, i7, i6 + localView.getMeasuredWidth(), i7 + localView.getMeasuredHeight());
					i4++;
				}
			}
		}
		for (int i5 = 0; i5 < i; i5++) {
			localView = getChildAt(i5);
			if (localView.getVisibility() != 8) {
				localLayoutParams = (LayoutParams) localView.getLayoutParams();
				ItemInfo localItemInfo;
				if ((!localLayoutParams.isDecor) && ((localItemInfo = infoForChild(localView)) != null)) {
					i7 = (int) (j * localItemInfo.offset);
					i8 = m + i7;
					i9 = n;
					if (localLayoutParams.needsMeasure) {
						localLayoutParams.needsMeasure = false;
						int i10;
						if (this.isExpand)
							i10 = View.MeasureSpec.makeMeasureSpec((int) ((j - m - i1) * localLayoutParams.widthFactor), 0);
						else
							i10 = View.MeasureSpec.makeMeasureSpec((int) ((j - m - i1) * localLayoutParams.widthFactor), 1073741824);
						int i11 = View.MeasureSpec.makeMeasureSpec(k - n - i2, 1073741824);
						localView.measure(i10, i11);
					}
					localView.layout(i8, i9, i8 + localView.getMeasuredWidth(), i9 + localView.getMeasuredHeight());
				}
			}
		}
		this.mTopPageBounds = n;
		this.mBottomPageBounds = (k - i2);
		this.mDecorChildCount = i4;
		this.mFirstLayout = false;
	}

	public void computeScroll() {
		if ((!this.mScroller.isFinished()) && (this.mScroller.computeScrollOffset())) {
			int i = getScrollX();
			int j = getScrollY();
			int k = this.mScroller.getCurrX();
			int m = this.mScroller.getCurrY();
			if ((i != k) || (j != m)) {
				scrollTo(k, m);
				if (!pageScrolled(k)) {
					this.mScroller.abortAnimation();
					scrollTo(0, m);
				}
			}
			ViewCompat.postInvalidateOnAnimation(this);
			return;
		}
		completeScroll(true);
	}

	private boolean pageScrolled(int paramInt) {
		if (this.mItems.size() == 0) {
			this.mCalledSuper = false;
			onPageScrolled(0, 0.0F, 0);
			if (!this.mCalledSuper)
				throw new IllegalStateException("onPageScrolled did not call superclass implementation");
			return false;
		}
		ItemInfo localItemInfo = infoForCurrentScrollPosition();
		int i = getWidth();
		int j = i + this.mPageMargin;
		float f1 = this.mPageMargin / i;
		int k = localItemInfo.position;
		float f2 = (paramInt / i - localItemInfo.offset) / (localItemInfo.widthFactor + f1);
		int m = (int) (f2 * j);
		this.mCalledSuper = false;
		onPageScrolled(k, f2, m);
		if (!this.mCalledSuper)
			throw new IllegalStateException("onPageScrolled did not call superclass implementation");
		return true;
	}

	protected void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
		int i;
		int j;
		int k;
		if (this.mDecorChildCount > 0) {
			i = getScrollX();
			j = getPaddingLeft();
			k = getPaddingRight();
			int m = getWidth();
			int n = getChildCount();
			for (int i1 = 0; i1 < n; i1++) {
				View localView2 = getChildAt(i1);
				LayoutParams localLayoutParams2 = (LayoutParams) localView2.getLayoutParams();
				if (localLayoutParams2.isDecor) {
					int i2 = localLayoutParams2.gravity & 0x7;
					int i3 = 0;
					switch (i2) {
					case 2:
					case 4:
					default:
						i3 = j;
						break;
					case 3:
						i3 = j;
						j += localView2.getWidth();
						break;
					case 1:
						i3 = Math.max((m - localView2.getMeasuredWidth()) / 2, j);
						break;
					case 5:
						i3 = m - k - localView2.getMeasuredWidth();
						k += localView2.getMeasuredWidth();
					}
					i3 += i;
					int i4 = i3 - localView2.getLeft();
					if (i4 != 0)
						localView2.offsetLeftAndRight(i4);
				}
			}
		}
		if (this.mOnPageChangeListener != null)
			this.mOnPageChangeListener.onPageScrolled(paramInt1, paramFloat, paramInt2);
		if (this.mInternalPageChangeListener != null)
			this.mInternalPageChangeListener.onPageScrolled(paramInt1, paramFloat, paramInt2);
		if (this.mPageTransformer != null) {
			i = getScrollX();
			j = getChildCount();
			for (k = 0; k < j; k++) {
				View localView1 = getChildAt(k);
				LayoutParams localLayoutParams1 = (LayoutParams) localView1.getLayoutParams();
				if (!localLayoutParams1.isDecor) {
					float f = (localView1.getLeft() - i) / getWidth();
					this.mPageTransformer.transformPage(localView1, f);
				}
			}
		}
		this.mCalledSuper = true;
	}

	private void completeScroll(boolean paramBoolean) {
		int i = this.mScrollState == 2 ? 1 : 0;
		if (i != 0) {
			setScrollingCacheEnabled(false);
			this.mScroller.abortAnimation();
			int j = getScrollX();
			int k = getScrollY();
			int m = this.mScroller.getCurrX();
			int n = this.mScroller.getCurrY();
			if ((j != m) || (k != n))
				scrollTo(m, n);
		}
		this.mPopulatePending = false;
		for (int j = 0; j < this.mItems.size(); j++) {
			ItemInfo localItemInfo = (ItemInfo) this.mItems.get(j);
			if (localItemInfo.scrolling) {
				i = 1;
				localItemInfo.scrolling = false;
			}
		}
		if (i != 0)
			if (paramBoolean)
				ViewCompat.postOnAnimation(this, this.mEndScrollRunnable);
			else
				this.mEndScrollRunnable.run();
	}

	private boolean isGutterDrag(float paramFloat1, float paramFloat2) {
		return ((paramFloat1 < this.mGutterSize) && (paramFloat2 > 0.0F)) || ((paramFloat1 > getWidth() - this.mGutterSize) && (paramFloat2 < 0.0F));
	}

	private void enableLayers(boolean paramBoolean) {
		int i = getChildCount();
		for (int j = 0; j < i; j++) {
			int k = paramBoolean ? 2 : 0;
			ViewCompat.setLayerType(getChildAt(j), k, null);
		}
	}

	public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent) {
		int i = paramMotionEvent.getAction() & 0xFF;
		if ((i == 3) || (i == 1)) {
			this.mIsBeingDragged = false;
			this.mIsUnableToDrag = false;
			this.mActivePointerId = -1;
			if (this.mVelocityTracker != null) {
				this.mVelocityTracker.recycle();
				this.mVelocityTracker = null;
			}
			return false;
		}
		if (i != 0) {
			if (this.mIsBeingDragged)
				return true;
			if (this.mIsUnableToDrag)
				return false;
		}
		switch (i) {
		case 2:
			int j = this.mActivePointerId;
			if (j != -1) {
				int k = MotionEventCompat.findPointerIndex(paramMotionEvent, j);
				float f1 = MotionEventCompat.getX(paramMotionEvent, k);
				float f2 = f1 - this.mLastMotionX;
				float f3 = Math.abs(f2);
				float f4 = MotionEventCompat.getY(paramMotionEvent, k);
				float f5 = Math.abs(f4 - this.mInitialMotionY);
				if ((f2 != 0.0F) && (!isGutterDrag(this.mLastMotionX, f2)) && (canScroll(this, false, (int) f2, (int) f1, (int) f4))) {
					this.mLastMotionX = f1;
					this.mLastMotionY = f4;
					this.mIsUnableToDrag = true;
					return false;
				}
				if ((f3 > this.mTouchSlop) && (f3 * 0.5F > f5)) {
					this.mIsBeingDragged = true;
					setScrollState(1);
					this.mLastMotionX = (f2 > 0.0F ? this.mInitialMotionX + this.mTouchSlop : this.mInitialMotionX - this.mTouchSlop);
					this.mLastMotionY = f4;
					setScrollingCacheEnabled(true);
				} else if (f5 > this.mTouchSlop) {
					this.mIsUnableToDrag = true;
				}
				if ((this.mIsBeingDragged) && (performDrag(f1)))
					ViewCompat.postInvalidateOnAnimation(this);
			}
			break;
		case 0:
			this.mLastMotionX = (this.mInitialMotionX = paramMotionEvent.getX());
			this.mLastMotionY = (this.mInitialMotionY = paramMotionEvent.getY());
			this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, 0);
			this.mIsUnableToDrag = false;
			this.mScroller.computeScrollOffset();
			if ((this.mScrollState == 2) && (Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX()) > this.mCloseEnough)) {
				this.mScroller.abortAnimation();
				this.mPopulatePending = false;
				populate();
				this.mIsBeingDragged = true;
				setScrollState(1);
			} else {
				completeScroll(false);
				this.mIsBeingDragged = false;
			}
			break;
		case 6:
			onSecondaryPointerUp(paramMotionEvent);
		}
		if (this.mVelocityTracker == null)
			this.mVelocityTracker = VelocityTracker.obtain();
		this.mVelocityTracker.addMovement(paramMotionEvent);
		return this.mIsBeingDragged;
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		if (this.mFakeDragging)
			return true;
		if ((paramMotionEvent.getAction() == 0) && (paramMotionEvent.getEdgeFlags() != 0))
			return false;
		if ((this.mAdapter == null) || (this.mAdapter.getCount() == 0))
			return false;
		if (this.mVelocityTracker == null)
			this.mVelocityTracker = VelocityTracker.obtain();
		this.mVelocityTracker.addMovement(paramMotionEvent);
		int i = paramMotionEvent.getAction();
		boolean bool = false;
		switch (i & 0xFF) {
		case 0:
			this.mScroller.abortAnimation();
			this.mPopulatePending = false;
			populate();
			this.mIsBeingDragged = true;
			setScrollState(1);
			this.mLastMotionX = (this.mInitialMotionX = paramMotionEvent.getX());
			this.mLastMotionY = (this.mInitialMotionY = paramMotionEvent.getY());
			this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, 0);
			break;
		case 2:
			int j;
			float f1;
			if (!this.mIsBeingDragged) {
				j = MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId);
				f1 = MotionEventCompat.getX(paramMotionEvent, j);
				float f3 = Math.abs(f1 - this.mLastMotionX);
				float f4 = MotionEventCompat.getY(paramMotionEvent, j);
				float f5 = Math.abs(f4 - this.mLastMotionY);
				if ((f3 > this.mTouchSlop) && (f3 > f5)) {
					this.mIsBeingDragged = true;
					this.mLastMotionX = (f1 - this.mInitialMotionX > 0.0F ? this.mInitialMotionX + this.mTouchSlop : this.mInitialMotionX - this.mTouchSlop);
					this.mLastMotionY = f4;
					setScrollState(1);
					setScrollingCacheEnabled(true);
				}
			}
			if (this.mIsBeingDragged) {
				j = MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId);
				f1 = MotionEventCompat.getX(paramMotionEvent, j);
				bool |= performDrag(f1);
			}
			break;
		case 1:
			if (this.mIsBeingDragged) {
				VelocityTracker localVelocityTracker = this.mVelocityTracker;
				localVelocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
				int m = (int) VelocityTrackerCompat.getXVelocity(localVelocityTracker, this.mActivePointerId);
				this.mPopulatePending = true;
				int n = getWidth();
				int i1 = getScrollX();
				ItemInfo localItemInfo = infoForCurrentScrollPosition();
				int i2 = localItemInfo.position;
				float f6 = (i1 / n - localItemInfo.offset) / localItemInfo.widthFactor;
				int i3 = MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId);
				float f7 = MotionEventCompat.getX(paramMotionEvent, i3);
				int i4 = (int) (f7 - this.mInitialMotionX);
				int i5 = determineTargetPage(i2, f6, m, i4);
				setCurrentItemInternal(i5, true, true, m);
				this.mActivePointerId = -1;
				endDrag();
				bool = this.mLeftEdge.onRelease() | this.mRightEdge.onRelease();
			}
			break;
		case 3:
			if (this.mIsBeingDragged) {
				scrollToItem(this.mCurItem, true, 0, false);
				this.mActivePointerId = -1;
				endDrag();
				bool = this.mLeftEdge.onRelease() | this.mRightEdge.onRelease();
			}
			break;
		case 5:
			int k = MotionEventCompat.getActionIndex(paramMotionEvent);
			float f2 = MotionEventCompat.getX(paramMotionEvent, k);
			this.mLastMotionX = f2;
			this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, k);
			break;
		case 6:
			onSecondaryPointerUp(paramMotionEvent);
			this.mLastMotionX = MotionEventCompat.getX(paramMotionEvent, MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId));
		case 4:
		}
		if (bool)
			ViewCompat.postInvalidateOnAnimation(this);
		return true;
	}

	private boolean performDrag(float paramFloat) {
		boolean bool = false;
		float f1 = this.mLastMotionX - paramFloat;
		this.mLastMotionX = paramFloat;
		float f2 = getScrollX();
		float f3 = f2 + f1;
		int i = getWidth();
		float f4 = i * this.mFirstOffset;
		float f5 = i * this.mLastOffset;
		int j = 1;
		int k = 1;
		ItemInfo localItemInfo1 = (ItemInfo) this.mItems.get(0);
		ItemInfo localItemInfo2 = (ItemInfo) this.mItems.get(this.mItems.size() - 1);
		if (localItemInfo1.position != 0) {
			j = 0;
			f4 = localItemInfo1.offset * i;
		}
		if (localItemInfo2.position != this.mAdapter.getCount() - 1) {
			k = 0;
			f5 = localItemInfo2.offset * i;
		}
		float f6;
		if (f3 < f4) {
			if (j != 0) {
				f6 = f4 - f3;
				bool = this.mLeftEdge.onPull(Math.abs(f6) / i);
			}
			f3 = f4;
		} else if (f3 > f5) {
			if (k != 0) {
				f6 = f3 - f5;
				bool = this.mRightEdge.onPull(Math.abs(f6) / i);
			}
			f3 = f5;
		}
		this.mLastMotionX += f3 - (int) f3;
		scrollTo((int) f3, getScrollY());
		pageScrolled((int) f3);
		return bool;
	}

	private ItemInfo infoForCurrentScrollPosition() {
		int i = getWidth();
		float f1 = i > 0 ? getScrollX() / i : 0.0F;
		float f2 = i > 0 ? this.mPageMargin / i : 0.0F;
		int j = -1;
		float f3 = 0.0F;
		float f4 = 0.0F;
		int k = 1;
		ItemInfo localObject = null;
		for (int m = 0; m < this.mItems.size(); m++) {
			ItemInfo localItemInfo = (ItemInfo) this.mItems.get(m);
			if ((k == 0) && (localItemInfo.position != j + 1)) {
				localItemInfo = this.mTempItem;
				localItemInfo.offset = (f3 + f4 + f2);
				localItemInfo.position = (j + 1);
				localItemInfo.widthFactor = this.mAdapter.getPageWidth(localItemInfo.position);
				m--;
			}
			float f5 = localItemInfo.offset;
			float f6 = f5;
			float f7 = f5 + localItemInfo.widthFactor + f2;
			if ((k != 0) || (f1 >= f6)) {
				if ((f1 < f7) || (m == this.mItems.size() - 1))
					return localItemInfo;
			} else
				return localObject;
			k = 0;
			j = localItemInfo.position;
			f3 = f5;
			f4 = localItemInfo.widthFactor;
			localObject = localItemInfo;
		}
		return localObject;
	}

	private int determineTargetPage(int paramInt1, float paramFloat, int paramInt2, int paramInt3) {
		int i;
		if ((Math.abs(paramInt3) > this.mFlingDistance) && (Math.abs(paramInt2) > this.mMinimumVelocity)) {
			i = paramInt2 > 0 ? paramInt1 : paramInt1 + 1;
		} else {
			float f = paramInt1 >= this.mCurItem ? 0.4F : 0.6F;
			i = (int) (paramInt1 + paramFloat + f);
		}
		if (this.mItems.size() > 0) {
			ItemInfo localItemInfo1 = (ItemInfo) this.mItems.get(0);
			ItemInfo localItemInfo2 = (ItemInfo) this.mItems.get(this.mItems.size() - 1);
			i = Math.max(localItemInfo1.position, Math.min(i, localItemInfo2.position));
		}
		return i;
	}

	public void draw(Canvas paramCanvas) {
		super.draw(paramCanvas);
		boolean bool = false;
		int i = ViewCompat.getOverScrollMode(this);
		if ((i == 0) || ((i == 1) && (this.mAdapter != null) && (this.mAdapter.getCount() > 1))) {
			int j;
			int k;
			int m;
			if (!this.mLeftEdge.isFinished()) {
				j = paramCanvas.save();
				k = getHeight() - getPaddingTop() - getPaddingBottom();
				m = getWidth();
				paramCanvas.rotate(270.0F);
				paramCanvas.translate(-k + getPaddingTop(), this.mFirstOffset * m);
				this.mLeftEdge.setSize(k, m);
				bool |= this.mLeftEdge.draw(paramCanvas);
				paramCanvas.restoreToCount(j);
			}
			if (!this.mRightEdge.isFinished()) {
				j = paramCanvas.save();
				k = getWidth();
				m = getHeight() - getPaddingTop() - getPaddingBottom();
				paramCanvas.rotate(90.0F);
				paramCanvas.translate(-getPaddingTop(), -(this.mLastOffset + 1.0F) * k);
				this.mRightEdge.setSize(m, k);
				bool |= this.mRightEdge.draw(paramCanvas);
				paramCanvas.restoreToCount(j);
			}
		} else {
			this.mLeftEdge.finish();
			this.mRightEdge.finish();
		}
		if (bool)
			ViewCompat.postInvalidateOnAnimation(this);
	}

	protected void onDraw(Canvas paramCanvas) {
		super.onDraw(paramCanvas);
		if ((this.mPageMargin > 0) && (this.mMarginDrawable != null) && (this.mItems.size() > 0) && (this.mAdapter != null)) {
			int i = getScrollX();
			int j = getWidth();
			float f1 = this.mPageMargin / j;
			int k = 0;
			ItemInfo localItemInfo = (ItemInfo) this.mItems.get(0);
			float f2 = localItemInfo.offset;
			int m = this.mItems.size();
			int n = localItemInfo.position;
			int i1 = ((ItemInfo) this.mItems.get(m - 1)).position;
			for (int i2 = n; i2 < i1; i2++) {
				while ((i2 > localItemInfo.position) && (k < m))
					localItemInfo = (ItemInfo) this.mItems.get(++k);
				float f3;
				if (i2 == localItemInfo.position) {
					f3 = (localItemInfo.offset + localItemInfo.widthFactor) * j;
					f2 = localItemInfo.offset + localItemInfo.widthFactor + f1;
				} else {
					float f4 = this.mAdapter.getPageWidth(i2);
					f3 = (f2 + f4) * j;
					f2 += f4 + f1;
				}
				if (f3 + this.mPageMargin > i) {
					this.mMarginDrawable.setBounds((int) f3, this.mTopPageBounds, (int) (f3 + this.mPageMargin + 0.5F), this.mBottomPageBounds);
					this.mMarginDrawable.draw(paramCanvas);
				}
				if (f3 > i + j)
					break;
			}
		}
	}

	public boolean beginFakeDrag() {
		if (this.mIsBeingDragged)
			return false;
		this.mFakeDragging = true;
		setScrollState(1);
		this.mInitialMotionX = (this.mLastMotionX = 0.0F);
		if (this.mVelocityTracker == null)
			this.mVelocityTracker = VelocityTracker.obtain();
		else
			this.mVelocityTracker.clear();
		long l = SystemClock.uptimeMillis();
		MotionEvent localMotionEvent = MotionEvent.obtain(l, l, 0, 0.0F, 0.0F, 0);
		this.mVelocityTracker.addMovement(localMotionEvent);
		localMotionEvent.recycle();
		this.mFakeDragBeginTime = l;
		return true;
	}

	public void endFakeDrag() {
		if (!this.mFakeDragging)
			throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
		VelocityTracker localVelocityTracker = this.mVelocityTracker;
		localVelocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
		int i = (int) VelocityTrackerCompat.getXVelocity(localVelocityTracker, this.mActivePointerId);
		this.mPopulatePending = true;
		int j = getWidth();
		int k = getScrollX();
		ItemInfo localItemInfo = infoForCurrentScrollPosition();
		int m = localItemInfo.position;
		float f = (k / j - localItemInfo.offset) / localItemInfo.widthFactor;
		int n = (int) (this.mLastMotionX - this.mInitialMotionX);
		int i1 = determineTargetPage(m, f, i, n);
		setCurrentItemInternal(i1, true, true, i);
		endDrag();
		this.mFakeDragging = false;
	}

	public void fakeDragBy(float paramFloat) {
		if (!this.mFakeDragging)
			throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
		this.mLastMotionX += paramFloat;
		float f1 = getScrollX();
		float f2 = f1 - paramFloat;
		int i = getWidth();
		float f3 = i * this.mFirstOffset;
		float f4 = i * this.mLastOffset;
		ItemInfo localItemInfo1 = (ItemInfo) this.mItems.get(0);
		ItemInfo localItemInfo2 = (ItemInfo) this.mItems.get(this.mItems.size() - 1);
		if (localItemInfo1.position != 0)
			f3 = localItemInfo1.offset * i;
		if (localItemInfo2.position != this.mAdapter.getCount() - 1)
			f4 = localItemInfo2.offset * i;
		if (f2 < f3)
			f2 = f3;
		else if (f2 > f4)
			f2 = f4;
		this.mLastMotionX += f2 - (int) f2;
		scrollTo((int) f2, getScrollY());
		pageScrolled((int) f2);
		long l = SystemClock.uptimeMillis();
		MotionEvent localMotionEvent = MotionEvent.obtain(this.mFakeDragBeginTime, l, 2, this.mLastMotionX, 0.0F, 0);
		this.mVelocityTracker.addMovement(localMotionEvent);
		localMotionEvent.recycle();
	}

	public boolean isFakeDragging() {
		return this.mFakeDragging;
	}

	private void onSecondaryPointerUp(MotionEvent paramMotionEvent) {
		int i = MotionEventCompat.getActionIndex(paramMotionEvent);
		int j = MotionEventCompat.getPointerId(paramMotionEvent, i);
		if (j == this.mActivePointerId) {
			int k = i == 0 ? 1 : 0;
			this.mLastMotionX = MotionEventCompat.getX(paramMotionEvent, k);
			this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, k);
			if (this.mVelocityTracker != null)
				this.mVelocityTracker.clear();
		}
	}

	private void endDrag() {
		this.mIsBeingDragged = false;
		this.mIsUnableToDrag = false;
		if (this.mVelocityTracker != null) {
			this.mVelocityTracker.recycle();
			this.mVelocityTracker = null;
		}
	}

	private void setScrollingCacheEnabled(boolean paramBoolean) {
		if (this.mScrollingCacheEnabled != paramBoolean)
			this.mScrollingCacheEnabled = paramBoolean;
	}

	protected boolean canScroll(View paramView, boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3) {
		if ((paramView instanceof ViewGroup)) {
			ViewGroup localViewGroup = (ViewGroup) paramView;
			int i = paramView.getScrollX();
			int j = paramView.getScrollY();
			int k = localViewGroup.getChildCount();
			for (int m = k - 1; m >= 0; m--) {
				View localView = localViewGroup.getChildAt(m);
				if ((paramInt2 + i >= localView.getLeft()) && (paramInt2 + i < localView.getRight()) && (paramInt3 + j >= localView.getTop()) && (paramInt3 + j < localView.getBottom())
						&& (canScroll(localView, true, paramInt1, paramInt2 + i - localView.getLeft(), paramInt3 + j - localView.getTop())))
					return true;
			}
		}
		return (paramBoolean) && (ViewCompat.canScrollHorizontally(paramView, -paramInt1));
	}

	public boolean dispatchKeyEvent(KeyEvent paramKeyEvent) {
		return (super.dispatchKeyEvent(paramKeyEvent)) || (executeKeyEvent(paramKeyEvent));
	}

	public boolean executeKeyEvent(KeyEvent paramKeyEvent) {
		boolean bool = false;
		if (paramKeyEvent.getAction() == 0)
			switch (paramKeyEvent.getKeyCode()) {
			case 21:
				bool = arrowScroll(17);
				break;
			case 22:
				bool = arrowScroll(66);
				break;
			case 61:
				if (Build.VERSION.SDK_INT >= 11)
					if (KeyEventCompat.hasNoModifiers(paramKeyEvent))
						bool = arrowScroll(2);
					else if (KeyEventCompat.hasModifiers(paramKeyEvent, 1))
						bool = arrowScroll(1);
				break;
			}
		return bool;
	}

	public boolean arrowScroll(int paramInt) {
		View localView1 = findFocus();
		if (localView1 == this)
			localView1 = null;
		boolean bool = false;
		View localView2 = FocusFinder.getInstance().findNextFocus(this, localView1, paramInt);
		if ((localView2 != null) && (localView2 != localView1)) {
			int i;
			int j;
			if (paramInt == 17) {
				i = getChildRectInPagerCoordinates(this.mTempRect, localView2).left;
				j = getChildRectInPagerCoordinates(this.mTempRect, localView1).left;
				if ((localView1 != null) && (i >= j))
					bool = pageLeft();
				else
					bool = localView2.requestFocus();
			} else if (paramInt == 66) {
				i = getChildRectInPagerCoordinates(this.mTempRect, localView2).left;
				j = getChildRectInPagerCoordinates(this.mTempRect, localView1).left;
				if ((localView1 != null) && (i <= j))
					bool = pageRight();
				else
					bool = localView2.requestFocus();
			}
		} else if ((paramInt == 17) || (paramInt == 1)) {
			bool = pageLeft();
		} else if ((paramInt == 66) || (paramInt == 2)) {
			bool = pageRight();
		}
		if (bool)
			playSoundEffect(SoundEffectConstants.getContantForFocusDirection(paramInt));
		return bool;
	}

	private Rect getChildRectInPagerCoordinates(Rect paramRect, View paramView) {
		if (paramRect == null)
			paramRect = new Rect();
		if (paramView == null) {
			paramRect.set(0, 0, 0, 0);
			return paramRect;
		}
		paramRect.left = paramView.getLeft();
		paramRect.right = paramView.getRight();
		paramRect.top = paramView.getTop();
		paramRect.bottom = paramView.getBottom();
		ViewGroup localViewGroup;
		for (ViewParent localViewParent = paramView.getParent(); ((localViewParent instanceof ViewGroup)) && (localViewParent != this); localViewParent = localViewGroup.getParent()) {
			localViewGroup = (ViewGroup) localViewParent;
			paramRect.left += localViewGroup.getLeft();
			paramRect.right += localViewGroup.getRight();
			paramRect.top += localViewGroup.getTop();
			paramRect.bottom += localViewGroup.getBottom();
		}
		return paramRect;
	}

	boolean pageLeft() {
		if (this.mCurItem > 0) {
			setCurrentItem(this.mCurItem - 1, true);
			return true;
		}
		return false;
	}

	boolean pageRight() {
		if ((this.mAdapter != null) && (this.mCurItem < this.mAdapter.getCount() - 1)) {
			setCurrentItem(this.mCurItem + 1, true);
			return true;
		}
		return false;
	}

	public void addFocusables(ArrayList<View> paramArrayList, int paramInt1, int paramInt2) {
		int i = paramArrayList.size();
		int j = getDescendantFocusability();
		if (j != 393216)
			for (int k = 0; k < getChildCount(); k++) {
				View localView = getChildAt(k);
				if (localView.getVisibility() == 0) {
					ItemInfo localItemInfo = infoForChild(localView);
					if ((localItemInfo != null) && (localItemInfo.position == this.mCurItem))
						localView.addFocusables(paramArrayList, paramInt1, paramInt2);
				}
			}
		if ((j != 262144) || (i == paramArrayList.size())) {
			if (!isFocusable())
				return;
			if (((paramInt2 & 0x1) == 1) && (isInTouchMode()) && (!isFocusableInTouchMode()))
				return;
			if (paramArrayList != null)
				paramArrayList.add(this);
		}
	}

	public void addTouchables(ArrayList<View> paramArrayList) {
		for (int i = 0; i < getChildCount(); i++) {
			View localView = getChildAt(i);
			if (localView.getVisibility() == 0) {
				ItemInfo localItemInfo = infoForChild(localView);
				if ((localItemInfo != null) && (localItemInfo.position == this.mCurItem))
					localView.addTouchables(paramArrayList);
			}
		}
	}

	protected boolean onRequestFocusInDescendants(int paramInt, Rect paramRect) {
		int m = getChildCount();
		int i;
		int j;
		int k;
		if ((paramInt & 0x2) != 0) {
			i = 0;
			j = 1;
			k = m;
		} else {
			i = m - 1;
			j = -1;
			k = -1;
		}
		int n = i;
		while (n != k) {
			View localView = getChildAt(n);
			if (localView.getVisibility() == 0) {
				ItemInfo localItemInfo = infoForChild(localView);
				if ((localItemInfo != null) && (localItemInfo.position == this.mCurItem) && (localView.requestFocus(paramInt, paramRect)))
					return true;
			}
			n += j;
		}
		return false;
	}

	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent paramAccessibilityEvent) {
		int i = getChildCount();
		for (int j = 0; j < i; j++) {
			View localView = getChildAt(j);
			if (localView.getVisibility() == 0) {
				ItemInfo localItemInfo = infoForChild(localView);
				if ((localItemInfo != null) && (localItemInfo.position == this.mCurItem) && (localView.dispatchPopulateAccessibilityEvent(paramAccessibilityEvent)))
					return true;
			}
		}
		return false;
	}

	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams();
	}

	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams paramLayoutParams) {
		return generateDefaultLayoutParams();
	}

	protected boolean checkLayoutParams(ViewGroup.LayoutParams paramLayoutParams) {
		return ((paramLayoutParams instanceof LayoutParams)) && (super.checkLayoutParams(paramLayoutParams));
	}

	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet paramAttributeSet) {
		return new LayoutParams(getContext(), paramAttributeSet);
	}

	static class ViewPositionComparator implements Comparator<View> {
		public int compare(View paramView1, View paramView2) {
			ViewPager.LayoutParams localLayoutParams1 = (ViewPager.LayoutParams) paramView1.getLayoutParams();
			ViewPager.LayoutParams localLayoutParams2 = (ViewPager.LayoutParams) paramView2.getLayoutParams();
			if (localLayoutParams1.isDecor != localLayoutParams2.isDecor)
				return localLayoutParams1.isDecor ? 1 : -1;
			return localLayoutParams1.position - localLayoutParams2.position;
		}
	}

	public static class LayoutParams extends ViewGroup.LayoutParams {
		public boolean isDecor;
		public int gravity;
		float widthFactor = 0.0F;
		boolean needsMeasure;
		int position;
		int childIndex;

		public LayoutParams() {
			super(-1, -1);
		}

		public LayoutParams(Context paramContext, AttributeSet paramAttributeSet) {
			super(paramContext, paramAttributeSet);
			TypedArray localTypedArray = paramContext.obtainStyledAttributes(paramAttributeSet, ViewPager.LAYOUT_ATTRS);
			this.gravity = localTypedArray.getInteger(0, 48);
			localTypedArray.recycle();
		}
	}

	private class PagerObserver extends DataSetObserver {
		private PagerObserver() {
		}

		public void onChanged() {
			ViewPager.this.dataSetChanged();
		}

		public void onInvalidated() {
			ViewPager.this.dataSetChanged();
		}
	}

	class MyAccessibilityDelegate extends AccessibilityDelegateCompat {
		MyAccessibilityDelegate() {
		}

		public void onInitializeAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent) {
			super.onInitializeAccessibilityEvent(paramView, paramAccessibilityEvent);
			paramAccessibilityEvent.setClassName(ViewPager.class.getName());
		}

		public void onInitializeAccessibilityNodeInfo(View paramView, AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat) {
			super.onInitializeAccessibilityNodeInfo(paramView, paramAccessibilityNodeInfoCompat);
			paramAccessibilityNodeInfoCompat.setClassName(ViewPager.class.getName());
			paramAccessibilityNodeInfoCompat.setScrollable((ViewPager.this.mAdapter != null) && (ViewPager.this.mAdapter.getCount() > 1));
			if ((ViewPager.this.mAdapter != null) && (ViewPager.this.mCurItem >= 0) && (ViewPager.this.mCurItem < ViewPager.this.mAdapter.getCount() - 1))
				paramAccessibilityNodeInfoCompat.addAction(4096);
			if ((ViewPager.this.mAdapter != null) && (ViewPager.this.mCurItem > 0) && (ViewPager.this.mCurItem < ViewPager.this.mAdapter.getCount()))
				paramAccessibilityNodeInfoCompat.addAction(8192);
		}

		public boolean performAccessibilityAction(View paramView, int paramInt, Bundle paramBundle) {
			if (super.performAccessibilityAction(paramView, paramInt, paramBundle))
				return true;
			switch (paramInt) {
			case 4096:
				if ((ViewPager.this.mAdapter != null) && (ViewPager.this.mCurItem >= 0) && (ViewPager.this.mCurItem < ViewPager.this.mAdapter.getCount() - 1)) {
					ViewPager.this.setCurrentItem(ViewPager.this.mCurItem + 1);
					return true;
				}
				return false;
			case 8192:
				if ((ViewPager.this.mAdapter != null) && (ViewPager.this.mCurItem > 0) && (ViewPager.this.mCurItem < ViewPager.this.mAdapter.getCount())) {
					ViewPager.this.setCurrentItem(ViewPager.this.mCurItem - 1);
					return true;
				}
				return false;
			}
			return false;
		}
	}

	public static class SavedState extends View.BaseSavedState {
		int position;
		Parcelable adapterState;
		ClassLoader loader;
		public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks() {
			public ViewPager.SavedState createFromParcel(Parcel paramAnonymousParcel, ClassLoader paramAnonymousClassLoader) {
				return new ViewPager.SavedState(paramAnonymousParcel, paramAnonymousClassLoader);
			}

			public ViewPager.SavedState[] newArray(int paramAnonymousInt) {
				return new ViewPager.SavedState[paramAnonymousInt];
			}
		});

		public SavedState(Parcelable paramParcelable) {
			super(paramParcelable);
		}

		public void writeToParcel(Parcel paramParcel, int paramInt) {
			super.writeToParcel(paramParcel, paramInt);
			paramParcel.writeInt(this.position);
			paramParcel.writeParcelable(this.adapterState, paramInt);
		}

		public String toString() {
			return "FragmentPager.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " position=" + this.position + "}";
		}

		SavedState(Parcel paramParcel, ClassLoader paramClassLoader) {
			super(paramParcel);
			if (paramClassLoader == null)
				paramClassLoader = getClass().getClassLoader();
			this.position = paramParcel.readInt();
			this.adapterState = paramParcel.readParcelable(paramClassLoader);
			this.loader = paramClassLoader;
		}
	}

	static abstract interface Decor {
	}

	static abstract interface OnAdapterChangeListener {
		public abstract void onAdapterChanged(PagerAdapter paramPagerAdapter1, PagerAdapter paramPagerAdapter2);
	}

	public static abstract interface PageTransformer {
		public abstract void transformPage(View paramView, float paramFloat);
	}

	public static class SimpleOnPageChangeListener implements ViewPager.OnPageChangeListener {
		public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
		}

		public void onPageSelected(int paramInt) {
		}

		public void onPageScrollStateChanged(int paramInt) {
		}
	}

	public static abstract interface OnPageChangeListener {
		public abstract void onPageScrolled(int paramInt1, float paramFloat, int paramInt2);

		public abstract void onPageSelected(int paramInt);

		public abstract void onPageScrollStateChanged(int paramInt);
	}

	static class ItemInfo {
		Object object;
		int position;
		boolean scrolling;
		float widthFactor;
		float offset;
	}
}
