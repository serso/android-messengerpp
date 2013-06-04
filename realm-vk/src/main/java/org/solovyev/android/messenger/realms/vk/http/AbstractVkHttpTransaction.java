package org.solovyev.android.messenger.realms.vk.http;

import android.util.Log;
import com.google.gson.JsonParseException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.realms.RealmRuntimeException;
import org.solovyev.android.messenger.realms.vk.VkRealm;

import javax.annotation.Nonnull;
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

	@Nonnull
	private final VkRealm realm;

	protected AbstractVkHttpTransaction(@Nonnull VkRealm realm, @Nonnull String method) {
		this(realm, method, HttpMethod.GET);
	}

	protected AbstractVkHttpTransaction(@Nonnull VkRealm realm, @Nonnull String method, @Nonnull HttpMethod httpMethod) {
		super(URI + method, httpMethod);
		this.realm = realm;
	}

	@Nonnull
	protected VkRealm getRealm() {
		return realm;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = new ArrayList<NameValuePair>();
		result.add(new BasicNameValuePair("access_token", getRealm().getConfiguration().getAccessToken()));
		return result;
	}

	@Override
	public R getResponse(@Nonnull HttpResponse response) {
		try {
			final HttpEntity httpEntity = response.getEntity();
			final String json = EntityUtils.toString(httpEntity);

			Log.d(AbstractVkHttpTransaction.class.getSimpleName(), "Json: " + json);

			try {
				return getResponseFromJson(json);
			} catch (JsonParseException e) {
				throw new RealmRuntimeException(realm.getId(), VkResponseErrorException.newInstance(json, this));
			} catch (IllegalJsonException e) {
				throw new RealmRuntimeException(realm.getId(), VkResponseErrorException.newInstance(json, this));
			}
		} catch (IOException e) {
			throw new HttpRuntimeIoException(e);
		}
	}

	protected abstract R getResponseFromJson(@Nonnull String json) throws IllegalJsonException;
}
