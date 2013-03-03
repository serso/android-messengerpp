package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.core.R;

public class MessengerRealmDefsActivity extends MessengerFragmentActivity {

    public MessengerRealmDefsActivity() {
        super(R.layout.msg_main, false, true);
    }

    public static void startActivity(@Nonnull Context context) {
        final Intent result = new Intent();
        result.setClass(context, MessengerRealmDefsActivity.class);
        context.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFragment(R.id.content_first_pane, new MessengerRealmDefsFragment(), null);
    }

}
