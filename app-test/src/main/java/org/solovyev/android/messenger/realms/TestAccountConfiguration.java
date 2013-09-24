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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TestAccountConfiguration)) return false;

		TestAccountConfiguration that = (TestAccountConfiguration) o;

		if (testIntField != that.testIntField) return false;
		if (!testStringField.equals(that.testStringField)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = testStringField.hashCode();
		result = 31 * result + testIntField;
		return result;
	}
}
