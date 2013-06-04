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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 4/13/13
 * Time: 11:44 PM
 */
final class VkSubmitApprovalFormHttpTransaction extends AbstractHttpTransaction<String> {

	private final Element approvalForm;

	public VkSubmitApprovalFormHttpTransaction(Element approvalForm) {
		super(approvalForm.attr("action"), HttpMethod.POST);
		this.approvalForm = approvalForm;
	}

	@Override
	public String getResponse(@Nonnull HttpResponse response) {
		boolean ok = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
		if (!ok) {
			throw new HttpRuntimeIoException(new IOException());
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
		for (Element input : approvalForm.getElementsByTag("input")) {
			result.add(new BasicNameValuePair(input.attr("name"), input.val()));
		}
		return result;
	}
}
