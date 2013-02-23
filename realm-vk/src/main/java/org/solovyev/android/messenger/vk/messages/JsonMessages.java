package org.solovyev.android.messenger.vk.messages;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                throw new JsonParseException("Unexpected JSON type: " + json.getClass());
            }

            return result;
        }
    }

    @NotNull
    public Integer getCount() {
        return count == null ? 0 : count;
    }
}
