package org.solovyev.android.messenger.realms.vk.auth;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.RealmRuntimeException;
import org.solovyev.android.messenger.realms.vk.http.VkResponseErrorException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* User: serso
* Date: 4/13/13
* Time: 11:26 PM
*/
final class VkAccessTokenHttpTransaction extends AbstractHttpTransaction<JsonAuthResult> {

    // todo serso: add VkConfiguration

    @Nonnull
    private final String code;

    public VkAccessTokenHttpTransaction(@Nonnull String code) {
        super("https://api.vk.com/oauth/access_token", HttpMethod.GET);
        this.code = code;
    }

    @Nonnull
    @Override
    public JsonAuthResult getResponse(@Nonnull HttpResponse response) {
        boolean ok = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        if (!ok) {
            throw new RealmRuntimeException();
        }

        final String json;
        try {
            json = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }

        try {
            return JsonAuthResult.fromJson(json);
        } catch (IllegalJsonException e) {
            throw VkResponseErrorException.newInstance(json, this);
        }
    }

    @Nonnull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> result = new ArrayList<NameValuePair>();
        result.add(new BasicNameValuePair("client_id", "2970921"));
        result.add(new BasicNameValuePair("client_secret", "Scm7M1vxOdDjpeVj81jw"));
        result.add(new BasicNameValuePair("code", code));
        result.add(new BasicNameValuePair("redirect_uri", "http://oauth.vk.com/blank.html"));
        return result;
    }
}
