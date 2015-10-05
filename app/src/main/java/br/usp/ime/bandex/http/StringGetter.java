package br.usp.ime.bandex.http;

/** Adaptado de RAJ AMAL
 * Autor: RAJ AMAL
 * Fonte: http://www.learn2crack.com/2013/10/android-asynctask-json-parsing-example.html
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class StringGetter {

    static InputStream is = null;
    static String returnValue = "";

    // constructor
    public StringGetter() {

    }

    /* Returns the String corresponding to the url
       Returns null if any error occurred, like internet connection down
    */
    public String getStringFromUrl(final String url) {
        // The thread that is waiting to execute the HTTP request
        final Thread waitingThread = Thread.currentThread();
        final AtomicBoolean running = new AtomicBoolean(true);
        final Thread t = new Thread() {
                public void run() {
                    try {
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet(url);
                        HttpResponse httpResponse = httpClient.execute(httpGet);
                        HttpEntity httpEntity = httpResponse.getEntity();
                        is = httpEntity.getContent();

                        if (running.get()) {
                            // Signal the waiting thread that it can do the HTTP request now
                            waitingThread.interrupt();
                        }
                    } catch (Exception e) {
                        // Some problem, just ignore it
                        e.printStackTrace();
                    }
                }
            };

        try {
            // Start name resolution
                t.start();
                // Sleep for as long as we are willing to wait for the DNS resolution
                Thread.sleep(5000);
                // If we slept the entire time without getting interrupted, the DNS resolution took too long
                //  so assume we have no connectivity.
                running.set(false); // We don't want to be interrupted anymore
                // Don't even bother trying the HTTP request now, we've used up all the time we have
                return null;
            } catch (InterruptedException ie) {
                // We got interrupted, so the DNS resolution must have been successful. Do the HTTP request now
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(new String(line.getBytes("ISO-8859-1"), "UTF-8"));
                    }
                    is.close();
                    returnValue = sb.toString();
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                    return null;
                }
            return returnValue;
            }
        /*} catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }*/
    }
}
