package org.solovyev.android.messenger.realms.sms;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.users.BaseEditUserFragment;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.MutableAProperties;

import static org.solovyev.android.messenger.users.User.PROPERTY_FIRST_NAME;
import static org.solovyev.android.messenger.users.User.PROPERTY_LAST_NAME;
import static org.solovyev.android.messenger.users.User.PROPERTY_PHONE;
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
