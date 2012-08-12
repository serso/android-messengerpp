package org.solovyev.android.network;

/**
* User: serso
* Date: 7/26/12
* Time: 5:27 PM
*/
public enum NetworkState {
    UNKNOWN,

    /**
     * This state is returned if there is connectivity to any network *
     */
    CONNECTED,
    /**
     * This state is returned if there is no connectivity to any network. This is set
     * to true under two circumstances:
     * <ul>
     * <li>When connectivity is lost to one network, and there is no other available
     * network to attempt to switch to.</li>
     * <li>When connectivity is lost to one network, and the attempt to switch to
     * another network fails.</li>
     */
    NOT_CONNECTED
}
