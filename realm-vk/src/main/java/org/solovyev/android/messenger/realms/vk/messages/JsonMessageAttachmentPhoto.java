package org.solovyev.android.messenger.realms.vk.messages;

import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/13/12
 * Time: 7:24 PM
 */
public class JsonMessageAttachmentPhoto implements JsonMessageAttachment {

	@Nullable
	private Integer pid;

	@Nullable
	private Integer owner_id;

	@Nullable
	private String src;

	@Nullable
	private String src_big;
}
