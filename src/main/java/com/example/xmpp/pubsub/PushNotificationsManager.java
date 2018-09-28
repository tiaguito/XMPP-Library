package com.example.xmpp.pubsub;

import android.util.Log;
import com.example.xmpp.connection.XMPPConnectionManager;
import com.example.xmpp.entities.Node;
import com.example.xmpp.entities.User;
import com.example.xmpp.interfaces.XMPPListener;
import com.example.xmpp.interfaces.XMPPSubscriptionsListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubException;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.jivesoftware.smackx.xdata.Form;
import java.util.List;

public class PushNotificationsManager implements ItemEventListener {
    private static volatile PushNotificationsManager instance;
    private static PubSubManager pubSubManager;
    private static User user;

    private PushNotificationsManager() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        pubSubManager = PubSubManager.getInstance(XMPPConnectionManager.getInstance().getConnection());
        // TODO: rethink this because it is only one user per connection yet
        // don't know if I will have to add more stuff
        user = new User("", "");
        user.setName(XMPPConnectionManager.getInstance().getConnection().getUser().asEntityBareJidString());
        addNodeListeners();
    }

    public synchronized static PushNotificationsManager getInstance() {
        if (instance == null) {
            synchronized (PushNotificationsManager.class) {
                //Check for the second time.
                //if there is no instance available... create new one
                if (instance == null) {
                    instance = new PushNotificationsManager();
                }
            }
        }

        return instance;
    }

    /**
     * This method search for previously susbcribed nodes of a user and automatically
     * sets up listeners for new publications
     */
    private void addNodeListeners() {
        try {
            List<Subscription> subscriptions = pubSubManager.getSubscriptions();
            for(int i = 0; i < subscriptions.size(); i++) {
                org.jivesoftware.smackx.pubsub.Node node = pubSubManager.getNode(subscriptions.get(i).getNode());
                node.addItemEventListener(this);
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

    // TODO: CHECK BOTH THE SUBSCRIBE AND UNSUBSCRIBE METHODS BECAUSE
    // TODO: I HAVE SOME ISSUES MANAGING THE LISTENERS
    // TODO: ONCE THE APLICATION CLOSES OR IS CLOSED BY THE SYSTEM
    // TODO: SO THE METHODS NEED TO BE MORE ROBUST
    public void subscribe(String nodeName, XMPPSubscriptionsListener listener) {
        try {
            LeafNode node = pubSubManager.getLeafNode(nodeName);
            node.addItemEventListener(this);
            node.addItemEventListener(listener);
            node.subscribe(user.getName());
            listener.onSuccess(nodeName);
        } catch (PubSubException.NotALeafNodeException e) {
            listener.onError(e);
        } catch (SmackException.NoResponseException e) {
            listener.onError(e);
        } catch (SmackException.NotConnectedException e) {
            listener.onError(e);
        } catch (InterruptedException e) {
            listener.onError(e);
        } catch (XMPPException.XMPPErrorException e) {
            listener.onError(e);
        } catch (PubSubException.NotAPubSubNodeException e) {
            listener.onError(e);
        }

        /*
        PushNotificationsManager pushNotificationsManager =
                PushNotificationsManager.getInstanceFor(ConnectionXMPP.getInstance().getConnection());

        if(pushNotificationsManager.isSupported()) {
            Log.e(TAG, "onClick: Push Notifications Enabled in this node");
            pushNotificationsManager.enable(
                    ConnectionXMPP.getInstance().getConnection().getUser(), currentItem.getName());
        }*/
    }

    public void unsubscribe(String nodeName, XMPPSubscriptionsListener listener) {
        try {
            LeafNode node = pubSubManager.getLeafNode(nodeName);
            node.removeItemEventListener(this);
            node.removeItemEventListener(listener);
            node.unsubscribe(user.getName());
         } catch (PubSubException.NotALeafNodeException e) {
            listener.onError(e);
        } catch (SmackException.NoResponseException e) {
            listener.onError(e);
        } catch (SmackException.NotConnectedException e) {
            listener.onError(e);
        } catch (InterruptedException e) {
            listener.onError(e);
        } catch (XMPPException.XMPPErrorException e) {
            listener.onError(e);
        } catch (PubSubException.NotAPubSubNodeException e) {
            listener.onError(e);
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

    @Override
    public void handlePublishedItems(ItemPublishEvent items) {
        Log.i("XMPPLibrary", "handlePublishedItems: new items received from a node");
    }

    public void createNode(String nodeName, XMPPListener<LeafNode> callback) {
        try {
            pubSubManager.createNode(nodeName);
            callback.onSuccess(pubSubManager.getLeafNode(nodeName));
        } catch (SmackException.NoResponseException e) {
            callback.onFailure(e);
        } catch (XMPPException.XMPPErrorException e) {
            callback.onFailure(e);
        } catch (SmackException.NotConnectedException e) {
            callback.onFailure(e);
        } catch (InterruptedException e) {
            callback.onFailure(e);
        } catch (PubSubException.NotALeafNodeException e) {
            callback.onFailure(e);
        } catch (PubSubException.NotAPubSubNodeException e) {
            callback.onFailure(e);
        }
    }

    public void createNode(String nodeName, Form config, XMPPListener<LeafNode> callback) {
        try {
            pubSubManager.createNode(nodeName, config);
            callback.onSuccess(pubSubManager.getLeafNode(nodeName));
        } catch (SmackException.NoResponseException e) {
            callback.onFailure(e);
        } catch (XMPPException.XMPPErrorException e) {
            callback.onFailure(e);
        } catch (SmackException.NotConnectedException e) {
            callback.onFailure(e);
        } catch (InterruptedException e) {
            callback.onFailure(e);
        } catch (PubSubException.NotALeafNodeException e) {
            e.printStackTrace();
        } catch (PubSubException.NotAPubSubNodeException e) {
            e.printStackTrace();
        }
    }

    public void deleteNode(String nodeName, XMPPListener<Void> callback) {

        try {
            pubSubManager.deleteNode(nodeName);
            callback.onSuccess(null);
        } catch (SmackException.NoResponseException e) {
            callback.onFailure(e);
        } catch (XMPPException.XMPPErrorException e) {
            callback.onFailure(e);
        } catch (SmackException.NotConnectedException e) {
            callback.onFailure(e);
        } catch (InterruptedException e) {
            callback.onFailure(e);
        }
    }

    public void publish(String nodeId, String title, String body, XMPPListener<Item> callback) {
        try {
            LeafNode leafNode = pubSubManager.getLeafNode(nodeId);

            PayloadItem entry = new PayloadItem("test" + System.currentTimeMillis(),
                    new SimplePayload("entry", "http://www.w3.org/2005/Atom",
                            "<entry xmlns='http://www.w3.org/2005/Atom'><title>" + title + "</title><summary>" +
                                    body + "</summary></entry>"));
            leafNode.publish(entry);
            callback.onSuccess(entry);
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

    public void getAllNotifications(String nodeName, XMPPListener<List<PayloadItem>> callback) {
        try {
            LeafNode subscribeNode = pubSubManager.getLeafNode(nodeName);
            List<PayloadItem> items = subscribeNode.getItems();
            callback.onSuccess(items);
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

    public Node getSubscriptionState(DiscoverItems.Item item) {
        Node node = new Node();
        node.setSubscriptionStatus("");
        node.setName("");
        try {
            LeafNode leafNode = pubSubManager.getLeafNode(item.getNode());
            List<Subscription> subscriptions = leafNode.getSubscriptions();
            for(int i = 0; i < subscriptions.size(); i++) {
                String subscriptionStatus = subscriptions.get(i).getState().toString();
                node.setName(item.getNode());
                node.setSubscriptionStatus(subscriptionStatus);
            }
        } catch (PubSubException.NotALeafNodeException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (PubSubException.NotAPubSubNodeException e) {
            e.printStackTrace();
        }

        return node;
    }
}
