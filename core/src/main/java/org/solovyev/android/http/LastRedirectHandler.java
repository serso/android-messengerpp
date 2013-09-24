package org.solovyev.android.http;

import java.net.URI;

import javax.annotation.Nullable;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 11:33 PM
 */
public final class LastRedirectHandler extends DefaultRedirectHandler {

	@Nullable
	public URI lastRedirectedUri;

	@Override
	public boolean isRedirectRequested(HttpResponse response, HttpContext context) {

		return super.isRedirectRequested(response, context);
	}

	@Override
	public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
		lastRedirectedUri = super.getLocationURI(response, context);
		return lastRedirectedUri;
	}

	@Nullable
	public URI getLastRedirectedUri() {
		return lastRedirectedUri;
	}
}
