package org.solovyev.android.messenger;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.CollectionEqualizer;
import org.solovyev.common.equals.Equalizer;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class DefaultLinkedEntitiesDaoTest<E extends Identifiable> extends DefaultMessengerTest {

	@Nullable
	private final Equalizer<E> equalsEqualizer;

	@Nullable
	private final Equalizer<E> sameEqualizer;

	@Nonnull
	private LinkedEntitiesDao<E> dao;

	protected DefaultLinkedEntitiesDaoTest(@Nullable Equalizer<E> equalsEqualizer, @Nullable Equalizer<E> sameEqualizer) {
		this.equalsEqualizer = equalsEqualizer;
		this.sameEqualizer = sameEqualizer;
	}

	protected DefaultLinkedEntitiesDaoTest(@Nullable Equalizer<E> sameEqualizer) {
		this(null, sameEqualizer);
	}

	protected DefaultLinkedEntitiesDaoTest() {
		this(null, null);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		dao = getDao();
	}

	@Nonnull
	protected abstract LinkedEntitiesDao<E> getDao();

	@Test
	public void testShouldLoadLinkedEntityIds() throws Exception {
		final Collection<String> linkedIds = dao.readLinkedEntityIds(getId());
		assertEquals(getLinkedIds(), linkedIds);
		assertFalse(linkedIds.isEmpty());
	}

	@Test
	public void testMerge() throws Exception {
		final AccountData ad = getAccountData1();
		final List<E> addedEntities = newArrayList();
		for(int i = 0; i < 5; i++) {
			addedEntities.add(newLinkedEntity(ad, i));
		}

		final List<String> removedEntityIds = newArrayList();
		final List<E> entitiesFromDb = getLinkedEntities(ad);
		removedEntityIds.add(entitiesFromDb.remove(0).getId());
		removedEntityIds.add(entitiesFromDb.remove(0).getId());

		final List<E> entities = newArrayList(entitiesFromDb);
		entities.addAll(addedEntities);
		shuffle(entities);

		final MergeDaoResult<E, String> result = dao.mergeLinkedEntities(getId(), entities, true, true);

		assertEntitiesSame(entitiesFromDb, result.getUpdatedObjects());
		assertEntitiesSame(addedEntities, result.getAddedObjects());
		// removal now is not supported
		// assertEquals(removedEntityIds, result.getRemovedObjectIds());
	}

	@Nonnull
	protected abstract E newLinkedEntity(@Nonnull AccountData ad, int i);

	@Nonnull
	protected abstract List<E> getLinkedEntities(@Nonnull AccountData ad);

	@Nonnull
	protected abstract String getId();

	@Nonnull
	protected abstract Collection<String> getLinkedIds();

	/*
	**********************************************************************
	*
	*                           COMPARISON
	*
	**********************************************************************
	*/

	protected final void assertEntitiesSame(@Nonnull Collection<E> c1, @Nonnull Collection<E> c2) {
		Assert.assertTrue(Objects.areEqual(c1, c2, new CollectionEqualizer<E>(sameEqualizer)));
	}

	protected final void assertEntitiesEqual(@Nonnull Collection<E> c1, @Nonnull Collection<E> c2) {
		Assert.assertTrue(Objects.areEqual(c1, c2, new CollectionEqualizer<E>(equalsEqualizer)));
	}

	protected final void assertEntitiesSame(@Nonnull E e1, @Nonnull E e2) {
		assertTrue(areSame(e1, e2));
	}

	protected final void assertEntitiesEqual(@Nonnull E e1, @Nonnull E e2) {
		assertTrue(areEqual(e1, e2));
	}

	protected final boolean areSame(@Nonnull E e1, @Nonnull E e2) {
		return Objects.areEqual(e1, e2, sameEqualizer);
	}

	protected final boolean areEqual(@Nonnull E e1, @Nonnull E e2) {
		return Objects.areEqual(e1, e2, equalsEqualizer);
	}

	/*
	**********************************************************************
	*
	*                           STATIC/INNER
	*
	**********************************************************************
	*/
}
