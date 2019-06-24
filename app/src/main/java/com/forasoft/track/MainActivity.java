package com.forasoft.track;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.io.IOException;

public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    final String LOG_TAG = "myLogs";

    private int position_now = 2;
    private final int max_index_track = 3;
    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    AssetManager assets;
    AssetFileDescriptor assetFileDescriptor_previous = null;
    AssetFileDescriptor assetFileDescriptor_now = null;
    AssetFileDescriptor assetFileDescriptor_next = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        assets = getAssets();
        // востанавливаем прошлое состояние до выхода
        try {
            assetFileDescriptor_previous = assets.openFd("1.mp3");
            assetFileDescriptor_now = assets.openFd("2.mp3");
            assetFileDescriptor_next = assets.openFd("3.mp3");
        }catch (IOException e){
            Log.e("error_io", "нет файлов");
        }

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStart(assetFileDescriptor_now);
            }
        });
    }

    public void onClickStart(AssetFileDescriptor afd_now) {
        releaseMP();

        try {
            mediaPlayer = new MediaPlayer();
            // задает источник данных для проигрывания
            if (afd_now.getDeclaredLength() < 0) {
                mediaPlayer.setDataSource(afd_now.getFileDescriptor());
            } else {
                mediaPlayer.setDataSource(afd_now.getFileDescriptor(), afd_now.getStartOffset(), afd_now.getDeclaredLength());
            }
            // задает аудио-поток, который будет использован для проигрывания
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("error", "Ошибка при подключении к mp3 файлам, afd - null");
        }
        mediaPlayer.start();


        if (mediaPlayer == null)
            return;

        mediaPlayer.setOnCompletionListener(this);
    }


    public void onClick(View view) {
        if (mediaPlayer == null)
            return;
        switch (view.getId()) {
            case R.id.pause:
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                break;
            case R.id.resume:
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                break;
            case R.id.stop:
                mediaPlayer.stop();
                break;
            case R.id.previouse:
                balancer(false);
                onClickStart(assetFileDescriptor_now);
                break;
            case R.id.next:
                assetFileDescriptor_previous = assetFileDescriptor_now;
                assetFileDescriptor_now = assetFileDescriptor_next;
                assetFileDescriptor_next = null;
                position_now++;
                balancer(true);
                onClickStart(assetFileDescriptor_now);
                break;
        }
    }


    // vector = true - next
    // vector = false - previouse
    private void balancer(boolean vector){
        if(vector){

        }
        else{
            position_now--;
            assetFileDescriptor_next = assetFileDescriptor_now;
            assetFileDescriptor_now = assetFileDescriptor_previous;
            if(position_now != 1){
                assetFileDescriptor_previous = assetFileDescriptor_now = assets.openFd((position_now - 1) + ".mp3");
                return;
            }else{
                assetFileDescriptor_previous = assetFileDescriptor_now = assets.openFd(max_index_track + ".mp3");;
                return;
            }
        }
    }

    // метод слушателя OnPreparedListener. Вызывается, когда плеер готов к проигрыванию.
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared");
        mp.start();
    }

    // метод слушателя OnCompletionListener. Вызывается, когда достигнут конец проигрываемого содержимого
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "onCompletion");
    }


    // освобождаем ресурсы текущего проигрывателя
    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMP();
    }
}