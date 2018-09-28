package com.example.xmpp.interfaces;

import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

public interface XMPPSubscriptionsListener extends ItemEventListener {
    void onSuccess(String nodeName);
    void onError(Exception e);
}
