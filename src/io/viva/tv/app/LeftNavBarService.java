package io.viva.tv.app;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

public class LeftNavBarService {
	private final Map<Integer, LeftNavBar> map;
	private static final LeftNavBarService service = new LeftNavBarService();

	private LeftNavBarService() {
		this.map = new HashMap<Integer, LeftNavBar>();
	}

	public static LeftNavBarService instance() {
		return service;
	}

	public LeftNavBar getLeftNavBar(Activity activity) {
		if (this.map.get(Integer.valueOf(activity.hashCode())) == null) {
			LeftNavBar leftNavBar = new LeftNavBar(activity);
			this.map.put(Integer.valueOf(activity.hashCode()), leftNavBar);
		}
		return (LeftNavBar) this.map.get(Integer.valueOf(activity.hashCode()));
	}
}
