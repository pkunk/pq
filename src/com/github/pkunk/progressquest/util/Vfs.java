package com.github.pkunk.progressquest.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * User: pkunk
 * Date: 2012-01-31
 */
public class Vfs {
    private static final String TAG = Vfs.class.getCanonicalName();

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String SETTINGS = "Settings";

    public static final String EQ = "=";
    public static final String SEPARATOR = ";";

    public static final String ZIP_EXT = ".zip";
    public static final String BAK_EXT = ".bak";


    public static void setPlayerId(Context context, String playerId) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("playerId", playerId);
        editor.commit();
    }

    public static String getPlayerId(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return settings.getString("playerId", null);
    }

    public static void writeToFile(Context context, String fileName, Map<String, List<String>> dataMap) throws IOException {
        OutputStream os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
        try {
            for (Map.Entry<String, List<String>> dataEntry : dataMap.entrySet()) {
                byte[] bytes = toByteArray(dataEntry.getValue());
                ZipEntry entry = new ZipEntry(dataEntry.getKey());
                zos.putNextEntry(entry);
                zos.write(bytes);
                zos.closeEntry();
            }
        } finally {
            try {
                zos.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    public static Map<String, List<String>> readFromFile(Context context, String fileName) throws IOException {

        Map<String, List<String>> result = new HashMap<String, List<String>>();

        InputStream is = context.openFileInput(fileName);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        try {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                String entryName = ze.getName();
                List<String> entryData = fromByteArray(baos.toByteArray());
                result.put(entryName, entryData);
            }
        } finally {
            try {
                zis.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return result;
    }

    public static String[] getSaveFiles (Context context) {
        File saveDir = context.getFilesDir();
        String[] saveFiles = saveDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(ZIP_EXT)) {
                    return true;
                }
                return false;
            }
        });
        return saveFiles;
    }

    public static Map<String, List<String>> readEntryFromFiles (Context context, String[] fileNames, String entry) {
        Map<String, List<String>> result = new HashMap<String, List<String>>(fileNames.length);
        File saveDir = context.getFilesDir();
        for (String fileName : fileNames) {
            try {
                File file = new File(saveDir, fileName);
                ZipFile zipFile = new ZipFile(file);
                ZipEntry zipEntry = zipFile.getEntry(entry);
                InputStream is = new BufferedInputStream(zipFile.getInputStream(zipEntry));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                List<String> entryData = fromByteArray(baos.toByteArray());
                result.put(fileName, entryData);
            } catch (IOException ioe) {
                Log.e(TAG, ioe.getMessage());
                ioe.printStackTrace();
            }
        }
        return result;
    }

    public static boolean deleteFile(Context context, String fileName) {
        File saveDir = context.getFilesDir();
        File file = new File(saveDir, fileName);
        return file.delete();
    }

    private static List<String> fromByteArray(byte[] array) {
        String read = new String(array, UTF8);
        List<String> result = Arrays.asList(read.split("\n"));
        return result;
    }

    private static byte[] toByteArray(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            builder.append(s).append("\n");
        }
        String result = builder.toString();
        return result.getBytes(UTF8);
    }
}
