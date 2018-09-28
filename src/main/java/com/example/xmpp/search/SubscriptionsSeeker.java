package com.example.xmpp.search;

import android.os.AsyncTask;

import com.example.xmpp.connection.XMPPConnectionManager;
import com.example.xmpp.interfaces.XMPPListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;

import java.util.List;

public class SubscriptionsSeeker extends AsyncTask<Void, Void, List<Subscription>> {

    private final XMPPListener<List<Subscription>> callback;
    private Exception exception;

    public SubscriptionsSeeker(XMPPListener<List<Subscription>> callback) {
        this.callback = callback;
    }

    @Override
    protected List<Subscription> doInBackground(Void... voids) {
        List<Subscription> listSubs = null;
        try {
            PubSubManager manager = PubSubManager.getInstance(XMPPConnectionManager.getInstance().getConnection());
            listSubs = manager.getSubscriptions();
        } catch (SmackException.NoResponseException e) {
            exception = e;
        } catch (XMPPException.XMPPErrorException e) {
            exception = e;
        } catch (SmackException.NotConnectedException e) {
            exception = e;
        } catch (InterruptedException e) {
            exception = e;
        }

        return listSubs;
    }

    @Override
    protected void onPostExecute(List<Subscription> subscriptions) {
        if(callback != null) {
            if(exception == null) {
                callback.onSuccess(subscriptions);
            } else {
                callback.onFailure(exception);
            }
        }
        callback.onFinish();
    }
}
