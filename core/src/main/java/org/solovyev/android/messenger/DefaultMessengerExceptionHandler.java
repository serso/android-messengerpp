package org.solovyev.android.messenger;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.Threads;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.realms.RealmConnectionException;
import org.solovyev.android.messenger.realms.RealmException;
import org.solovyev.android.messenger.realms.UnsupportedRealmException;

import javax.annotation.Nonnull;

@Singleton
public final class DefaultMessengerExceptionHandler implements MessengerExceptionHandler {

    @Nonnull
    private final Context context; 
    
    @Nonnull
    private final Handler uiHandler = Threads.newUiHandler();

    @Inject
    public DefaultMessengerExceptionHandler(@Nonnull Application application) {
        this.context = application;
    }

    @Override
    public void handleException(@Nonnull final Throwable e) {
        // todo serso: translate
        if (e instanceof UnsupportedRealmException) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Account was removed, if this message still occurs - contact developers!", Toast.LENGTH_LONG).show();
                }
            });
        } else if (e instanceof RealmConnectionException) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Some errors occurred while connecting to remote server, check internet connection and try again!", Toast.LENGTH_LONG).show();
                }
            });
        } else if (e instanceof RealmException) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Some account exception occurred!", Toast.LENGTH_LONG).show();
                }
            });
        } else if (e instanceof HttpRuntimeIoException) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "No internet connection available: connect to the network and try again!", Toast.LENGTH_LONG).show();
                }
            });
        } else if (e instanceof IllegalJsonRuntimeException) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "The response from server is not valid!", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Something is going wrong!", Toast.LENGTH_LONG).show();
                }
            });
        }
        Log.e(MessengerApplication.TAG, e.getMessage(), e);
    }
}
