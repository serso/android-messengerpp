package org.solovyev.android.messenger;

import android.content.Context;

import javax.annotation.Nonnull;

import org.solovyev.android.tasks.NamedContextTask;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 10:23 PM
 */
public abstract class MessengerContextTask<C extends Context, V> extends MessengerContextCallback<C, V> implements NamedContextTask<C, V> {

	@Nonnull
	private final String name;

	protected MessengerContextTask(@Nonnull String name) {
		this.name = name;
	}

	@Nonnull
	@Override
	public final String getName() {
		return name;
	}
}
