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

package org.solovyev.android.messenger.users;

import android.telephony.PhoneNumberUtils;

import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	@Nonnull
	private String getFormattedNumber() {
		return PhoneNumberUtils.formatNumber(this.number);
	};

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

	@Override
	public String toString() {
		return "Phone number: " + number;
	}

	public boolean same(@Nonnull PhoneNumber that) {
		if(this.getNumber().equals(that.getNumber())) {
			return true;
		}

		return this.getFormattedNumber().equals(that.getFormattedNumber());
	}
}
