package org.solovyev.android.messenger.view;

import org.solovyev.android.list.ListItem;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/24/13
 * Time: 11:55 AM
 */
public interface IdentifiableListItem extends ListItem {

    @Nonnull
    String getId();

}
