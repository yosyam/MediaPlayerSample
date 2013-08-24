package net.metax.mediaplayersample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RemoteControlClient;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 *
 * Created by yoshi on 13/08/20.
 */
public class PlayerService extends Service implements OnCompletionListener,
        OnPreparedListener, OnErrorListener, MusicFocusable, PrepareMusicRetrieverTask.MusicRetrieverPreparedLister {

    final static String TAG = "PlayerService";

    // ACTIONの定義 startServiceで使う
    public static final String ACTION_STATE_CHANGED = "net.metax.mediaplayer.ACTION_STATE_CHANGED";
    public static final String ACTION_PLAYPAUSE = "net.metax.mediaplayer.ACTION_PLAYPAUSE";
    public static final String ACTION_PLAY = "net.metax.mediaplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "net.metax.mediaplayer.ACTION_PAUSE";
    public static final String ACTION_SKIP = "net.metax.mediaplayer.ACTION_SKIP";
    public static final String ACTION_REWIND = "net.metax.mediaplayer.ACTION_REWIND";
    public static final String ACTION_STOP = "net.metax.mediaplayer.ACTION_STOP";
    public static final String ACTION_REQUEST_STATE = "net.metax.mediaplayer.ACTION_REQUEST_STATE";

    public static final float DUCK_VOLUME = 0.1f;

    final int NOTIFICATION_ID = 1;

    private MediaPlayer mPlayer = null;

    private NotificationManager mNotificationManager;
    private AudioManager mAudioManager;
    private AudioFocusHelper mAudioFocusHelper;

    private Bitmap mDummyAlbumArt;

    private ComponentName mMediaButtonReceiverComponent;

    private boolean mStartPlayingAfterRetrieve = false;
    private RemoteControlClient mRemoteControlClient;
    private boolean mIsOnlyPrepare;

    private List<MusicItem> mItems;
    private int mIndex;

    @Override
    public void onMusicRetrieverPrepared(List<MusicItem> items) {
        mState = State.Stopped;
        mItems = items;
        sendPlayerState();
        if (mStartPlayingAfterRetrieve) {
            tryToGetAudioFocus();
            playNextSong(false);
        }
    }

    enum AudioFocus {
        NoFocusNoDuck,
        NoFocusCanDuck,
        Focused
    }

    private AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    private Notification mNotification = null;

    enum State {
        Retrieving,
        Stopped,
        Preparing,
        Playing,
        Paused
    }

    private State mState = State.Retrieving;

    private long mRelaxTime = System.currentTimeMillis();

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mIndex = (mIndex + 1) % mItems.size();
        playNextSong(false);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Media player error!", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

        mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (mIsOnlyPrepare) {
            mState = State.Stopped;
        } else {
            mState = State.Playing;
        }

        updateNotification();
        sendPlayerState();

        if (!mIsOnlyPrepare) {
            configAndStartMediaPlayer();
        }

    }

    private void sendPlayerState() {
        if (mItems != null) {
            MusicItem playingItem = mItems.get(mIndex);
            Intent intent = new Intent(ACTION_STATE_CHANGED);
            intent.putExtra("artist", playingItem.artist);
            intent.putExtra("album", playingItem.album);
            intent.putExtra("title", playingItem.title);
            intent.putExtra("state", mState.toString());
            if (mPlayer != null) {
                intent.putExtra("currentPosition", mPlayer.getCurrentPosition());
            }
            sendBroadcast(intent);
        }
    }

    private void updateNotification() {
        boolean playing = mState == State.Playing;

        mNotification.icon = playing ? R.drawable.playing : R.drawable.pausing;
        int playPauseRes = playing ? R.drawable.media_pause_s : R.drawable.media_play_s;
        mNotification.contentView.setImageViewResource(R.id.buttonPlayPause, playPauseRes);

        MusicItem playingItem = mItems.get(mIndex);
        mNotification.contentView.setTextViewText(R.id.textArtist, playingItem.artist);
        mNotification.contentView.setTextViewText(R.id.textAlbum, playingItem.album);
        mNotification.contentView.setTextViewText(R.id.textTitle, playingItem.title);
        long current;
        if (mState == State.Stopped) {
            current = 0;
        } else {
            current = mPlayer.getCurrentPosition();
        }
        mNotification.contentView.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() - current, null, playing);
//        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        if (mState != State.Stopped) {
            startForeground(NOTIFICATION_ID, mNotification);
        }

    }



    @Override
    public void onGainedAudioFocus() {
        mAudioFocus = AudioFocus.Focused;
        if (mState == State.Playing) {
            configAndStartMediaPlayer();
        }
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
        if (mPlayer != null && mPlayer.isPlaying()) {
            configAndStartMediaPlayer();
        }
    }

    private Thread mSelfStopThread = new Thread() {
        @Override
        public void run() {
            while (true) {
                boolean needSleep = false;
                if (mState == PlayerService.State.Preparing ||
                        mState == PlayerService.State.Playing ||
                        mState == PlayerService.State.Paused) {
                    needSleep = true;
                } else if (mRelaxTime + 10 * 1000 * 60 > System.currentTimeMillis()) {
                    needSleep = true;
                }
                if (!needSleep) {
                    break;
                }
                try {
                    Thread.sleep(1 * 1000 * 60);
                } catch (InterruptedException e) {
                }
            }
            PlayerService.this.stopSelf();
        }
    };

    /**
     * MediaPlayerの作成、起動済みであればリセット
     */
    private void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        } else {
            mPlayer.reset();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        new PrepareMusicRetrieverTask(this).execute(getApplicationContext());

        mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);

        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.dummy_album_art);

        mMediaButtonReceiverComponent = new ComponentName(this, PlayerReceiver.class);

        Intent intent;

        // notificationの定義

        intent = new Intent(this, PlayerRemoteControlActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.remote_control);
        mNotification = new Notification();
        mNotification.icon = R.drawable.playing;
        mNotification.contentView = views;
        mNotification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        mNotification.contentIntent = pi;

        intent = new Intent(ACTION_REWIND);
        PendingIntent piRewind = PendingIntent.getService(this, R.id.buttonRewind, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.buttonRewind, piRewind);

        intent = new Intent(ACTION_PLAYPAUSE);
        PendingIntent piPlayPause = PendingIntent.getService(this, R.id.buttonPlayPause, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.buttonPlayPause, piPlayPause);

        intent = new Intent(ACTION_SKIP);
        PendingIntent piSkip = PendingIntent.getService(this, R.id.buttonSkip, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.buttonSkip, piSkip);

        intent = new Intent(ACTION_STOP);
        PendingIntent piStop = PendingIntent.getService(this, R.id.buttonStop, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.buttonStop, piStop);

        mSelfStopThread.start();
    }

    private void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                mState = State.Paused;
                updateNotification();
                sendPlayerState();
            }
            return;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck) {
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);
        } else {
            mPlayer.setVolume(1.0f, 1.0f);
        }

        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            updateNotification();
            sendPlayerState();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        String action = intent.getAction();
        if (action.equals(ACTION_PLAYPAUSE)) {
            processTogglePlaybackRequest();
        } else if (action.equals(ACTION_PLAY)) {
            processPlayRequest();
        } else if (action.equals(ACTION_PAUSE)) {
            processPauseRequest();
        } else if (action.equals(ACTION_SKIP)) {
            processSkipRequest();
        } else if (action.equals(ACTION_STOP)) {
            processStopRequest();
            if (intent.getBooleanExtra("cancel", false)) {
                mNotificationManager.cancel(NOTIFICATION_ID);
            }
        } else if (action.equals(ACTION_REWIND)) {
            processRewindRequest();
        } else if (action.equals(ACTION_REQUEST_STATE)) {
            sendPlayerState();
        }

        return START_NOT_STICKY;
    }

    private void processRewindRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            if (mPlayer.getCurrentPosition()  > 2000) {
                mPlayer.seekTo(0);
                updateNotification();
                sendPlayerState();
            } else {
                tryToGetAudioFocus();
                mIndex = (mIndex + mItems.size() - 1) % mItems.size();
                playNextSong(false);
            }
        } else {
            mIndex = (mIndex + mItems.size() - 1) % mItems.size();
            playNextSong(true);
        }



    }

    private void processSkipRequest() {
        if (mState == State.Playing || mState == State.Paused) {
            tryToGetAudioFocus();
            mIndex = (mIndex + 1) % mItems.size();
            playNextSong(false);
        } else if (mState == State.Stopped) {
            mIndex = (mIndex + 1) % mItems.size();
            playNextSong(true);
        }
    }

    private void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
    }

    private void processPlayRequest() {
        Log.d(TAG, "processPlayRequest:" + mState);
        if (mState == State.Retrieving) {
            mStartPlayingAfterRetrieve = true;
            return;
        }

        tryToGetAudioFocus();

        if (mState == State.Stopped) {
            playNextSong(false);
        } else if (mState == State.Paused) {
            mState = State.Playing;
            configAndStartMediaPlayer();
        }
        if (mRemoteControlClient != null) {
            mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
    }

    private void processPauseRequest() {
        Log.d(TAG, "processPauseRequest:" + mState);
        if (mState == State.Retrieving) {
            mStartPlayingAfterRetrieve = false;
            return;
        }

        if (mState == State.Playing) {
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false);
            updateNotification();
            sendPlayerState();
        }

        if (mRemoteControlClient != null) {
            mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
    }

    private void processStopRequest() {
        processStopRequest(false);
    }

    private void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;
            relaxResources(true);
            giveUpAudioFocus();
            if (mRemoteControlClient != null) {
                mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            }

            updateNotification();
            sendPlayerState();
        }
    }

    private void relaxResources(boolean releaseMediaPlayer) {

        if (releaseMediaPlayer && mPlayer != null) {
            stopForeground(true);
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        mRelaxTime = System.currentTimeMillis();
    }

    private void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null && mAudioFocusHelper.abandonFocus()) {
            mAudioFocus = AudioFocus.NoFocusNoDuck;
        }
    }


    private void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null && mAudioFocusHelper.requestFocus()) {
            mAudioFocus = AudioFocus.Focused;
        }
    }

    private void playNextSong(boolean isOnlyPrepare) {
        mIsOnlyPrepare = isOnlyPrepare;
        mState = State.Stopped;
        relaxResources(false);

        try {
            MusicItem playingItem = mItems.get(mIndex);
            if (playingItem == null) {
                Toast.makeText(this, "No available music to play.", Toast.LENGTH_LONG).show();
                processStopRequest(true);
                return;
            }
            createMediaPlayerIfNeeded();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(getApplicationContext(), playingItem.getURI());

            mState = State.Preparing;

            mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

            if (mRemoteControlClient == null) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.setComponent(mMediaButtonReceiverComponent);
                mRemoteControlClient = new RemoteControlClient(PendingIntent.getBroadcast(this, 0, intent,0));
                mAudioManager.registerRemoteControlClient(mRemoteControlClient);
            }

            mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);

            mRemoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                    | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                    | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                    | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                    | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS );

            mRemoteControlClient.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, playingItem.artist)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, playingItem.album)
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, playingItem.title)
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, playingItem.duration)
                    .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt).apply();
            mPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e("MusicService", "IOException playing next song: " + e.getMessage());
            e.printStackTrace();
        }
    }




}
