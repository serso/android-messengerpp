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

package org.solovyev.android;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;

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
