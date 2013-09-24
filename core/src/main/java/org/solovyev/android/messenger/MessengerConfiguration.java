package org.solovyev.android.messenger;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.realms.Realm;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:24 PM
 */
public interface MessengerConfiguration {

	@Nonnull
	Collection<Realm> getRealms();

}
