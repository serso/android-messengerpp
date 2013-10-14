package org.solovyev.common.collections.multimap;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ObjectsAddedUpdaterTest extends ThreadSafeMultimapTest{

	@Test
	public void testShouldAddObjectsIfNotExist() throws Exception {
		final int sizeBefore = multimap.get(5).size();
		final TestObject oldObject = multimap.get(5).get(1);
		final TestObject newObject1 = new TestObject(111);
		final TestObject newObject2 = new TestObject(123);
		final List<TestObject> objectsToAdd = Arrays.asList(oldObject, newObject1, newObject2);

		multimap.update(5, new ObjectsAddedUpdater<TestObject>(objectsToAdd));

		final List<TestObject> actual = multimap.get(5);
		assertTrue(actual.contains(newObject1));
		assertTrue(actual.contains(newObject2));
		assertTrue(actual.contains(oldObject));
		assertEquals(sizeBefore + 2, actual.size());
	}

	@Test
	public void testShouldNotAddExistingObject() throws Exception {
		final List<TestObject> expected = multimap.asMap().get(5);

		multimap.update(5, new ObjectsAddedUpdater<TestObject>(Arrays.asList(multimap.get(5).get(3))));

		final List<TestObject> actual = multimap.asMap().get(5);
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}
}
