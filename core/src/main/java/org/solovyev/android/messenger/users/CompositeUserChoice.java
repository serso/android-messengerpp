package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;

public final class CompositeUserChoice {

	@Nonnull
	private final CharSequence name;

	private final int id;

	private CompositeUserChoice(@Nonnull CharSequence name, int id) {
		this.name = name;
		this.id = id;
	}

	@Nonnull
	public static CompositeUserChoice newInstance(@Nonnull CharSequence name, int id) {
		return new CompositeUserChoice(name, id);
	}

	@Nonnull
	public CharSequence getName() {
		return name;
	}

	public int getId() {
		return id;
	}
}
