package org.solovyev.android.messenger.vk.messages;

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
