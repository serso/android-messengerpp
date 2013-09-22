package org.solovyev.android.messenger.users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 1:22 AM
 */
public enum UserEventType {
	added,
	changed,

	contact_added,
	contact_added_batch {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof List;
		}
	},
	// data == id of removed contact for current user
	contact_removed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof String;
		}
	},

	chat_added,
	chat_added_batch {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof List;
		}
	},
	// data == id of removed chat for current user
	chat_removed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof String;
		}
	},

	/**
	 * Fired when contact presence is changed to available/online
	 * Data: contact (User) - contact for which presence is changed
	 */
	contact_online {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof User;
		}
	},

	/**
	 * Fired when contact presence is changed to unavailable/offline
	 * Data: contact (User) - contact for which presence is changed
	 */
	contact_offline {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof User;
		}
	},

	/**
	 * Fires when contacts presence has changed,
	 * Data: list of contacts for whom presence have been changed
	 */
	contacts_presence_changed  {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof List;
		}
	},

	// Number of unread messages in private chat has changed
	unread_messages_count_changed {
		@Override
		protected void checkData(@Nullable Object data) {
			assert data instanceof Integer;
		}
	};

	@Nonnull
	public final UserEvent newEvent(@Nonnull User user) {
		return newEvent(user, null);
	}

	@Nonnull
	public final UserEvent newEvent(@Nonnull User user, @Nullable Object data) {
		checkData(data);
		return new UserEvent(user, this, data);
	}

	protected void checkData(@Nullable Object data) {
		assert data == null;
	}
}
