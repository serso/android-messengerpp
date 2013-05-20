package org.solovyev.android.messenger.realms.vk.messages;

import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/13/12
 * Time: 7:24 PM
 */
public class JsonMessageAttachmentVideo implements JsonMessageAttachment {

	@Nullable
	private Integer vid;

	@Nullable
	private Integer owner_id;

	@Nullable
	private String title;

	@Nullable
	private String description;

	@Nullable
	private Integer duration;

	@Nullable
	private String image;

	@Nullable
	private String image_big;

	@Nullable
	private String image_small;

	@Nullable
	private Integer views;

	@Nullable
	private String date;

}
