package org.solovyev.android;

import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class Views2 {

	private static final String TAG = "Views";

	@Nullable
	private static Field startMarginField;

	@Nullable
	private static Field endMarginField;

	static {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			try {
				startMarginField = getField(ViewGroup.MarginLayoutParams.class, "startMargin");
				endMarginField = getField(ViewGroup.MarginLayoutParams.class, "endMargin");
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	}

	public static void setMarginStart(ViewGroup.MarginLayoutParams params, int value) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			params.setMarginStart(value);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (startMarginField != null) {
				setField(params, startMarginField, value);
			}
		}
	}

	public static void setMarginEnd(ViewGroup.MarginLayoutParams params, int value) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			params.setMarginEnd(value);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (endMarginField != null) {
				setField(params, endMarginField, value);
			}
		}
	}

	private static boolean setField(Object object, String fieldName, Object value) {
		if (object == null) {
			return false;
		}
		final Field field = getField(object.getClass(), fieldName);
		return setField(object, field, value);
	}

	private static boolean setField(Object object, Field field, Object value) {
		if (field != null) {
			try {
				field.set(object, value);
				return true;
			} catch (IllegalAccessException e) {
				Log.e(TAG, e.getMessage(), e);
			} catch (ExceptionInInitializerError e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		return false;
	}

	private static Field getField(Class<?> clazz, String fieldName) {
		for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
			try {
				final Field field = c.getDeclaredField(fieldName);
				if (field != null) {
					field.setAccessible(true);
					return field;
				}
			} catch (NoSuchFieldException e) {
				// continue
			} catch (SecurityException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		return null;
	}
}
