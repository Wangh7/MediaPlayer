package com.example.wangh7.player;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Button play;
    private Button pause;
    private Button stop;
    private SeekBar seekBar;
    int time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play = (Button) findViewById(R.id.play);
        pause = (Button) findViewById(R.id.pause);
        stop = (Button) findViewById(R.id.stop);

        TextView title = (TextView)findViewById(R.id.title_tx);
        TextView artist = (TextView)findViewById(R.id.artist_tx);
        TextView album = (TextView)findViewById(R.id.album_tx);

        seekBar = (SeekBar)findViewById(R.id.seekbar);
        ImageView imageView = (ImageView) findViewById(R.id.image_1);
        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        stop.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            initMediaPlayer();
        }
        time = mediaPlayer.getDuration();
        seekBar.setMax(time);

    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            if(mediaPlayer != null) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        }
    };

    private void initMediaPlayer() {     //初始化播放器
        try {
            File file = new File(getExternalStorageDirectory(),
                    "test.mp3");
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            //MediaStore.Audio.Media._ID;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initMediaPlayer();
                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
                break;
            case R.id.pause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;
            case R.id.stop:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.reset();
                    initMediaPlayer();
                }
                break;
            default:
                break;
        }
    }

    protected void onDestory() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
