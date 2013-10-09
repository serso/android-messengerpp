package org.solovyev.android.messenger.realms.sms;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.connection.AbstractAccountConnection;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.ChatMessage;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.messages.LiteChatMessageImpl;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static android.telephony.SmsMessage.createFromPdu;
import static org.solovyev.android.messenger.App.getApplication;
import static org.solovyev.android.messenger.accounts.AccountService.NO_ACCOUNT_ID;
import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.entities.Entities.makeEntityId;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.realms.sms.SmsRealm.*;
import static org.solovyev.android.messenger.users.User.*;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.android.messenger.users.Users.newUser;
import static org.solovyev.android.properties.Properties.newProperty;
import static org.solovyev.common.text.Strings.isEmpty;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 9:22 PM
 */
final class SmsAccountConnection extends AbstractAccountConnection<SmsAccount> {

	@Nullable
	private volatile ReportsBroadcastReceiver receiver;

	SmsAccountConnection(@Nonnull SmsAccount account, @Nonnull Context context) {
		super(account, context, false);
	}

	@Override
	protected void start0() throws AccountConnectionException {
		if (receiver == null) {
			receiver = new ReportsBroadcastReceiver();
			final Application application = getApplication();
			application.registerReceiver(receiver, new IntentFilter(INTENT_SENT));
			application.registerReceiver(receiver, new IntentFilter(INTENT_DELIVERED));

			final IntentFilter intentReceivedFilter = new IntentFilter(INTENT_RECEIVED);
			intentReceivedFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
			application.registerReceiver(receiver, intentReceivedFilter);
		}
	}

	@Override
	protected void stop0() {
		unregisterReceiver();
	}

	private void unregisterReceiver() {
		if (receiver != null) {
			getApplication().unregisterReceiver(receiver);
			receiver = null;
		}
	}

	private class ReportsBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (intent.getAction().equals(INTENT_RECEIVED)) {
					onSmsReceived(this, intent);
				} else {
					// todo serso: sent/delivered report
				}

			} catch (AccountException e) {
				Log.e(SmsRealm.TAG, e.getMessage(), e);
			}
		}
	}

	private void onSmsReceived(@Nonnull BroadcastReceiver broadcastReceiver, @Nonnull Intent intent) throws AccountException {
		final SmsAccount account = getAccount();
		final Multimap<String, String> messagesByPhoneNumber = getMessagesByPhoneNumber(intent);

		if (!messagesByPhoneNumber.isEmpty()) {
			final User user = account.getUser();
			final UserService userService = App.getUserService();
			final ChatService chatService = App.getChatService();

			final List<User> contacts = userService.getUserContacts(user.getEntity());

			for (Map.Entry<String, Collection<String>> entry : messagesByPhoneNumber.asMap().entrySet()) {
				final User contact = findOrCreateContact(entry.getKey(), contacts);
				final Chat chat = chatService.getOrCreatePrivateChat(user.getEntity(), contact.getEntity());

				final List<ChatMessage> messages = new ArrayList<ChatMessage>(entry.getValue().size());
				for (String message : entry.getValue()) {
					final ChatMessage chatMessage = toChatMessage(message, account, contact, user);
					if (chatMessage != null) {
						messages.add(chatMessage);
					}
				}

				chatService.saveChatMessages(chat.getEntity(), messages, false);
			}
		}

		if (account.getConfiguration().isStopFurtherProcessing()) {
			broadcastReceiver.abortBroadcast();
		}
	}

	@Nonnull
	private Multimap<String, String> getMessagesByPhoneNumber(@Nonnull Intent intent) {
		final Multimap<String, String> smss = ArrayListMultimap.create();

		final Bundle extras = intent.getExtras();
		if (extras != null) {
			final Object[] smsExtras = (Object[]) extras.get(SmsRealm.INTENT_EXTRA_PDUS);
			final String smsFormat = extras.getString(SmsRealm.INTENT_EXTRA_FORMAT);

			String fromAddress = null;
			final StringBuilder message = new StringBuilder(255 * smsExtras.length);
			for (Object smsExtra : smsExtras) {
				final SmsMessage smsPart = createFromPdu((byte[]) smsExtra);
				message.append(smsPart.getMessageBody());
				fromAddress = smsPart.getOriginatingAddress();
			}

			if (!isEmpty(message) && !isEmpty(fromAddress)) {
				smss.put(fromAddress, message.toString());
			}
		}

		return smss;
	}

	@Nullable
	private ChatMessage toChatMessage(@Nonnull String message, @Nonnull Account account, @Nonnull User from, @Nonnull User to) {
		if (!isEmpty(message)) {
			final LiteChatMessageImpl liteChatMessage = Messages.newLiteMessage(generateEntity(account));
			liteChatMessage.setBody(message);
			liteChatMessage.setAuthor(from.getEntity());
			liteChatMessage.setRecipient(to.getEntity());
			liteChatMessage.setSendDate(DateTime.now());
			// new message by default unread
			return Messages.newMessage(liteChatMessage, false);
		} else {
			return null;
		}
	}

	@Nullable
	private User findOrCreateContact(@Nonnull final String phone, @Nonnull List<User> contacts) {
		User result = findContactByPhone(phone, contacts);
		if (result == null) {
			result = toUser(phone);

			final SmsAccount account = getAccount();
			App.getUserService().mergeUserContacts(account.getUser().getEntity(), Arrays.asList(result), false, false);
		}
		return result;
	}

	@Nonnull
	private User toUser(@Nonnull String phone) {
		final SmsAccount account = getAccount();

		final MutableUser user = newEmptyUser(newEntity(account.getId(), NO_ACCOUNT_ID, makeEntityId(account.getId(), phone)));
		user.setFirstName(phone);

		final MutableAProperties properties = user.getProperties();
		properties.setProperty(PROPERTY_PHONE, phone);
		properties.setProperty(PROPERTY_PHONES, phone);

		return user;
	}

	@Nullable
	private User findContactByPhone(@Nonnull final String phone, @Nonnull List<User> contacts) {
		return Iterables.find(contacts, new Predicate<User>() {
			@Override
			public boolean apply(@Nullable User contact) {
				if (contact != null) {
					// first try to find by default phone property
					final String contactPhone = contact.getPropertyValueByName(PROPERTY_PHONE);
					if (contactPhone != null && contactPhone.equals(phone)) {
						return true;
					}

					// then try find by 'phones' property
					final String phones = contact.getPropertyValueByName(PROPERTY_PHONES);
					if (phones != null) {
						for (String userPhone: Splitter.on(User.PROPERTY_PHONES_SEPARATOR).omitEmptyStrings().split(phones)) {
							if (userPhone.equals(phone)) {
								return true;
							}
						}
					}

					return false;
				} else {
					return false;
				}
			}
		}, null);
	}
}
