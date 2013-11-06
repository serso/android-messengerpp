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

package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.msg.Message;

/**
 * User: serso
 * Date: 3/23/13
 * Time: 5:17 PM
 */

/**
 * Common messenger events
 */
public enum MessengerEventType {

	// data == number of unread messages
	unread_messages_count_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Integer;
		}
	},

	notification_removed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Message;
		}
	},

	notification_added {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Message;
		}
	};

	@Nonnull
	public final MessengerEvent newEvent(@Nullable Object data) {
		checkData(data);
		return new MessengerEvent(this, data);
	}

	protected void checkData(@Nullable Object data) {
		assert data == null;
	}
}
