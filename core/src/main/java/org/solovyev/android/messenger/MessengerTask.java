package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

import org.solovyev.tasks.NamedTask;

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
		App.getExceptionHandler().handleException(t);
	}

	@Nonnull
	@Override
	public final String getName() {
		return name;
	}

}
