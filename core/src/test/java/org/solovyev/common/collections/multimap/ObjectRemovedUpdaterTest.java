package org.solovyev.common.collections.multimap;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

public class ObjectRemovedUpdaterTest extends ThreadSafeMultimapTest {

	@Test
	public void testShouldRemoveObjectIfExists() throws Exception {
		final TestObject oldObject = multimap.get(3).get(2);
		multimap.update(3, new ObjectRemovedUpdater<TestObject>(oldObject));
		assertFalse(multimap.get(3).contains(oldObject));
	}

	@Test
	public void testShouldLeaveOldListIfObjectDoesntExist() throws Exception {
		final List<TestObject> listBefore = multimap.asMap().get(3);
		multimap.update(3, new ObjectRemovedUpdater<TestObject>(new TestObject(111)));
		final List<TestObject> listAfter = multimap.asMap().get(3);
		assertSame(listBefore, listAfter);
	}
}
