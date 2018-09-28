package com.example.xmpp.utils;

import android.util.Log;

import com.example.xmpp.entities.Payload;

import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ItemParser {

    public static final String TAG = ItemParser.class.getSimpleName();

    public static List<Payload> parsePayloadItems(List<PayloadItem> items) {
        List<Payload> publishedItems = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) {
            publishedItems.add(new Payload());
            int index = items.get(i).getPayload().toString().indexOf(" ") + 2;
            int length = items.get(i).getPayload().toString().length() - 1;
            Log.e(TAG, "Persisted Item: " + items.get(i).getPayload().toString().substring(index, length));
            XmlPullParserFactory factory = null;
            try {
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(items.get(i).getPayload().toString().substring(index, length)));
                int eventType = parser.getEventType();
                String tagName = "";
                String text;

                while(eventType != XmlPullParser.END_DOCUMENT) {
                    switch(eventType) {
                        case XmlPullParser.START_TAG:
                            tagName = parser.getName();
                            break;
                        case XmlPullParser.TEXT:
                            text = parser.getText();
                            if(tagName.equals("title")) {
                                publishedItems.get(i).setTitle(text);
                            } else if(tagName.equals("summary")) {
                                publishedItems.get(i).setSummary(text);
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return publishedItems;
    }
}
