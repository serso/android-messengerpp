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
