package org.solovyev.android.messenger;

import org.solovyev.android.messenger.security.MessengerSecurityService;
import org.solovyev.common.security.SecurityService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import static org.mockito.Mockito.mock;

public class TestSecurityService implements MessengerSecurityService {
	@Nonnull
	@Override
	public SecurityService<byte[], byte[], byte[]> getSecurityService() {
		return mock(SecurityService.class);
	}

	@Nonnull
	@Override
	public SecurityService<String, String, String> getStringSecurityService() {
		return mock(SecurityService.class);
	}

	@Nullable
	@Override
	public SecretKey getSecretKey() {
		return mock(SecretKey.class);
	}
}
