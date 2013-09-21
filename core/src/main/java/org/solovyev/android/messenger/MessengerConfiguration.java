package org.solovyev.android.messenger;

import org.solovyev.android.messenger.realms.Realm;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:24 PM
 */
public interface MessengerConfiguration {

	@Nonnull
	Collection<Realm> getRealms();

}
