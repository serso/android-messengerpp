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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.google.common.base.Function;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AbstractAccount;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.AccountSyncData;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.CompositeUserChoice;
import org.solovyev.android.messenger.users.PhoneNumber;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Intent.ACTION_CALL;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.App.newTag;
import static org.solovyev.android.messenger.users.CompositeUserChoice.newCompositeUserChoice;
import static org.solovyev.android.messenger.users.PhoneNumber.newPhoneNumber;
import static org.solovyev.android.messenger.users.User.PROPERTY_PHONE;
import static org.solovyev.android.properties.Properties.newProperty;
import static org.solovyev.common.text.Strings.isEmpty;

final class SmsAccount extends AbstractAccount<SmsAccountConfiguration> {

	static final String TAG = newTag(SmsAccount.class.getSimpleName());

	public SmsAccount(@Nonnull String id, @Nonnull Realm realm, @Nonnull User user, @Nonnull SmsAccountConfiguration configuration, @Nonnull AccountState state, @Nonnull AccountSyncData syncData) {
		super(id, realm, user, configuration, state, syncData);
	}

	@Nonnull
	@Override
	public SmsRealm getRealm() {
		return (SmsRealm) super.getRealm();
	}

	@Nonnull
	@Override
	protected AccountConnection createConnection(@Nonnull Context context) {
		return new SmsAccountConnection(this, context);
	}

	@Nullable
	@Override
	protected synchronized SmsAccountConnection getConnection() {
		return (SmsAccountConnection) super.getConnection();
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		return context.getString(getRealm().getNameResId());
	}

	@Nonnull
	@Override
	public AccountUserService getAccountUserService() {
		return new SmsAccountUserService(this);
	}

	@Nonnull
	@Override
	public AccountChatService getAccountChatService() {
		return new SmsAccountChatService(this);
	}

	@Override
	public boolean isCompositeUser(@Nonnull User user) {
		return true;
	}

	@Override
	public boolean isCompositeUserDefined(@Nonnull User user) {
		final String phoneNumber = user.getPropertyValueByName(PROPERTY_PHONE);
		return !isEmpty(phoneNumber);
	}

	@Nonnull
	@Override
	public List<CompositeUserChoice> getCompositeUserChoices(@Nonnull User user) {
		final AtomicInteger index = new AtomicInteger(0);
		return newArrayList(transform(user.getPhoneNumbers(), new Function<String, CompositeUserChoice>() {
			@Override
			public CompositeUserChoice apply(String phone) {
				return newCompositeUserChoice(phone, index.getAndIncrement());
			}
		}));
	}

	@Nonnull
	@Override
	public User applyCompositeChoice(@Nonnull CompositeUserChoice compositeUserChoice, @Nonnull User user) {
		return user.cloneWithNewProperty(newProperty(PROPERTY_PHONE, compositeUserChoice.getName().toString()));
	}

	@Override
	public boolean isCompositeUserChoicePersisted() {
		return true;
	}

	@Override
	public int getCompositeDialogTitleResId() {
		return R.string.mpp_sms_composite_dialog_title;
	}


	@Override
	public boolean canSendMessage(@Nonnull Chat chat) {
		if (chat.isPrivate()) {
			final User recipient = App.getUserService().getUserById(chat.getSecondUser());
			final String phoneNumber = recipient.getPhoneNumber();
			if (newPhoneNumber(phoneNumber).isSendable()) {
				return true;
			} else {
				if (!isEmpty(phoneNumber)) {
					return false;
				}

				return existsSendablePhoneNumber(recipient);
			}
		} else {
			return false;
		}
	}

	private boolean existsSendablePhoneNumber(@Nonnull User recipient) {
		final Set<String> phoneNumbers = recipient.getPhoneNumbers();
		for (String phoneNumber : phoneNumbers) {
			if (newPhoneNumber(phoneNumber).isSendable()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canCall(@Nonnull User contact) {
		final String phoneNumber = contact.getPhoneNumber();
		if (newPhoneNumber(phoneNumber).isCallable()) {
			return true;
		} else {
			if (!isEmpty(phoneNumber)) {
				return false;
			}

			return existsCallablePhoneNumber(contact);
		}
	}

	private boolean existsCallablePhoneNumber(@Nonnull User contact) {
		final Set<String> phoneNumbers = contact.getPhoneNumbers();
		for (String phoneNumber : phoneNumbers) {
			if (newPhoneNumber(phoneNumber).isCallable()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void call(@Nonnull User contact, @Nonnull Context context) {
		final PhoneNumber phoneNumber = newPhoneNumber(contact.getPhoneNumber());
		if (phoneNumber.isCallable()) {
			// we need to return after call to application => enable listener
			final SmsAccountConnection connection = getConnection();
			if (connection != null) {
				connection.setCallFromUs(phoneNumber.getNumber());
			}

			final Intent callIntent = new Intent(ACTION_CALL, Uri.parse("tel:" + phoneNumber.getNumber()));
			context.startActivity(callIntent);
		}
	}

}
