package com.example.xmpp.search;

import android.os.AsyncTask;
import com.example.xmpp.connection.XMPPConnectionManager;
import com.example.xmpp.interfaces.XMPPListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.Affiliation;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import java.util.Iterator;

public class CreatedNodesSeeker {

    public static void seekNodes(XMPPListener<String> callback) {
        Seeker seeker = new Seeker(callback);
        seeker.execute();
    }

    private static class Seeker extends AsyncTask<Void, Void, Iterator<Affiliation>> {
        private XMPPListener<String> callback;
        private Iterator<Affiliation> affiliations;
        private Exception exception;

        public Seeker(XMPPListener<String> callback) {
            this.callback = callback;
        }

        @Override
        protected Iterator<Affiliation> doInBackground(Void... voids) {
            try {
                PubSubManager manager = PubSubManager.getInstance(XMPPConnectionManager.getInstance().getConnection());
                affiliations = manager.getAffiliations().iterator();
            } catch (SmackException.NoResponseException e) {
                exception = e;
            } catch (XMPPException.XMPPErrorException e) {
                exception = e;
            } catch (SmackException.NotConnectedException e) {
                exception = e;
            } catch (InterruptedException e) {
                exception = e;
            }
            return affiliations;
        }

        protected void onPostExecute(Iterator<Affiliation> affiliations) {
            if(callback != null) {
                if(exception == null) {
                    while (affiliations.hasNext()) {
                        String node= affiliations.next().getNode();
                        callback.onSuccess(node);
                    }
                } else {
                    callback.onFailure(exception);
                }
            }
            callback.onFinish();
        }

    }
}
