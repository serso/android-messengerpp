package org.solovyev.android.messenger.realms.sms;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.chats.Chats;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.getBroadcast;
import static org.solovyev.android.messenger.App.getApplication;
import static org.solovyev.android.messenger.realms.sms.SmsRealm.INTENT_EXTRA_SMS_ID;
import static org.solovyev.android.messenger.realms.sms.SmsRealm.makeSmsDeliveredAction;
import static org.solovyev.android.messenger.realms.sms.SmsRealm.makeSmsSentAction;

final class SmsAccountChatService implements AccountChatService {

	static final String MESSAGE_PROPERTY_PHONE = "phone_number";

	@Nonnull
	private final SmsAccount account;

	public SmsAccountChatService(@Nonnull SmsAccount account) {
		this.account = account;
	}

	@Nonnull
	@Override
	public List<Message> getMessages(@Nonnull String accountUserId) throws AccountConnectionException {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getNewerMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId) throws AccountConnectionException {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull String accountUserId, @Nonnull Integer offset) throws AccountConnectionException {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<AccountChat> getChats(@Nonnull String accountUserId) throws AccountConnectionException {
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public String sendMessage(@Nonnull Chat chat, @Nonnull Message message) throws AccountConnectionException {
		final String phoneNumber = getPhoneNumber(message);
		if(!Strings.isEmpty(phoneNumber)) {
			final Application application = getApplication();

			final Intent sentIntent = new Intent(makeSmsSentAction(message.getId()));
			sentIntent.putExtra(INTENT_EXTRA_SMS_ID, message.getEntity().getEntityId());

			final Intent deliveredIntent = new Intent(makeSmsDeliveredAction(message.getId()));
			deliveredIntent.putExtra(INTENT_EXTRA_SMS_ID, message.getEntity().getEntityId());

			final SmsAccountConnection connection = account.getAccountConnection();
			if (connection != null) {
				final BroadcastReceiver receiver = connection.getReceiver();
				application.registerReceiver(receiver, new IntentFilter(sentIntent.getAction()));
				application.registerReceiver(receiver, new IntentFilter(deliveredIntent.getAction()));
			}

			SmsManager.getDefault().sendTextMessage(phoneNumber, null, message.getBody(),
					getBroadcast(application, 0, sentIntent, FLAG_ONE_SHOT),
					getBroadcast(application, 0, deliveredIntent, FLAG_ONE_SHOT));

			return null;
		}
		return null;
	}

	@Nullable
	private String getPhoneNumber(@Nonnull Message message) {
		String phoneNumber = message.getProperties().getPropertyValue(MESSAGE_PROPERTY_PHONE);
		if (Strings.isEmpty(phoneNumber)) {
			final User recipient = App.getUserService().getUserById(message.getRecipient());
			phoneNumber = recipient.getPropertyValueByName(User.PROPERTY_PHONE);
		}
		return phoneNumber;
	}

	@Override
	public void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException {
		if (recipient == null) {
			recipient = App.getUserService().getUserById(message.getRecipient());
		}

		final String phoneNumber = recipient.getPhoneNumber();
		if (phoneNumber != null) {
			message.getProperties().setProperty(MESSAGE_PROPERTY_PHONE, phoneNumber);
		}
	}

	@Nonnull
	@Override
	public Chat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) throws AccountConnectionException {
		return Chats.newPrivateChat(accountChat);
	}
}
