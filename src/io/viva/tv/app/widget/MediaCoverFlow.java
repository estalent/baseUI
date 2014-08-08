package io.viva.tv.app.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;

public class MediaCoverFlow extends CoverFlow {
	private static final String TAG = "MediaCoverFlow";
	private static final boolean DEBUG = false;
	private static final int DEFAULT_MID_ITEM_COUNT = 3;
	private int mMidItemCount = 3;

	public MediaCoverFlow(Context context) {
		super(context);
	}

	public MediaCoverFlow(Context context, AttributeSet attrs) {
		super(context, attrs, 16842864);
	}

	public MediaCoverFlow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected boolean getChildStaticTransformation(View child, Transformation t) {
		transformChidMatrix(child, t);
		return true;
	}

	public void setMidItemCount(int num) {
		if (num <= 0) {
			num = 1;
		}

		if ((num & 0x1) != 1) {
			num++;
		}

		this.mMidItemCount = num;
	}

	private void transformChidMatrixMidItemCount1(View child, Transformation t) {
		int childCenter = getCenterOfView(child);

		int childWidth = child.getWidth();

		int rotationAngle = 0;

		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		if (childCenter == this.mCoveflowCenter) {
			transformImageBitmap(child, t, 0);
		} else {
			if (this.mCoveflowCenter - childCenter > 0) {
				rotationAngle = (int) ((this.mCoveflowCenter - childCenter) / childWidth * this.mMaxRotationAngle / 2.0F);
			} else {
				rotationAngle = (int) ((this.mCoveflowCenter - childCenter) / childWidth * this.mMaxRotationAngle / 2.0F);
			}

			if (Math.abs(rotationAngle) > this.mMaxRotationAngle) {
				rotationAngle = rotationAngle < 0 ? -this.mMaxRotationAngle : this.mMaxRotationAngle;
			}

			transformImageBitmap(child, t, rotationAngle);

			int childDistance = Math.abs(childCenter - this.mCoveflowCenter);

			float distanceRotate = 1 * (childWidth + this.mSpacing);
			float distanceShift = 2 * (childWidth + this.mSpacing);

			float shiftArea = childWidth / 1.6F;

			if (this.mCoveflowCenter - childCenter > 0) {
				if ((childDistance >= distanceRotate) && (childDistance < distanceShift)) {
					Matrix matrix = t.getMatrix();

					float radio = 1.0F * (childDistance - distanceRotate) / (childWidth + this.mSpacing);

					matrix.postTranslate(shiftArea * radio, 0.0F);
				} else if (childDistance >= distanceShift) {
					Matrix matrix = t.getMatrix();

					float radio = 1.0F * (childDistance - distanceRotate) / (childWidth + this.mSpacing);

					matrix.postTranslate(shiftArea * radio, 0.0F);
				}
			} else if ((childDistance >= distanceRotate) && (childDistance <= distanceShift)) {
				Matrix matrix = t.getMatrix();
				float radio = 1.0F * (childDistance - distanceRotate) / (childWidth + this.mSpacing);

				matrix.postTranslate(-shiftArea * radio, 0.0F);
			} else if (childDistance >= distanceShift) {
				Matrix matrix = t.getMatrix();
				float radio = 1.0F * (childDistance - distanceRotate) / (childWidth + this.mSpacing);

				matrix.postTranslate(-shiftArea * radio, 0.0F);
			}
		}
	}

	private void transformChidMatrixMidItemCount3(View child, Transformation t) {
		int childCenter = getCenterOfView(child);
		int childWidth = child.getWidth();
		int rotationAngle = 0;
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		int notTransformLen = childWidth + this.mSpacing;
		if (Math.abs(childCenter - this.mCoveflowCenter) <= notTransformLen) {
			transformImageBitmap(child, t, 0);
		} else {
			if (this.mCoveflowCenter - childCenter > 0) {
				rotationAngle = (int) ((this.mCoveflowCenter - childCenter - notTransformLen) / childWidth * this.mMaxRotationAngle / 2.0F);
			} else {
				rotationAngle = (int) ((this.mCoveflowCenter - childCenter + notTransformLen) / childWidth * this.mMaxRotationAngle / 2.0F);
			}

			if (Math.abs(rotationAngle) > this.mMaxRotationAngle) {
				rotationAngle = rotationAngle < 0 ? -this.mMaxRotationAngle : this.mMaxRotationAngle;
			}

			transformImageBitmap(child, t, rotationAngle);

			int childDistance = Math.abs(childCenter - this.mCoveflowCenter);

			float distanceRotate = 2 * (childWidth + this.mSpacing);
			float distanceShift = 3 * (childWidth + this.mSpacing);

			float shiftArea = childWidth / 1.6F;

			if (this.mCoveflowCenter - childCenter > 0) {
				if ((childDistance >= distanceRotate) && (childDistance < distanceShift)) {
					Matrix matrix = t.getMatrix();

					float radio = 1.0F * (childDistance - distanceRotate) / (childWidth + this.mSpacing);

					matrix.postTranslate(shiftArea * radio, 0.0F);
				} else if (childDistance >= distanceShift) {
					Matrix matrix = t.getMatrix();
					matrix.postTranslate(shiftArea, 0.0F);
				}
			} else if ((childDistance >= distanceRotate) && (childDistance <= distanceShift)) {
				Matrix matrix = t.getMatrix();
				float radio = 1.0F * (childDistance - distanceRotate) / (childWidth + this.mSpacing);

				matrix.postTranslate(-shiftArea * radio, 0.0F);
			} else if (childDistance >= distanceShift) {
				Matrix matrix = t.getMatrix();
				matrix.postTranslate(-shiftArea, 0.0F);
			}
		}
	}

	private void transformChidMatrixMidItemCount5(View child, Transformation t) {
		int childCenter = getCenterOfView(child);
		int childWidth = child.getWidth();
		int rotationAngle = 0;
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);

		int notTransformLen = 2 * (childWidth + this.mSpacing);

		if (Math.abs(childCenter - this.mCoveflowCenter) <= notTransformLen) {
			transformImageBitmap(child, t, 0);
		} else {
			if (this.mCoveflowCenter - childCenter > 0) {
				rotationAngle = (int) ((this.mCoveflowCenter - childCenter - notTransformLen) / childWidth * this.mMaxRotationAngle / 2.0F);
			} else {
				rotationAngle = (int) ((this.mCoveflowCenter - childCenter + notTransformLen) / childWidth * this.mMaxRotationAngle / 2.0F);
			}

			if (Math.abs(rotationAngle) > this.mMaxRotationAngle) {
				rotationAngle = rotationAngle < 0 ? -this.mMaxRotationAngle : this.mMaxRotationAngle;
			}

			transformImageBitmap(child, t, rotationAngle);

			int childDistance = Math.abs(childCenter - this.mCoveflowCenter);

			float distanceRotate = 3 * (childWidth + this.mSpacing);
			float distanceShift = 4 * (childWidth + this.mSpacing);

			float shiftArea = childWidth / 1.6F;

			if (this.mCoveflowCenter - childCenter > 0) {
				if ((childDistance >= distanceRotate) && (childDistance < distanceShift)) {
					Matrix matrix = t.getMatrix();

					float radio = 1.0F * (childDistance - distanceRotate) / (childWidth + this.mSpacing);

					matrix.postTranslate(shiftArea * radio, 0.0F);
				} else if (childDistance >= distanceShift) {
					Matrix matrix = t.getMatrix();
					matrix.postTranslate(shiftArea, 0.0F);
				}
			} else if ((childDistance >= distanceRotate) && (childDistance <= distanceShift)) {
				Matrix matrix = t.getMatrix();
				float radio = 1.0F * (childDistance - distanceRotate) / (childWidth + this.mSpacing);

				matrix.postTranslate(-shiftArea * radio, 0.0F);
			} else if (childDistance >= distanceShift) {
				Matrix matrix = t.getMatrix();
				matrix.postTranslate(-shiftArea, 0.0F);
			}
		}
	}

	private void transformChidMatrix(View child, Transformation t) {
		if (this.mMidItemCount == 5)
			transformChidMatrixMidItemCount5(child, t);
		else if (this.mMidItemCount == 3)
			transformChidMatrixMidItemCount3(child, t);
		else
			transformChidMatrixMidItemCount1(child, t);
	}
}
