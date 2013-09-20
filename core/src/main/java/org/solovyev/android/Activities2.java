package org.solovyev.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/5/13
 * Time: 3:00 PM
 */
public final class Activities2 {

	private Activities2() {
		throw new AssertionError();
	}


	public static void startActivity(@Nonnull Context context, @Nonnull Intent intent) {
		if (!(context instanceof Activity)) {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		context.startActivity(intent);
	}
}
