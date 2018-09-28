package com.example.xmpp.notifications;

import android.util.Log;

import com.example.xmpp.connection.XMPPConnectionManager;
import com.example.xmpp.interfaces.XMPPSubscriptionsListener;

import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubException;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

import java.util.ArrayList;
import java.util.List;

public class XMPPNotifications implements ItemEventListener {
    private static volatile XMPPNotifications instance;
    private boolean status = false;
    private ArrayList<XMPPSubscriptionsListener> listeners = new ArrayList<>();
    private String TAG = XMPPNotifications.class.getSimpleName();

    private XMPPNotifications() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        ReconnectionManager.setEnabledPerDefault(true);
        ReconnectionManager.setDefaultFixedDelay(5);
    }

    public synchronized static XMPPNotifications getInstance() {
        if (instance == null) {
            synchronized (XMPPNotifications.class) {
                //Check for the second time.
                //if there is no instance available... create new one
                if (instance == null) {
                    instance = new XMPPNotifications();
                }
            }
        }

        return instance;
    }

    public void setEnablePushNotification(boolean status) {
        this.status = status;
    }

    public boolean isEnabledPushNotification() {
        return status;
    }

    public void listen(XMPPSubscriptionsListener listener) {
        if(isEnabledPushNotification()) {
            PubSubManager pubSubManager =
                    PubSubManager.getInstance(XMPPConnectionManager.getInstance().getConnection());
            try {
                List<Subscription> subscriptions = pubSubManager.getSubscriptions();
                for(int i = 0; i < subscriptions.size(); i++) {
                    Node node = pubSubManager.getNode(subscriptions.get(i).getNode());
                    node.addItemEventListener(this);
                    listeners.add(listener);
                }
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (PubSubException.NotAPubSubNodeException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void handlePublishedItems(ItemPublishEvent items) {
        Log.i(TAG, "handlePublishedItems: new notifications arrived");
        for(int i = 0; i < listeners.size(); i++) {
            listeners.get(i).handlePublishedItems(items);
        }
    }
}
