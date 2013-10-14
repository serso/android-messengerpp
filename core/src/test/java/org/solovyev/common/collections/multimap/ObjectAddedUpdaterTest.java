package org.solovyev.common.collections.multimap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObjectAddedUpdaterTest extends ThreadSafeMultimapTest {

	@Test
	public void testShouldAddObjectIfNotExists() throws Exception {
		final TestObject addedObject = new TestObject(111);
		multimap.update(3, new ObjectAddedUpdater<TestObject>(addedObject));
		assertTrue(multimap.get(3).contains(addedObject));
	}

	@Test
	public void testShouldNotAddObjectIfAlreadyExists() throws Exception {
		int sizeBefore = multimap.get(3).size();
		final TestObject oldObject = multimap.get(3).get(0);
		multimap.update(3, new ObjectAddedUpdater<TestObject>(oldObject));
		assertEquals(sizeBefore, multimap.get(3).size());
	}
}
