package org.solovyev.android.messenger.vk.longpoll;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.longpoll.LongPollResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:23 AM
 */
public class VkGetLongPollingDataHttpTransaction extends AbstractHttpTransaction<LongPollResult> {

    @NotNull
    private final LongPollServerData longPollServerData;

    VkGetLongPollingDataHttpTransaction(@NotNull LongPollServerData longPollServerData) {
        super("http://" + longPollServerData.getServerUri(), HttpMethod.GET);
        this.longPollServerData = longPollServerData;
    }

    @Override
    public LongPollResult getResponse(@NotNull HttpResponse response) {
        try {
            final HttpEntity httpEntity = response.getEntity();
            final String json = EntityUtils.toString(httpEntity);
            Log.i("LongPolling", json);

            // prepare builder
            final GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(LongPollUpdate.class, new LongPollUpdate.Adapter());
            final Gson gson = builder.create();

            final JsonLongPollData jsonLongPollData = gson.fromJson(json, JsonLongPollData.class);
            return jsonLongPollData.toResult();
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        } catch (IllegalJsonException e) {
            throw new IllegalJsonRuntimeException(e);
        }
    }

    @NotNull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> result = new ArrayList<NameValuePair>();

        result.add(new BasicNameValuePair("act", "a_check"));
        result.add(new BasicNameValuePair("key", longPollServerData.getKey()));
        result.add(new BasicNameValuePair("ts", String.valueOf(longPollServerData.getTimeStamp())));
        result.add(new BasicNameValuePair("wait", "25"));
        result.add(new BasicNameValuePair("mode", "0"));
        //todo serso: check if necessary
        result.add(new BasicNameValuePair(CoreConnectionPNames.CONNECTION_TIMEOUT, "30000"));

        return result;
    }
}
