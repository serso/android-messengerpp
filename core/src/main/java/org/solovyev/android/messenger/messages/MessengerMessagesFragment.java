package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.inject.Inject;
import org.solovyev.android.AThreads;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.view.PublicPullToRefreshListView;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.PullToRefreshListViewProvider;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:38 PM
 */
public final class MessengerMessagesFragment extends AbstractMessengerListFragment<ChatMessage, MessageListItem> implements PullToRefreshListViewProvider {

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
    private RealmService realmService;


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

    private Realm realm;

    @Nullable
    private JEventListener<ChatEvent> chatEventListener;

    public MessengerMessagesFragment() {
        super(TAG, false, false);
    }

    public MessengerMessagesFragment(@Nonnull Chat chat) {
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

        if (chat != null) {
            // chat is set => fragment was just created => we need to load realm
            realm = realmService.getRealmById(chat.getEntity().getRealmId());
        } else {
            // first - restore state
            final Entity realmChat = savedInstanceState.getParcelable(CHAT);
            if (realmChat != null) {
                chat = this.chatService.getChatById(realmChat);
            }

            if (chat == null) {
                Log.e(TAG, "Chat is null: unable to find chat with id: " + realmChat);
                getActivity().finish();
            } else {
                realm = realmService.getRealmById(chat.getEntity().getRealmId());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);

        final ListView listView = getListView(root);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(true);

        final View messagesFooter = ViewFromLayoutBuilder.newInstance(R.layout.mpp_messages_footer).build(this.getActivity());

        /*final ImageView bubbleUserIconImageView = (ImageView) messagesFooter.findViewById(R.id.mpp_bubble_user_icon_imageview);
        getUserService().setUserIcon(getUser(), bubbleUserIconImageView);*/

        listView.addFooterView(messagesFooter, null, false);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // then call parent
        super.onActivityCreated(savedInstanceState);

        chatEventListener = new UiThreadUserChatListener();
        this.chatService.addListener(chatEventListener);
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        final EditText messageBody = (EditText) root.findViewById(R.id.message_body);

        final Button sendButton = (Button) root.findViewById(R.id.send_message_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageText = Strings.toHtml(messageBody.getText());

                if (!Strings.isEmpty(messageText)) {
                    final Activity activity = getActivity();
                    //Toast.makeText(activity, "Sending...", Toast.LENGTH_SHORT).show();

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

        final Button clearButton = (Button) root.findViewById(R.id.clear_message_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageBody.setText("");
            }
        });


        // user` icon
        final ImageView userIcon = (ImageView) root.findViewById(R.id.message_icon);

        final String imageUri = getUser().getPropertyValueByName("photo");
        if (!Strings.isEmpty(imageUri)) {
            imageLoader.loadImage(imageUri, userIcon, R.drawable.empty_icon);
        }
    }

    @Nonnull
    private User getUser() {
        return realm.getUser();
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
                new SyncChatMessagesForChatAsyncTask(this, getActivity()).execute(new SyncChatMessagesForChatAsyncTask.Input(getUser().getEntity(), chat.getEntity(), false));
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

            final PublicPullToRefreshListView pullToRefreshListView = getPullToRefreshListView();
            if ( pullToRefreshListView != null ) {
                pullToRefreshListView.setRefreshingInternal(false);
            }

            final int count = lv.getCount();

            new SyncChatMessagesForChatAsyncTask(this, getActivity()) {
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
            }.execute(new SyncChatMessagesForChatAsyncTask.Input(getUser().getEntity(), chat.getEntity(), true));
        }
    }

    @Nonnull
    @Override
    protected MessagesAdapter createAdapter() {
        return new MessagesAdapter(getActivity(), getUser(), chat);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nonnull
    @Override
    protected MessengerAsyncTask<Void, Void, List<ChatMessage>> createAsyncLoader(@Nonnull MessengerListItemAdapter<MessageListItem> adapter, @Nonnull Runnable onPostExecute) {
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
            if ( event.getType() == ChatEventType.changed ) {
               if ( chat.equals(event.getChat()) ) {
                   chat = event.getChat();
               }
            }
            AThreads.tryRunOnUiThread(getActivity(), new Runnable() {
                @Override
                public void run() {
                    getAdapter().onEvent(event);
                }
            });
        }
    }

    private class MessagesAsyncLoader extends AbstractAsyncLoader<ChatMessage, MessageListItem> {

        public MessagesAsyncLoader(MessengerListItemAdapter<MessageListItem> adapter, Runnable onPostExecute) {
            super(MessengerMessagesFragment.this.getActivity(), adapter, onPostExecute);
        }

        @Nonnull
        @Override
        protected List<ChatMessage> getElements(@Nonnull Context context) {
            return MessengerApplication.getServiceLocator().getChatMessageService().getChatMessages(chat.getEntity());
        }

        @Override
        protected Comparator<? super MessageListItem> getComparator() {
            return MessageListItem.Comparator.getInstance();
        }

        @Nonnull
        @Override
        protected MessageListItem createListItem(@Nonnull ChatMessage message) {
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
                    protected void onSuccessPostExecute(@Nonnull Input result) {
                        super.onSuccessPostExecute(result);
                        // let's wait 0.5 sec while sorting & filtering
                        scrollToTheEnd(500);
                    }
                }.execute(new SyncChatMessagesForChatAsyncTask.Input(getUser().getEntity(), chat.getEntity(), false));
            }
        }
    }
}
