package org.solovyev.common.collections.multimap;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class ObjectChangedMapUpdaterTest extends ThreadSafeMultimapTest {

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

}
