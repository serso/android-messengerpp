package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:56 PM
 */
public enum RealmDefGuiEventType {

    /**
     * Fired when realm def is clicked in the list of realm definitions
     */
    realm_def_clicked,

    /**
     * Fired when editing of realm in this realm definition is finished: either by pressing back or by saving realm
     */
    realm_def_edit_finished;


    @Nonnull
    public static RealmDefGuiEvent newRealmDefClickedEvent(@Nonnull RealmDef realmDef) {
        return new RealmDefGuiEvent(realmDef, realm_def_clicked, null);
    }

    public static RealmDefGuiEvent newRealmDefEditFinishedEvent(@Nonnull RealmDef realmDef) {
        return new RealmDefGuiEvent(realmDef, realm_def_edit_finished, null);
    }
}
