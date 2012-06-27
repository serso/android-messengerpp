package org.solovyev.android.messenger.users;

import android.os.IBinder;
import android.os.RemoteException;
import org.solovyev.android.messenger.MessengerApi;
import org.solovyev.android.messenger.ServiceCallback;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 9:35 PM
 */
public class DummyMessengerApi implements MessengerApi {


    @Override
    public void loginUser(String login, String password, ServiceCallback callback) throws RemoteException {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public IBinder asBinder() {
        return null;
    }
}
