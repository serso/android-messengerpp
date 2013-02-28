package org.solovyev.android.messenger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.AbstractRealmConfigurationFragment;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmService;

public class RealmConfigurationActivity extends MessengerFragmentActivity {

    @NotNull
    private static final String EXTRA_REALM_DEF_ID = "realm_def_id";

    @NotNull
    private static final String EXTRA_REALM_ID = "realm_id";

    @Inject
    @NotNull
    private RealmService realmService;

    public RealmConfigurationActivity() {
        super(R.layout.msg_main, false, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        if (intent != null) {
            final String realmDefId = intent.getStringExtra(EXTRA_REALM_DEF_ID);
            final String realmId = intent.getStringExtra(EXTRA_REALM_ID);
            if (realmId != null) {
                final Realm realm = realmService.getRealmById(realmId);
                prepareUiForEdit(realm);
            } else if (realmDefId != null) {
                final RealmDef realmDef = realmService.getRealmDefById(realmDefId);
                prepareUiForCreate(realmDef);
            } else {
                finish();
            }
        }
    }

    private void prepareUiForCreate(@NotNull RealmDef realmDef) {
        setFragment(R.id.content_first_pane, realmDef.getConfigurationFragmentClass(), null);

    }

    private void prepareUiForEdit(@NotNull Realm realm) {
        final Bundle fragmentArgs = new Bundle();
        fragmentArgs.putString(AbstractRealmConfigurationFragment.EXTRA_REALM_ID, realm.getId());
        setFragment(R.id.content_first_pane, realm.getRealmDef().getConfigurationFragmentClass(), fragmentArgs);
    }

    public static void startForNewRealm(@NotNull Context context, @NotNull RealmDef realmDef) {
        final Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), RealmConfigurationActivity.class);
        intent.putExtra(EXTRA_REALM_DEF_ID, realmDef.getId());
        context.startActivity(intent);
    }

    public static void startForEditRealm(@NotNull Context context, @NotNull Realm realm) {
        final Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), RealmConfigurationActivity.class);
        intent.putExtra(EXTRA_REALM_ID, realm.getId());
        context.startActivity(intent);
    }
}
