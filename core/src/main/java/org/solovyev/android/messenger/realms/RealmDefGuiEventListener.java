package org.solovyev.android.messenger.realms;

import org.solovyev.android.messenger.MessengerFragmentActivity;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

/**
* User: serso
* Date: 3/8/13
* Time: 11:46 AM
*/
public class RealmDefGuiEventListener implements EventListener<RealmDefGuiEvent> {

    @Nonnull
    private MessengerFragmentActivity activity;

    public RealmDefGuiEventListener(@Nonnull MessengerFragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onEvent(@Nonnull RealmDefGuiEvent event) {
        final RealmDef realmDef = event.getRealmDef();

        switch (event.getType()) {
            case realm_def_clicked:
                if (activity.isDualPane()) {
                    activity.getFragmentService().setSecondFragment(realmDef.getConfigurationFragmentClass(), null, new RealmDefFragmentReuseCondition(realmDef), BaseRealmConfigurationFragment.FRAGMENT_TAG, false);
                } else {
                    activity.getFragmentService().setFirstFragment(realmDef.getConfigurationFragmentClass(), null, new RealmDefFragmentReuseCondition(realmDef), BaseRealmConfigurationFragment.FRAGMENT_TAG, true);
                }
                break;
            case realm_def_edit_finished:
                activity.getFragmentService().goBack();
                break;
        }
    }
}
