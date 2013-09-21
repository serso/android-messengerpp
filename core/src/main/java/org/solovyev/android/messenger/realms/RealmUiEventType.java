package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:56 PM
 */
public enum RealmUiEventType {

	/**
	 * Fired when realm is clicked in the list of realms
	 */
	realm_clicked,

	/**
	 * Fired when editing of account in this realm is finished: either by pressing back or by saving account
	 */
	account_edit_finished;


	@Nonnull
	public static RealmUiEvent newRealmClickedEvent(@Nonnull Realm realm) {
		return new RealmUiEvent(realm, realm_clicked, null);
	}

	public static RealmUiEvent newAccountEditFinishedEvent(@Nonnull Realm realm) {
		return new RealmUiEvent(realm, account_edit_finished, null);
	}
}
