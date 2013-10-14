package org.solovyev.common.collections.multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.solovyev.common.collections.multimap.ThreadSafeMultimap.newThreadSafeMultimap;

public class ObjectChangedMapUpdaterTest {

	@Nonnull
	private ThreadSafeMultimap<Integer, TestObject> multimap;

	@Nonnull
	private Map<Integer, List<TestObject>> map;

	@Before
	public void setUp() throws Exception {
		map = new HashMap<Integer, List<TestObject>>();
		for (int i = 0; i < 10; i++) {
			final List<TestObject> testObjects = new ArrayList<TestObject>();
			for (int j = 10 * i; j < 10 * i + 10; j++) {
				testObjects.add(new TestObject(i));
			}
			map.put(i, testObjects);
		}
		multimap = newThreadSafeMultimap(map);
	}

	@Test
	public void testShouldUpdateObject() throws Exception {
		final TestObject oldObject = map.get(5).get(5);
		final TestObject expected = new TestObject(oldObject.index);
		multimap.update(new ObjectChangedMapUpdater<Integer, TestObject>(expected));
		assertNotSame(oldObject, multimap.get(5).get(5));
		assertSame(expected, multimap.get(5).get(5));
	}

	@Test
	public void testShouldNotCopyAllListsIfObjectDoesntExist() throws Exception {
		final TestObject oldObject = new TestObject(10000);
		final List<TestObject> expected = multimap.asMap().get(5);
		multimap.update(new ObjectChangedMapUpdater<Integer, TestObject>(oldObject));
		final List<TestObject> actual = multimap.asMap().get(5);
		assertSame(expected, actual);
	}

	private final class TestObject {

		private final int index;

		private TestObject(int index) {
			this.index = index;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			TestObject that = (TestObject) o;

			if (index != that.index) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return index;
		}
	}

}
