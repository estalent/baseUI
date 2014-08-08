package io.viva.tv.app;

public class StyleFetcher {
	public static int left_nav_bar_type = 0;

	public static void setStyle(int type) {
		left_nav_bar_type = type;
	}

	public static int getStyle() {
		return left_nav_bar_type;
	}
}
