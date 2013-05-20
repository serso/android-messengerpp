package org.solovyev.android.messenger;

import android.content.Context;
import org.solovyev.android.tasks.ContextCallback;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:28 PM
 */
public abstract class MessengerContextCallback<C extends Context, V> implements ContextCallback<C, V> {

	@Override
	public void onFailure(@Nonnull C context, Throwable t) {
		MessengerApplication.getServiceLocator().getExceptionHandler().handleException(t);
	}
}
