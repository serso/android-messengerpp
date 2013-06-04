package org.solovyev.android.messenger.realms.vk.auth;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.solovyev.android.http.AbstractHttpTransaction;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.http.HttpRuntimeIoException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 11:30 PM
 */
final class VkLoginHttpTransaction extends AbstractHttpTransaction<Void> {

	@Nonnull
	private final String login;

	@Nonnull
	private final String password;

	public VkLoginHttpTransaction(@Nonnull String login, @Nonnull String password) {
		super("http://vk.com/login.php", HttpMethod.GET);
		this.login = login;
		this.password = password;
	}

	@Override
	public Void getResponse(@Nonnull HttpResponse response) {
		boolean ok = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
		if (!ok) {
			throw new HttpRuntimeIoException(new IOException());
		}
		return null;
	}

	@Nonnull
	@Override
	public List<NameValuePair> getRequestParameters() {
		final List<NameValuePair> result = new ArrayList<NameValuePair>();
		result.add(new BasicNameValuePair("email", login));
		result.add(new BasicNameValuePair("pass", password));
		return result;
	}
}
