package org.solovyev.android.messenger.realms.vk.messages;

import java.lang.reflect.Type;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
