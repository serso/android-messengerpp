package org.solovyev.android.messenger.realms;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.security.InvalidCredentialsException;

import java.util.List;

public class AbstractRealmConfigurationFragment<R extends Realm<?>> extends RoboSherlockFragment {

    @NotNull
    public static final String EXTRA_REALM_ID = "realm_id";

    @Inject
    @NotNull
    private RealmService realmService;

    private R editedRealm;

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

    public R getEditedRealm() {
        return editedRealm;
    }

    public boolean isNewRealm() {
        return editedRealm == null;
    }

    protected void saveRealm(@NotNull RealmBuilder realmBuilder, @Nullable RealmSaveHandler realmSaveHandler) {
        new AsynRealmSaver(this.getActivity(), realmService, realmSaveHandler).execute(realmBuilder);
    }

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
            if (context instanceof Activity) {
                ((Activity) context).finish();
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

    public static interface RealmSaveHandler {

        /**
         *
         * @param e exception during saving the realm
         * @return true if exception was consumed and no further action is required
         */
        boolean onFailure(@NotNull Exception e);

    }
}
