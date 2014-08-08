package io.viva.tv.app;

import io.viva.baseui.R;
import io.viva.tv.app.widget.NumberIndicator;

import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TabDisplay implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	public static final int LAST_POSITION = -2;
	private static final TabImpl NONE = null;
	private static final String TAG = "TabDisplay";
	private static final boolean DEBUG = true;
	private final Context mContext;
	private final TabAdapter mAdapter;
	TabListView mList;
	boolean mExpanded;
	boolean isLeft;
	private ViewGroup mView;
	private LeftNavBar.OnClickTabIndexListener listener;
	private AbsNavView mParent;
	private SparseArray<Integer> mNumberArray = new SparseArray<Integer>();
	TabHorizontalListView mHorizontalList;
	TextView mTopTitle;
	int tabsLayoutRes;
	int mChangedIndex = -1;
	boolean mSelectedClean = false;
	boolean mFakeSelected = false;

	boolean unFocus = false;

	public TabDisplay(Context context, ViewGroup parent, TypedArray attributes) {
		this(context, parent, attributes, true);
	}

	public TabDisplay(Context context, ViewGroup parent, TypedArray attributes, boolean isLeft) {
		this.mContext = context;
		this.mAdapter = new TabAdapter(context);
		this.isLeft = isLeft;
		createView(parent);
		this.mParent = ((AbsNavView) parent);
	}

	public void setDisplayStyle(int displayStyle) {
		if (this.isLeft)
			switch (displayStyle) {
			case 1:
				setTabsLayoutRes(R.layout.leftnav_bar_tab_mangosteen);
				this.mList.setDisplayStyle(displayStyle);
				break;
			case 0:
			default:
				setTabsLayoutRes(R.layout.leftnav_bar_tab);
				this.mList.setDisplayStyle(displayStyle);
			}
	}

	public void setUnFocus(boolean unFocus) {
		this.unFocus = unFocus;
	}

	public boolean getunFocus() {
		return this.unFocus;
	}

	public ViewGroup getTabDisplay() {
		return this.mView;
	}

	private void createView(ViewGroup parent) {
		this.mView = ((ViewGroup) parent.findViewById(R.id.tabDisplay));
		if (this.isLeft) {
			this.mList = ((TabListView) this.mView.findViewById(R.id.tab_list_view));
			this.mList.setFocusableInTouchMode(true);
			this.mList.setAdapter(this.mAdapter);

			this.mList.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
			this.mList.setOnItemClickListener(this);
			this.mList.setOnItemSelectedListener(this);
			this.mList.setOnItemLongClickListener(this);
			this.tabsLayoutRes = R.layout.leftnav_bar_tab;
			this.mList.setTabDisplay(this);
		} else {
			this.mHorizontalList = ((TabHorizontalListView) this.mView.findViewById(R.id.tab_list_view));

			this.mHorizontalList.setFocusableInTouchMode(true);
			this.mHorizontalList.setAdapter(this.mAdapter);

			this.mHorizontalList.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
			this.mHorizontalList.setOnItemClickListener(this);
			this.mHorizontalList.setOnItemSelectedListener(this);
			this.mHorizontalList.setOnItemLongClickListener(this);
			this.mTopTitle = ((TextView) this.mView.findViewById(R.id.top_title));
			this.tabsLayoutRes = R.layout.top_nav_bar_tabs_item;
			this.mHorizontalList.setTabDisplay(this);
		}
	}

	public void setTabsLayoutRes(int resId) {
		this.tabsLayoutRes = resId;
	}

	public void showNumberIndicator(int tabIndex, int number) {
		if (((Integer) this.mNumberArray.get(tabIndex, Integer.valueOf(0))).intValue() != number) {
			this.mChangedIndex = tabIndex;
			this.mNumberArray.put(tabIndex, Integer.valueOf(number));
			this.mAdapter.notifyDataSetChanged();
		}
	}

	public void setMaxNumberIndicator(int max) {
		if ((this.mNumberArray != null) && (this.mNumberArray.size() > 0))
			;
	}

	void setTopTitleVisibility(int visibility) {
		if (this.mTopTitle != null)
			this.mTopTitle.setVisibility(visibility);
	}

	void setTopTitleName(CharSequence text) {
		if (this.mTopTitle != null)
			this.mTopTitle.setText(text);
	}

	CharSequence getTopTitleName() {
		return this.mTopTitle != null ? this.mTopTitle.getText() : null;
	}

	int getTopTitleVisibility() {
		return this.mTopTitle != null ? this.mTopTitle.getVisibility() : 8;
	}

	void setTopListVisibility(int visibility) {
		this.mHorizontalList.setVisibility(visibility);
	}

	int getTopListVisibility() {
		return this.mHorizontalList.getVisibility();
	}

	View getView() {
		return this.isLeft ? this.mList : this.mHorizontalList;
	}

	TabDisplay setVisible(boolean visible) {
		if (this.isLeft) {
			this.mList.setVisibility(visible ? View.VISIBLE : View.GONE);
		} else {
			this.mHorizontalList.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
		this.mAdapter.setSelectionActive(visible);
		return this;
	}

	TabDisplay setExpanded(boolean expanded) {
		this.mExpanded = expanded;
		this.mAdapter.refresh();
		return this;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	boolean lostFocusOnTabListView() {
		if ((getCount() > 0) && (getSelected() != null)) {
			int selectPosition = getSelected().getPosition();
			if (selectPosition == getCount() - 1) {
				this.mList.lostFocus();
				return true;
			}
		}
		return false;
	}

	void resetFocusOnTabListView() {
		if (this.mList != null)
			this.mList.resetFocus();
	}

	public void add(TabImpl tab, int position, boolean setSelected) {
		if (position == LAST_POSITION) {
			position = this.mAdapter.getCount();
		}
		this.mAdapter.insert(tab, position);
		if (setSelected) {
			select(tab);
		}
	}

	public TabImpl get(int position) {
		if ((this.mAdapter != null) && (position >= 0) && (position < this.mAdapter.getCount())) {
			return (TabImpl) this.mAdapter.getItem(position);
		}
		return null;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void select(TabImpl tab) {
		View view = getView();
		if ((view instanceof TabListView)) {
			TabListView list = (TabListView) view;
			list.prepareSelectorMovingAnimator();
			list.setSelection(tab.getPosition());
			list.startSelectorMovingAnimator();
		} else if ((view instanceof TabHorizontalListView)) {
			TabHorizontalListView list = (TabHorizontalListView) view;
			list.prepareSelectorMovingAnimator();
			if (getSelected() != null) {
				Log.d("TabHorizontalListView", "---------select selectedPosition = " + getSelected().getPosition());
			}
			if (getSelected() != null) {
				Log.d("TabHorizontalListView", "---------select new Position = " + tab.getPosition());
			}
			list.setSelection(tab.getPosition());
			list.startSelectorMovingAnimator();
		}
		this.mAdapter.setSelected(tab);
	}

	public TabImpl getSelected() {
		return this.mAdapter.getSelected();
	}

	public void setOnClickTabIndexListener(LeftNavBar.OnClickTabIndexListener listener) {
		this.listener = listener;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void onSelectionChanged(TabImpl oldSelection, TabImpl newSelection) {
		FragmentTransaction transaction = null;
		FragmentManager fragmentManager = null;
		Context context = this.mParent.getAcitivity();

		int oldPosition = oldSelection == null ? 0 : oldSelection.getPosition();
		int newPosition = newSelection == null ? 0 : newSelection.getPosition();

		if ((context != null) && ((context instanceof Activity))) {
			fragmentManager = ((Activity) context).getFragmentManager();
			transaction = fragmentManager.beginTransaction().disallowAddToBackStack();

			if (oldPosition > newPosition) {
				if (this.isLeft) {
					transaction.setCustomAnimations(R.anim.tui_left_nav_switch_move_in, 0);
				} else {
					transaction.setCustomAnimations(R.anim.tui_top_nav_switch_move_in, 0);
				}

			} else if (oldPosition < newPosition) {
				if (this.isLeft) {
					transaction.setCustomAnimations(R.anim.tui_left_nav_switch_down_move_in, 0);
				} else {
					transaction.setCustomAnimations(R.anim.tui_top_nav_switch_right_move_in, 0);
				}

			}

			if (oldSelection == newSelection) {
				if ((newSelection != NONE) && (newSelection.getCallback() != null)) {
					newSelection.getCallback().onTabReselected(newSelection, transaction);
				}
			} else {
				if ((oldSelection != NONE) && (oldSelection.getCallback() != null)) {
					oldSelection.getCallback().onTabUnselected(oldSelection, transaction);
				}
				if ((newSelection != NONE) && (newSelection.getCallback() != null)) {
					newSelection.getCallback().onTabSelected(newSelection, transaction);
				}
			}

			if (transaction != null) {
				Log.d(TAG, "-------------isEmpty = " + transaction.isEmpty());
				if (!transaction.isEmpty()) {
					int result = transaction.commitAllowingStateLoss();
					Log.d(TAG, "----------transaction.commitAllowingStateLoss() result = " + result);
					transaction = null;
				}

			}

			if (this.isLeft) {
				this.mList.setHighlighted(this.mAdapter.getPosition(newSelection));
			} else {
				this.mHorizontalList.setHighlighted(this.mAdapter.getPosition(newSelection));
			}
		}
	}

	public int getCount() {
		return this.mAdapter.getCount();
	}

	public void removeAll() {
		this.mAdapter.clear();
	}

	public void remove(TabImpl tab) {
		this.mAdapter.remove(tab);
	}

	public void remove(int position) {
		remove((TabImpl) this.mAdapter.getItem(position));
	}

	private static void detachFromParent(View view) {
		if (view == null) {
			return;
		}
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);
		}
	}

	public void cleartSelectionState() {
		if ((this.mAdapter != null) && (this.mAdapter.getCount() > 0)) {
			this.mAdapter.cleartSelectionState();
		}
	}

	public void recoverSelectionState() {
		if ((this.mAdapter != null) && (this.mAdapter.getCount() > 0)) {
			this.mAdapter.recoverSelectionState();
		}
	}

	public void setSelectedClean(boolean isClean) {
		this.mSelectedClean = isClean;
	}

	public void setFakeSelected(boolean isFake) {
		this.mFakeSelected = isFake;
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "--------------onItemLongClick----------");
		return false;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "-------------onItemClick, position = " + position);

		if ((position >= 0) && (position < getCount())) {
			TabImpl tab = (TabImpl) this.mAdapter.getItem(position);
			this.mAdapter.setSelected(tab);
		}
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "-------------onItemSelected, position = " + position);

		if ((position >= 0) && (position < getCount())) {
			TabImpl tab = (TabImpl) this.mAdapter.getItem(position);
			this.mAdapter.setSelected(tab);
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {
		Log.d(TAG, "-------------onNothingSelected-----------");
	}

	private final class TabAdapter extends ArrayAdapter<TabImpl> {
		final Map<TabImpl, TabFrame> mCachedViews;
		private TabImpl mSelection;
		private boolean mIsSelectionActive;
		private TabImpl mSavedSelection;

		TabAdapter(Context context) {
			super(context, 0);
			this.mCachedViews = new HashMap<TabImpl, TabFrame>();
			this.mSelection = TabDisplay.NONE;
			this.mSavedSelection = TabDisplay.NONE;
			this.mIsSelectionActive = true;
		}

		public void setSelectionActive(boolean active) {
			if (active == this.mIsSelectionActive) {
				return;
			}
			if (active) {
				this.mIsSelectionActive = true;
				setSelected(this.mSavedSelection);
				this.mSavedSelection = TabDisplay.NONE;
			} else {
				this.mSavedSelection = this.mSelection;
				setSelected(TabDisplay.NONE);
				this.mIsSelectionActive = false;
			}
		}

		public void setSelected(TabImpl tab) {
			if (!this.mIsSelectionActive) {
				this.mSavedSelection = tab;
				return;
			}
			TabImpl oldSelection = this.mSelection;
			this.mSelection = tab;
			if (oldSelection != this.mSelection) {
				setSelectionState(oldSelection, false);
				setSelectionState(this.mSelection, true);
			}

			int position = TabDisplay.this.mAdapter.getPosition(tab);
			if ((TabDisplay.this.listener != null) && (position >= 0)) {
				TabDisplay.this.listener.onClickTabIndex(tab, position);
			} else {
				TabDisplay.this.onSelectionChanged(oldSelection, this.mSelection);
			}
		}

		public TabImpl getSelected() {
			return this.mIsSelectionActive ? this.mSelection : this.mSavedSelection;
		}

		private boolean isSelected(TabImpl tab) {
			return (tab != TabDisplay.NONE) && (!TabDisplay.this.mSelectedClean) && (tab == getSelected());
		}

		private void setSelectionState(TabImpl tab, boolean selected) {
			if ((tab != TabDisplay.NONE) && (this.mCachedViews.containsKey(tab))) {
				((TabFrame) this.mCachedViews.get(tab)).select(selected);
			}
		}

		public void cleartSelectionState() {
			if (this.mCachedViews.containsKey(this.mSelection)) {
				TabFrame tabFrame = (TabFrame) this.mCachedViews.get(this.mSelection);
				if (tabFrame != null) {
					tabFrame.select(false);
				}
			}
		}

		public void recoverSelectionState() {
			if (this.mCachedViews.containsKey(this.mSelection)) {
				TabFrame tabFrame = (TabFrame) this.mCachedViews.get(this.mSelection);
				if (tabFrame != null) {
					tabFrame.select(true);
				}
			}
		}

		public void refresh() {
			for (TabFrame frame : this.mCachedViews.values()) {
				frame.expand(TabDisplay.this.mExpanded);
			}
		}

		public int getItemViewType(int position) {
			return -1;
		}

		public void insert(TabImpl tab, int position) {
			super.insert(tab, position);
			updatePositions(false);
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public void remove(TabImpl tab) {
			TabDisplay.detachFromParent(tab.getCustomView());
			this.mCachedViews.remove(tab);
			super.remove(tab);
			updatePositions(false);
			if (isSelected(tab)) {
				setSelected(getCount() == 0 ? TabDisplay.NONE : (TabImpl) getItem(Math.max(0, tab.getPosition() - 1)));
			}
			tab.setPosition(-1);
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public void clear() {
			updatePositions(true);
			for (int i = 0; i < getCount(); i++) {
				TabDisplay.detachFromParent(((TabImpl) getItem(i)).getCustomView());
			}
			this.mCachedViews.clear();
			setSelected(TabDisplay.NONE);
			super.clear();
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public View getView(int position, View convertView, ViewGroup parent) {
			TabImpl tab = (TabImpl) getItem(position);
			if (!this.mCachedViews.containsKey(tab)) {
				TabFrame frame = (TabFrame) LayoutInflater.from(TabDisplay.this.mContext).inflate(TabDisplay.this.tabsLayoutRes, parent, false);

				if (tab.hasCustomView())
					frame.configureCustom(tab.getCustomView());
				else {
					frame.configureNormal(tab.getIcon(), tab.getText());
				}

				this.mCachedViews.put(tab, frame);
			}
			setSelectionState(tab, isSelected(tab));
			TabFrame result = (TabFrame) this.mCachedViews.get(tab);
			if (TabDisplay.this.mFakeSelected)
				result.setFakeSelected(true);
			else {
				result.setFakeSelected(false);
			}
			result.expand(TabDisplay.this.mExpanded);

			if ((TabDisplay.this.mChangedIndex == position) && (!TabDisplay.this.isLeft)) {
				TabDisplay.this.mChangedIndex = -1;
				NumberIndicator numberIdicator = (NumberIndicator) result.findViewById(2114584598);
				if (numberIdicator != null) {
					Integer newNumber = (Integer) TabDisplay.this.mNumberArray.get(position, Integer.valueOf(0));
					if (newNumber.intValue() == 0) {
						numberIdicator.setNumber(0);
						numberIdicator.hide();
					} else if (newNumber.intValue() > 0) {
						numberIdicator.setNumber(newNumber.intValue());
						numberIdicator.show();
					}
				}
			}

			return result;
		}

		private void updatePositions(boolean allInvalid) {
			for (int i = 0; i < getCount(); i++) {
				((TabImpl) getItem(i)).setPosition(allInvalid ? -1 : i);
			}
		}
	}
}
