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

package org.solovyev.android.messenger.realms.vk.http;

import android.util.Log;
import org.solovyev.android.http.HttpTransaction;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.vk.VkError;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 2:04 PM
 */
public class VkResponseErrorException extends RuntimeException {

	@Nonnull
	private final VkError error;

	@Nonnull
	private final HttpTransaction<?> httpTransaction;

	@Nonnull
	public static VkResponseErrorException newInstance(@Nonnull String json, @Nonnull HttpTransaction<?> httpTransaction) {
		VkResponseErrorException result;
		try {
			result = new VkResponseErrorException(VkError.fromJson(json), httpTransaction);
		} catch (IllegalJsonException e) {
			result = handleException(json, httpTransaction, e);
		} catch (RuntimeException e) {
			result = handleException(json, httpTransaction, e);
		}
		return result;
	}

	@Nonnull
	private static VkResponseErrorException handleException(@Nonnull String json,
															@Nonnull HttpTransaction<?> httpTransaction,
															@Nonnull Exception e) {
		VkResponseErrorException result;
		Log.e(VkResponseErrorException.class.getSimpleName(), json);
		Log.e(VkResponseErrorException.class.getSimpleName(), e.getMessage());
		result = new VkResponseErrorException(VkError.newInstance("UnableToParseJson", "Unable to parse JSON from server!"), httpTransaction);
		return result;
	}

	public VkResponseErrorException(@Nonnull VkError error, @Nonnull HttpTransaction<?> httpTransaction) {
		this.error = error;
		this.httpTransaction = httpTransaction;
	}

	@Nonnull
	public VkError getError() {
		return this.error;
	}
}
