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
