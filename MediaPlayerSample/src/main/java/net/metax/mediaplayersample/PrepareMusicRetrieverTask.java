package net.metax.mediaplayersample;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by yoshi on 13/08/24.
 */
public class PrepareMusicRetrieverTask extends AsyncTask<Context, Void, List<MusicItem>> {
    private MusicRetrieverPreparedLister mListener;

    public PrepareMusicRetrieverTask(MusicRetrieverPreparedLister listener) {
        mListener = listener;
    }

    @Override
    protected List<MusicItem> doInBackground(Context... contexts) {
        return MusicItem.getMusicItems(contexts[0]);
    }

    @Override
    protected void onPostExecute(List<MusicItem> musicItems) {
        mListener.onMusicRetrieverPrepared(musicItems);
    }

    public interface MusicRetrieverPreparedLister {
        public void onMusicRetrieverPrepared(List<MusicItem> items);
    }
}
