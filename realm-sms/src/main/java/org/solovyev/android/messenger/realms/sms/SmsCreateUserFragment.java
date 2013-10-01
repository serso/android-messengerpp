package org.solovyev.android.messenger.realms.sms;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.solovyev.android.messenger.accounts.BaseCreateUserFragment;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.entities.Entities.generateEntity;
import static org.solovyev.android.messenger.users.User.*;
import static org.solovyev.android.messenger.users.Users.newUser;
import static org.solovyev.android.properties.Properties.newProperty;

public class SmsCreateUserFragment extends BaseCreateUserFragment<SmsAccount> {

	@Nonnull
	private EditText firstNameEditText;

	@Nonnull
	private EditText lastNameEditText;

	@Nonnull
	private EditText phoneEditText;

	public SmsCreateUserFragment() {
		super(R.layout.mpp_realm_sms_create_user);
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		firstNameEditText = (EditText) root.findViewById(R.id.mpp_sms_firstname_edittext);
		lastNameEditText = (EditText) root.findViewById(R.id.mpp_sms_lastname_edittext);
		phoneEditText = (EditText) root.findViewById(R.id.mpp_sms_phone_edittext);
	}

	@Nullable
	@Override
	protected User validateData() {
		final String firstName = firstNameEditText.getText().toString();
		final String lastName = lastNameEditText.getText().toString();
		final String phone = phoneEditText.getText().toString();
		return validateData(firstName, lastName, phone);
	}

	private User validateData(@Nullable String firstName, @Nullable String lastName, @Nullable String phone) {
		boolean ok = true;

		if (Strings.isEmpty(phone)) {
			Toast.makeText(getActivity(), "Phone must be set!", Toast.LENGTH_SHORT).show();
			ok = false;
		}

		if (ok) {
			final List<AProperty> properties = new ArrayList<AProperty>();
			properties.add(newProperty(PROPERTY_PHONE, phone));
			if (firstName != null) {
				properties.add(newProperty(PROPERTY_FIRST_NAME, firstName));
			}

			if (lastName != null) {
				properties.add(newProperty(PROPERTY_LAST_NAME, lastName));
			}
			return newUser(generateEntity(getAccount()), Users.newNeverSyncedUserSyncData(), properties);
		} else {
			return null;
		}
	}
}
