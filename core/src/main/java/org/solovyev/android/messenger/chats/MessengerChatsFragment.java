package org.solovyev.android.messenger.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.AbstractMessengerListItemAdapter;
import org.solovyev.android.messenger.UiThreadRunnable;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:37 PM
 */
public class MessengerChatsFragment extends AbstractMessengerListFragment<Chat> {

    @NotNull
    private static final String TAG = "ChatsFragment";

    @Nullable
    private ChatEventListener chatEventListener;

    public MessengerChatsFragment() {
        super(TAG);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        chatEventListener = new UiThreadUserChatListener();
        getServiceLocator().getChatService().addChatEventListener(chatEventListener);
    }

    @Override
    protected void updateRightPane() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (chatEventListener != null) {
            getServiceLocator().getChatService().removeChatEventListener(chatEventListener);
        }
    }

    @Override
    protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
        return new AbstractOnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getServiceLocator().getSyncService().sync(SyncTask.user_chats, getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            completeRefresh();
                        }
                    });
                    Toast.makeText(getActivity(), "Chats sync started!", Toast.LENGTH_SHORT).show();
                } catch (TaskIsAlreadyRunningException e) {
                    e.showMessage(getActivity());
                }
            }
        };
    }

    @Override
    protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
        return new AbstractOnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getServiceLocator().getSyncService().sync(SyncTask.user_chats, getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            completeRefresh();
                        }
                    });
                    Toast.makeText(getActivity(), "Chats sync started!", Toast.LENGTH_SHORT).show();
                } catch (TaskIsAlreadyRunningException e) {
                    e.showMessage(getActivity());
                }
            }
        };
    }

    @NotNull
    @Override
    protected AbstractMessengerListItemAdapter createAdapter() {
        return new ChatsAdapter(getActivity(), getUser());
    }

    @NotNull
    @Override
    protected MessengerAsyncTask<Void, Void, List<Chat>> createAsyncLoader(@NotNull AbstractMessengerListItemAdapter adapter, @NotNull Runnable onPostExecute) {
        return new ChatsAsyncLoader(getUser(), getActivity(), adapter, onPostExecute);
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    private class UiThreadUserChatListener implements ChatEventListener {

        @Override
        public void onChatEvent(@NotNull final Chat eventChat, @NotNull final ChatEventType chatEventType, @Nullable final Object data) {
            new UiThreadRunnable(getActivity(), new Runnable() {
                @Override
                public void run() {
                    getAdapter().onChatEvent(eventChat, chatEventType, data);
                }
            }).run();
        }
    }

    @Override
    protected boolean isFilterEnabled() {
        return true;
    }

    @NotNull
    protected ChatsAdapter getAdapter() {
        return (ChatsAdapter) super.getAdapter();
    }
}
