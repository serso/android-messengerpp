package org.solovyev.android.messenger.notifications;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/5/13
 * Time: 6:25 PM
 */
public interface NotificationSolution {

	void solve(@Nonnull Notification notification);
}
