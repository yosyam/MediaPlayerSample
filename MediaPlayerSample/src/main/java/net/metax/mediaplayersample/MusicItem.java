package net.metax.mediaplayersample;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yoshi on 13/08/19.
 */
public class MusicItem implements Comparable<Object> {

    private static final String TAG = "MusicItem";
    final long id;
    final String artist;
    final String title;
    final String album;
    final int truck;
    final long duration;

    public MusicItem(long id, String artist, String title ,String album, int truck, long duration) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.truck = truck;
        this.duration = duration;
    }

    public Uri getURI() {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    public static List<MusicItem> getMusicItems(Context context) {
        List<MusicItem> items = new LinkedList<MusicItem>();

        ContentResolver cr = context.getContentResolver();

        Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);

        if (cur != null) {
            if (cur.moveToFirst()) {
                Log.i(TAG, "Listing...");

                int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);
                int idTruck = cur.getColumnIndex(MediaStore.Audio.Media.TRACK);

                Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
                Log.i(TAG, "ID column index: " + String.valueOf(idColumn));
                do {
                    Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
                    items.add(new MusicItem(cur.getLong(idColumn),
                            cur.getString(artistColumn),
                            cur.getString(titleColumn),
                            cur.getString(albumColumn),
                            cur.getInt(idTruck),
                            cur.getLong(durationColumn)));

                } while (cur.moveToNext());

                Log.i(TAG, "Done querying media. MusicRetriever is ready.");
            }
            cur.close();
        }

        Collections.sort(items);
        return items;
    }

    @Override
    public int compareTo(Object another) {
        if (another == null) {
            return 1;
        }
        MusicItem item = (MusicItem) another;
        int result = album.compareTo(item.album);
        if (result != 0) {
            return result;
        }
        return truck - item.truck;
    }
}
