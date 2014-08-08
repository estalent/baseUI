package io.viva.tv.app.widget;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

public abstract class PagerAdapter {
	private DataSetObservable mObservable = new DataSetObservable();
	public static final int POSITION_UNCHANGED = -1;
	public static final int POSITION_NONE = -2;

	public abstract int getCount();

	public void startUpdate(ViewGroup paramViewGroup) {
		startUpdate(paramViewGroup);
	}

	public Object instantiateItem(ViewGroup paramViewGroup, int paramInt) {
		return instantiateItem(paramViewGroup, paramInt);
	}

	public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
		destroyItem(paramViewGroup, paramInt, paramObject);
	}

	public void setPrimaryItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
		setPrimaryItem(paramViewGroup, paramInt, paramObject);
	}

	public void finishUpdate(ViewGroup paramViewGroup) {
		finishUpdate(paramViewGroup);
	}

	
	public void startUpdate(View paramView) {
	}

	
	public Object instantiateItem(View paramView, int paramInt) {
		throw new UnsupportedOperationException("Required method instantiateItem was not overridden");
	}

	
	public void destroyItem(View paramView, int paramInt, Object paramObject) {
		throw new UnsupportedOperationException("Required method destroyItem was not overridden");
	}

	
	public void setPrimaryItem(View paramView, int paramInt, Object paramObject) {
	}

	
	public void finishUpdate(View paramView) {
	}

	public abstract boolean isViewFromObject(View paramView, Object paramObject);

	public Parcelable saveState() {
		return null;
	}

	public void restoreState(Parcelable paramParcelable, ClassLoader paramClassLoader) {
	}

	public int getItemPosition(Object paramObject) {
		return -1;
	}

	public void notifyDataSetChanged() {
		this.mObservable.notifyChanged();
	}

	void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
		this.mObservable.registerObserver(paramDataSetObserver);
	}

	void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
		this.mObservable.unregisterObserver(paramDataSetObserver);
	}

	public CharSequence getPageTitle(int paramInt) {
		return null;
	}

	public float getPageWidth(int paramInt) {
		return 1.0F;
	}
}

