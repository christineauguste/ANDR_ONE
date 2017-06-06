package info.appteve.radioelectro;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

public class ParserHttpUrl {

    @SuppressLint("DefaultLocale")
    public static String getUrl(String url) {
        String mUrllink = url.toUpperCase();
        if (mUrllink.endsWith(".FLAC")) {
            return url;
        } else if (mUrllink.endsWith(".MP3")) {
            return url;
        } else if (mUrllink.endsWith(".WAV")) {
            return url;
        } else if (mUrllink.endsWith(".M4A")) {
            return url;
        } else if (mUrllink.endsWith(".PLS")) {
            return url;

        } else if (mUrllink.endsWith(".M3U")) {
            ParserFilesM3U parserM3U = new ParserFilesM3U();
            LinkedList<String> urls = parserM3U.getRawUrl(url);
            if ((urls.size() > 0)) {
                return urls.get(0);
            }
        } else if (mUrllink.endsWith(".ASX")) {
            ParserFilesASX parserASX = new ParserFilesASX();
            LinkedList<String> urls = parserASX.getRawUrl(url);
            if ((urls.size() > 0)) {
                return urls.get(0);
            }
        } else {
            URLConnection urlConnection = getConnection(url);
            if (urlConnection != null) {
                String mContentDisposition = urlConnection.getHeaderField("Content-Disposition");

                Log.v("INFO", "Requesting: " + url + " Headers: " + urlConnection.getHeaderFields());

                String mContentType = urlConnection.getContentType();
                if (mContentType != null) {
                    mContentType = mContentType.toUpperCase();
                }
                if (mContentDisposition != null && mContentDisposition.toUpperCase().endsWith("M3U")) {
                    ParserFilesM3U m3u = new ParserFilesM3U();
                    LinkedList<String> urls = m3u.getRawUrl(urlConnection);
                    if (urls.size() > 0) {
                        return urls.getFirst();
                    }
                } else if (mContentType != null && mContentType.contains("AUDIO/X-SCPLS")) {
                    return url;
                } else if (mContentType != null && mContentType.contains("VIDEO/X-MS-ASF")) {
                    ParserFilesASX asx = new ParserFilesASX();
                    LinkedList<String> urls = asx.getRawUrl(url);
                    if ((urls.size() > 0)) {
                        return urls.get(0);
                    }
                    ParserFilesPLS pls = new ParserFilesPLS();
                    urls = pls.getRawUrl(url);
                    if ((urls.size() > 0)) {
                        return urls.get(0);
                    }
                } else if (mContentType != null && mContentType.contains("AUDIO/MPEG")) {
                    return url;
                } else if (mContentType != null && mContentType.contains("AUDIO/X-MPEGURL")) {
                    ParserFilesM3U m3u = new ParserFilesM3U();
                    LinkedList<String> urls = m3u.getRawUrl(url);
                    if ((urls.size() > 0)) {
                        return urls.get(0);
                    }
                } else {
                    Log.d("LOG", "Not Found");
                }
            }
        }
        return url;
    }

    private static URLConnection getConnection(String url) {
        URLConnection urlConnection;
        try {
            urlConnection = new URL(url).openConnection();
            return urlConnection;
        } catch (MalformedURLException e) {

        } catch (IOException e) {

        }
        return null;
    }

}
