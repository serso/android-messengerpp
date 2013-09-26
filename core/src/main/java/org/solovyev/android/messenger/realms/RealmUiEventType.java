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
	realm_edit_finished;

	@Nonnull
	public RealmUiEvent newEvent(@Nonnull Realm realm) {
		return new RealmUiEvent(realm, this, null);
	}
}
