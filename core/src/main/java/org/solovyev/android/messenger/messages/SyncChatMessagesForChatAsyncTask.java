package org.solovyev.android.messenger.messages;

import android.content.Context;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
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
                AbstractMessengerListFragment.getServiceLocator().getChatService().syncNewerChatMessagesForChat(input.chatId, input.userId, context);
            } else {
                AbstractMessengerListFragment.getServiceLocator().getChatService().syncOlderChatMessagesForChat(input.chatId, input.userId, context);
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
        private Integer userId;

        @NotNull
        private String chatId;

        private boolean older;

        public Input(@NotNull Integer userId, @NotNull String chatId, boolean older) {
            this.userId = userId;
            this.chatId = chatId;
            this.older = older;
        }
    }
}
