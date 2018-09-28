package com.example.xmpp.connection;

import android.util.Log;

import com.example.xmpp.config.ServerConfiguration;
import com.example.xmpp.entities.User;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class XMPPConnectionManager
        implements ConnectionListener, ReconnectionListener, PingFailedListener {
    private static final String TAG = "XMPPLibrary";
    private static volatile XMPPConnectionManager instance;
    private static AbstractXMPPConnection connection;
    private static ReconnectionManager reconnectionManager;
    private static XMPPTCPConnectionConfiguration config;
    private boolean authenticated = false;
    private boolean connected;

    private XMPPConnectionManager() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        ReconnectionManager.setEnabledPerDefault(true);
        ReconnectionManager.setDefaultFixedDelay(5);
    }

    public synchronized static XMPPConnectionManager getInstance() {
        if (instance == null) {
            synchronized (XMPPConnectionManager.class) {
                //Check for the second time.
                //if there is no instance available... create new one
                if (instance == null) {
                    instance = new XMPPConnectionManager();
                }
            }
        }

        return instance;
    }

    public void connect() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Beginning connection");
                    addConnectionListener(XMPPConnectionManager.this);
                    connection.connect();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }


    public void addConnectionListener(ConnectionListener connectionListener) {
        connection.addConnectionListener(connectionListener);
    }

    public void setReconnectionAllowed(boolean allow) {
        if(allow) {
            if(connection.isAuthenticated()) {
                reconnectionManager = ReconnectionManager.getInstanceFor(connection);

                if(reconnectionManager.isAutomaticReconnectEnabled()) {
                    reconnectionManager.addReconnectionListener(this);
                }
            }
        } else {
            reconnectionManager.disableAutomaticReconnection();
            reconnectionManager.removeReconnectionListener(this);
            reconnectionManager = null;
        }
    }

    public void login(User user) {
        try {
            Log.e(TAG, "Beginning login");
            if(connection.isConnected()) {

                connection.login(user.getName(), user.getPassword());
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public AbstractXMPPConnection getConnection() {
        return connection;
    }

    private void setupConnection() {
        if(config != null) {
            connection = new XMPPTCPConnection(config);
        } else {
            throw new RuntimeException("Please set the server configurations.");
        }
    }

    public void setupConnection(ServerConfiguration serverConfig) {
        if(connection != null && connection.isConnected()) {
            connection.disconnect();
        }

        try {
            InetAddress addr = InetAddress.getByName(serverConfig.getHostAddres());
            DomainBareJid serviceName = JidCreate.domainBareFrom(serverConfig.getXmppDomain());
            config = XMPPTCPConnectionConfiguration.builder()
                    // TODO: Change
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setXmppDomain(serviceName)
                    .setHostAddress(addr)
                    .setPort(serverConfig.getServerPort())
                    // TODO: Change
                    .enableDefaultDebugger()
                    .build();

            connection = new XMPPTCPConnection(config);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if(connection.isConnected()) {
            connection.disconnect();
        }
        setupConnection();
    }

    @Override
    public void connected(XMPPConnection connection) {
        Log.i(TAG, "connected");
        connected = true;
    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.i(TAG, "authenticated");
        authenticated = true;
    }

    @Override
    public void connectionClosed() {
        Log.i(TAG, "connectionClosed");
        authenticated = false;
        connected = false;
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.i(TAG, "connectionClosedOnError\n" + e.getMessage());
        authenticated = false;
        connected = false;
    }

    @Override
    public void reconnectingIn(int seconds) {

    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.e(TAG, "reconnectionFailed: failed reconnection \n" + e.getMessage());
    }

    @Override
    public void pingFailed() {
        Log.e(TAG, "pingFailed");
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean isConnected() {
        return connected;
    }
}
