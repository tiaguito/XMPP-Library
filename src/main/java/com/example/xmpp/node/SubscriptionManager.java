package com.example.xmpp.node;

import com.example.xmpp.connection.XMPPConnectionManager;
import com.example.xmpp.interfaces.XMPPListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubException;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

public class SubscriptionManager {

    public static void unsubscribe(String nodeName, XMPPListener<Void> callback) {
        try {
            PubSubManager manager = PubSubManager.getInstance(XMPPConnectionManager.getInstance().getConnection());
            String userJid = XMPPConnectionManager.getInstance().getConnection().getUser().asBareJid().toString();
            LeafNode node = manager.getLeafNode(nodeName);
            //node.removeItemEventListener(listener);
            node.unsubscribe(userJid);
            callback.onSuccess(null);
        } catch (PubSubException.NotALeafNodeException e) {
            callback.onFailure(e);
        } catch (SmackException.NoResponseException e) {
            callback.onFailure(e);
        } catch (SmackException.NotConnectedException e) {
            callback.onFailure(e);
        } catch (InterruptedException e) {
            callback.onFailure(e);
        } catch (XMPPException.XMPPErrorException e) {
            callback.onFailure(e);
        } catch (PubSubException.NotAPubSubNodeException e) {
            callback.onFailure(e);
        }

        /*
        PushNotificationsManager pushNotificationsManager =
                PushNotificationsManager.getInstanceFor(ConnectionXMPP.getInstance().getConnection());

        if(pushNotificationsManager.isSupported()) {
            Log.e(TAG, "onClick: Push Notifications Enabled in this node");
            pushNotificationsManager.disable(
                    ConnectionXMPP.getInstance().getConnection().getUser(), currentItem.getName());
        }*/

    }

    public static void subscribe(String nodeName, XMPPListener<Subscription> callback) {
        try {
            PubSubManager manager = PubSubManager.getInstance(XMPPConnectionManager.getInstance().getConnection());
            String userJid = XMPPConnectionManager.getInstance().getConnection().getUser().asBareJid().toString();
            LeafNode node = manager.getLeafNode(nodeName);
            //node.addItemEventListener(listener);
            Subscription subscription = node.subscribe(userJid);
            callback.onSuccess(subscription);
        } catch (PubSubException.NotALeafNodeException e) {
            callback.onFailure(e);
        } catch (SmackException.NoResponseException e) {
            callback.onFailure(e);
        } catch (SmackException.NotConnectedException e) {
            callback.onFailure(e);
        } catch (InterruptedException e) {
            callback.onFailure(e);
        } catch (XMPPException.XMPPErrorException e) {
            callback.onFailure(e);
        } catch (PubSubException.NotAPubSubNodeException e) {
            callback.onFailure(e);
        }
    }
}
