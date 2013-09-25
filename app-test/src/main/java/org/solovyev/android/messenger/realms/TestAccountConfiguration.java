package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AccountConfiguration;
import org.solovyev.common.JObject;

public class TestAccountConfiguration extends JObject implements AccountConfiguration {

	@Nonnull
	private String testStringField;

	private int testIntField;

	// for json
	public TestAccountConfiguration() {
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
		if (!testStringField.equals(that.testStringField)) return false;

		return true;
	}

	@Override
	public boolean isSameCredentials(AccountConfiguration c) {
		return isSameAccount(c);
	}

	@Override
	public void applySystemData(AccountConfiguration oldConfiguration) {
	}
}
