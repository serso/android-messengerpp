package org.solovyev.android.messenger.messages;

import javax.annotation.Nonnull;
import org.joda.time.DateTime;
import org.solovyev.common.VersionedEntity;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:09 PM
 */
public interface Message extends VersionedEntity {

    @Nonnull
    DateTime getCreationDate();

    @Nonnull
    Integer getCreator();
}
