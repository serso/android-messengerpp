/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.view;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public abstract class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/

	private static final String TAG = SwipeGestureListener.class.getSimpleName();

	// NOTE: float type in order to avoid float cast in calculations
	private static final float MIN_DISTANCE = 80f;
	private static final float MAX_OFF_PATH = 250f;
	private static final float THRESHOLD_VELOCITY = 100f;

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	private final int minDistance;
	private final int maxOffPath;
	private final int thresholdVelocity;


	public SwipeGestureListener(Activity activity) {
		final DisplayMetrics dm = activity.getResources().getDisplayMetrics();

		minDistance = (int) (MIN_DISTANCE * dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		maxOffPath = (int) (MAX_OFF_PATH * dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		thresholdVelocity = (int) (THRESHOLD_VELOCITY * dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	@Override
	public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		boolean result = false;

		try {
			final float dy = e1.getY() - e2.getY();
			if (Math.abs(dy) <= maxOffPath) {
				final boolean velocityOk = Math.abs(velocityX) > thresholdVelocity;
				final float dx = e1.getX() - e2.getX();

				if (velocityOk) {
					if (dx > minDistance) {
						onSwipeToLeft();
						result = true;
					} else if (dx < -minDistance) {
						onSwipeToRight();
						result = true;
					}
				}
			}
		} catch (Exception e) {
			Log.w(TAG, e.getMessage(), e);
		}

		return result;
	}

	protected abstract void onSwipeToRight();

	protected abstract void onSwipeToLeft();
}
