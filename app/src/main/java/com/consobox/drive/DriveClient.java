package com.consobox.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tiny Google Drive v3 REST client.
 * Uses the "drive.file" scope: the app only sees the files and folders it created itself.
 * Backups are stored in a "ConsoBox" folder at the root of the user's Drive.
 */
public class DriveClient {
    private static final String API = "https://www.googleapis.com/drive/v3/";
    private static final String UPLOAD = "https://www.googleapis.com/upload/drive/v3/files";
    private static final String FOLDER_MIME = "application/vnd.google-apps.folder";
    public static final String FOLDER_NAME = "ConsoBox";
    /** Number of backups kept on Drive; older ones are deleted. */
    public static final int MAX_BACKUPS = 12;

    private final String mToken;
    private final SharedPreferences mPref;

    public static class DriveFile {
        public final String id;
        public final String name;
        public final String createdTime;
        public final long size;

        DriveFile(JSONObject o) {
            id = o.optString("id");
            name = o.optString("name");
            createdTime = o.optString("createdTime");
            size = o.optLong("size", 0);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class DriveException extends IOException {
        public final int code;

        DriveException(int code, String message) {
            super(code + " " + message);
            this.code = code;
        }
    }

    public DriveClient(Context context, String accessToken) {
        mToken = accessToken;
        mPref = DriveBackup.prefs(context);
    }

    public String getUserEmail() throws IOException {
        try {
            JSONObject o = new JSONObject(request("GET", API + "about?fields=user(emailAddress)", null, null));
            return o.getJSONObject("user").optString("emailAddress", null);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /** Uploads a new backup file and removes the oldest ones beyond {@link #MAX_BACKUPS}. */
    public DriveFile uploadBackup(byte[] data) throws IOException {
        String folderId = getOrCreateFolder();
        String name = "consobox-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(new Date()) + ".bak";
        String boundary = "consobox-" + UUID.randomUUID();
        try {
            JSONObject meta = new JSONObject();
            meta.put("name", name);
            meta.put("mimeType", "application/octet-stream");
            meta.put("parents", new JSONArray().put(folderId));
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            body.write(("--" + boundary + "\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(meta.toString().getBytes(StandardCharsets.UTF_8));
            body.write(("\r\n--" + boundary + "\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(data);
            body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            String resp = request("POST", UPLOAD + "?uploadType=multipart&fields=id,name,createdTime,size",
                    "multipart/related; boundary=" + boundary, body.toByteArray());
            DriveFile file = new DriveFile(new JSONObject(resp));
            pruneOldBackups();
            return file;
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /** Backups, most recent first. */
    public List<DriveFile> listBackups() throws IOException {
        List<DriveFile> res = new ArrayList<>();
        String folderId = findFolder();
        if (folderId == null) {
            return res;
        }
        String q = "'" + folderId + "' in parents and trashed=false and mimeType!='" + FOLDER_MIME + "'";
        String url = API + "files?spaces=drive&orderBy=createdTime%20desc&pageSize=100&fields=files(id,name,createdTime,size)&q=" + Uri.encode(q);
        try {
            JSONArray files = new JSONObject(request("GET", url, null, null)).getJSONArray("files");
            for (int i = 0; i < files.length(); i++) {
                res.add(new DriveFile(files.getJSONObject(i)));
            }
        } catch (JSONException e) {
            throw new IOException(e);
        }
        return res;
    }

    public InputStream download(String fileId) throws IOException {
        HttpURLConnection conn = open("GET", API + "files/" + fileId + "?alt=media", null);
        int code = conn.getResponseCode();
        if (code / 100 != 2) {
            throw new DriveException(code, readError(conn));
        }
        return conn.getInputStream();
    }

    private void pruneOldBackups() {
        try {
            List<DriveFile> files = listBackups();
            for (int i = MAX_BACKUPS; i < files.size(); i++) {
                request("DELETE", API + "files/" + files.get(i).id, null, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getOrCreateFolder() throws IOException {
        String id = findFolder();
        if (id != null) {
            return id;
        }
        try {
            JSONObject meta = new JSONObject();
            meta.put("name", FOLDER_NAME);
            meta.put("mimeType", FOLDER_MIME);
            String resp = request("POST", API + "files?fields=id", "application/json; charset=UTF-8",
                    meta.toString().getBytes(StandardCharsets.UTF_8));
            id = new JSONObject(resp).getString("id");
            mPref.edit().putString(DriveBackup.PREF_FOLDER_ID, id).apply();
            return id;
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private String findFolder() throws IOException {
        String cached = mPref.getString(DriveBackup.PREF_FOLDER_ID, null);
        if (cached != null) {
            try {
                JSONObject o = new JSONObject(request("GET", API + "files/" + cached + "?fields=id,trashed", null, null));
                if (!o.optBoolean("trashed", false)) {
                    return cached;
                }
            } catch (DriveException e) {
                if (e.code != 404) {
                    throw e;
                }
            } catch (JSONException ignored) {
            }
            mPref.edit().remove(DriveBackup.PREF_FOLDER_ID).apply();
        }
        String q = "name='" + FOLDER_NAME + "' and mimeType='" + FOLDER_MIME + "' and trashed=false";
        try {
            JSONArray files = new JSONObject(request("GET", API + "files?spaces=drive&fields=files(id)&q=" + Uri.encode(q), null, null))
                    .getJSONArray("files");
            if (files.length() == 0) {
                return null;
            }
            String id = files.getJSONObject(0).getString("id");
            mPref.edit().putString(DriveBackup.PREF_FOLDER_ID, id).apply();
            return id;
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    private HttpURLConnection open(String method, String url, String contentType) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Authorization", "Bearer " + mToken);
        if (contentType != null) {
            conn.setRequestProperty("Content-Type", contentType);
        }
        return conn;
    }

    private String request(String method, String url, String contentType, byte[] body) throws IOException {
        HttpURLConnection conn = open(method, url, contentType);
        try {
            if (body != null) {
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(body.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                }
            }
            int code = conn.getResponseCode();
            if (code / 100 != 2) {
                throw new DriveException(code, readError(conn));
            }
            if (code == 204) {
                return "";
            }
            return readAll(conn.getInputStream());
        } finally {
            conn.disconnect();
        }
    }

    private static String readError(HttpURLConnection conn) {
        try {
            InputStream es = conn.getErrorStream();
            if (es == null) {
                return conn.getResponseMessage();
            }
            String txt = readAll(es);
            try {
                return new JSONObject(txt).getJSONObject("error").optString("message", txt);
            } catch (JSONException e) {
                return txt;
            }
        } catch (IOException e) {
            return "";
        }
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        try (InputStream is = in) {
            while ((n = is.read(buf)) != -1) {
                bos.write(buf, 0, n);
            }
        }
        return bos.toString("UTF-8");
    }
}
