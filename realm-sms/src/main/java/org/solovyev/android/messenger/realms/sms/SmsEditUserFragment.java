package org.solovyev.android.messenger.realms.sms;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import org.solovyev.android.messenger.accounts.BaseEditUserFragment;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperties;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.MutableAProperties;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.users.User.*;
import static org.solovyev.android.messenger.users.Users.newUser;
import static org.solovyev.android.properties.Properties.newProperty;
import static org.solovyev.common.text.Strings.isEmpty;

public class SmsEditUserFragment extends BaseEditUserFragment<SmsAccount> {

	@Nonnull
	private EditText firstNameEditText;

	@Nonnull
	private EditText lastNameEditText;

	@Nonnull
	private EditText phoneEditText;

	public SmsEditUserFragment() {
		super(R.layout.mpp_realm_sms_edit_user);
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
			Toast.makeText(getActivity(), "Phone must be set!", Toast.LENGTH_SHORT).show();
			ok = false;
		}

		if (ok) {
			final MutableUser user = getOrCreateUser();

			final MutableAProperties properties = user.getProperties();
			properties.setProperty(PROPERTY_PHONE, phone);
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

			return user;
		} else {
			return null;
		}
	}

}
