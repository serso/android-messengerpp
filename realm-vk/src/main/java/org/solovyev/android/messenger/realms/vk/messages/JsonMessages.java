package org.solovyev.android.messenger.realms.vk.messages;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
