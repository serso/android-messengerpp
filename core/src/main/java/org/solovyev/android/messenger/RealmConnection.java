package org.solovyev.android.messenger;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 5:52 PM
 */
public interface RealmConnection {

    void start();

    void stop();

    boolean isStopped();
}
