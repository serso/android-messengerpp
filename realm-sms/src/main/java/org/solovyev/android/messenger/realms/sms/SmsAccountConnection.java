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

package org.solovyev.android.messenger.realms.sms;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import org.joda.time.DateTime;
import org.solovyev.android.Threads;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.connection.BaseAccountConnection;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessageState;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.PhoneNumber;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.properties.MutableAProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.SmsMessage.createFromPdu;
import static android.telephony.TelephonyManager.*;
import static com.google.common.collect.Iterables.any;
import static java.util.Arrays.asList;
import static org.solovyev.android.messenger.App.*;
import static org.solovyev.android.messenger.accounts.AccountService.NO_ACCOUNT_ID;
import static org.solovyev.android.messenger.entities.Entities.makeEntityId;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.messages.MessageState.delivered;
import static org.solovyev.android.messenger.messages.MessageState.sent;
import static org.solovyev.android.messenger.messages.Messages.newIncomingMessage;
import static org.solovyev.android.messenger.messages.Messages.newOutgoingMessage;
import static org.solovyev.android.messenger.realms.sms.Call.newNoCall;
import static org.solovyev.android.messenger.realms.sms.SmsRealm.*;
import static org.solovyev.android.messenger.users.PhoneNumber.newPhoneNumber;
import static org.solovyev.android.messenger.users.User.PROPERTY_PHONE;
import static org.solovyev.android.messenger.users.User.PROPERTY_PHONES;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;
import static org.solovyev.common.text.Strings.isEmpty;

final class SmsAccountConnection extends BaseAccountConnection<SmsAccount> {

	@Nullable
	private volatile ReportsBroadcastReceiver receiver;

	@Nonnull
	private final CallListener callListener;

	SmsAccountConnection(@Nonnull SmsAccount account, @Nonnull Context context) {
		super(account, context);
		callListener = new CallListener(context);
	}

	public void setCallFromUs(@Nonnull String number) {
		callListener.setCallFromUs(number);
	}

	@Override
	protected void start0() throws AccountConnectionException {
		if (receiver == null) {
			receiver = new ReportsBroadcastReceiver();
			final Application application = getApplication();

			final IntentFilter intentReceivedFilter = new IntentFilter(INTENT_RECEIVED);
			intentReceivedFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
			application.registerReceiver(receiver, intentReceivedFilter);
		}

		getTelephonyManager().listen(callListener.phoneStateListener, LISTEN_CALL_STATE);
	}

	@Nonnull
	private TelephonyManager getTelephonyManager() {
		return (TelephonyManager) getContext().getSystemService(TELEPHONY_SERVICE);
	}

	@Override
	protected void stop0() {
		getTelephonyManager().listen(callListener.phoneStateListener, LISTEN_NONE);
		unregisterReceiver();
	}

	@Nonnull
	public ReportsBroadcastReceiver getReceiver() throws AccountConnectionException {
		final ReportsBroadcastReceiver result = receiver;
		if (result != null) {
			return result;
		} else {
			throw new AccountConnectionException(getAccount().getId());
		}
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
				final String action = intent.getAction();
				if (action.equals(INTENT_RECEIVED)) {
					onSmsReceived(this, intent);
				} else if (action.startsWith(INTENT_SENT_PREFIX)) {
					onSmsIntent(intent, sent);
				} else if (action.startsWith(INTENT_DELIVERED_PREFIX)) {
					onSmsIntent(intent, delivered);
				}

			} catch (AccountException e) {
				Log.e(SmsRealm.TAG, e.getMessage(), e);
			}
		}
	}

	private void onSmsIntent(@Nonnull Intent intent, @Nonnull MessageState state) {
		final String entityId = intent.getStringExtra(INTENT_EXTRA_SMS_ID);
		if (!isEmpty(entityId)) {
			final Message message = getMessageService().getMessage(entityId);
			if (message != null) {
				getChatService().updateMessageState(message.cloneWithNewState(state));
			}
		}
	}

	private void onSmsReceived(@Nonnull BroadcastReceiver broadcastReceiver, @Nonnull Intent intent) throws AccountException {
		final SmsAccount account = getAccount();
		final Multimap<String, SmsData> messagesByPhoneNumber = getMessagesByPhoneNumber(intent);

		if (!messagesByPhoneNumber.isEmpty()) {
			final User user = account.getUser();
			final UserService userService = getUserService();
			final ChatService chatService = getChatService();

			final List<User> contacts = userService.getContacts(user.getEntity());

			for (Map.Entry<String, Collection<SmsData>> entry : messagesByPhoneNumber.asMap().entrySet()) {
				final User contact = findOrCreateContact(entry.getKey(), contacts);
				final Chat chat = chatService.getOrCreatePrivateChat(user.getEntity(), contact.getEntity());

				final List<Message> messages = new ArrayList<Message>(entry.getValue().size());
				for (SmsData smsData : entry.getValue()) {
					final Message message = toMessage(smsData, account, contact, chat);
					if (message != null) {
						messages.add(message);
					}
				}

				chatService.saveMessages(chat.getEntity(), messages);
			}
		}

		if (account.getConfiguration().isStopFurtherProcessing()) {
			broadcastReceiver.abortBroadcast();
		}
	}

	@Nonnull
	private Multimap<String, SmsData> getMessagesByPhoneNumber(@Nonnull Intent intent) {
		final Multimap<String, SmsData> smss = ArrayListMultimap.create();

		final Bundle extras = intent.getExtras();
		if (extras != null) {
			final Object[] smsExtras = (Object[]) extras.get(SmsRealm.INTENT_EXTRA_PDUS);
			final String smsFormat = extras.getString(SmsRealm.INTENT_EXTRA_FORMAT);

			String fromAddress = null;
			Long sendTime = null;
			final StringBuilder message = new StringBuilder(255 * smsExtras.length);
			for (Object smsExtra : smsExtras) {
				final SmsMessage smsMessage = createFromPdu((byte[]) smsExtra);
				message.append(smsMessage.getMessageBody());
				fromAddress = smsMessage.getOriginatingAddress();
				sendTime = smsMessage.getTimestampMillis();
			}

			if (!isEmpty(message) && !isEmpty(fromAddress) && sendTime != null) {
				smss.put(fromAddress, new SmsData(message.toString(), sendTime));
			}
		}

		return smss;
	}

	private static class SmsData {

		@Nonnull
		private final String body;

		private final long sendTime;

		private SmsData(@Nonnull String body, long sendTime) {
			this.body = body;
			this.sendTime = sendTime;
		}
	}

	@Nullable
	private static Message toMessage(@Nonnull SmsData smsData, @Nonnull Account account, @Nonnull User from, @Nonnull Chat chat) {
		if (!isEmpty(smsData.body)) {
			final MutableMessage message = newIncomingMessage(account, chat, smsData.body, null, from.getEntity());
			message.setSendDate(new DateTime(smsData.sendTime));
			return message;
		} else {
			return null;
		}
	}

	@Nonnull
	public User findOrCreateContact(@Nonnull final String phone, @Nonnull List<User> contacts) {
		User result = findContactByPhone(phone, contacts);
		if (result == null) {
			result = toUser(phone);

			final SmsAccount account = getAccount();
			App.getUserService().mergeContacts(account, asList(result), false, false);
		}
		return result;
	}

	@Nonnull
	private User toUser(@Nonnull String phone) {
		return toUser(newPhoneNumber(phone));
	}

	@Nonnull
	private User toUser(@Nonnull PhoneNumber phoneNumber) {
		final SmsAccount account = getAccount();

		final MutableUser user = newEmptyUser(newEntity(account.getId(), NO_ACCOUNT_ID, makeEntityId(account.getId(), phoneNumber.getNumber())));
		user.setFirstName(phoneNumber.getNumber());

		final MutableAProperties properties = user.getProperties();
		if (phoneNumber.isValid()) {
			properties.setProperty(PROPERTY_PHONE, phoneNumber.getNumber());
			properties.setProperty(PROPERTY_PHONES, phoneNumber.getNumber());
		}

		return user;
	}

	@Nullable
	private static User findContactByPhone(@Nonnull final String phone, @Nonnull List<User> contacts) {
		final SamePhonePredicate predicate = new SamePhonePredicate(newPhoneNumber(phone));

		return Iterables.find(contacts, new Predicate<User>() {
			@Override
			public boolean apply(@Nullable User contact) {
				if (contact != null) {
					// first try to find by default phone property
					if (predicate.apply(contact.getPropertyValueByName(PROPERTY_PHONE))) {
						return true;
					} else {
						return any(contact.getPhoneNumbers(), predicate);
					}
				} else {
					return false;
				}
			}
		}, null);
	}

	private void onCall(@Nonnull Call call) {
		final String number = call.getNumber();
		if (!isEmpty(number)) {
			final SmsAccount account = getAccount();
			final User user = account.getUser();
			final List<User> contacts = getUserService().getContacts(user.getEntity());
			final User contact = findOrCreateContact(number, contacts);
			try {
				final Chat chat = getChatService().getOrCreatePrivateChat(user.getEntity(), contact.getEntity());

				final String messageBody = formatMessageBody(call);

				final MutableMessage message;
				if (call.isIncoming()) {
					message = newIncomingMessage(account, chat, messageBody, null, contact.getEntity());
				} else {
					message = newOutgoingMessage(account, chat, messageBody, null);
					message.setState(MessageState.sent);
				}
				message.setRead(true);
				message.setSendDate(call.getDate());
				getChatService().saveMessages(chat.getEntity(), asList(message));
			} catch (AccountException e) {
				App.getExceptionHandler().handleException(e);
			}
		}
	}

	@Nonnull
	private String formatMessageBody(@Nonnull Call call) {
		final Resources resources = App.getApplication().getResources();
		final String messageBody;
		if (call.isIncoming()) {
			messageBody = resources.getString(R.string.mpp_sms_incoming_call);
		} else {
			messageBody = resources.getString(R.string.mpp_sms_outgoing_call);
		}
		return messageBody;
	}

	@Nonnull
	static String millisToMinutesAndSeconds(long millis) {
		final long seconds = (millis / 1000L) % 60L;
		final long minutes = (millis / 1000L) / 60L;
		return String.format("%dm %ds", minutes, seconds);
	}

	private static class SamePhonePredicate implements Predicate<String> {

		@Nonnull
		private final PhoneNumber phoneNumber;

		public SamePhonePredicate(@Nonnull PhoneNumber phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		@Override
		public boolean apply(@Nullable String phone) {
			if (phone != null) {
				if (newPhoneNumber(phone).same(phoneNumber)) {
					return true;
				}
			}

			return false;
		}
	}

	private final class CallListener {

		@Nonnull
		private final Context context;

		private boolean callFromUs = false;

		@Nonnull
		private Call call = newNoCall();

		private volatile PhoneStateListener phoneStateListener;

		public CallListener(@Nonnull Context context) {
			this.context = context;

			// PhoneStateListener can be created only on UI thread
			if (Threads.isUiThread()) {
				// if UI thread just create
				phoneStateListener = new CallPhoneStateListener();
			} else {
				// if not UI thread then run it on UI handler and wait for result
				createListenerAndWait();
			}
		}

		private void createListenerAndWait() {
			getUiHandler().post(new Runnable() {
				@Override
				public void run() {
					phoneStateListener = new CallPhoneStateListener();
				}
			});

			while (phoneStateListener == null) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
		}

		public void setCallFromUs(@Nonnull String number) {
			this.call = Call.newOutgoingCall(number);
			this.callFromUs = true;
		}

		private class CallPhoneStateListener extends PhoneStateListener {

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
					case CALL_STATE_RINGING:
						call = Call.newIncomingCall(incomingNumber);
						break;
					case CALL_STATE_OFFHOOK:
						break;
					case CALL_STATE_IDLE:
						call.onEnd();
						onCall(call);

						// if we initiated call => we need to return to our screen => start activity
						if (callFromUs) {
							callFromUs = false;
							final Intent intent = new Intent(context, getMainActivityClass());
							intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(intent);
						}

						call = newNoCall();
						break;
				}
			}
		}
	}

}
