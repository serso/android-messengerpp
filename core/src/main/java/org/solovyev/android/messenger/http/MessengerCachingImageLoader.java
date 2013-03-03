package org.solovyev.android.messenger.http;

import android.app.Application;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nonnull;
import org.solovyev.android.http.CachingImageLoader;

/**
 * User: serso
 * Date: 8/12/12
 * Time: 10:43 PM
 */
@Singleton
public class MessengerCachingImageLoader extends CachingImageLoader {

    @Inject
    public MessengerCachingImageLoader(@Nonnull Application context) {
        super(context, "messenger");
    }
}
