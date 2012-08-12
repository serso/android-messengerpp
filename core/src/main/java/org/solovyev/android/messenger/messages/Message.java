package org.solovyev.android.messenger.messages;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.solovyev.common.VersionedEntity;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:09 PM
 */
public interface Message extends VersionedEntity {

    @NotNull
    DateTime getCreationDate();

    @NotNull
    Integer getCreator();
}
