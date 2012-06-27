package org.solovyev.android;

import android.os.Looper;

/**
 * User: serso
 * Date: 6/27/12
 * Time: 1:54 PM
 */
public class AndroidUtils2 {

    private AndroidUtils2() {
        throw new AssertionError();
    }

    public static boolean isUiThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
