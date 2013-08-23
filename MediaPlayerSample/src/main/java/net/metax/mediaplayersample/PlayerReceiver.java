package net.metax.mediaplayersample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by yoshi on 13/08/23.
 */
public class PlayerReceiver extends BroadcastReceiver {
    private static final String TAG = "PlayerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            context.startService(new Intent(PlayerService.ACTION_PAUSE));
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
                return;
            }

            Log.d(TAG, "onReceive: action:" + (
                    keyEvent.getAction() == KeyEvent.ACTION_DOWN ? "DOWN" :
                    keyEvent.getAction() == KeyEvent.ACTION_UP ? "UP" :
                    keyEvent.getAction() == KeyEvent.ACTION_MULTIPLE ? "MULTIPLE" : "Unknown")
                    + " keyCode:" + (
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ? "MEDIA_PLAY_PAUSE" :
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_STOP ? "MEDIA_STOP" :
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT ? "MEDIA_NEXT" :
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS ? "KEYCODE_MEDIA_PREVIOUS" : "Unknown")
                    + " intent:" + intent);

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    context.startService(new Intent(PlayerService.ACTION_PLAYPAUSE));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    context.startService(new Intent(PlayerService.ACTION_PLAY));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    context.startService(new Intent(PlayerService.ACTION_PAUSE));
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    context.startService(new Intent(PlayerService.ACTION_STOP));
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    context.startService(new Intent(PlayerService.ACTION_SKIP));
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    context.startService(new Intent(PlayerService.ACTION_REWIND));
                    break;
            }
        }
    }
}
