package org.solovyev.android.messenger.vk.secutiry;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.AuthDataImpl;
import org.solovyev.android.messenger.vk.VkConfigurationImpl;
import org.solovyev.android.messenger.vk.http.VkResponseErrorException;
import org.solovyev.common.text.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 5/28/12
 * Time: 1:19 PM
 */
public class VkAuthenticationHttpTransaction
        extends AbstractHttpTransaction<AuthData> {

    @NotNull
    private final String login;

    @NotNull
    private final String password;

    @Nullable
    private final ResolvedCaptcha captcha;

    public VkAuthenticationHttpTransaction(@NotNull String login, @NotNull String password) {
        this(login, password, null);
    }

    public VkAuthenticationHttpTransaction(@NotNull String login, @NotNull String password, @Nullable ResolvedCaptcha captcha) {
        super("https://api.vk.com/oauth/token", HttpMethod.GET);
        this.login = login;
        this.password = password;
        this.captcha = captcha;
    }

    @NotNull
    @Override
    public List<NameValuePair> getRequestParameters() {
        final List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("grant_type", "password"));
        params.add(new BasicNameValuePair("client_id", VkConfigurationImpl.getInstance().getClientId()));
        params.add(new BasicNameValuePair("client_secret", VkConfigurationImpl.getInstance().getClientSecret()));
        params.add(new BasicNameValuePair("username", this.login));
        params.add(new BasicNameValuePair("password", this.password));
        params.add(new BasicNameValuePair("scope", VkAuthScopeParam.getAllFieldsRequestParameter()));
        if (captcha != null) {
            params.add(new BasicNameValuePair("captcha_sid", this.captcha.getCaptchaSid()));
            params.add(new BasicNameValuePair("captcha_key", this.captcha.getCaptchaKey()));
        }

        return params;
    }

    @NotNull
    @Override
    public AuthData getResponse(@NotNull HttpResponse response) {

        try {
            final String json = Strings.convertStream(response.getEntity().getContent());
            try {
                return JsonResult.toAuthResult(json, login);
            } catch (IllegalJsonException e) {
                throw VkResponseErrorException.newInstance(json, this);
            }
        } catch (IOException e) {
            throw new HttpRuntimeIoException(e);
        }
    }

    private static class JsonResult {

        @Nullable
        private String access_token;

        @Nullable
        private Integer expires_in;

        @Nullable
        private String user_id;

        @NotNull
        private static AuthData toAuthResult(@NotNull JsonResult jsonResult, @NotNull String login) {
            final AuthDataImpl result = new AuthDataImpl();

            result.setAccessToken(jsonResult.access_token);
            result.setRealmUserId(jsonResult.user_id);
            result.setRealmUserLogin(login);

            return result;
        }

        @NotNull
        public static AuthData toAuthResult(@NotNull String json, @NotNull String login) throws IllegalJsonException {
            final Gson gson = new Gson();

            final JsonResult jsonResult = gson.fromJson(json, JsonResult.class);

            if (jsonResult.access_token == null || jsonResult.expires_in == null || jsonResult.user_id == null) {
                throw new IllegalJsonException();
            }

            return toAuthResult(jsonResult, login);
        }
    }
}
