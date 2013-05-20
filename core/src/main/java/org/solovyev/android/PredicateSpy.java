package org.solovyev.android;

import com.google.common.base.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 10:48 PM
 */
public final class PredicateSpy<T> implements Predicate<T> {

	@Nonnull
	private final Predicate<T> predicate;

	@Nonnull
	private final Collection<T> appliedElements;

	private PredicateSpy(@Nonnull Predicate<T> predicate, @Nonnull Collection<T> appliedElements) {
		this.predicate = predicate;
		this.appliedElements = appliedElements;
	}

	@Nonnull
	public static <T> PredicateSpy<T> spyOn(@Nonnull Predicate<T> predicate, @Nonnull Collection<T> appliedElements) {
		return new PredicateSpy<T>(predicate, appliedElements);
	}

	@Override
	public boolean apply(@Nullable T input) {
		boolean applied = predicate.apply(input);
		if (applied) {
			appliedElements.add(input);
		}
		return applied;
	}
}
