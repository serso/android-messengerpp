package org.solovyev.android.messenger.realms;

import org.solovyev.common.JCloneable;

public interface RealmConfiguration extends JCloneable<RealmConfiguration> {

    int hashCode();

    boolean equals(Object o);
}
