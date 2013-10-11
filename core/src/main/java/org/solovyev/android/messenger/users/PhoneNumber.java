package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.text.Strings;

import static android.telephony.PhoneNumberUtils.isGlobalPhoneNumber;
import static android.telephony.PhoneNumberUtils.isWellFormedSmsAddress;
import static org.solovyev.common.text.Strings.isEmpty;

public final class PhoneNumber {

	@Nonnull
	private String number = "";

	private boolean valid;

	private boolean sendable;

	private PhoneNumber() {
	}

	@Nonnull
	private static String toStandardPhoneNumber(@Nonnull String phoneNumber) {
		return phoneNumber.replace(" ", "");
	}

	@Nonnull
	public static PhoneNumber newPhoneNumber(@Nullable String phoneNumber) {
		final PhoneNumber result = new PhoneNumber();
		if (!isEmpty(phoneNumber)) {
			result.number = toStandardPhoneNumber(phoneNumber);
			result.valid = isValidPhoneNumber(result.number);
			result.sendable = isValidSmsPhoneNumber(result.number);
		}
		return result;
	}

	@Nonnull
	public String getNumber() {
		return number;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isCallable() {
		return valid;
	}

	public boolean isSendable() {
		return sendable;
	}

	private static boolean isValidPhoneNumber(@Nullable String phoneNumber) {
		if (!Strings.isEmpty(phoneNumber)) {
			return isGlobalPhoneNumber(phoneNumber);
		} else {
			return false;
		}
	}

	private static boolean isValidSmsPhoneNumber(@Nullable String phoneNumber) {
		if (!Strings.isEmpty(phoneNumber)) {
			return isWellFormedSmsAddress(phoneNumber);
		} else {
			return false;
		}
	}
}
