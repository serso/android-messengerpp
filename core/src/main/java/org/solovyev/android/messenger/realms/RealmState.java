package org.solovyev.android.messenger.realms;

public enum RealmState {
	enabled,

	/**
	 * Temporary state, indicates that user requested realm removal, but it cannot be done instantly => this states indicates that realm will be removed soon (e.g. on the next app boot)
	 */
	removed,

	/**
	 * Realm may be disable by app due to some error occurred in it (e.g. connection problems)
	 * NOTE: this state is reset every start up
	 */
	disabled_by_app,


	disabled_by_user;
}
