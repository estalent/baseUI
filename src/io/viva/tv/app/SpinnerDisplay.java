package io.viva.tv.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class SpinnerDisplay {
	private final Context mContext;
	private Spinner mView;
	private ActionBar.OnNavigationListener mListener;
	private boolean mExpanded;

	SpinnerDisplay(Context context, ViewGroup parent, TypedArray attributes) {
		this.mContext = context;
		createView(parent, attributes);
	}

	View getView() {
		return this.mView;
	}

	SpinnerDisplay setVisible(boolean visible) {
		this.mView.setVisibility(visible ? View.VISIBLE : View.GONE);
		return this;
	}

	SpinnerDisplay setExpanded(boolean expanded) {
		this.mExpanded = expanded;
		refreshSelectedItem();
		return this;
	}

	public void setContent(SpinnerAdapter adapter, ActionBar.OnNavigationListener listener) {
		this.mListener = listener;
		this.mView.setAdapter(adapter);
		refreshSelectedItem();
	}

	public void setSelected(int position) {
		this.mView.setSelection(position);
	}

	public int getSelected() {
		return this.mView.getSelectedItemPosition();
	}

	public int getCount() {
		return this.mView.getCount();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void refreshSelectedItem() {
		View selected = this.mView.getSelectedView();
		if (selected == null) {
			return;
		}
		selected.setActivated(this.mExpanded);
	}

	private void createView(ViewGroup parent, TypedArray attributes) {
		this.mView = ((Spinner) LayoutInflater.from(mContext).inflate(2114453516, parent, false));

		this.mView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (SpinnerDisplay.this.mListener != null) {
					SpinnerDisplay.this.mListener.onNavigationItemSelected(position, id);
				}
				SpinnerDisplay.this.refreshSelectedItem();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
}
