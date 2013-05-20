package org.solovyev.android.messenger;

import android.content.Context;
import org.solovyev.common.msg.AbstractMessage;
import org.solovyev.common.msg.MessageLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public final class MessengerNotification extends AbstractMessage {

	@Nonnull
	private final Context context;

	public MessengerNotification(@Nonnull Context context, int messageResId, @Nonnull MessageLevel messageType, @Nullable Object... parameters) {
		super(String.valueOf(messageResId), messageType, parameters);
		this.context = context;
	}

	public MessengerNotification(@Nonnull Context context, int messageResId, @Nonnull MessageLevel messageType, @Nonnull List<?> parameters) {
		super(String.valueOf(messageResId), messageType, parameters);
		this.context = context;
	}

	@Nullable
	@Override
	protected String getMessagePattern(@Nonnull Locale locale) {
		final int messageResId = Integer.valueOf(getMessageCode());
		final List<Object> parameters = getParameters();
		return context.getString(messageResId, parameters.toArray(new Object[parameters.size()]));
	}
}
