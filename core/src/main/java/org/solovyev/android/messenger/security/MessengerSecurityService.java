package org.solovyev.android.messenger.security;

import org.solovyev.common.security.SecurityService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import static org.solovyev.android.messenger.App.newTag;

public interface MessengerSecurityService {

	@Nonnull
	String TAG = newTag("SecurityService");

	@Nonnull
	SecurityService<byte[], byte[], byte[]> getSecurityService();

	@Nonnull
	SecurityService<String, String, String> getStringSecurityService();

	@Nullable
	SecretKey getSecretKey();
}
