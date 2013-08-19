package net.metax.mediaplayersample;

import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class PlayerActivity extends Activity implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener{

    private static final String TAG = "PlayerActivity";
    private ImageButton mButtonPlayPause;
    private ImageButton mButtonRewind;
    private ImageButton mButtonSkip;
    private ImageButton mButtonStop;
    private TextView mTextViewAlbum;
    private TextView mTextViewArtist;
    private TextView mTextViewTitle;
    private Chronometer mChronometer;
    private List<MusicItem> mItems;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();
    private int mIndex;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mButtonPlayPause = (ImageButton) findViewById(R.id.buttonPlayPause);
        mButtonRewind = (ImageButton) findViewById(R.id.buttonRewind);
        mButtonSkip = (ImageButton) findViewById(R.id.buttonSkip);
        mButtonStop = (ImageButton) findViewById(R.id.buttonStop);
        mTextViewAlbum = (TextView) findViewById(R.id.textAlbum);
        mTextViewArtist = (TextView) findViewById(R.id.textArtist);
        mTextViewTitle = (TextView) findViewById(R.id.textTitle);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        mButtonPlayPause.setOnClickListener(this);
        mButtonRewind.setOnClickListener(this);
        mButtonSkip.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setEnabledButton(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mItems = MusicItem.getMusicItems(getApplicationContext());
        Log.d(TAG, "onResume: MusicItems.size() :" + mItems.size());
        if (mItems.size() != 0) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            prepare();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mChronometer.stop();
        }
    }

    private void prepare() {
        setEnabledButton(false);

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        MusicItem playingItem = mItems.get(mIndex);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), playingItem.getURI());
            mMediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        mTextViewArtist.setText(playingItem.artist);
        mTextViewAlbum.setText(playingItem.album);
        mTextViewTitle.setText(playingItem.title);
        mButtonPlayPause.setImageResource(R.drawable.media_play);
        mButtonPlayPause.setContentDescription("Play");
        mChronometer.setBase(SystemClock.elapsedRealtime());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.player, menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        super.onMenuItemSelected(featureId, item);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
    }

    @Override
    public void onClick(View v) {
        boolean isPlaying = mMediaPlayer.isPlaying();
        if (v == mButtonPlayPause) {
            if (isPlaying) {
                mMediaPlayer.pause();
                mChronometer.stop();
                mButtonPlayPause.setImageResource(R.drawable.media_play);
                mButtonPlayPause.setContentDescription("Play");
            } else {
                mMediaPlayer.start();
                mChronometer.setBase(SystemClock.elapsedRealtime() - mMediaPlayer.getCurrentPosition());
                mChronometer.start();
                mButtonPlayPause.setImageResource(R.drawable.media_pause);
                mButtonPlayPause.setContentDescription("Pause");
            }
        } else if (v == mButtonRewind) {
            if (isPlaying && mMediaPlayer.getCurrentPosition() > 2000 ) {
                mMediaPlayer.seekTo(0);
                mChronometer.setBase(SystemClock.elapsedRealtime());
            } else {
                mIndex = (mIndex + mItems.size() - 1) % mItems.size();
                onClick(mButtonStop);
                if (isPlaying) {
                    onClick(mButtonPlayPause);
                }
            }
        } else if (v == mButtonSkip) {
            mIndex = (mIndex + 1) % mItems.size();
            onClick(mButtonStop);
            if (isPlaying) {
                onClick(mButtonPlayPause);
            }
        } else if (v == mButtonStop) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            prepare();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onClick(mButtonSkip);
                while (!mButtonPlayPause.isEnabled()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
                onClick(mButtonPlayPause);
            }
        });
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        Log.d(TAG, "onInfo:" + (what == MediaPlayer.MEDIA_INFO_UNKNOWN ? "MEDIA_INFO_UNKNOWN" :
                what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING ? "MEDIA_INFO_VIDEO_TRACK_LAGGING" :
                what == MediaPlayer.MEDIA_INFO_BUFFERING_START ? "MEDIA_INFO_BUFFERING_START" :
                what == MediaPlayer.MEDIA_INFO_BUFFERING_END ? "MEDIA_INFO_BUFFERING_END" :
                what == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING ? "MEDIA_INFO_BAD_INTERLEAVING" :
                what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE ? "MEDIA_INFO_NOT_SEEKABLE" :
                what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE ? "MEDIA_INFO_METADATA_UPDATE" :
                "Unknown"));
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared");
        setEnabledButton(true);
    }

    private void setEnabledButton(final boolean enabled) {
        Log.d(TAG, "setEnabledButton:" + enabled);
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                mButtonPlayPause.setEnabled(enabled);
                mButtonSkip.setEnabled(enabled);
                mButtonRewind.setEnabled(enabled);
                mButtonStop.setEnabled(enabled);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown:" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                onClick(mButtonPlayPause);
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                onClick(mButtonSkip);
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                onClick(mButtonRewind);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}