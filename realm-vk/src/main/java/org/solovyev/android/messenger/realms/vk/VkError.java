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

package org.solovyev.android.messenger.realms.vk;

import com.google.gson.Gson;
import org.solovyev.android.captcha.Captcha;
import org.solovyev.android.messenger.http.IllegalJsonException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 2:05 PM
 */
public class VkError {

	@Nonnull
	private String errorId;

	@Nullable
	private String errorDescription;

	@Nullable
	private Captcha captcha;

	@Nonnull
	public static VkError newInstance(@Nonnull String errorId, @Nullable String errorDescription) {
		final VkError result = new VkError();

		result.errorId = errorId;
		result.errorDescription = errorDescription;

		return result;
	}

	@Nonnull
	public static VkError fromJson(@Nonnull String json) throws IllegalJsonException {
		final Gson gson = new Gson();
		final VkErrorJsonWrapper vkErrorJsonWrapper = gson.fromJson(json, VkErrorJsonWrapper.class);
		if (vkErrorJsonWrapper.error == null) {
			throw new IllegalJsonException();
		} else if (vkErrorJsonWrapper.error.error_code == null) {
			throw new IllegalJsonException();
		}
		return fromJson(vkErrorJsonWrapper.error);
	}

	@Nonnull
	private static VkError fromJson(@Nonnull VkErrorJson json) throws IllegalJsonException {
		final VkError result = new VkError();

		result.errorId = json.error_code;
		result.errorDescription = json.error_msg;
		if (json.captcha_sid != null) {
			if (json.captcha_img == null) {
				throw new IllegalJsonException();
			}
			result.captcha = new Captcha(json.captcha_sid, json.captcha_img);
		}

		return result;
	}

	private static class VkErrorJsonWrapper {

		@Nullable
		private VkErrorJson error;

	}

	private static class VkErrorJson {

		@Nullable
		private String error_code;

		@Nullable
		private String error_msg;

		@Nullable
		private String captcha_sid;

		@Nullable
		private String captcha_img;

	}

	@Nonnull
	public String getErrorId() {
		return errorId;
	}

	@Nullable
	public String getErrorDescription() {
		return errorDescription;
	}

	@Nullable
	public Captcha getCaptcha() {
		return captcha;
	}
}
