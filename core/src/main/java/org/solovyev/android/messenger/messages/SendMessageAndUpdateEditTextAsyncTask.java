package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.chats.Chat;

class SendMessageAndUpdateEditTextAsyncTask extends SendMessageAsyncTask {

	@Nonnull
	private final WeakReference<EditText> messageBodyRef;

	public SendMessageAndUpdateEditTextAsyncTask(@Nonnull Activity activity, @Nonnull EditText messageBody, @Nonnull Chat chat) {
		super(activity, chat);
		this.messageBodyRef = new WeakReference<EditText>(messageBody);
	}

	@Override
	protected void onSuccessPostExecute(@Nullable List<ChatMessage> result) {
		super.onSuccessPostExecute(result);
		final EditText messageBody = messageBodyRef.get();
		if (messageBody != null) {
			messageBody.setText("");
		}
	}
}
