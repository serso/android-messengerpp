/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.solovyev.android.messenger.security.MessengerSecurityService;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.SecurityService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSecurityService implements MessengerSecurityService {
	@Nonnull
	@Override
	public SecurityService<byte[], byte[], byte[]> getSecurityService() {
		return createMockSecurityService(byte[].class, byte[].class);
	}

	@Nonnull
	@Override
	public SecurityService<String, String, String> getStringSecurityService() {
		return createMockSecurityService(String.class, String.class);
	}

	private <E, D, H> SecurityService<E, D, H> createMockSecurityService(@Nonnull Class<? extends E> e, @Nonnull Class<? extends D> d) {
		final SecurityService<E, D, H> securityService = mock(SecurityService.class);
		final Cipherer<E, D> cipherer = mock(Cipherer.class);
		when(cipherer.decrypt(any(SecretKey.class), any(e))).thenAnswer(new Answer<D>() {
			@Override
			public D answer(InvocationOnMock invocationOnMock) throws Throwable {
				return (D) invocationOnMock.getArguments()[1];
			}
		});
		when(cipherer.encrypt(any(SecretKey.class), any(d))).thenAnswer(new Answer<E>() {
			@Override
			public E answer(InvocationOnMock invocationOnMock) throws Throwable {
				return (E) invocationOnMock.getArguments()[1];
			}
		});
		when(securityService.getCipherer()).thenReturn(cipherer);
		return securityService;
	}

	@Nullable
	@Override
	public SecretKey getSecretKey() {
		return mock(SecretKey.class);
	}
}
