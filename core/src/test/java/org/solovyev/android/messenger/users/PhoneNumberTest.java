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

package org.solovyev.android.messenger.users;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;
import static org.solovyev.android.messenger.users.PhoneNumber.newPhoneNumber;

@RunWith(RobolectricTestRunner.class)
public class PhoneNumberTest {

	@Test
	public void testShouldBeTheSame() throws Exception {
		assertTrue(newPhoneNumber("345-67-89").same(newPhoneNumber("3456789")));
		assertTrue(newPhoneNumber("+345  67-89").same(newPhoneNumber("+345  6789")));
	}
}
