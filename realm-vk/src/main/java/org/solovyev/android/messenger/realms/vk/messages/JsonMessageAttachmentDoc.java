package org.solovyev.android.messenger.realms.vk.messages;

import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/13/12
 * Time: 7:24 PM
 */
public class JsonMessageAttachmentDoc implements JsonMessageAttachment{

    @Nullable
    private Integer did;

    @Nullable
    private Integer owner_id;

    @Nullable
    private String title;

    @Nullable
    private Integer size;

    @Nullable
    private String ext;

    @Nullable
    private String url;


}
