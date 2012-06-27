package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.*;
import org.solovyev.android.messenger.sync.SyncTask;
import org.solovyev.android.messenger.sync.TaskIsAlreadyRunningException;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 6:53 PM
 */
public class MessengerFriendsListActivity extends MessengerListActivity {

    @NotNull
    private FriendsAdapter adapter;

    @Nullable
    private UserEventListener userEventListener;

    @Override
    protected View.OnClickListener getSyncButtonListener() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new FriendsAdapter(this, getUser());

        userEventListener = new UiThreadUserEventListener();
        getServiceLocator().getUserService().addUserEventListener(userEventListener);

        new FriendsAsyncLoader(getUser(), this, adapter, null).execute();

        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent,
                                    final View view,
                                    final int position,
                                    final long id) {
                final ListItem listItem = (ListItem) parent.getItemAtPosition(position);

                final ListItem.OnClickAction onClickAction = listItem.getOnClickAction();
                if (onClickAction != null) {
                    onClickAction.onClick(MessengerFriendsListActivity.this, adapter, getListView());
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final ListItem listItem = (ListItem) parent.getItemAtPosition(position);

                final ListItem.OnClickAction onLongClickAction = listItem.getOnLongClickAction();
                if (onLongClickAction != null) {
                    onLongClickAction.onClick(MessengerFriendsListActivity.this, adapter, getListView());
                    return true;
                }

                return false;
            }
        });

        final ImageButton syncButton = createFooterButton(R.drawable.refresh, R.string.c_refresh);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getServiceLocator().getSyncService().sync(SyncTask.user_friends, MessengerFriendsListActivity.this, null);
                } catch (TaskIsAlreadyRunningException e) {
                    e.showMessage(MessengerFriendsListActivity.this);
                }
            }
        });
        getFooterLeft().addView(syncButton);

        final ImageButton logoutButton = createFooterButton(org.solovyev.android.ext.R.drawable.home, org.solovyev.android.ext.R.string.c_home);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getServiceLocator().getAuthServiceFacade().logoutUser(MessengerFriendsListActivity.this);
                MessengerLoginActivity.startActivity(MessengerFriendsListActivity.this);
            }
        });
        getFooterLeft().addView(logoutButton);
    }

    @NotNull
    public ServiceLocator getServiceLocator() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (userEventListener != null) {
            getServiceLocator().getUserService().removeUserEventListener(userEventListener);
        }
    }

    private class UiThreadUserEventListener implements UserEventListener {

        @Override
        public void onUserEvent(@NotNull final User eventUser, @NotNull final UserEventType userEventType, final @Nullable Object data) {
            new UiThreadRunnable(MessengerFriendsListActivity.this, new Runnable() {
                @Override
                public void run() {
                    MessengerFriendsListActivity.this.adapter.onUserEvent(eventUser, userEventType, data);
                }
            }).run();
        }
    }
}
