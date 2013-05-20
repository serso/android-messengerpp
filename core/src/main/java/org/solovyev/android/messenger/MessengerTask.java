package org.solovyev.android.messenger;

import org.solovyev.tasks.NamedTask;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 1:23 PM
 */
public abstract class MessengerTask<V> implements NamedTask<V> {

	@Nonnull
	private final String name;

	protected MessengerTask(@Nonnull String name) {
		this.name = name;
	}

	@Override
	public void onFailure(Throwable t) {
		MessengerApplication.getServiceLocator().getExceptionHandler().handleException(t);
	}

	@Nonnull
	@Override
	public final String getName() {
		return name;
	}

}
