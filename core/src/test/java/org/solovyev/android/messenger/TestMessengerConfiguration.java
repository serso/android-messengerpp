package org.solovyev.android.messenger;

import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.TestRealm;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 9:42 PM
 */
public class TestMessengerConfiguration implements MessengerConfiguration {

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		final List<Realm> realms = new ArrayList<Realm>();
		realms.add(new TestRealm());
		return realms;
	}
}
