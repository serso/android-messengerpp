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
