package com.example.xmpp.config;

public class ServerConfiguration {
    private static volatile ServerConfiguration instance;
    private String xmppDomain;  // default: localhost
    private String hostAddres;  // default: 192.168.1.81
    private int serverPort;     // default: 5222

    public ServerConfiguration() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public synchronized static ServerConfiguration getInstance() {
        if (instance == null) {
            synchronized (ServerConfiguration.class) {
                //Check for the second time.
                //if there is no instance available... create new one
                if (instance == null) {
                    instance = new ServerConfiguration();
                }
            }
        }

        return instance;
    }

    public String getXmppDomain() {
        return xmppDomain;
    }

    public void setXmppDomain(String xmppDomain) {
        this.xmppDomain = xmppDomain;
    }

    public String getHostAddres() {
        return hostAddres;
    }

    public void setHostAddres(String hostAddres) {
        this.hostAddres = hostAddres;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
