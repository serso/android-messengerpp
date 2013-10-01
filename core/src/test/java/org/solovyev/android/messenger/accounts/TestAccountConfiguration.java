package org.solovyev.android.messenger.accounts;

import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

public class TestAccountConfiguration extends JObject implements AccountConfiguration {

	@Nonnull
	private String testStringField;

	private int testIntField;

	@Nonnull
	private String anotherTestStringField = "";

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
}
