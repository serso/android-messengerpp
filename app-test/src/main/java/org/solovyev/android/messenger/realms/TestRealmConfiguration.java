package org.solovyev.android.messenger.realms;

import org.solovyev.common.JObject;

import javax.annotation.Nonnull;

public class TestRealmConfiguration extends JObject implements RealmConfiguration {

    @Nonnull
    private String testStringField;

    private int testIntField;

    // for json
    public TestRealmConfiguration() {
    }

    public TestRealmConfiguration(@Nonnull String testStringField, int testIntField) {
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
    public TestRealmConfiguration clone() {
        return (TestRealmConfiguration) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestRealmConfiguration)) return false;

        TestRealmConfiguration that = (TestRealmConfiguration) o;

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
