package org.solovyev.android.messenger.xmpp;

import android.os.Bundle;
import org.solovyev.android.messenger.MessengerFragmentActivity;

public class XmppRealmConfigurationActivity extends MessengerFragmentActivity {

    public XmppRealmConfigurationActivity() {
        super(R.layout.msg_main, false, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFragment(org.solovyev.android.messenger.R.id.content_first_pane, new XmppRealmConfigurationFragment());
    }
}
