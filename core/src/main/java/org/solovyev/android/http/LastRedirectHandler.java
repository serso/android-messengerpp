/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.http;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 11:33 PM
 */
public final class LastRedirectHandler extends DefaultRedirectHandler {

	@Nullable
	public URI lastRedirectedUri;

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
