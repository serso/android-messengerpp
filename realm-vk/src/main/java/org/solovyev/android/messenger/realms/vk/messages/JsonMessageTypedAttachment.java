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

package org.solovyev.android.messenger.realms.vk.messages;

import com.google.gson.*;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * User: serso
 * Date: 6/13/12
 * Time: 7:19 PM
 */
public class JsonMessageTypedAttachment {

	@Nullable
	private String type;

	@Nullable
	private JsonMessageAttachment attachment;

	public static class Adapter implements JsonDeserializer<JsonMessageTypedAttachment> {

		@Override
		public JsonMessageTypedAttachment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			final JsonMessageTypedAttachment result = new JsonMessageTypedAttachment();

			if (json.isJsonObject()) {
				final JsonObject typedAttachment = json.getAsJsonObject();

				final String type = typedAttachment.getAsJsonPrimitive("type").getAsString();

				if ("photo".equals(type)) {

				} else if ("audio".equals(type)) {

				} else if ("video".equals(type)) {

				} else if ("doc".equals(type)) {

				} else {

				}

			} else {
				throw new JsonParseException("Unexpected JSON type: " + json.getClass());
			}

			return result;
		}
	}

}
