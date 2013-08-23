package net.metax.mediaplayersample;

/**
 * Created by yoshi on 13/08/23.
 */
public interface MusicFocusable {
    /**
     * オーディオフォーカスを取得
     */
    public void onGainedAudioFocus();

    /**
     * オーディオフォーカスを失う
     * @param canDuck docked モード（低ボリューム）であるならtrue、そうでないならfalse
     */
    public void onLostAudioFocus(boolean canDuck);

}
