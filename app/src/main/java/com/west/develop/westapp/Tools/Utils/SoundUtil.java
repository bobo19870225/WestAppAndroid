package com.west.develop.westapp.Tools.Utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import com.west.develop.westapp.R;

/**
 * Created by Develop0 on 2017/12/30.
 */

public class SoundUtil {


    private static SoundPool getSoundPool(Context context) {
        SoundPool soundPool;

        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //设置媒体音量为最大值

        if (volume < (((float)maxVolume * 2) / 3)) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND);
        }

        Log.e("Volume", volume + "-" + maxVolume);
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入音频的数量
            builder.setMaxStreams(1);
            //AudioAttributes是一个封装音频各种属性的类
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
        } else {
            //第一个参数是可以支持的声音数量，第二个是声音类型，第三个是声音品质
            soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        }
        return soundPool;
    }

    /**
     * 设备断开连接提示音
     *
     * @param context
     */
    public static void deviceLostSound(Context context) {
        SoundPool soundPool = getSoundPool(context);
        //第一个参数Context,第二个参数资源Id，第三个参数优先级
        soundPool.load(context, R.raw.device_lost_alert, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(1, 1, 1, 0, 0, 1);
            }
        });
    }

    /**
     * 程序退出提示音
     * 单次播放
     * @param context
     */
    public static void programExitSound(Context context) {
        SoundPool soundPool = getSoundPool(context);
        //第一个参数Context,第二个参数资源Id，第三个参数优先级
        soundPool.load(context, R.raw.program_exit, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(1, 1, 1, 0, 0, 1);
            }
        });
    }



    /**
     * 程序退出提示音
     * 重复播放
     */
    private static MediaPlayer mediaPlayer ;//= new MediaPlayer();
    public static void deviceExitTipSound(Context context) {
        try {
            getSoundPool(context);
            mediaPlayer = MediaPlayer.create(context,R.raw.program_exit);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    /**
                     * 重复播放
                     */
                    mp.start();
                    mp.setLooping(true);
                }
            });

            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            deviceExitTipSound(context);
        }

    }

    /**
     * 停止播放
     */
    public static void deviceExitTipSoundStop(){
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }
        mediaPlayer.release();

    }

}
