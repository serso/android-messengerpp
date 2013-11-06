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

import java.util.List;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class ObjectsChangedMapUpdaterTest extends ThreadSafeMultimapTest {

	@Test
	public void testShouldUpdateObject() throws Exception {
		final TestObject oldObject = map.get(5).get(5);
		final TestObject expected = new TestObject(oldObject.index);
		multimap.update(new ObjectsChangedMapUpdater<Integer, TestObject>(expected));
		assertNotSame(oldObject, multimap.get(5).get(5));
		assertSame(expected, multimap.get(5).get(5));
	}

	@Test
	public void testShouldNotCopyAllListsIfObjectDoesntExist() throws Exception {
		final TestObject oldObject = new TestObject(10000);
		final List<TestObject> expected = multimap.asMap().get(5);
		multimap.update(new ObjectsChangedMapUpdater<Integer, TestObject>(oldObject));
		final List<TestObject> actual = multimap.asMap().get(5);
		assertSame(expected, actual);
	}

}
