package org.solovyev.android.messenger.vk.http;

import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.RuntimeIoException;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.ServiceLocator;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 1:13 PM
 */
public abstract class AbstractVkHttpTransaction<R> extends AbstractHttpTransaction<R> {

    private static final String URI = "https://api.vkontakte.ru/method/";

    protected AbstractVkHttpTransaction(@NotNull String method) {
        this(method, HttpMethod.GET);
    }

    protected AbstractVkHttpTransaction(@NotNull String method, @NotNull HttpMethod httpMethod) {
        super(URI + method, httpMethod);
    }

    @NotNull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final ArrayList<NameValuePair> result = new ArrayList<NameValuePair>();
        try {
            result.add(new BasicNameValuePair("access_token", getServiceLocator().getAuthServiceFacade().getAuthData().getAccessToken()));
        } catch (UserIsNotLoggedInException e) {
            // todo serso: think
        }
        return result;
    }

    @NotNull
    private ServiceLocator getServiceLocator() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator();
    }

    @Override
    public R getResponse(@NotNull HttpResponse response) {
        try {
            final HttpEntity httpEntity = response.getEntity();
            final String json = EntityUtils.toString(httpEntity);

            Log.d(AbstractVkHttpTransaction.class.getSimpleName(), "Json: " + json);

            try {
                return getResponseFromJson(json);
            } catch (IllegalJsonException e) {
                throw VkResponseErrorException.newInstance(json, this);
            }
        } catch (IOException e) {
            throw new RuntimeIoException(e);
        }
    }

    protected abstract R getResponseFromJson(@NotNull String json) throws IllegalJsonException;
}
