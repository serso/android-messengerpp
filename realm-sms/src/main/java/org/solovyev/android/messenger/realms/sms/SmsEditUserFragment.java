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

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import org.solovyev.android.messenger.users.BaseEditUserFragment;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.MutableAProperties;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.App.showToast;
import static org.solovyev.android.messenger.users.User.*;
import static org.solovyev.common.text.Strings.isEmpty;

public class SmsEditUserFragment extends BaseEditUserFragment<SmsAccount> {

	@Nonnull
	private EditText firstNameEditText;

	@Nonnull
	private EditText lastNameEditText;

	@Nonnull
	private EditText phoneEditText;

	@Nonnull
	private CheckBox dontSaveInPhoneCheckbox;

	public SmsEditUserFragment() {
		super(R.layout.mpp_realm_sms_edit_user);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		dontSaveInPhoneCheckbox = (CheckBox) root.findViewById(R.id.mpp_sms_dont_save_into_phone_checkbox);
		firstNameEditText = (EditText) root.findViewById(R.id.mpp_sms_firstname_edittext);
		lastNameEditText = (EditText) root.findViewById(R.id.mpp_sms_lastname_edittext);
		phoneEditText = (EditText) root.findViewById(R.id.mpp_sms_phone_edittext);

		if (!isNewUser()) {
			final User user = getUser();
			firstNameEditText.setText(user.getFirstName());
			lastNameEditText.setText(user.getLastName());
			phoneEditText.setText(user.getPropertyValueByName(PROPERTY_PHONE));

			dontSaveInPhoneCheckbox.setChecked(!user.getEntity().isAccountEntityIdSet());
		}
	}

	@Nullable
	@Override
	protected MutableUser validateData() {
		final String firstName = firstNameEditText.getText().toString();
		final String lastName = lastNameEditText.getText().toString();
		final String phone = phoneEditText.getText().toString();
		return validateData(firstName, lastName, phone);
	}

	@Nullable
	private MutableUser validateData(@Nullable String firstName, @Nullable String lastName, @Nullable String phone) {
		boolean ok = true;

		if (isEmpty(phone)) {
			final User oldUser = getUser();
			if (oldUser == null || !Strings.isEmpty(oldUser.getPhoneNumber())) {
				// we shall forbid saving NEW contact with no phone number but we shall allow save OLD contact with
				// no number if it hadn't got it
				showToast(R.string.mpp_sms_phone_must_be_set);
				ok = false;
			}
		}

		if (ok) {
			final MutableUser user = getOrCreateUser();

			final MutableAProperties properties = user.getProperties();
			if (!Strings.isEmpty(phone)) {
				properties.setProperty(PROPERTY_PHONE, phone);
			} else {
				properties.removeProperty(PROPERTY_PHONE);
			}

			if (!isEmpty(firstName)) {
				properties.setProperty(PROPERTY_FIRST_NAME, firstName);
			} else {
				properties.removeProperty(PROPERTY_FIRST_NAME);
			}

			if (!isEmpty(lastName)) {
				properties.setProperty(PROPERTY_LAST_NAME, lastName);
			} else {
				properties.removeProperty(PROPERTY_LAST_NAME);
			}

			if (!user.getEntity().isAccountEntityIdSet()) {
				if (!dontSaveInPhoneCheckbox.isChecked()) {
					// todo serso: save to phones contact
				}
			}

			return user;
		} else {
			return null;
		}
	}

}
