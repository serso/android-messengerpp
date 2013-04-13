package org.solovyev.android.messenger.realms.vk.auth;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.realms.RealmRuntimeException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* User: serso
* Date: 4/13/13
* Time: 11:27 PM
*/
final class VkAuthorizationHttpTransaction extends AbstractHttpTransaction<String> {

    public VkAuthorizationHttpTransaction() {
        super("http://api.vk.com/oauth/authorize", HttpMethod.GET);
    }

    @Override
    public String getResponse(@Nonnull HttpResponse response) {
        boolean ok = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
        if (!ok) {
            throw new RealmRuntimeException();
        }

        try {
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }

    @Nonnull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> result = new ArrayList<NameValuePair>();
        result.add(new BasicNameValuePair("client_id", "2970921"));
        result.add(new BasicNameValuePair("redirect_uri", "http://oauth.vk.com/blank.html"));
        result.add(new BasicNameValuePair("response_type", "code"));
        return result;
    }
}
