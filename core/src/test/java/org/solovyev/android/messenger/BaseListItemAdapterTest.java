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

package org.solovyev.android.messenger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;
import static org.solovyev.android.messenger.BaseListItemAdapter.intToPositiveLong;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class BaseListItemAdapterTest {

	@Test
	public void testIdShouldBePositive() throws Exception {
		assertEquals(0, intToPositiveLong(0));
		assertEquals(1, intToPositiveLong(1));
		assertEquals(10000, intToPositiveLong(10000));
		assertEquals(Integer.MAX_VALUE, intToPositiveLong(Integer.MAX_VALUE));
		assertEquals((long) Integer.MAX_VALUE + 1, intToPositiveLong(-1));
		assertEquals((long) Integer.MAX_VALUE + 100, intToPositiveLong(-100));
		assertEquals((long) Integer.MAX_VALUE - Integer.MIN_VALUE - 1, intToPositiveLong(Integer.MIN_VALUE + 1));
		assertEquals((long) Integer.MAX_VALUE - Integer.MIN_VALUE, intToPositiveLong(Integer.MIN_VALUE));
	}

	void assertEquals(long expected, long actual) {
		assertTrue(actual >= 0);
		Assert.assertEquals(expected, actual);
	}
}
