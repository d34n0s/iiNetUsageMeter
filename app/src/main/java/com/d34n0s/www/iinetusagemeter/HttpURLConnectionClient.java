package com.d34n0s.www.iinetusagemeter;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
// google are focusing on http url connection now as part of the core Java API.

public class HttpURLConnectionClient {

    public static String getData(String uri) {  //When this method is called it passes in the uri string.

        BufferedReader reader = null;

        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            //gets content from the web,
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            //  grabbing 1 line at a time from the site until it reaches an empty line..
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }



        }
    }
    //This method only runs if connecting to the secure feed.
    public static String getData(String uri, String userName, String password) {

        BufferedReader reader = null;
        HttpURLConnection con = null;

        byte[] loginBytes = (userName + ":" + password).getBytes();
        StringBuilder loginBuilder = new StringBuilder()
                .append("Basic ")
                .append(Base64.encodeToString(loginBytes, Base64.DEFAULT));



        try {
            URL url = new URL(uri);
            con = (HttpURLConnection) url.openConnection();

            con.addRequestProperty("Authorization", loginBuilder.toString());

            //gets content from the web,
            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            //  grabbing 1 line at a time from the site until it reaches an empty line..

            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            int status = 0;
            try {
                status = con.getResponseCode();
                Log.d("HttpManager", "HTTP response code: " + status);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }



            }



        }
    }

}