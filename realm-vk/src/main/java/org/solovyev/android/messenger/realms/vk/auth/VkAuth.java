package org.solovyev.android.messenger.realms.vk.auth;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.solovyev.android.http.AHttpClient;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.http.LastRedirectHandler;
import org.solovyev.android.messenger.accounts.AccountRuntimeException;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 11:30 PM
 */
public final class VkAuth {

	private VkAuth() {
		throw new AssertionError();
	}

	@Nonnull
	public static JsonAuthResult doOauth2Authorization(@Nonnull String login, @Nonnull String password) throws InvalidCredentialsException {
		try {
			final AHttpClient httpClient = HttpTransactions.newHttpClient();

			final LastRedirectHandler redirectHandler = new LastRedirectHandler();
			httpClient.getHttpClient().setRedirectHandler(redirectHandler);

			final String code;

			final Element authForm = requestAuthorizationForm(httpClient, login, password);
			if (authForm != null) {
				final Element approvalForm = requestApprovalForm(httpClient, authForm, login, password);
				if (approvalForm != null) {
					approveRequest(httpClient, approvalForm);
					code = tryGetCode(redirectHandler);
				} else {
					code = tryGetCode(redirectHandler);
				}
			} else {
				code = tryGetCode(redirectHandler);
			}

			return requestAccessToken(httpClient, code);

		} catch (AccountRuntimeException e) {
			throw new InvalidCredentialsException(e);
		} catch (IOException e) {
			throw new InvalidCredentialsException(e);
		}
	}

	@Nonnull
	private static JsonAuthResult requestAccessToken(AHttpClient httpClient, @Nonnull final String code) throws IOException {
		return httpClient.execute(new VkAccessTokenHttpTransaction(code));
	}

	@Nonnull
	private static String tryGetCode(@Nonnull LastRedirectHandler redirectHandler) throws InvalidCredentialsException {
		final String code = extractCode(redirectHandler.getLastRedirectedUri());
		if (code != null) {
			return code;
		} else {
			throw new InvalidCredentialsException();
		}
	}

	private static void approveRequest(@Nonnull AHttpClient httpClient, @Nonnull final Element approvalForm) throws IOException {
		httpClient.execute(new VkSubmitApprovalFormHttpTransaction(approvalForm));
	}

	@Nullable
	private static String extractCode(@Nullable URI uri) {
		if (uri == null) {
			return null;
		}

		final String result;
		final String fragment = uri.getFragment();
		if (fragment == null) {
			result = null;
		} else {
			if (fragment.startsWith("code=")) {
				result = fragment.substring(5);
			} else {
				result = null;
			}
		}

		return result;
	}

	@Nullable
	private static Element requestApprovalForm(AHttpClient httpClient, final Element authForm, @Nonnull String login, @Nonnull String password) throws IOException {
		final String html = httpClient.execute(new VkSubmitAuthFormHttpTransaction(authForm, login, password));

		final Document htmlDocument = Jsoup.parse(html);
		final Elements forms = htmlDocument.getElementsByTag("form");
		final Element approvalForm;
		if (forms.size() > 0) {
			approvalForm = forms.get(0);
		} else {
			approvalForm = null;
		}
		return approvalForm;
	}

	@Nullable
	private static Element requestAuthorizationForm(@Nonnull AHttpClient httpClient, @Nonnull String login, @Nonnull String password) throws IOException {
		httpClient.execute(new VkLoginHttpTransaction(login, password));

		final String html = httpClient.execute(new VkAuthorizationHttpTransaction());

		final Document htmlDocument = Jsoup.parse(html);
		final Elements forms = htmlDocument.getElementsByTag("form");
		final Element form;
		if (forms.size() > 0) {
			form = forms.get(0);
		} else {
			form = null;
		}
		return form;
	}

}
