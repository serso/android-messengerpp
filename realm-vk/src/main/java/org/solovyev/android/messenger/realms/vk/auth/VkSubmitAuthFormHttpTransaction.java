package org.solovyev.android.messenger.realms.vk.auth;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Element;
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
 * Time: 11:29 PM
 */
final class VkSubmitAuthFormHttpTransaction extends AbstractHttpTransaction<String> {

	@Nonnull
	private final Element authForm;

	@Nonnull
	private final String login;

	@Nonnull
	private final String password;

	public VkSubmitAuthFormHttpTransaction(@Nonnull Element authForm, @Nonnull String login, @Nonnull String password) {
		super(authForm.attr("action"), HttpMethod.POST);
		this.authForm = authForm;
		this.login = login;
		this.password = password;
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

		for (Element input : authForm.getElementsByTag("input")) {
			result.add(new BasicNameValuePair(input.attr("name"), input.val()));
		}
		result.add(new BasicNameValuePair("email", login));
		result.add(new BasicNameValuePair("pass", password));

		return result;
	}
}
