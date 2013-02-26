package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.R;

public class MessengerRealmsActivity extends MessengerFragmentActivity {

    public MessengerRealmsActivity() {
        super(R.layout.msg_main, false, true);
    }

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerRealmsActivity.class);
        activity.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFragment(R.id.content_first_pane, new MessengerRealmsFragment());
    }

}
