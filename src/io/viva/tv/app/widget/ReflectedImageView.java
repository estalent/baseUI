package io.viva.tv.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ReflectedImageView extends ImageView {
	private Bitmap mOriginBitmap;
	private int mImageReflectionRatio;
	private int mReflectionGap;
	private int mOriginImageWidth;
	private int mOriginImageHeight;

	public ReflectedImageView(Context context) {
		super(context);
	}

	public ReflectedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ReflectedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setImageBitmap(Bitmap bm) {
		this.mOriginBitmap = bm;
		Bitmap temp = createReflectedImages(bm);
		super.setImageBitmap(temp);
	}

	public Bitmap getOriginBitmap() {
		return this.mOriginBitmap;
	}

	public void setImageReflectionRatio(int imageReflectionRatio) {
		this.mImageReflectionRatio = imageReflectionRatio;
	}

	public void setReflectionGap(int reflectionGap) {
		this.mReflectionGap = reflectionGap;
	}

	public int getImageReflectionRatio() {
		return this.mImageReflectionRatio;
	}

	public int getReflectionGap() {
		return this.mReflectionGap;
	}

	public int getOriginImageWidth() {
		return this.mOriginImageWidth;
	}

	public void setOriginImageWidth(int originImageWidth) {
		this.mOriginImageWidth = originImageWidth;
	}

	public int getOriginImageHeight() {
		return this.mOriginImageHeight;
	}

	public void setOriginImageHeight(int originImageHeight) {
		this.mOriginImageHeight = originImageHeight;
	}

	public Bitmap createReflectedImages(Bitmap originalImage) {
		int width = this.mOriginImageWidth;
		int height = this.mOriginImageHeight;
		Matrix matrix = new Matrix();
		matrix.preScale(1.0F, -1.0F);
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height * this.mImageReflectionRatio, width, height - height * this.mImageReflectionRatio, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height + height * this.mImageReflectionRatio, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(originalImage, 0.0F, 0.0F, null);
		Paint deafaultPaint = new Paint();
		deafaultPaint.setColor(17170445);
		canvas.drawBitmap(reflectionImage, 0.0F, height + this.mReflectionGap, null);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0.0F, originalImage.getHeight(), 0.0F, bitmapWithReflection.getHeight() + this.mReflectionGap, 1895825407, 16777215, Shader.TileMode.CLAMP);

		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawRect(0.0F, height, width, bitmapWithReflection.getHeight() + this.mReflectionGap, paint);
		return bitmapWithReflection;
	}
}
