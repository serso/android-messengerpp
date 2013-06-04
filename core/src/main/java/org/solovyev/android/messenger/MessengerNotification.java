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

	@Nullable
	private final Runnable oneClickSolution;

	private MessengerNotification(@Nonnull Context context, int messageResId, @Nonnull MessageLevel messageType, @Nullable Runnable oneClickSolution, @Nullable Object... parameters) {
		super(String.valueOf(messageResId), messageType, parameters);
		this.context = context;
		this.oneClickSolution = oneClickSolution;
	}

	private MessengerNotification(@Nonnull Context context, int messageResId, @Nonnull MessageLevel messageType, @Nonnull List<?> parameters, @Nullable Runnable oneClickSolution) {
		super(String.valueOf(messageResId), messageType, parameters);
		this.context = context;
		this.oneClickSolution = oneClickSolution;
	}

	@Nonnull
	public static MessengerNotification newInstance(@Nonnull Context context, int messageResId, @Nonnull MessageLevel messageType, @Nullable Runnable oneClickSolution, @Nullable Object... parameters) {
		return new MessengerNotification(context, messageResId, messageType, oneClickSolution, parameters);
	}

	@Nonnull
	public static MessengerNotification newInstance(@Nonnull Context context, int messageResId, @Nonnull MessageLevel messageType, @Nullable Runnable oneClickSolution, @Nonnull List<?> parameters) {
		return new MessengerNotification(context, messageResId, messageType, parameters, oneClickSolution);
	}

	@Nullable
	@Override
	protected String getMessagePattern(@Nonnull Locale locale) {
		final int messageResId = Integer.valueOf(getMessageCode());
		final List<Object> parameters = getParameters();
		return context.getString(messageResId, parameters.toArray(new Object[parameters.size()]));
	}

	public void solveOnClick() {
		if (oneClickSolution != null) {
			oneClickSolution.run();
		}
	}
}
