package net.cattaka.android.humitemp4ble.util;

import android.graphics.Canvas;
import android.graphics.Paint;

public class CtkCanvasUtil {
	public enum Gravity {
		GRAVITY_LEFT,
		GRAVITY_RIGHT,
		GRAVITY_TOP,
		GRAVITY_BOTTOM,
		GRAVITY_CENTER
	}
	
	public static void drawText(Canvas canvas, Paint paint, float x, float y, String text, Gravity vertical, Gravity horizontal) {
		float sizeX = paint.measureText(text);
		float sizeY = paint.getTextSize();
		float tx = x;
		float ty = y;
		switch (vertical) {
		case GRAVITY_TOP:
			ty += sizeY;
			break;
		case GRAVITY_CENTER:
			ty += sizeY / 2f;
			break;
		case GRAVITY_BOTTOM:
			// OK
			break;
		}
		switch (horizontal) {
		case GRAVITY_RIGHT:
			tx -= sizeX;
			break;
		case GRAVITY_CENTER:
			tx -= sizeX / 2f;
			break;
		case GRAVITY_LEFT:
			// OK
			break;
		}
		
		canvas.drawText(text, tx, ty, paint);
	}
}
