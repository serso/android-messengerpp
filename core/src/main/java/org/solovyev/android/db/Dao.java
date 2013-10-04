package org.solovyev.android.db;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface Dao<E> {

	void create(@Nonnull E entity);

	@Nullable
	E read(@Nonnull String id);

	@Nonnull
	Collection<E> readAll();

	void update(@Nonnull E entity);

	void delete(@Nonnull E entity);

	void deleteById(@Nonnull String id);

	void deleteAll();
}
