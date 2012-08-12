package org.solovyev.android.messenger;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import roboguice.activity.RoboListActivity;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:28 PM
 */
public abstract class MessengerListActivity extends RoboListActivity {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELD
    *
    **********************************************************************
    */
    @Inject
    @NotNull
    private UserService userService;

    @Inject
    @NotNull
    private SyncService syncService;


    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */
    @NotNull
    private final MessengerCommonActivity activity;

    protected MessengerListActivity() {
        activity = new MessengerCommonActivityImpl(R.layout.msg_main_list, getSyncButtonListener());
    }

    @NotNull
    protected UserService getUserService() {
        return userService;
    }

    @NotNull
    protected SyncService getSyncService() {
        return syncService;
    }

    @Nullable
    protected abstract View.OnClickListener getSyncButtonListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity.onCreate(this);
    }

    @NotNull
    public User getUser() {
        return activity.getUser();
    }

    @NotNull
    public ViewGroup getFooterCenter() {
        return activity.getFooterCenter(this);
    }

    @NotNull
    public ViewGroup getFooterRight() {
        return activity.getFooterRight(this);
    }

    @NotNull
    public ViewGroup getFooterLeft() {
        return activity.getFooterLeft(this);
    }

    @NotNull
    public ImageButton createFooterButton(int imageResId, int contentDescriptionResId) {
        return activity.createFooterImageButton(imageResId, contentDescriptionResId, this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.activity.onRestart(this);
    }
}
