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

package org.solovyev.android.messenger.realms.test;

import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.common.JObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.common.Objects.areEqual;

public class TestAccountConfiguration extends JObject implements AccountConfiguration {

	@Nonnull
	private String testStringField;

	private int testIntField;

	@Nonnull
	private String anotherTestStringField = "";

	@Nullable
	private Integer accountUserId;

	// for json
	public TestAccountConfiguration() {
	}

	public TestAccountConfiguration(@Nullable Integer accountUserId) {
		this.accountUserId = accountUserId;
	}

	public TestAccountConfiguration(@Nonnull String testStringField, int testIntField) {
		this.testStringField = testStringField;
		this.testIntField = testIntField;
	}

	@Nonnull
	public String getTestStringField() {
		return testStringField;
	}

	public int getTestIntField() {
		return testIntField;
	}

	public void setTestStringField(@Nonnull String testStringField) {
		this.testStringField = testStringField;
	}

	@Nonnull
	public String getAnotherTestStringField() {
		return anotherTestStringField;
	}

	public void setAnotherTestStringField(@Nonnull String anotherTestStringField) {
		this.anotherTestStringField = anotherTestStringField;
	}

	public void setTestIntField(int testIntField) {
		this.testIntField = testIntField;
	}

	@Nonnull
	@Override
	public TestAccountConfiguration clone() {
		return (TestAccountConfiguration) super.clone();
	}

	@Override
	public boolean isSameAccount(AccountConfiguration c) {
		if (this == c) return true;
		if (!(c instanceof TestAccountConfiguration)) return false;

		TestAccountConfiguration that = (TestAccountConfiguration) c;

		if (testIntField != that.testIntField) return false;
		if (!areEqual(accountUserId, that.accountUserId)) return false;

		return true;
	}

	@Override
	public boolean isSameCredentials(AccountConfiguration c) {
		boolean same = isSameAccount(c);
		if(same) {
			final TestAccountConfiguration that = (TestAccountConfiguration) c;
			same = this.testStringField.equals(that.testStringField);
		}
		return same;
	}

	@Override
	public boolean isSame(AccountConfiguration c) {
		boolean same = isSameCredentials(c);
		if(same) {
			final TestAccountConfiguration that = (TestAccountConfiguration) c;
			same = this.anotherTestStringField.equals(that.anotherTestStringField);
		}
		return same;
	}

	@Override
	public void applySystemData(AccountConfiguration oldConfiguration) {
	}

	@Nullable
	public Integer getAccountUserId() {
		return accountUserId;
	}
}
