package org.solovyev.android.messenger;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.realms.Realm;

import com.google.inject.Singleton;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 9:42 PM
 */
@Singleton
public class TestMessengerConfiguration implements MessengerConfiguration {

	@Nonnull
	@Override
	public Collection<Realm> getRealms() {
		return Arrays.<Realm>asList();
	}
}
