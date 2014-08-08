package io.viva.tv.app.animation;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.GridLayoutAnimationController;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtils {
	private static final int TOGETHER = 0;
	private static final int SEQUENTIALLY = 1;
	private static HashMap<Integer, Interpolator> s_cachedInterpolator = new HashMap<Integer, Interpolator>();

	public static long currentAnimationTimeMillis() {
		return SystemClock.uptimeMillis();
	}

	public static Animation loadAnimation(Context context, int id) throws Resources.NotFoundException {
		XmlResourceParser parser = null;
		try {
			parser = context.getResources().getAnimation(id);
			return createAnimationFromXml(context, parser);
		} catch (XmlPullParserException ex) {
			Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));

			rnf.initCause(ex);
			throw rnf;
		} catch (IOException ex) {
			Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));

			rnf.initCause(ex);
			throw rnf;
		} finally {
			if (parser != null)
				parser.close();
		}
	}

	private static Animation createAnimationFromXml(Context c, XmlPullParser parser) throws XmlPullParserException, IOException {
		return createAnimationFromXml(c, parser, null, Xml.asAttributeSet(parser));
	}

	private static Animation createAnimationFromXml(Context c, XmlPullParser parser, AnimationSet parent, AttributeSet attrs) throws XmlPullParserException, IOException {
		Animation anim = null;

		int depth = parser.getDepth();
		int type;
		while ((((type = parser.next()) != 3) || (parser.getDepth() > depth)) && (type != 1)) {
			if (type == 2) {
				String name = parser.getName();

				if (name.equals("set")) {
					anim = new AnimationSet(c, attrs);
					createAnimationFromXml(c, parser, (AnimationSet) anim, attrs);
				} else if (name.equals("alpha")) {
					anim = new AlphaAnimation(c, attrs);
				} else if (name.equals("scale")) {
					anim = new ScaleAnimation(c, attrs);
				} else if (name.equals("rotate")) {
					anim = new RotateAnimation(c, attrs);
				} else if (name.equals("translate")) {
					anim = new TranslateAnimation(c, attrs);
				} else {
					throw new RuntimeException("Unknown animation name: " + parser.getName());
				}

				if (parent != null) {
					parent.addAnimation(anim);
				}
			}
		}
		return anim;
	}

	public static LayoutAnimationController loadLayoutAnimation(Context context, int id) throws Resources.NotFoundException {
		XmlResourceParser parser = null;
		try {
			parser = context.getResources().getAnimation(id);
			return createLayoutAnimationFromXml(context, parser);
		} catch (XmlPullParserException ex) {
			Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));

			rnf.initCause(ex);
			throw rnf;
		} catch (IOException ex) {
			Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));

			rnf.initCause(ex);
			throw rnf;
		} finally {
			if (parser != null)
				parser.close();
		}
	}

	private static LayoutAnimationController createLayoutAnimationFromXml(Context c, XmlPullParser parser) throws XmlPullParserException, IOException {
		return createLayoutAnimationFromXml(c, parser, Xml.asAttributeSet(parser));
	}

	private static LayoutAnimationController createLayoutAnimationFromXml(Context c, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
		LayoutAnimationController controller = null;

		int depth = parser.getDepth();
		int type;
		while ((((type = parser.next()) != 3) || (parser.getDepth() > depth)) && (type != 1)) {
			if (type == 2) {
				String name = parser.getName();

				if ("layoutAnimation".equals(name))
					controller = new LayoutAnimationController(c, attrs);
				else if ("gridLayoutAnimation".equals(name))
					controller = new GridLayoutAnimationController(c, attrs);
				else {
					throw new RuntimeException("Unknown layout animation name: " + name);
				}
			}
		}

		return controller;
	}

	public static Animation makeInAnimation(Context c, boolean fromLeft) {
		Animation a;
		if (fromLeft) {
			a = loadAnimation(c, 17432578);
		} else {
			a = loadAnimation(c, 17432663);
		}

		a.setInterpolator(new DecelerateInterpolator());
		a.setStartTime(currentAnimationTimeMillis());
		return a;
	}

	public static Animation makeOutAnimation(Context c, boolean toRight) {
		Animation a;
		if (toRight) {
			a = loadAnimation(c, 17432579);
		} else {
			a = loadAnimation(c, 17432666);
		}

		a.setInterpolator(new AccelerateInterpolator());
		a.setStartTime(currentAnimationTimeMillis());
		return a;
	}

	public static Animation makeInChildBottomAnimation(Context c) {
		Animation a = loadAnimation(c, 17432662);

		a.setInterpolator(new AccelerateInterpolator());
		a.setStartTime(currentAnimationTimeMillis());
		return a;
	}

	public static Interpolator loadInterpolator(Context context, int id) throws Resources.NotFoundException {
		Interpolator foundInterpolator = (Interpolator) s_cachedInterpolator.get(Integer.valueOf(id));

		if (null == foundInterpolator) {
			XmlResourceParser parser = null;
			try {
				parser = context.getResources().getAnimation(id);
				foundInterpolator = createInterpolatorFromXml(context, parser);
				s_cachedInterpolator.put(Integer.valueOf(id), foundInterpolator);
			} catch (XmlPullParserException ex) {
				Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));

				rnf.initCause(ex);
				throw rnf;
			} catch (IOException ex) {
				Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));

				rnf.initCause(ex);
				throw rnf;
			} finally {
				if (parser != null) {
					parser.close();
				}
			}
		}
		return foundInterpolator;
	}

	private static Interpolator createInterpolatorFromXml(Context c, XmlPullParser parser) throws XmlPullParserException, IOException {
		Interpolator interpolator = null;

		int depth = parser.getDepth();
		int type;
		while ((((type = parser.next()) != 3) || (parser.getDepth() > depth)) && (type != 1)) {
			if (type == 2) {
				AttributeSet attrs = Xml.asAttributeSet(parser);

				String name = parser.getName();

				if (name.equals("linearInterpolator"))
					interpolator = new LinearInterpolator(c, attrs);
				else if (name.equals("accelerateInterpolator"))
					interpolator = new AccelerateInterpolator(c, attrs);
				else if (name.equals("decelerateInterpolator"))
					interpolator = new DecelerateInterpolator(c, attrs);
				else if (name.equals("accelerateDecelerateInterpolator"))
					interpolator = new AccelerateDecelerateInterpolator(c, attrs);
				else if (name.equals("cycleInterpolator"))
					interpolator = new CycleInterpolator(c, attrs);
				else if (name.equals("anticipateInterpolator"))
					interpolator = new AnticipateInterpolator(c, attrs);
				else if (name.equals("overshootInterpolator"))
					interpolator = new OvershootInterpolator(c, attrs);
				else if (name.equals("anticipateOvershootInterpolator"))
					interpolator = new AnticipateOvershootInterpolator(c, attrs);
				else if (name.equals("bounceInterpolator"))
					interpolator = new BounceInterpolator(c, attrs);
				else if (name.equals("circInInterpolator"))
					interpolator = new CircInInterpolator(c, attrs);
				else if (name.equals("circInOutInterpolator"))
					interpolator = new CircInOutInterpolator(c, attrs);
				else if (name.equals("circOutInterpolator"))
					interpolator = new CircOutInterpolator(c, attrs);
				else {
					throw new RuntimeException("Unknown interpolator name: " + parser.getName());
				}
			}

		}

		return interpolator;
	}
}
