package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 12:49 PM
 */
public enum RealmGuiEventType {

    /**
     * Fired when realm is clicked in the list of realms
     */
    realm_clicked,

    /**
     * Fired when editing of realm is requested (e.g. 'Edit' button clicked)
     */
    realm_edit_requested,

    /**
     * Fired when editing of realm is finished (e.g. user pressed 'Back' or 'Save' button)
     * Data; removed (boolean) - true is realm was removed as a result of editing
     */
    realm_edit_finished;

    @Nonnull
    public static RealmGuiEvent newRealmClickedEvent(@Nonnull Realm realm) {
        return new RealmGuiEvent(realm_clicked, realm, null);
    }

    @Nonnull
    public static RealmGuiEvent newRealmEditRequestedEvent(@Nonnull Realm realm) {
        return new RealmGuiEvent(realm_edit_requested, realm, null);
    }

    @Nonnull
    public static RealmGuiEvent newRealmEditFinishedEvent(@Nonnull Realm realm, boolean removed) {
        return new RealmGuiEvent(realm_edit_finished, realm, removed);
    }
}
