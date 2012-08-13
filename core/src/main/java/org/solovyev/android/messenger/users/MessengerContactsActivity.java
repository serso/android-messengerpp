package org.solovyev.android.messenger.users;

import android.app.Activity;
import android.content.Intent;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.R;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:52 PM
 */
public class MessengerContactsActivity extends MessengerFragmentActivity {

    public MessengerContactsActivity() {
        super(R.layout.msg_main);
    }

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerContactsActivity.class);
        activity.startActivity(result);
    }
}
