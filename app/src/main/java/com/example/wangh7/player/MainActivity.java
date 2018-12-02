package com.example.wangh7.player;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private ArrayList<Map<String, Object>> listems = null;//需要显示在listview里的信息
    public static ArrayList<MusicInfo> musicList = null; //音乐信息列表
    private static int currentposition = -1;//当前播放列表里哪首音乐
    private static int playstate = 0;//播放状态：0顺序；1循环；2随机；3单曲
    private Button state;
    private Button playOrPause;
    private Button stop;
    private Button next;
    private Button prev;
    private SeekBar seekBar;
    private TextView title;
    private TextView artist;
    private TextView album;
    private TextView ontime;
    private TextView alltime;
    private ImageView imageView;
    private ListView musicListView = null;
    int time;
    Handler handler = new Handler();//界面更新控制

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        state = (Button) findViewById(R.id.state);
        playOrPause = (Button) findViewById(R.id.play_or_pause);
        stop = (Button) findViewById(R.id.stop);
        next = (Button) findViewById(R.id.next);
        prev = (Button) findViewById(R.id.prev);

        TextView title = (TextView) findViewById(R.id.title_tx);
        TextView artist = (TextView) findViewById(R.id.artist_tx);
        TextView album = (TextView) findViewById(R.id.album_tx);

        musicListView = (ListView) findViewById(R.id.list_lv);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        imageView = (ImageView) findViewById(R.id.image_1);
        state.setOnClickListener(this);
        playOrPause.setOnClickListener(this);
        stop.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);


        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            initMediaPlayer();
        }


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
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        }
    };

    Runnable updatesb = new Runnable() {
        //TextView ontime = (TextView) findViewById(R.id.ontime_tx);
        @Override
        public void run() {
            TextView ontime = (TextView) findViewById(R.id.ontime_tx);

            switch (playstate) {
                case 0:
                    state.setText("ORDER");
                    break;
                case 1:
                    state.setText("LOOP");
                    break;
                case 2:
                    state.setText("RAND");
                    break;
                case 3:
                    state.setText("SINGLE");
                default:
                    break;
            }
            if(mediaPlayer.isPlaying())
                playOrPause.setText("pause");
            else
                playOrPause.setText("play");
            ontime.setText(getOnTime(mediaPlayer.getCurrentPosition()));
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(updatesb, 100);
        }
    };

    private void initMediaPlayer() {     //初始化播放器
        musicListView = (ListView) findViewById(R.id.list_lv);

        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击播放音乐，不过需要判断一下当前是否有音乐在播放，需要关闭正在播放的
                //position 可以获取到点击的是哪一个，去 musicList 里寻找播放
                currentposition = position;
                handler.post(updatesb);
                player(currentposition);
            }
        });

        musicList = scanAllAudioFiles();
        //这里其实可以直接在扫描时返回 ArrayList<Map<String, Object>>()
        listems = new ArrayList<Map<String, Object>>();
        for (Iterator iterator = musicList.iterator(); iterator.hasNext(); ) {
            Map<String, Object> map = new HashMap<String, Object>();
            MusicInfo mp3Info = (MusicInfo) iterator.next();
//            map.put("id",mp3Info.getId());
            map.put("title", mp3Info.getTitle());
            map.put("artist", mp3Info.getArtist());
            map.put("album", mp3Info.getAlbum());
//            map.put("albumid", mp3Info.getAlbumId());
            map.put("duration", mp3Info.getDur());
            map.put("time", mp3Info.getTime());
            map.put("size", mp3Info.getSize());
            map.put("url", mp3Info.getUrl());

            map.put("bitmap", mp3Info.getBm());
            //map.put("bitmap", R.drawable.ic_launcher_background);
            listems.add(map);

        }

        SimpleAdapter mSimpleAdapter = new SimpleAdapter(
                this,
                listems,
                R.layout.single_music,
                new String[]{"bitmap", "title", "artist", "album"},
                new int[]{R.id.image_sg, R.id.title_sg, R.id.artist_sg, R.id.album_sg}
        );
        //listview里加载数据
        musicListView.setAdapter(mSimpleAdapter);


        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] searchlist = new String[]{MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA};

/*
        Cursor cursor = resolver.query(uri, searchlist, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        //while (cursor.moveToNext()) {
        cursor.moveToNext();
        cursor.moveToNext();
        //cursor.moveToNext();
        //cursor.moveToNext();
        //cursor.moveToNext();
        //cursor.moveToNext();


        //获取音乐的路径，这个参数我们实际上不会用到，不过在调试程序的时候可以方便我们看到音乐的真实路径，确定寻找的文件的确就在我们规定的目录当中
            //        String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            //获取音乐的ID
                   String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            //通过URI和ID，组合出改音乐特有的Uri地址
                  Uri musicUri = Uri.withAppendedPath(uri, id);
            //获取音乐的名称
            //    Log.e(TAG,"mingzi:");
            String title_t = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            // Log.e(TAG,tilte);
            //获取艺人
               String artist_t = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

               //获取专辑名字
            String album_t = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            //获取音乐的时长，单位是毫秒
                 long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            //获取该音乐所在专辑的id
                 int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID));
            //再通过AlbumId组合出专辑的Uri地址
                    Uri albumUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);


            //title.setText(musicInfo.getTitle());
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(MainActivity.this, musicUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MusicInfo musicInfo = new MusicInfo();
        musicInfo.setId(Integer.valueOf(id));
        musicInfo.setTitle(title_t);
        musicInfo.setArtist(artist_t);
        musicInfo.setAlbum(album_t);
        musicInfo.setAlbumId(albumId);
            Bitmap bm = getAlbumArt(albumId);
            musicInfo.setBm(bm);
            //
        /*
                title.setText("曲    名:"+musicInfo.getTitle());
                artist.setText("艺术家:"+musicInfo.getArtist());
                album.setText("专    辑:"+musicInfo.getAlbum());
                //albumid.setText("专辑id:"+albumId);
            //}
            imageView.setImageBitmap(musicInfo.getBm());

        //artist.setText("123123");
        //}
        cursor.close();
*/

    }

    public String getOnTime(int time) {
        String timew;
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        timew = String.format("%02d:%02d", minute, second);
        return timew;
    }

    public void player(int position) {
        TextView title = (TextView) findViewById(R.id.title_tx);
        TextView artist = (TextView) findViewById(R.id.artist_tx);
        TextView album = (TextView) findViewById(R.id.album_tx);
        TextView allTime = (TextView) findViewById(R.id.alltime_tx);
        ImageView imageView = (ImageView) findViewById(R.id.image_1);
        int id = musicList.get(position).getId();
        String titlep = musicList.get(position).getTitle();
        String artistp = musicList.get(position).getArtist();
        String albump = musicList.get(position).getAlbum();
        String timep = musicList.get(position).getTime();
        int dur = musicList.get(position).getDur();
        seekBar.setMax(dur);
        title.setText(titlep);
        artist.setText(artistp);
        album.setText(albump);
        allTime.setText(timep);
        imageView.setImageBitmap(musicList.get(position).getBm());
        Uri musicUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(MainActivity.this, musicUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getAlbumArt(int album_id) {
        //TextView albumid = (TextView) findViewById(R.id.albumid_tx);
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToFirst();
            album_art = cur.getString(0);
            //albumid.setText(""+cur.getCount());
        }
        cur.close();
        Bitmap bm = null;
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art);
        } else {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
        }
        return bm;
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
            case R.id.state:
                playstate = playstate + 1;
                if (playstate > 3)
                    playstate = 0;
                break;
            case R.id.play_or_pause:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playOrPause.setText("PLAY");
                } else {
                    mediaPlayer.start();
                    playOrPause.setText("PAUSE");
                }
                break;
            case R.id.stop:
                if (mediaPlayer.isPlaying()) {
                    TextView ontime = (TextView) findViewById(R.id.ontime_tx);
                    mediaPlayer.reset();
                    seekBar.setProgress(0);
                    ontime.setText("00:00");
                    initMediaPlayer();
                    handler.removeCallbacks(updatesb);
                }
                break;
            case R.id.next:
                nextSong();
                break;
            case R.id.prev:
                prevSong();
                break;
            default:
                break;
        }
    }

    public void nextSong() {//下一首歌
        switch (playstate) {
            case 0: {
                if (mediaPlayer.isPlaying()) {
                    if (currentposition < musicList.size() - 1) {
                        currentposition = currentposition + 1;
                        player(currentposition);
                    } else
                        Toast.makeText(MainActivity.this, "This is the last song", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case 1: {
                if (mediaPlayer.isPlaying()) {
                    if (currentposition < musicList.size() - 1) {
                        currentposition = currentposition + 1;
                        player(currentposition);
                    } else {
                        currentposition = 0;
                        player(currentposition);
                    }
                }
            }
            break;
            case 2: {
                if (mediaPlayer.isPlaying()) {
                    int i;
                    do{
                        i = (new Random()).nextInt(musicList.size());
                    }while(currentposition==i);
                    currentposition = i;
                    player(currentposition);
                }
            }
            break;
            case 3:{
                player(currentposition);
            }break;
            default:
                break;
        }
    }

    public void prevSong() {
        switch (playstate) {
            case 0: {
                if (mediaPlayer.isPlaying()) {
                    if (currentposition > 0) {
                        currentposition = currentposition - 1;
                        player(currentposition);
                    } else
                        Toast.makeText(MainActivity.this, "This is the first song", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case 1: {
                if (mediaPlayer.isPlaying()) {
                    if (currentposition > 0) {
                        currentposition = currentposition - 1;
                        player(currentposition);
                    } else {
                        currentposition = musicList.size() - 1;
                        player(currentposition);
                    }
                }
            }
            break;
            case 2: {
                if (mediaPlayer.isPlaying()) {
                    int i;
                    do{
                        i = (new Random()).nextInt(musicList.size());
                    }while(currentposition==i);
                    currentposition = i;
                    player(currentposition);
                }
            }
            break;
            case 3:{
                player(currentposition);
            }break;
            default:
                break;
        }
    }

    public ArrayList<MusicInfo> scanAllAudioFiles() {
        //生成动态数组，并且转载数据
        ArrayList<MusicInfo> mylist = new ArrayList<MusicInfo>();

        /*查询媒体数据库
        参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
        视频：MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        图片;MediaStore.Images.Media.EXTERNAL_CONTENT_URI

*/
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //遍历媒体数据库
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                //歌曲编号
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌曲标题
                String tilte = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌曲的专辑名：MediaStore.Audio.Media.ALBUM
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                //歌曲的歌手名： MediaStore.Audio.Media.ARTIST
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //歌曲文件的路径 ：MediaStore.Audio.Media.DATA
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                //歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
                Long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));

                Bitmap bm = getAlbumArt(albumId);

                if (size > 1024 * 800) {//大于800K
                    MusicInfo musicInfo = new MusicInfo();
                    musicInfo.setId(id);
                    musicInfo.setArtist(artist);
                    musicInfo.setSize(size);
                    musicInfo.setTitle(tilte);
                    musicInfo.setTime(duration);
                    musicInfo.setUrl(url);
                    musicInfo.setDur(duration);
                    musicInfo.setAlbum(album);
                    musicInfo.setAlbumId(albumId);
                    musicInfo.setBm(bm);
                    mylist.add(musicInfo);

                }
                cursor.moveToNext();
            }
        }
        return mylist;
    }

    protected void onDestory() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
