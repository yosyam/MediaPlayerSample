package net.metax.mediaplayersample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by yoshi on 13/08/24.
 */
public class PlayerRemoteControlActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "PlayerRemoteControlActivity";
    private ImageButton mButtonPlayPause;
    private ImageButton mButtonSkip;
    private ImageButton mButtonRewind;
    private ImageButton mButtonStop;
    private TextView mTextViewArtist;
    private TextView mTextViewAlbum;
    private TextView mTextViewTitle;
    private Chronometer mChronometer;

    private Handler mHandler = new Handler();
    private long mCurrentPosition;
    private IntentFilter mFilter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            mHandler.post(new Runnable() {
                public void run() {
                    mTextViewArtist.setText(intent.getStringExtra("artist"));
                    mTextViewAlbum.setText(intent.getStringExtra("album"));
                    mTextViewTitle.setText(intent.getStringExtra("title"));
                    String state = intent.getStringExtra("state");
                    mCurrentPosition = intent.getIntExtra("currentPosition", 0);
                    if (state.equals(PlayerService.State.Playing.toString())) {
                        playing();
                    } else if (state.equals(PlayerService.State.Paused.toString())) {
                        paused();
                    } else if (state.equals(PlayerService.State.Stopped.toString())) {
                        stopped();
                    }
                }
            });
        }
    };

    private void stopped() {
        mChronometer.stop();
        mButtonPlayPause.setImageResource(R.drawable.media_play);
        mButtonPlayPause.setContentDescription("Play");
        mChronometer.setBase(SystemClock.elapsedRealtime());
    }

    private void paused() {
        mCurrentPosition = SystemClock.elapsedRealtime() - mChronometer.getBase();
        mChronometer.stop();
        mButtonPlayPause.setImageResource(R.drawable.media_play);
        mButtonPlayPause.setContentDescription("Play");
    }

    private void playing() {
        mChronometer.setBase(SystemClock.elapsedRealtime() - mCurrentPosition);
        mChronometer.start();
        mButtonPlayPause.setImageResource(R.drawable.media_pause);
        mButtonPlayPause.setContentDescription("Pause");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mButtonPlayPause = (ImageButton) findViewById(R.id.buttonPlayPause);
        mButtonSkip = (ImageButton) findViewById(R.id.buttonSkip);
        mButtonRewind = (ImageButton) findViewById(R.id.buttonRewind);
        mButtonStop = (ImageButton) findViewById(R.id.buttonStop);
        mTextViewArtist = (TextView) findViewById(R.id.textArtist);
        mTextViewAlbum = (TextView) findViewById(R.id.textAlbum);
        mTextViewTitle = (TextView) findViewById(R.id.textTitle);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        mButtonPlayPause.setOnClickListener(this);
        mButtonSkip.setOnClickListener(this);
        mButtonRewind.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);

        mFilter = new IntentFilter();
        mFilter.addAction(PlayerService.ACTION_STATE_CHANGED);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        registerReceiver(mReceiver, mFilter);
        startService(new Intent(PlayerService.ACTION_REQUEST_STATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onClick(View view) {
        if (view == mButtonPlayPause) {
            if (mButtonPlayPause.getContentDescription().equals("Play")) {
                startService(new Intent(PlayerService.ACTION_PLAY));
                playing();
            } else {
                startService(new Intent(PlayerService.ACTION_PAUSE));
                paused();
            }
        } else if (view == mButtonSkip) {
            startService(new Intent(PlayerService.ACTION_SKIP));
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
        } else if (view == mButtonRewind) {
            startService(new Intent(PlayerService.ACTION_REWIND));
            mChronometer.setBase(SystemClock.elapsedRealtime());
        } else if (view == mButtonStop) {
            Intent intent = new Intent(PlayerService.ACTION_STOP);
            intent.putExtra("cancel", true);
            startService(intent);
            stopped();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            //TODO: キーイベントの追加
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                onClick(mButtonPlayPause);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
