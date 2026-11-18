package com.consobox.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Minimal HTTP helpers (the Apache HttpClient used originally was removed from Android). */
public class Network {
    private static final int TIMEOUT_MS = 15000;

    private static HttpURLConnection open(String uri) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        return conn;
    }

    public static String makeRequest(String uri) {
        StringBuilder reponse = new StringBuilder();
        HttpURLConnection conn = null;
        try {
            conn = open(uri);
            if (conn.getResponseCode() / 100 != 2) {
                return "";
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    reponse.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return reponse.toString();
    }

    public static boolean makeRequest(String uri, File f) {
        HttpURLConnection conn = null;
        try {
            conn = open(uri);
            if (conn.getResponseCode() / 100 != 2) {
                return false;
            }
            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 OutputStream out = new FileOutputStream(f)) {
                byte[] buffer = new byte[8192];
                int size;
                while ((size = in.read(buffer)) != -1) {
                    out.write(buffer, 0, size);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
