package com.example.xmpp.search;

import android.os.AsyncTask;

import com.example.xmpp.config.ServerConfiguration;
import com.example.xmpp.connection.XMPPConnectionManager;
import com.example.xmpp.interfaces.XMPPListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.IOException;
import java.util.Iterator;

public class EntitySeeker {

    public static void seekEntities(XMPPListener<String> callback) {
        Seeker seeker = new Seeker(callback);
        seeker.execute();
    }

    private static class Seeker extends AsyncTask<Void, Void, Iterator> {
        private final XMPPListener<String> callback;
        private Exception exception;

        public Seeker(XMPPListener<String> callback) {
            this.callback = callback;
        }

        @Override
        protected Iterator doInBackground(Void... voids) {
            Iterator it = null;
            try {
                DomainBareJid serviceName =
                        JidCreate.domainBareFrom(ServerConfiguration.getInstance().getXmppDomain());
                // Obtain the ServiceDiscoveryManager associated with my ConnectionXMPP
                ServiceDiscoveryManager discoManager =
                        ServiceDiscoveryManager.getInstanceFor(XMPPConnectionManager.getInstance().getConnection());
                // Get the items of a given XMPP entity
                // This example gets the items associated with online catalog service

                DiscoverItems discoItems = discoManager.discoverItems(serviceName);
                // Get the discovered items of the queried XMPP entity
                it = discoItems.getItems().iterator();
                // Display the items of the remote XMPP entity
            } catch (SmackException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            } catch (XMPPException e) {
                exception = e;
            } catch (InterruptedException e) {
                exception = e;
            }

            return it;
        }

        protected void onPostExecute(Iterator iterator) {
            if(callback != null) {
                if(exception == null) {
                    while (iterator.hasNext()) {
                        DiscoverItems.Item item = (DiscoverItems.Item) iterator.next();
                        callback.onSuccess(item.getEntityID().toString());
                    }
                } else {
                    callback.onFailure(exception);
                }
            }

            callback.onFinish();
        }
    }
}
