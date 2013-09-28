package org.solovyev.android.messenger.messages;

import org.joda.time.DateTime;
import org.junit.Test;
import org.solovyev.android.messenger.chats.ChatMessage;

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
		final List<ChatMessage> messages = new ArrayList<ChatMessage>();
		for(int i = 0; i < 5; i++) {
			messages.add(newMockMessage(now.plusDays(i)));
		}

		for(int i = 5; i < 10; i++) {
			messages.add(null);
		}

		sort(messages, new Comparator<ChatMessage>() {
			@Override
			public int compare(ChatMessage lhs, ChatMessage rhs) {
				return compareSendDatesLatestFirst(lhs, rhs);
			}
		});

		for(int i = 1; i < 5; i++) {
			assertTrue(messages.get(i - 1).getSendDate().isAfter(messages.get(i).getSendDate()));
		}

		for(int i = 5; i < 10; i++) {
			assertNull(messages.get(i));
		}
	}

	@Test
	public void testDatesShouldBeStoredInDescendantOrder() throws Exception {
		final DateTime now = DateTime.now();
		final List<ChatMessage> messages = new ArrayList<ChatMessage>();
		for(int i = 0; i < 10; i++) {
			messages.add(newMockMessage(now.plusDays(i)));
		}

		sort(messages, new Comparator<ChatMessage>() {
			@Override
			public int compare(ChatMessage lhs, ChatMessage rhs) {
				return compareSendDatesLatestFirst(lhs, rhs);
			}
		});

		for(int i = 1; i < 10; i++) {
			assertTrue(messages.get(i - 1).getSendDate().isAfter(messages.get(i).getSendDate()));
		}
	}

	@Test
	public void testComparisonResultShouldBePositiveForLaterDate() throws Exception {
		final DateTime now = DateTime.now();
		final ChatMessage lm = newMockMessage(now);
		final ChatMessage rm = newMockMessage(now.plusDays(1));
		assertTrue(compareSendDatesLatestFirst(lm, rm) > 0);
	}

	@Test
	public void testComparisonResultShouldBeNegativeForEarlierDate() throws Exception {
		final DateTime now = DateTime.now();
		final ChatMessage lm = newMockMessage(now);
		final ChatMessage rm = newMockMessage(now.minusDays(1));
		assertTrue(compareSendDatesLatestFirst(lm, rm) < 0);
	}

}
