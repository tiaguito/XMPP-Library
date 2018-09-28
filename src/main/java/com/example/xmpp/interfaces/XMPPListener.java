package com.example.xmpp.interfaces;

public interface XMPPListener<T> {
    public void onSuccess(T item);
    public void onFailure(Exception e);
    public void onFinish();
}
