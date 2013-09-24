package org.solovyev.android.messenger;

import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.tasks.ContextCallback;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:28 PM
 */
public abstract class MessengerContextCallback<C extends Context, V> implements ContextCallback<C, V> {

	@Override
	public void onFailure(@Nonnull C context, Throwable t) {
		App.getExceptionHandler().handleException(t);
	}
}
