package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import java.util.List;

public class BaseRealmConfigurationFragment<R extends Realm<?>> extends RoboSherlockFragment {

    @NotNull
    public static final String EXTRA_REALM_ID = "realm_id";

    @Inject
    @NotNull
    private RealmService realmService;

    @Inject
    @NotNull
    private MessengerMultiPaneManager multiPaneManager;

    private R editedRealm;

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
                editedRealm = (R) realmService.getRealmById(realmId);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = ViewFromLayoutBuilder.newInstance(layoutResId).build(this.getActivity());

        getMultiPaneManager().fillContentPane(this.getActivity(), container, result);

        result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return result;
    }

    public R getEditedRealm() {
        return editedRealm;
    }

    public boolean isNewRealm() {
        return editedRealm == null;
    }

    protected void removeRealm(@NotNull Realm realm) {
        new AsynRealmRemover(this.getActivity(), realmService).execute(realm);
    }

    protected void saveRealm(@NotNull RealmBuilder realmBuilder, @Nullable RealmSaveHandler realmSaveHandler) {
        new AsynRealmSaver(this.getActivity(), realmService, realmSaveHandler).execute(realmBuilder);
    }

    protected void backButtonPressed() {
        final EventManager eventManager = RoboGuice.getInjector(getActivity()).getInstance(EventManager.class);
        eventManager.fire(new FinishedEvent());
    }

    @NotNull
    protected MessengerMultiPaneManager getMultiPaneManager() {
        return multiPaneManager;
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    private static class AsynRealmSaver extends MessengerAsyncTask<RealmBuilder, Integer, Void> {

        @NotNull
        private final RealmService realmService;

        @Nullable
        private final RealmSaveHandler realmSaveHandler;

        private AsynRealmSaver(@NotNull Activity context,
                               @NotNull RealmService realmService,
                               @Nullable RealmSaveHandler realmSaveHandler) {
            super(context, true);
            this.realmService = realmService;
            this.realmSaveHandler = realmSaveHandler;
        }

        @Override
        protected Void doWork(@NotNull List<RealmBuilder> realmBuilders) {
            for (RealmBuilder realmBuilder : realmBuilders) {
                try {
                    realmService.saveRealm(realmBuilder);
                } catch (InvalidCredentialsException e) {
                    throwException(e);
                } catch (RealmAlreadyExistsException e) {
                    throwException(e);
                }
            }

            return null;
        }

        @Override
        protected void onSuccessPostExecute(@Nullable Void result) {
            final Context context = getContext();
            if (context != null) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(new FinishedEvent());
            }
        }

        @Override
        protected void onFailurePostExecute(@NotNull Exception e) {
            boolean consumed = realmSaveHandler != null && realmSaveHandler.onFailure(e);
            if (!consumed) {
                if (e instanceof InvalidCredentialsException) {
                    Toast.makeText(getContext(), "Invalid credentials!", Toast.LENGTH_SHORT).show();
                    Log.e("XmppRealm", e.getMessage(), e);
                } else if (e instanceof RealmAlreadyExistsException) {
                    Toast.makeText(getContext(), "Same account alraedy configured!", Toast.LENGTH_SHORT).show();
                    Log.e("XmppRealm", e.getMessage(), e);
                } else {
                    super.onFailurePostExecute(e);
                }
            }
        }
    }

    private static class AsynRealmRemover extends MessengerAsyncTask<Realm, Integer, Void> {

        @NotNull
        private final RealmService realmService;

        private AsynRealmRemover(@NotNull Activity context,
                                 @NotNull RealmService realmService) {
            super(context, true);
            this.realmService = realmService;
        }

        @Override
        protected Void doWork(@NotNull List<Realm> realms) {
            for (Realm realm : realms) {
                realmService.removeRealm(realm.getId());
            }

            return null;
        }

        @Override
        protected void onSuccessPostExecute(@Nullable Void result) {
            final Context context = getContext();
            if (context != null) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(new FinishedEvent());
            }
        }
    }

    public static interface RealmSaveHandler {

        /**
         * @param e exception during saving the realm
         * @return true if exception was consumed and no further action is required
         */
        boolean onFailure(@NotNull Exception e);

    }

    public static final class FinishedEvent {

    }
}
