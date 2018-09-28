package com.example.xmpp.search;

import android.os.AsyncTask;
import android.util.Log;

import com.example.xmpp.config.ServerConfiguration;
import com.example.xmpp.connection.XMPPConnectionManager;
import com.example.xmpp.interfaces.XMPPListener;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.Affiliation;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubException;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NodeSearcher {
    private static final String TAG = NodeSearcher.class.getSimpleName();

    public static void serchNodes(String query, XMPPListener<DiscoverItems.Item> callback) {
        Searcher searcher = new Searcher(query, callback);
        searcher.execute();
    }

    private static class Searcher extends AsyncTask<Void, Void, List<DiscoverItems.Item>> {

        private final XMPPListener<DiscoverItems.Item> callback;
        private Exception exception;
        private List<DiscoverItems.Item> nodes = new ArrayList<>();

        // TODO: add search conforming the query options
        // TODO: for now it only searches for all the available nodes
        private String query;

        public Searcher(String query, XMPPListener<DiscoverItems.Item> callback) {
            this.callback = callback;
            this.query = query;
        }

        @Override
        protected List<DiscoverItems.Item> doInBackground(Void... voids) {

            try {
                PubSubManager manager = PubSubManager.getInstance(XMPPConnectionManager.getInstance().getConnection());
                ServiceDiscoveryManager discoManager =
                        ServiceDiscoveryManager.getInstanceFor(XMPPConnectionManager.getInstance().getConnection());
                DomainBareJid serviceName =
                        JidCreate.domainBareFrom(ServerConfiguration.getInstance().getXmppDomain());
                DiscoverItems discoveredItems = discoManager.discoverItems(serviceName);
                Iterator<DiscoverItems.Item> entities = discoveredItems.getItems().iterator();

                while(entities.hasNext()) {
                    discoveredItems = null;
                    try {
                        discoveredItems =
                                discoManager.discoverItems(JidCreate.domainBareFrom(entities.next().getEntityID().toString()));
                    } catch (XMPPException.XMPPErrorException e) {

                    }
                    if(discoveredItems != null) {
                        for(DiscoverItems.Item item : discoveredItems.getItems()) {
                            LeafNode leafNode = manager.getLeafNode(item.getNode());
                            List<Affiliation> affiliations = leafNode.getAffiliations();
                            Log.e(TAG, "Affiliations size: " + affiliations.size());

                            /**
                             * if you own a node in this application
                             * you cannot subscribe to it
                             */
                            if(affiliations.size() != 0) {
                                for(int i = 0; i < affiliations.size(); i++) {
                                    if(!affiliations.get(i).getAffiliation().name().equals("owner") ||
                                            !affiliations.get(i).getAffiliation().name().equals("outcast")) {
                                        nodes.add(item);
                                    }
                                }
                            } else {
                                nodes.add(item);
                            }
                        }
                    }
                }
            } catch (SmackException.NoResponseException e) {
                exception = e;
            } catch (SmackException.NotConnectedException e) {
                exception = e;
            } catch (InterruptedException e) {
                exception = e;
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (PubSubException.NotALeafNodeException e) {
                e.printStackTrace();
            } catch (PubSubException.NotAPubSubNodeException e) {
                e.printStackTrace();
            }

            return nodes;
        }

        @Override
        protected void onPostExecute(List<DiscoverItems.Item> nodes) {
            if(callback != null) {
                if(exception == null) {
                    for(int i = 0; i < nodes.size(); i++) {
                        callback.onSuccess(nodes.get(i));
                    }
                } else {
                    callback.onFailure(exception);
                }
            }
            callback.onFinish();
        }
    }
}