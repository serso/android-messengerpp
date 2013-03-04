package org.solovyev.android.messenger.realms.vk.messages;

import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/13/12
 * Time: 7:24 PM
 */
public class JsonMessageAttachmentAudio implements JsonMessageAttachment {

    @Nullable
    private Integer aid;

    @Nullable
    private Integer owner_id;

    @Nullable
    private String performer;

    @Nullable
    private String title;

    @Nullable
    private Integer duration;

    @Nullable
    private String url;

}
