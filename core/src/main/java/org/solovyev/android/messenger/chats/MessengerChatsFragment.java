package org.solovyev.android.messenger.chats;

import android.os.Bundle;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.AThreads;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.ToggleFilterInputMenuItem;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.AbstractOnRefreshListener;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:37 PM
 */
public class MessengerChatsFragment extends AbstractMessengerListFragment<UserChat, ChatListItem> {

    @Nonnull
    private static final String TAG = "ChatsFragment";

    @Nullable
    private ChatEventListener chatEventListener;

    public MessengerChatsFragment() {
        super(TAG, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        chatEventListener = new UiThreadUserChatListener();
        getChatService().addChatEventListener(chatEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (chatEventListener != null) {
            getChatService().removeChatEventListener(chatEventListener);
        }
    }

    @Override
    protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
        return new AbstractOnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getSyncService().sync(SyncTask.user_chats, new Runnable() {
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
                    getSyncService().sync(SyncTask.user_chats, new Runnable() {
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

    @Nonnull
    @Override
    protected ChatsAdapter createAdapter() {
        return new ChatsAdapter(getActivity());
    }

    @Nonnull
    @Override
    protected MessengerAsyncTask<Void, Void, List<UserChat>> createAsyncLoader(@Nonnull MessengerListItemAdapter<ChatListItem> adapter, @Nonnull Runnable onPostExecute) {
        return new ChatsAsyncLoader(getActivity(), adapter, onPostExecute, getRealmService());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class UiThreadUserChatListener implements ChatEventListener {

        @Override
        public void onChatEvent(@Nonnull final Chat eventChat, @Nonnull final ChatEventType chatEventType, @Nullable final Object data) {
            AThreads.tryRunOnUiThread(getActivity(), new Runnable() {
                @Override
                public void run() {
                    getAdapter().onChatEvent(eventChat, chatEventType, data);
                }
            });
        }
    }

    @Nonnull
    protected ChatsAdapter getAdapter() {
        return (ChatsAdapter) super.getAdapter();
    }

    /*
    **********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

    private ActivityMenu<Menu, MenuItem> menu;

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
        final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

        menuItems.add(new ToggleFilterInputMenuItem(this));

        this.menu = ListActivityMenu.fromResource(R.menu.chats, menuItems, SherlockMenuHelper.getInstance());
        this.menu.onCreateOptionsMenu(this.getActivity(), menu);
    }

}
