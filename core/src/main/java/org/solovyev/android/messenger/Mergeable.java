package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

public interface Mergeable<E> {

	@Nonnull
	E merge(@Nonnull E that);
}
