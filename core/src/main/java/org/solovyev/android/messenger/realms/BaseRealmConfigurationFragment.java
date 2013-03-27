package org.solovyev.android.messenger.realms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.solovyev.android.Activities;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseRealmConfigurationFragment<T extends Realm<?>> extends RoboSherlockFragment {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @Nonnull
    public static final String EXTRA_REALM_ID = "realm_id";

    @Nonnull
    public static final String FRAGMENT_TAG = "realm-configuration";

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private MessengerMultiPaneManager multiPaneManager;

    @Inject
    @Nonnull
    private EventManager eventManager;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    private T editedRealm;

    private int layoutResId;

    protected BaseRealmConfigurationFragment(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            final String realmId = arguments.getString(EXTRA_REALM_ID);
            if (realmId != null) {
                try {
                    editedRealm = (T) realmService.getRealmById(realmId);
                } catch (UnsupportedRealmException e) {
                    MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
                    Activities.restartActivity(getActivity());
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = ViewFromLayoutBuilder.newInstance(layoutResId).build(this.getActivity());

        getMultiPaneManager().onCreatePane(this.getActivity(), container, result);

        result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return result;
    }

    public T getEditedRealm() {
        return editedRealm;
    }

    public boolean isNewRealm() {
        return editedRealm == null;
    }

    protected void removeRealm(@Nonnull Realm realm) {
        new AsyncRealmRemover(this.getActivity()).execute(realm);
    }

    protected void saveRealm(@Nonnull RealmBuilder realmBuilder, @Nullable RealmSaveHandler realmSaveHandler) {
        new AsynRealmSaver(this.getActivity(), realmService, realmSaveHandler).execute(realmBuilder);
    }

    protected void backButtonPressed() {
        T editedRealm = getEditedRealm();
        if (editedRealm != null) {
            eventManager.fire(RealmGuiEventType.newRealmEditFinishedEvent(editedRealm, RealmGuiEventType.FinishedState.back));
        } else {
            eventManager.fire(RealmDefGuiEventType.newRealmDefEditFinishedEvent(getRealmDef()));
        }
    }

    @Nonnull
    protected MessengerMultiPaneManager getMultiPaneManager() {
        return multiPaneManager;
    }

    @Nonnull
    public abstract RealmDef getRealmDef();

    @Nonnull
    protected CharSequence getFragmentTitle() {
        final String realmName = getString(getRealmDef().getNameResId());
        return getString(R.string.mpp_realm_configuration, realmName);
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    public static interface RealmSaveHandler {

        /**
         * @param e exception during saving the realm
         * @return true if exception was consumed and no further action is required
         */
        boolean onFailure(@Nonnull Exception e);

    }

}
