package org.solovyev.android.messenger.messages;

import java.util.Arrays;
import java.util.Random;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.solovyev.android.messenger.DefaultMessengerTest;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.solovyev.android.messenger.chats.ChatEventType.message_added;
import static org.solovyev.android.messenger.chats.ChatEventType.message_added_batch;
import static org.solovyev.android.messenger.chats.ChatEventType.message_changed;
import static org.solovyev.android.messenger.chats.ChatEventType.message_state_changed;
import static org.solovyev.android.messenger.chats.ChatEventType.user_is_not_typing;
import static org.solovyev.android.messenger.chats.ChatEventType.user_is_typing;
import static org.solovyev.android.messenger.messages.Messages.newOutgoingMessage;

public class MessagesAdapterTest extends DefaultMessengerTest {
	
	@Nonnull
	private MessagesAdapter adapter;

	@Nonnull
	private AccountData accountData;

	@Nonnull
	private AccountChat chat;

	@Nonnull
	private User contact;

	@Nonnull
	private final Random r = new Random(currentTimeMillis());

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		accountData = getAccountData1();
		chat = accountData.getChats().get(0);
		contact = chat.getParticipantsExcept(accountData.getAccount().getUser()).get(0);
		adapter = new MessagesAdapter(getApplication(), accountData.getAccount().getUser(), chat.getChat(), MessageListItemStyle.newFromDefaultPreferences(getApplication())){
			@Nonnull
			@Override
			String getTypingMessageBody() {
				return "User is typing...";
			}
		};
	}

	@Test
	public void testShouldAlwaysBeNoMoreThanOneTypingMessage() throws Exception {
		fireRandomEventsAndCheck(true, new OnlyOneTypingEventChecker());
		fireRandomEventsAndCheck(false, new OnlyOneTypingEventChecker());
	}

	@Test
	public void testShouldRemoveSendingEventsWhenMessageIsSent() throws Exception {
		final MutableMessage sentMessage = newOutgoingMessage(accountData.getAccount(), chat.getChat(), "test", "");
		adapter.addSendingMessage(sentMessage);
		assertEquals(1, adapter.getCount());

		final MutableMessage message = Messages.copySentMessage(sentMessage, accountData.getAccount(), "test_1");
		adapter.onEvent(message_added.newEvent(chat.getChat(), message));

		assertEquals(1, adapter.getCount());
		assertSame(message, adapter.getItem(0).getMessage());
	}

	private void fireRandomEventsAndCheck(boolean sendStopTypingEvent, @Nonnull Runnable checker) throws InterruptedException {
		for(int i = 0; i < 100; i++) {
			Thread.sleep(r.nextInt(10));

			final ChatEvent event;
			switch (r.nextInt(5)){
				case 0:
					event = message_added.newEvent(chat.getChat(), newMessage(i));
					break;
				case 1:
					event = message_added_batch.newEvent(chat.getChat(), Arrays.asList(newMessage(i)));
					break;
				case 2:
					final int position = r.nextInt(max(1, adapter.getCount()));
					if(position < adapter.getCount()) {
						final Message message = adapter.getItem(position).getMessage();
						if (r.nextBoolean()) {
							final MessageState newState = MessageState.values()[r.nextInt(MessageState.values().length)];
							event = message_state_changed.newEvent(chat.getChat(), message.cloneWithNewState(newState));
						} else {
							event = message_changed.newEvent(chat.getChat(), message);
						}
					} else {
						event = null;
					}
					break;
				case 3:
					if (r.nextBoolean()) {
						event = user_is_typing.newEvent(chat.getChat(), contact.getEntity());
					} else {
						if (sendStopTypingEvent) {
							event = user_is_not_typing.newEvent(chat.getChat(), contact.getEntity());
						} else {
							event = null;
						}
					}
					break;
				default:
					event = null;
					break;
			}

			if (event != null) {
				adapter.onEvent(event);
			}

			adapter.doWork(checker);
		}
	}

	private MutableMessage newMessage(int i) {
		if (r.nextBoolean()) {
			return newOutgoingMessage(accountData.getAccount(), chat.getChat(), Strings.generateRandomString(10), "");
		} else {
			return Messages.newIncomingMessage(accountData.getAccount(), chat.getChat(), Strings.generateRandomString(10), "", contact.getEntity());
		}
	}

	private class OnlyOneTypingEventChecker implements Runnable {
		@Override
		public void run() {
			boolean found = false;
			for (int j = 0; j < adapter.getCount(); j++) {
				final MessageListItem item = adapter.getItem(j);
				if(item.getId().endsWith(MessagesAdapter.TYPING_POSTFIX)) {
					if(found) {
						Assert.fail();
					} else {
						found = true;
					}
				}
			}
		}
	}
}
