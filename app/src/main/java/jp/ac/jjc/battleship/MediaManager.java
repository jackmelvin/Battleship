package jp.ac.jjc.battleship;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

class MediaManager {

    /**
     * SingleTon instance
     */
    private static MediaManager sInstance;

    private Context mContext;

    private MediaPlayer mediaPlayer;

    private int musicId;

    private MediaManager(Context context) {
        mContext = context.getApplicationContext();
//        mediaPlayer = MediaPlayer.create(mContext, R.raw.bg_music);
        musicId = R.raw.bg_music;
       loadMusic(musicId);
    }

    static MediaManager getInstance(Context context) {
        if (null == sInstance) {
            synchronized (MediaManager.class) {
                sInstance = new MediaManager(context);
            }
        }
        return sInstance;
    }

    void loadMusic(int musicId) {
        releaseMediaPlayer();
        mediaPlayer = new MediaPlayer();
        String mediaFileUriStr = "android.resource://" + mContext.getPackageName() + "/" + musicId;
        Uri mediaFileUri = Uri.parse(mediaFileUriStr);
        try {
            mediaPlayer.setDataSource(mContext, mediaFileUri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void play() {
        if(mediaPlayer == null) {
            loadMusic(musicId);
        }
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        }
    }

    void pause() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    void releaseMediaPlayer() {
        if(mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}