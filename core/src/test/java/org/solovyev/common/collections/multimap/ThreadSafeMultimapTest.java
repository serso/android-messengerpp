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

import org.junit.Before;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.solovyev.common.collections.multimap.ThreadSafeMultimap.newThreadSafeMultimap;

public abstract class ThreadSafeMultimapTest {


	@Nonnull
	protected ThreadSafeMultimap<Integer, TestObject> multimap;

	@Nonnull
	protected Map<Integer, List<TestObject>> map;

	@Before
	public void setUp() throws Exception {
		map = new HashMap<Integer, List<TestObject>>();
		for (int i = 0; i < 10; i++) {
			final List<TestObject> testObjects = new ArrayList<TestObject>();
			for (int j = 10 * i; j < 10 * i + 10; j++) {
				testObjects.add(new TestObject(j));
			}
			map.put(i, testObjects);
		}
		multimap = newThreadSafeMultimap(map);
	}

	protected static final class TestObject {

		final int index;

		TestObject(int index) {
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
