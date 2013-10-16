package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.google.inject.Inject;
import org.solovyev.android.Activities;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.*;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEvent;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.users.ContactUiEvent;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.messenger.view.PublicPullToRefreshListView;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.PullToRefreshListViewProvider;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;
import static org.solovyev.android.messenger.messages.MessageListItem.newMessageListItem;
import static org.solovyev.android.messenger.messages.UiMessageSender.trySendMessage;
import static org.solovyev.android.messenger.notifications.Notifications.newUndefinedErrorNotification;
import static org.solovyev.common.text.Strings.isEmpty;
import static org.solovyev.common.text.Strings.toHtml;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:38 PM
 */
public final class MessagesFragment extends AbstractListFragment<Message, MessageListItem> implements PullToRefreshListViewProvider {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/
	public static final String FRAGMENT_TAG = "messages";

    /*
	**********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

	@Inject
	@Nonnull
	private ChatService chatService;

	@Inject
	@Nonnull
	private ImageLoader imageLoader;

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private NotificationService notificationService;


    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */


	@Nonnull
	private static final String TAG = "MessagesFragment";

	@Nonnull
	private static final String CHAT = "chat";

	private Chat chat;

	private Account account;

	@Nullable
	private JEventListener<ChatEvent> chatEventListener;


	private EditText messageBody;

	public MessagesFragment() {
		super(TAG, false, false);
	}

	public MessagesFragment(@Nonnull Chat chat) {
		super(TAG, false, false);
		this.chat = chat;
	}

	@Nonnull
	public Chat getChat() {
		return chat;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			if (chat != null) {
				// chat is set => fragment was just created => we need to load realm
				account = accountService.getAccountById(chat.getEntity().getAccountId());
			} else {
				// first - restore state
				final Entity realmChat = savedInstanceState.getParcelable(CHAT);
				if (realmChat != null) {
					chat = this.chatService.getChatById(realmChat);
				}

				if (chat == null) {
					Log.e(TAG, "Chat is null: unable to find chat with id: " + realmChat);
					notificationService.add(newUndefinedErrorNotification());
					Activities.restartActivity(getActivity());
				} else {
					account = accountService.getAccountById(chat.getEntity().getAccountId());
				}
			}
		} catch (UnsupportedAccountException e) {
			App.getExceptionHandler().handleException(e);
			Activities.restartActivity(getActivity());
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// then call parent
		super.onActivityCreated(savedInstanceState);

		chatEventListener = new UiThreadUserChatListener();
		this.chatService.addListener(chatEventListener);
	}

	@Override
	public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup root = super.onCreateView(inflater, container, savedInstanceState);
		final Context context = getThemeContext();

		final View messageLayoutParent = ViewFromLayoutBuilder.newInstance(R.layout.mpp_list_item_message_editor).build(context);
		if(!account.canSendMessage(chat)) {
			messageLayoutParent.setVisibility(View.GONE);
		}

		final EditText messageText = (EditText) messageLayoutParent.findViewById(R.id.mpp_message_bubble_body_edittext);
		messageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == IME_ACTION_SEND) {
					sendMessage();
					handled = true;
				}
				return handled;
			}
		});

		root.addView(messageLayoutParent, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

		return root;
	}

	@Override
	public void onViewCreated(View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final View sendButton = root.findViewById(R.id.mpp_message_bubble_send_button);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});

		messageBody = (EditText) root.findViewById(R.id.mpp_message_bubble_body_edittext);
		messageBody.setText(getChatService().getDraftMessage(chat));
		messageBody.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (isEmpty(s)) {
					sendButton.setEnabled(false);
				} else {
					sendButton.setEnabled(true);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		// change title
		if (!getMultiPaneManager().isDualPane(getActivity())) {
			final com.actionbarsherlock.app.ActionBar actionBar = getSherlockActivity().getSupportActionBar();
			//actionBar.setIcon();
			if (chat.isPrivate()) {
				actionBar.setTitle(getString(R.string.mpp_private_chat_title, Users.getDisplayNameFor(chat.getSecondUser())));
			} else {
				actionBar.setTitle(getString(R.string.mpp_public_chat_title));
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getListeners().add(ContactUiEvent.class, new EventListener<ContactUiEvent>() {
			@Override
			public void onEvent(ContactUiEvent event) {
				switch (event.getType()) {
					case resend_message:
						sendMessage(event.getContact());
						break;
				}
			}
		});
		getListeners().add(MessageUiEvent.class, new EventListener<MessageUiEvent>() {
			@Override
			public void onEvent(MessageUiEvent event) {
				switch (event.getType()) {
					case quote:
						quoteMessage(event.getMessage());
						break;
				}
			}
		});
	}

	private void quoteMessage(@Nonnull Message message) {
		if (messageBody != null && !isEmpty(message.getBody())) {
			messageBody.append("\"" + message.getBody() + "\"");
		}
	}

	private void sendMessage() {
		sendMessage(null);
	}

	private void sendMessage(@Nullable User contact) {
		if (messageBody != null) {
			sendMessage(messageBody, contact);
		}
	}

	private void sendMessage(@Nonnull EditText messageEditText, @Nullable User recipient) {
		final Message message = trySendMessage(getActivity(), account, chat, recipient, toHtml(messageEditText.getText()));
		if (message != null) {
			messageEditText.setText("");
			getAdapter().addSendingMessage(message);
		}
	}

	@Nonnull
	private User getUser() {
		return account.getUser();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (chatEventListener != null) {
			this.chatService.removeListener(chatEventListener);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (chat != null) {
			outState.putParcelable(CHAT, chat.getEntity());
		}
	}

	@Override
	protected void fillListView(@Nonnull ListView lv, @Nonnull Context context) {
		super.fillListView(lv, context);

		lv.setDividerHeight(0);

		lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lv.setStackFromBottom(true);
	}

	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return new AbstractOnRefreshListener() {
			@Override
			public void onRefresh() {
				syncOlderMessages();
			}
		};
	}

	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return new AbstractOnRefreshListener() {
			@Override
			public void onRefresh() {
				new SyncMessagesForChatAsyncTask(this, getActivity()).executeInParallel(new SyncMessagesForChatAsyncTask.Input(getUser().getEntity(), chat.getEntity(), false));
			}
		};
	}

	@Override
	public void onTopReached() {
		super.onTopReached();

		syncOlderMessages();
	}

	private void syncOlderMessages() {
		final ListView lv = getListViewById();
		if (lv != null) {
			final Integer transcriptMode = lv.getTranscriptMode();
			lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);

			final PublicPullToRefreshListView pullToRefreshListView = getPullToRefreshListView();
			if (pullToRefreshListView != null) {
				pullToRefreshListView.setRefreshingInternal(false);
			}

			final int count = lv.getCount();

			new SyncMessagesForChatAsyncTask(this, getActivity()) {
				@Override
				protected void onSuccessPostExecute(@Nonnull Input result) {
					try {
						super.onSuccessPostExecute(result);
					} finally {

						// NOTE: small delay for data to be applied on the list
						lv.postDelayed(new ListViewPostActions(lv, transcriptMode, count), 500);
					}
				}

				@Override
				protected void onFailurePostExecute(@Nonnull Exception e) {
					try {
						super.onFailurePostExecute(e);
					} finally {

						// NOTE: small delay for data to be applied on the list
						lv.postDelayed(new ListViewPostActions(lv, transcriptMode, count), 500);

					}
				}
			}.executeInParallel(new SyncMessagesForChatAsyncTask.Input(getUser().getEntity(), chat.getEntity(), true));
		}
	}

	@Nonnull
	@Override
	protected MessagesAdapter createAdapter() {
		return new MessagesAdapter(getActivity(), getUser(), chat, MessageListItemStyle.newFromDefaultPreferences(getActivity()));
	}

	@Override
	public void onPause() {
		super.onPause();

		if(chat != null && messageBody != null) {
			getChatService().saveDraftMessage(chat, messageBody.getText().toString());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<Message>> createAsyncLoader(@Nonnull MessengerListItemAdapter<MessageListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new MessagesAsyncLoader(adapter, onPostExecute, MessageListItemStyle.newFromDefaultPreferences(getActivity()));
	}

	private void scrollToTheEnd(long delayMillis) {
		// set initial position to the end
		final ListView lv = getListViewById();
		if (lv != null) {
			lv.postDelayed(new Runnable() {
				public void run() {
					final int position = lv.getCount() - 1;
					lv.setSelection(position);
				}
			}, delayMillis);
		}
	}

	@Nullable
	private ListView getListViewById() {
		final View view = getView();
		if (view != null) {
			return (ListView) view.findViewById(android.R.id.list);
		} else {
			return null;
		}
	}

	@Nonnull
	@Override
	protected MessagesAdapter getAdapter() {
		return (MessagesAdapter) super.getAdapter();
	}

	private static class ListViewPostActions implements Runnable {

		@Nonnull
		private final ListView lv;

		@Nonnull
		private final Integer transcriptMode;

		private final int count;

		public ListViewPostActions(@Nonnull ListView lv, @Nonnull Integer transcriptMode, int count) {
			this.lv = lv;
			this.transcriptMode = transcriptMode;
			this.count = count;
		}

		@Override
		public void run() {
			lv.setTranscriptMode(transcriptMode);
			final int newCount = lv.getCount();
			int newPosition = newCount - count + 1;
			if (newPosition >= 0) {
				// todo serso: think
				//lv.setSelection(newPosition);
			}
		}
	}

	private class UiThreadUserChatListener extends AbstractJEventListener<ChatEvent> {

		private UiThreadUserChatListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final ChatEvent event) {
			if (event.getType() == ChatEventType.changed) {
				if (chat.equals(event.getChat())) {
					chat = event.getChat();
				}
			}

			Threads2.tryRunOnUiThread(MessagesFragment.this, new Runnable() {
				@Override
				public void run() {
					getAdapter().onEvent(event);
				}
			});
		}
	}

	private class MessagesAsyncLoader extends AbstractAsyncLoader<Message, MessageListItem> {

		@Nonnull
		private final MessageListItemStyle messageStyle;

		public MessagesAsyncLoader(MessengerListItemAdapter<MessageListItem> adapter, Runnable onPostExecute, @Nonnull MessageListItemStyle messageStyle) {
			super(MessagesFragment.this.getActivity(), adapter, onPostExecute);
			this.messageStyle = messageStyle;
		}

		@Nonnull
		@Override
		protected List<Message> getElements(@Nonnull Context context) {
			return App.getMessageService().getMessages(chat.getEntity());
		}

		@Nonnull
		@Override
		protected MessageListItem createListItem(@Nonnull Message message) {
			return newMessageListItem(getUser(), chat, message, messageStyle);
		}

		@Override
		protected void onSuccessPostExecute(@Nullable List<Message> elements) {
			super.onSuccessPostExecute(elements);

			scrollToTheEnd(200);

			// load new messages for chat
			final FragmentActivity activity = getActivity();
			if (activity != null) {
				new SyncMessagesForChatAsyncTask(null, activity) {
					@Override
					protected void onSuccessPostExecute(@Nonnull Input result) {
						super.onSuccessPostExecute(result);
						// let's wait 0.5 sec while sorting & filtering
						scrollToTheEnd(500);
					}
				}.executeInParallel(new SyncMessagesForChatAsyncTask.Input(getUser().getEntity(), chat.getEntity(), false));
			}
		}
	}

}
