package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 2:24 PM
 */

public interface MessengerEntity {

    @Nonnull
    String getId();

    boolean equals(Object o);

    int hashCode();
}
