package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.TestRealm;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class TestConfiguration implements Configuration {

	@Inject
	@Nonnull
	private TestRealm realm;

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		final List<Realm> realms = new ArrayList<Realm>();
		realms.add(realm);
		return realms;
	}
}
