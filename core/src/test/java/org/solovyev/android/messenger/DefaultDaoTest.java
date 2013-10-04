package org.solovyev.android.messenger;

import com.google.common.base.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.solovyev.android.db.Dao;
import org.solovyev.common.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static com.google.common.collect.Iterables.any;
import static org.junit.Assert.*;

public abstract class DefaultDaoTest<E> extends DefaultMessengerTestCase {

	@Nonnull
	private Dao<E> dao;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		dao = getDao();
	}

	@Nonnull
	protected abstract Dao<E> getDao();

	@Test
	public void testShouldInsertEntity() throws Exception {
		final E entity = insertEntity().entity;
		assertTrue(any(dao.readAll(), new SamePredicate(entity)));
	}

	@Nonnull
	private Entity<E> insertEntity() {
		final Entity<E> entity = newInsertEntity();
		dao.create(entity.entity);
		return entity;
	}

	@Test
	public void testShouldDeleteExistingEntity() throws Exception {
		// create entity
		final E entity = insertEntity().entity;
		try {
			dao.delete(entity);
			assertFalse(any(dao.readAll(), new EqualsPredicate(entity)));
		} catch (UnsupportedOperationException e) {
			onUnsupportedOperationException(e);
		}
	}

	private void onUnsupportedOperationException(@Nonnull UnsupportedOperationException e) {
		System.out.println("Delete test skipped due to following error at ");
		e.printStackTrace(System.out);
	}

	@Test
	public void testDeleteShouldNotAffectOtherEntities() throws Exception {
		final Collection<E> before = dao.readAll();
		final E entity = insertEntity().entity;
		try {
			dao.delete(entity);
			final Collection<E> after = dao.readAll();
			assertEntitiesSame(before, after);
		} catch (UnsupportedOperationException e) {
			onUnsupportedOperationException(e);
		}
	}

	@Test
	public void testReadAllShouldLoadAllEntities() throws Exception {
		final Collection<E> entities = populateEntities(dao);
		assertEntitiesSame(entities, dao.readAll());
	}

	@Test
	public void testShouldReadEntityById() throws Exception {
		final Entity<E> entity = insertEntity();
		assertEntitiesSame(entity.entity, dao.read(entity.id));
	}

	private void assertEntitiesSame(@Nonnull Collection<E> c1, @Nonnull Collection<E> c2) {
		assertEquals(c1.size(), c2.size());
		assertOneWaySame(c1, c2);
		assertOneWaySame(c2, c1);
	}

	private void assertOneWaySame(@Nonnull Collection<E> c1, @Nonnull Collection<E> c2) {
		for (E e1 : c1) {
			boolean found = false;
			for (E e2 : c2) {
				found = areSame(e1, e2);
				if (found) {
					break;
				}
			}

			assertTrue(found);
		}
	}

	private void assertEntitiesSame(@Nonnull E e1, @Nonnull E e2) {
		assertTrue(areSame(e1, e2));
	}

	protected boolean areSame(@Nonnull E e1, @Nonnull E e2) {
		return Objects.areEqual(e1, e2);
	}

	protected boolean areEqual(@Nonnull E e1, @Nonnull E e2) {
		return Objects.areEqual(e1, e2);
	}

	@Test
	public void testShouldUpdateEntity() throws Exception {
		final Entity<E> e1 = insertEntity();
		final Entity<E> e2 = newEntity(changeEntity(e1.entity), e1.id);
		dao.update(e2.entity);
		assertTrue(any(dao.readAll(), new SamePredicate(e2.entity)));
	}

	@Nonnull
	protected abstract Collection<E> populateEntities(@Nonnull Dao<E> dao);

	@Nonnull
	protected abstract Entity<E> newInsertEntity();

	@Nonnull
	protected abstract E changeEntity(@Nonnull E entity);

	protected static final class Entity<E> {
		@Nonnull
		private final E entity;

		@Nonnull
		private final String id;

		private Entity(@Nonnull E entity, @Nonnull String id) {
			this.entity = entity;
			this.id = id;
		}
	}

	@Nonnull
	protected static <E> Entity<E> newEntity(@Nonnull E entity, @Nonnull String id) {
		return new Entity<E>(entity, id);
	}

	private class SamePredicate implements Predicate<E> {

		@Nonnull
		private final E entity;

		public SamePredicate(@Nonnull E entity) {
			this.entity = entity;
		}

		@Override
		public boolean apply(@Nullable E e) {
			return e != null && areSame(e, entity);
		}
	}

	private class EqualsPredicate implements Predicate<E> {

		@Nonnull
		private final E entity;

		public EqualsPredicate(@Nonnull E entity) {
			this.entity = entity;
		}

		@Override
		public boolean apply(@Nullable E e) {
			return e != null && areEqual(e, entity);
		}
	}
}
