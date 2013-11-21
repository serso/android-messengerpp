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

package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.os.Bundle;
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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import org.solovyev.android.Activities;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.menu.AMenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.*;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.notifications.NotificationService;
import org.solovyev.android.messenger.users.ContactUiEvent;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.messenger.view.PublicPullToRefreshListView;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.PullToRefreshListViewProvider;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.JPredicate;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import roboguice.RoboGuice;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;
import static org.solovyev.android.messenger.messages.MessageListItem.newMessageListItem;
import static org.solovyev.android.messenger.messages.UiMessageSender.trySendMessage;
import static org.solovyev.android.messenger.notifications.Notifications.newUndefinedErrorNotification;
import static org.solovyev.common.text.Strings.isEmpty;
import static org.solovyev.common.text.Strings.toHtml;

public final class MessagesFragment extends BaseAsyncListFragment<Message, MessageListItem> implements PullToRefreshListViewProvider {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/
	public static final String FRAGMENT_TAG = "messages";

	@Nonnull
	private static final String TAG = "MessagesFragment";

	@Nonnull
	private static final String ARG_CHAT = "chat";


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

	private Chat chat;

	private Account account;

	@Nullable
	private JEventListener<ChatEvent> chatEventListener;

	private EditText messageBody;

	private ActivityMenu<Menu, MenuItem> menu;

	public MessagesFragment() {
		super(TAG, R.string.mpp_chat, false, false);
	}

	@Nonnull
	public static MultiPaneFragmentDef newMessagesFragmentDef(@Nonnull Context context, @Nonnull Chat chat, boolean addToBackStack) {
		final Bundle arguments = new Bundle();
		arguments.putParcelable(ARG_CHAT, chat.getEntity());
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, MessagesFragment.class, context, arguments, MessagesFragmentReuseCondition.forChat(chat));
	}

	@Override
	protected void onListLoaded() {
		// do not call super - we don't want to select message

		attachListeners();

		new SyncMessagesForChatAsyncTask(null, getActivity()).executeInParallel(new SyncMessagesForChatAsyncTask.Input(getUser().getEntity(), chat.getEntity(), false));
	}

	@Override
	protected void attachListeners() {
		super.attachListeners();

		chatEventListener = new UiThreadUserChatListener();
		this.chatService.addListener(chatEventListener);
	}

	@Nonnull
	public Chat getChat() {
		return chat;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		RoboGuice.getInjector(getActivity()).injectMembers(this);

		// first - restore state
		final Entity chatId = getArguments().getParcelable(ARG_CHAT);
		if (chatId != null) {
			chat = this.chatService.getChatById(chatId);
		}

		if (chat == null) {
			Log.e(TAG, "Chat is null: unable to find chat with id: " + chatId);
			notificationService.add(newUndefinedErrorNotification());
			Activities.restartActivity(getActivity());
		} else {
			account = accountService.getAccountById(chat.getEntity().getAccountId());
		}

		setHasOptionsMenu(true);

		super.onCreate(savedInstanceState);
	}

	@Override
	public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup root = super.onCreateView(inflater, container, savedInstanceState);
		final Context context = getThemeContext();

		final View messageLayoutParent = ViewFromLayoutBuilder.newInstance(R.layout.mpp_list_item_message_editor).build(context);
		if (!account.canSendMessage(chat)) {
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

		final String title;
		if (chat.isPrivate()) {
			title = getString(R.string.mpp_private_chat_title, Users.getDisplayNameFor(chat.getSecondUser()));
		} else {
			title = getString(R.string.mpp_public_chat_title);
		}
		getMultiPaneManager().showTitle(getSherlockActivity(), this, title);
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
			final MessagesAdapter adapter = getAdapter();
			adapter.addSendingMessage(message);
			getListView().smoothScrollToPosition(adapter.getCount());

		}
	}

	@Nonnull
	private User getUser() {
		return account.getUser();
	}

	@Override
	protected void detachListeners() {
		if (chatEventListener != null) {
			this.chatService.removeListener(chatEventListener);
		}
		super.detachListeners();
	}

	@Override
	public void onItemReachedFromTop(int position, int total) {
		// do nothing
	}

	@Override
	protected void fillListView(@Nonnull ListView lv, @Nonnull Context context) {
		super.fillListView(lv, context);

		lv.setDividerHeight(0);

		lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		lv.setStackFromBottom(true);
		lv.setFastScrollEnabled(false);
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

			new SyncMessagesForChatAsyncTask(this, getActivity()) {
				@Override
				protected void onSuccessPostExecute(@Nonnull Input result) {
					try {
						super.onSuccessPostExecute(result);
					} finally {

						// NOTE: small delay for data to be applied on the list
						lv.postDelayed(new ListViewPostActions(lv, transcriptMode), 500);
					}
				}

				@Override
				protected void onFailurePostExecute(@Nonnull Exception e) {
					try {
						super.onFailurePostExecute(e);
					} finally {

						// NOTE: small delay for data to be applied on the list
						lv.postDelayed(new ListViewPostActions(lv, transcriptMode), 500);

					}
				}
			}.executeInParallel(new SyncMessagesForChatAsyncTask.Input(getUser().getEntity(), chat.getEntity(), true));
		}
	}

	@Nonnull
	@Override
	protected MessagesAdapter createAdapter() {
		return new MessagesAdapter(getActivity(), getUser(), chat);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (chat != null && messageBody != null) {
			getChatService().saveDraftMessage(chat, messageBody.getText().toString());
		}
	}

	@Nonnull
	@Override
	protected MessengerAsyncTask<Void, Void, List<Message>> createAsyncLoader(@Nonnull BaseListItemAdapter<MessageListItem> adapter, @Nonnull Runnable onPostExecute) {
		return new MessagesAsyncLoader(adapter, onPostExecute);
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

		public ListViewPostActions(@Nonnull ListView lv, @Nonnull Integer transcriptMode) {
			this.lv = lv;
			this.transcriptMode = transcriptMode;
		}

		@Override
		public void run() {
			lv.setTranscriptMode(transcriptMode);
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

	private class MessagesAsyncLoader extends BaseAsyncLoader<Message, MessageListItem> {

		public MessagesAsyncLoader(BaseListItemAdapter<MessageListItem> adapter, Runnable onPostExecute) {
			super(MessagesFragment.this.getActivity(), adapter, onPostExecute);
		}

		@Nonnull
		@Override
		protected List<Message> getElements(@Nonnull Context context) {
			return App.getMessageService().getMessages(chat.getEntity());
		}

		@Nonnull
		@Override
		protected MessageListItem createListItem(@Nonnull Message message) {
			return newMessageListItem(getUser(), chat, message);
		}
	}


	/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final boolean triplePane = getMultiPaneManager().isTriplePane(getActivity());

		final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

		final ViewContactMenuItem viewContactMenuItem = new ViewContactMenuItem();
		menuItems.add(viewContactMenuItem);

		final ViewContactsMenuItem viewContactsMenuItem = new ViewContactsMenuItem();
		menuItems.add(viewContactsMenuItem);

		this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_messages, menuItems, SherlockMenuHelper.getInstance(), new JPredicate<AMenuItem<MenuItem>>() {
			@Override
			public boolean apply(@Nullable AMenuItem<MenuItem> menuItem) {
				if (menuItem == viewContactMenuItem) {
					return triplePane || !chat.isPrivate();
				} else if (menuItem == viewContactsMenuItem) {
					return triplePane || chat.isPrivate();
				}
				return false;
			}
		});
		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}

	private class ViewContactMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_view_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			getEventManager().fire(ChatUiEventType.show_participants.newEvent(chat));
		}
	}

	private class ViewContactsMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_view_contacts;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			getEventManager().fire(ChatUiEventType.show_participants.newEvent(chat));
		}
	}
}
