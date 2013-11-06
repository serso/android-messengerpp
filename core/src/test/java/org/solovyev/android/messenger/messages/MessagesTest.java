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

package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.solovyev.android.messenger.messages.Messages.compareSendDatesLatestFirst;
import static org.solovyev.android.messenger.messages.MessagesMock.newMockMessage;

public class MessagesTest {

	@Test
	public void testNullsShouldBeInTheEnd() throws Exception {
		final DateTime now = DateTime.now();
		final List<Message> messages = new ArrayList<Message>();
		for (int i = 0; i < 5; i++) {
			messages.add(newMockMessage(now.plusDays(i)));
		}

		for (int i = 5; i < 10; i++) {
			messages.add(null);
		}

		sort(messages, new Comparator<Message>() {
			@Override
			public int compare(Message lhs, Message rhs) {
				return compareSendDatesLatestFirst(lhs, rhs);
			}
		});

		for (int i = 1; i < 5; i++) {
			assertTrue(messages.get(i - 1).getSendDate().isAfter(messages.get(i).getSendDate()));
		}

		for (int i = 5; i < 10; i++) {
			assertNull(messages.get(i));
		}
	}

	@Test
	public void testDatesShouldBeStoredInDescendantOrder() throws Exception {
		final DateTime now = DateTime.now();
		final List<Message> messages = new ArrayList<Message>();
		for (int i = 0; i < 10; i++) {
			messages.add(newMockMessage(now.plusDays(i)));
		}

		sort(messages, new Comparator<Message>() {
			@Override
			public int compare(Message lhs, Message rhs) {
				return compareSendDatesLatestFirst(lhs, rhs);
			}
		});

		for (int i = 1; i < 10; i++) {
			assertTrue(messages.get(i - 1).getSendDate().isAfter(messages.get(i).getSendDate()));
		}
	}

	@Test
	public void testComparisonResultShouldBePositiveForLaterDate() throws Exception {
		final DateTime now = DateTime.now();
		final Message lm = newMockMessage(now);
		final Message rm = newMockMessage(now.plusDays(1));
		assertTrue(compareSendDatesLatestFirst(lm, rm) > 0);
	}

	@Test
	public void testComparisonResultShouldBeNegativeForEarlierDate() throws Exception {
		final DateTime now = DateTime.now();
		final Message lm = newMockMessage(now);
		final Message rm = newMockMessage(now.minusDays(1));
		assertTrue(compareSendDatesLatestFirst(lm, rm) < 0);
	}

}
