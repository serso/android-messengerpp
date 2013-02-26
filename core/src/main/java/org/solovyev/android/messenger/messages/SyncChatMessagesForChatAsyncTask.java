package org.solovyev.android.messenger.messages;

import android.content.Context;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.AbstractMessengerApplication;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.view.PullToRefreshListViewProvider;

import java.util.List;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 3:33 PM
 */
public class SyncChatMessagesForChatAsyncTask extends MessengerAsyncTask<SyncChatMessagesForChatAsyncTask.Input, Void, SyncChatMessagesForChatAsyncTask.Input> {

    @Nullable
    private PullToRefreshListViewProvider listViewProvider;

    public SyncChatMessagesForChatAsyncTask(@Nullable PullToRefreshListViewProvider listViewProvider,
                                            @NotNull Context context) {
        super(context);
        this.listViewProvider = listViewProvider;
    }

    @Override
    protected Input doWork(@NotNull List<Input> inputs) {
        assert inputs.size() == 1;
        final Input input = inputs.get(0);

        final Context context = getContext();
        if (context != null) {
            if (!input.older) {
                AbstractMessengerApplication.getServiceLocator().getChatService().syncNewerChatMessagesForChat(input.realmChat, input.realmUser);
            } else {
                AbstractMessengerApplication.getServiceLocator().getChatService().syncOlderChatMessagesForChat(input.realmChat, input.realmUser);
            }
        }

        return input;
    }

    @Override
    protected void onSuccessPostExecute(@NotNull Input result) {
        completeRefreshForListView();
    }

    @Override
    protected void onFailurePostExecute(@NotNull Exception e) {
        completeRefreshForListView();
        super.onFailurePostExecute(e);
    }

    private void completeRefreshForListView() {
        if (listViewProvider != null) {
            final PullToRefreshListView ptrlv = listViewProvider.getPullToRefreshListView();
            if (ptrlv != null) {
                ptrlv.onRefreshComplete();
            }
        }
    }

    public static class Input {

        @NotNull
        private RealmEntity realmUser;

        @NotNull
        private RealmEntity realmChat;

        private boolean older;

        public Input(@NotNull RealmEntity realmUser,
                     @NotNull RealmEntity realmChat,
                     boolean older) {
            this.realmUser = realmUser;
            this.realmChat = realmChat;
            this.older = older;
        }
    }
}
