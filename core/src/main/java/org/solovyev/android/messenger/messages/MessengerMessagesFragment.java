package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AThreads;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEventListener;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.MessageListItem;
import org.solovyev.android.messenger.chats.MessagesAdapter;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.PullToRefreshListViewProvider;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.Strings;

import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:38 PM
 */
public class MessengerMessagesFragment extends AbstractMessengerListFragment<ChatMessage, MessageListItem> implements PullToRefreshListViewProvider {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private ChatService chatService;

    @Inject
    @NotNull
    private ImageLoader imageLoader;

    @Inject
    @NotNull
    private RealmService realmService;

    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */


    @NotNull
    private static final String TAG = "MessagesFragment";

    @NotNull
    private static final String CHAT = "chat";

    private Chat chat;

    private Realm realm;

    @Nullable
    private ChatEventListener chatEventListener;

    public MessengerMessagesFragment() {
        super(TAG);
    }

    public MessengerMessagesFragment(@NotNull Chat chat) {
        super(TAG);
        this.chat = chat;
    }

    @Override
    protected boolean isFilterEnabled() {
        return false;
    }

    @NotNull
    public Chat getChat() {
        return chat;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final RelativeLayout result = ViewFromLayoutBuilder.<RelativeLayout>newInstance(org.solovyev.android.messenger.R.layout.msg_messages).build(this.getActivity());

        final View bubble = ViewFromLayoutBuilder.newInstance(org.solovyev.android.messenger.R.layout.msg_message_bubble).build(this.getActivity());

        final ViewGroup messageBubbleParent = (ViewGroup)result.findViewById(org.solovyev.android.messenger.R.id.message_bubble);
        messageBubbleParent.addView(bubble, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final View superResult = super.onCreateView(inflater, container, savedInstanceState);

        final ListView listView = getListView(superResult);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);

        final ViewGroup messagesParent = (ViewGroup)result.findViewById(org.solovyev.android.messenger.R.id.messages_list);
        messagesParent.addView(superResult);

        MessengerApplication.getMultiPaneManager().fillContentPane(this.getActivity(), container, result);

        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText messageBody = (EditText) view.findViewById(R.id.message_body);

        final Button sendButton = (Button) view.findViewById(R.id.send_message_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageText = Strings.toHtml(messageBody.getText());

                if (!Strings.isEmpty(messageText)) {
                    final Activity activity = getActivity();
                    Toast.makeText(activity, "Sending...", Toast.LENGTH_SHORT).show();

                    new SendMessageAsyncTask(activity, chat) {
                        @Override
                        protected void onSuccessPostExecute(@Nullable List<ChatMessage> result) {
                            super.onSuccessPostExecute(result);
                            messageBody.setText("");
                        }
                    }.execute(new SendMessageAsyncTask.Input(getUser(), messageText, chat));
                }
            }
        });

        final Button clearButton = (Button) view.findViewById(R.id.clear_message_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageBody.setText("");
            }
        });


        // user` icon
        final ImageView userIcon = (ImageView) view.findViewById(R.id.message_icon);

        final String imageUri = getUser().getPropertyValueByName("photo");
        if (!Strings.isEmpty(imageUri)) {
            imageLoader.loadImage(imageUri, userIcon, R.drawable.empty_icon);
        }
    }

    @NotNull
    private User getUser() {
        return realm.getUser();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // first - restore state
        if (chat == null) {
            final RealmEntity realmChat = savedInstanceState.getParcelable(CHAT);
            if (realmChat != null) {
                chat = this.chatService.getChatById(realmChat);
            }

            if (chat == null) {
                Log.e(TAG, "Chat is null: unable to find chat with id: " + realmChat);
                getActivity().finish();
            } else {
                realm = realmService.getRealmById(chat.getRealmChat().getRealmId());
            }
        }

        // then call parent
        super.onActivityCreated(savedInstanceState);

        chatEventListener = new UiThreadUserChatListener();
        this.chatService.addChatEventListener(chatEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (chatEventListener != null) {
            this.chatService.removeChatEventListener(chatEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (chat != null) {
            outState.putParcelable(CHAT, chat.getRealmChat());
        }
    }

    @Override
    protected void fillListView(@NotNull ListView lv, @NotNull Context context) {
        super.fillListView(lv, context);
        lv.setDividerHeight(0);
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
                new SyncChatMessagesForChatAsyncTask(this, getActivity()).execute(new SyncChatMessagesForChatAsyncTask.Input(getUser().getRealmUser(), chat.getRealmChat(), false));
            }
        };
    }

    @Override
    protected void onListViewTopReached() {
        super.onListViewTopReached();

        syncOlderMessages();
    }

    private void syncOlderMessages() {
        final ListView lv = getListViewById();
        if (lv != null) {
            final Integer transcriptMode = lv.getTranscriptMode();
            lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);

            final PullToRefreshListView2 pullToRefreshListView = getPullToRefreshListView();
            if ( pullToRefreshListView != null ) {
                pullToRefreshListView.setRefreshingInternal(false);
            }

            final int count = lv.getCount();

            new SyncChatMessagesForChatAsyncTask(this, getActivity()) {
                @Override
                protected void onSuccessPostExecute(@NotNull Input result) {
                    try {
                        super.onSuccessPostExecute(result);
                    } finally {

                        // NOTE: small delay for data to be applied on the list
                        lv.postDelayed(new ListViewPostActions(lv, transcriptMode, count), 500);
                    }
                }

                @Override
                protected void onFailurePostExecute(@NotNull Exception e) {
                    try {
                        super.onFailurePostExecute(e);
                    } finally {

                        // NOTE: small delay for data to be applied on the list
                        lv.postDelayed(new ListViewPostActions(lv, transcriptMode, count), 500);

                    }
                }
            }.execute(new SyncChatMessagesForChatAsyncTask.Input(getUser().getRealmUser(), chat.getRealmChat(), true));
        }
    }

    @NotNull
    @Override
    protected MessagesAdapter createAdapter() {
        return new MessagesAdapter(getActivity(), getUser(), chat);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @NotNull
    @Override
    protected MessengerAsyncTask<Void, Void, List<ChatMessage>> createAsyncLoader(@NotNull MessengerListItemAdapter<MessageListItem> adapter, @NotNull Runnable onPostExecute) {
        return new MessagesAsyncLoader(adapter, onPostExecute);
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

    @NotNull
    @Override
    protected MessagesAdapter getAdapter() {
        return (MessagesAdapter) super.getAdapter();
    }

    @Override
    public PullToRefreshListView2 getPullToRefreshListView() {
        return getPullLv();
    }

    private static class ListViewPostActions implements Runnable {

        @NotNull
        private final ListView lv;

        @NotNull
        private final Integer transcriptMode;

        private final int count;

        public ListViewPostActions(@NotNull ListView lv, @NotNull Integer transcriptMode, int count) {
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

    private class UiThreadUserChatListener implements ChatEventListener {

        @Override
        public void onChatEvent(@NotNull final Chat eventChat, @NotNull final ChatEventType chatEventType, @Nullable final Object data) {
            AThreads.tryRunOnUiThread(getActivity(), new Runnable() {
                @Override
                public void run() {
                    getAdapter().onChatEvent(eventChat, chatEventType, data);
                }
            });
        }
    }

    private class MessagesAsyncLoader extends AbstractAsyncLoader<ChatMessage, MessageListItem> {

        public MessagesAsyncLoader(MessengerListItemAdapter<MessageListItem> adapter, Runnable onPostExecute) {
            super(MessengerMessagesFragment.this.getActivity(), adapter, onPostExecute);
        }

        @NotNull
        @Override
        protected List<ChatMessage> getElements(@NotNull Context context) {
            return MessengerApplication.getServiceLocator().getChatMessageService().getChatMessages(chat.getRealmChat(), getActivity());
        }

        @Override
        protected Comparator<? super MessageListItem> getComparator() {
            return MessageListItem.Comparator.getInstance();
        }

        @NotNull
        @Override
        protected MessageListItem createListItem(@NotNull ChatMessage message) {
            return new MessageListItem(getUser(), chat, message);
        }

        @Override
        protected void onSuccessPostExecute(@Nullable List<ChatMessage> elements) {
            super.onSuccessPostExecute(elements);

            scrollToTheEnd(200);

            // load new messages for chat
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                new SyncChatMessagesForChatAsyncTask(null, activity) {
                    @Override
                    protected void onSuccessPostExecute(@NotNull Input result) {
                        super.onSuccessPostExecute(result);
                        // let's wait 0.5 sec while sorting & filtering
                        scrollToTheEnd(500);
                    }
                }.execute(new SyncChatMessagesForChatAsyncTask.Input(getUser().getRealmUser(), chat.getRealmChat(), false));
            }
        }
    }
}
