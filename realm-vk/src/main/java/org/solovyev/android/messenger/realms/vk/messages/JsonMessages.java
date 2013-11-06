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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:35 PM
 */
public class JsonMessages {

	@Nullable
	private Integer count;

	@Nullable
	private List<JsonMessage> response;

	@Nullable
	public List<JsonMessage> getResponse() {
		return response;
	}

	public static class Adapter implements JsonDeserializer<JsonMessages> {

		@Override
		public JsonMessages deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			final JsonMessages result = new JsonMessages();

			if (json.isJsonObject()) {
				final JsonObject response = json.getAsJsonObject();
				final JsonArray responseArray = response.getAsJsonArray("response");
				if (responseArray != null && responseArray.isJsonArray()) {
					boolean first = true;

					result.response = new ArrayList<JsonMessage>();
					for (JsonElement e : responseArray.getAsJsonArray()) {
						if (first) {
							result.count = e.getAsInt();
							first = false;
						} else {
							result.response.add((JsonMessage) context.deserialize(e, JsonMessage.class));
						}
					}
				} else {
					throw new JsonParseException("Unexpected JSON type: " + (responseArray == null ? null : responseArray.getClass()));
				}

			} else {
				throw new JsonParseException("Unexpected JSON type: " + json.getClass());
			}

			return result;
		}
	}

	@Nonnull
	public Integer getCount() {
		return count == null ? 0 : count;
	}
}
