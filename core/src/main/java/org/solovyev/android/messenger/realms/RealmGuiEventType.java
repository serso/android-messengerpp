package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 12:49 PM
 */
public enum RealmGuiEventType {

    /**
     * Fired when realm view is (e.g. realm is clicked in the list of realms)
     */
    realm_view_requested,

    /**
     * Fired when realm view is cancelled (e.g. user pressed 'Back' button)
     */
    realm_view_cancelled,

    /**
     * Fired when editing of realm is requested (e.g. 'Edit' button clicked)
     */
    realm_edit_requested,

    /**
     * Fired when editing of realm is finished (e.g. user pressed 'Back' or 'Save' button)
     * Data; state (FinishedState)
     */
    realm_edit_finished;

    @Nonnull
    public static RealmGuiEvent newRealmViewRequestedEvent(@Nonnull Realm realm) {
        return new RealmGuiEvent(realm_view_requested, realm, null);
    }

    @Nonnull
    public static RealmGuiEvent newRealmViewCancelledEvent(@Nonnull Realm realm) {
        return new RealmGuiEvent(realm_view_cancelled, realm, null);
    }

    @Nonnull
    public static RealmGuiEvent newRealmEditRequestedEvent(@Nonnull Realm realm) {
        return new RealmGuiEvent(realm_edit_requested, realm, null);
    }

    @Nonnull
    public static RealmGuiEvent newRealmEditFinishedEvent(@Nonnull Realm realm, @Nonnull FinishedState state) {
        return new RealmGuiEvent(realm_edit_finished, realm, state);
    }

    public static enum FinishedState {
        back,
        removed,
        saved;
    }
}
