package org.solovyev.android.messenger.messages;

import android.content.Context;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.MessengerApplication;
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
                                            @Nonnull Context context) {
        super(context);
        this.listViewProvider = listViewProvider;
    }

    @Override
    protected Input doWork(@Nonnull List<Input> inputs) {
        assert inputs.size() == 1;
        final Input input = inputs.get(0);

        final Context context = getContext();
        if (context != null) {
            if (!input.older) {
                MessengerApplication.getServiceLocator().getChatService().syncNewerChatMessagesForChat(input.realmChat, input.realmUser);
            } else {
                MessengerApplication.getServiceLocator().getChatService().syncOlderChatMessagesForChat(input.realmChat, input.realmUser);
            }
        }

        return input;
    }

    @Override
    protected void onSuccessPostExecute(@Nonnull Input result) {
        completeRefreshForListView();
    }

    @Override
    protected void onFailurePostExecute(@Nonnull Exception e) {
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

        @Nonnull
        private RealmEntity realmUser;

        @Nonnull
        private RealmEntity realmChat;

        private boolean older;

        public Input(@Nonnull RealmEntity realmUser,
                     @Nonnull RealmEntity realmChat,
                     boolean older) {
            this.realmUser = realmUser;
            this.realmChat = realmChat;
            this.older = older;
        }
    }
}
