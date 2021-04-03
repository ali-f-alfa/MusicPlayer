package com.example.musicplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button playBtn;
    public static MainActivity INSTANCE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        INSTANCE = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playBtn = (Button) findViewById(R.id.playBtn);
    }

    public static MainActivity GetMainActivity() {
        return INSTANCE;
    }

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final int REQUEST_PERMISSIONS = 12345;

    private static final int PERMISSIONS_COUNT = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsDenied() {
        for (int i = 0; i < PERMISSIONS_COUNT; i++) {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grandResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grandResults);
        if (arePermissionsDenied()) {
            ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate();
        } else {
            onResume();

        }
    }

    private boolean isMusicPlayerInit;

    private List<String> musicFileList;
    public ArrayList<Song> SongList = new ArrayList<>();

    private void fillMusicList() {
        getSongList();
        musicFileList.clear();
        for (int i = 0; i < SongList.size(); i++) {
            musicFileList.add(SongList.get(i).Data);
        }
//        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_MUSIC)));
//        addMusicFilesFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS)));
    }

    Song CurrentSong;

    public void getSongList() {
        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        byte[] art;
        //retrieve song info
        ContentResolver musicResolver = getApplicationContext().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        //iterate over results if valid
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int nameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumId = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int data = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumKey = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisName = musicCursor.getString(nameColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                int thisAlbumId = musicCursor.getInt(albumId);
                String thisData = musicCursor.getString(data);
                String AlbumKey = musicCursor.getString(albumKey);
                Uri imageUri = ContentUris.withAppendedId(sArtworkUri, thisAlbumId);

                SongList.add(new Song(thisId, thisName, thisTitle, thisArtist, thisAlbumId, thisData, AlbumKey, imageUri));

            }
            while (musicCursor.moveToNext());

        }
    }

    public void GoToSong(View view) {
        Intent i = new Intent(this, songActivity.class);
        startActivity(i);
    }

    public void LoadImage(Song song, int size, ImageView imageView) {

        Glide.with(imageView.getContext())
                .load(song.ImageUri)
                .override(size, size)
                .fitCenter()
                .into(imageView);
    }

    public void PlayButtonClick(View view) {

        AddController();
        if (mp.isPlaying()) {
            mp.pause();
            playBtn.setBackgroundResource(R.drawable.play);

        } else {
            mp.start();
            playBtn.setBackgroundResource(R.drawable.stop);
        }
    }

    public Button inputbox;

    public void AddController() {
       // LinearLayout container = (LinearLayout) findViewById(R.id.ControllerBox);
       // inputbox = new Button(this.getApplicationContext());
       // inputbox.setText("hello");
       // inputbox.setId(1);
      //  container.addView(inputbox);
      //  inputbox = new Button(this.getApplicationContext());
      //  inputbox.setId(2);
      //  inputbox.setText("hello");
       // container.addView(inputbox);
    }

    public void NextButtonClick(View view) { // TODO: 9/1/2020 repeat and shuffle must be implemented
        Button testBtn = (Button) findViewById(1);
        testBtn.setText("fdsfaf");
        Log.wtf("View", "next button clicked");
        NextButtonClickFunc();

    }

    private void NextButtonClickFunc() {
        songIdx++;
        if (songIdx == musicFileList.size()) {
            songIdx = 0;
        }
        final String musicFilePath = musicFileList.get(songIdx);
        final int songDuration = playMusicFile(musicFilePath) / 1000;

        doSeekBarStuffs(seekBar, songDuration, songDurationTextView);
    }

    public void PrevButtonClick(View view) { // TODO: 9/1/2020 restart song and repeat and shuffle must be implemented
        if (songPosition > 4) {
            songPosition = 0;
            mp.seekTo(0);
            return;
        }
        songIdx--;
        if (songIdx == -1) {
            songIdx = musicFileList.size() - 1;
        }
        final String musicFilePath = musicFileList.get(songIdx);
        final int songDuration = playMusicFile(musicFilePath) / 1000;

        doSeekBarStuffs(seekBar, songDuration, songDurationTextView);
    }


    public MediaPlayer mp = new MediaPlayer();

    private int playMusicFile(String path) {
        CurrentSong = SongList.get(songIdx);
        if (mp.isPlaying()) {
            mp.stop();
            t.cancel();
            Log.wtf("log", "t.cancel()");
        }
        //play button image change
        playBtn.setBackgroundResource(R.drawable.stop);

        //Song name label
        TextView playingSongLabel = (TextView) findViewById(R.id.playingSongLabel);
        playingSongLabel.setText(CurrentSong.Title);

        //song image loading
        songImage = (ImageView) findViewById(R.id.songImage);
        LoadImage(CurrentSong, 500, songImage);


        mp = new MediaPlayer();
        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mp.getDuration();
    }

    private void updateSongProgress() {

    }

    private int songPosition;
    public Timer t = new Timer();
    public boolean resetThread;
    public TextView songPositionTextView;
    public TextAdapter textAdapter;
    public int songIdx;
    public SeekBar seekBar;
    public TextView songDurationTextView;
    public Button nextBtn;
    public ImageView songImage;

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }
        if (!isMusicPlayerInit) {
            //List view stuffs :
            final ListView listView = findViewById(R.id.listView);
            textAdapter = new TextAdapter();
            Log.wtf("listview", "customAdapter sat");
            musicFileList = new ArrayList<>();
            fillMusicList();
            Log.wtf("listview", "music list filled");

            textAdapter.setData(SongList);
            listView.setAdapter(textAdapter);
            Log.wtf("listview", "listview adapter sat");

            seekBar = findViewById(R.id.seekBar);

            // when seekBar changes :
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    songPosition = progress;
                    songPositionTextView.setText(convertTime(songPosition));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mp.seekTo(songPosition * 1000);
                }
            });

            songPositionTextView = findViewById(R.id.currentPosition);
            songDurationTextView = findViewById(R.id.songDuration);

            //listener to click on a song :
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    songIdx = position;
                    final String musicFilePath = musicFileList.get(position);
                    final int songDuration = playMusicFile(musicFilePath) / 1000;
                    nextBtn = findViewById(R.id.NextBtn);
                    doSeekBarStuffs(seekBar, songDuration, songDurationTextView);

                }
            });
            isMusicPlayerInit = true;
        }
    }


    private void doSeekBarStuffs(final SeekBar seekBar, final int songDuration, TextView songDurationTextView) {

        seekBar.setMax(songDuration);
        songDurationTextView.setText(convertTime(songDuration));

        songPosition = 0;
        t = new Timer();
        Log.wtf("log", "new timer set");

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (mp.isPlaying()) {
                    Log.wtf("log", String.valueOf(songPosition));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seekBar.setProgress(songPosition);
                            songPositionTextView.setText(convertTime(songPosition++));
                        }
                    });
                } else if (songDuration <= songPosition) {
                    Log.wtf("fsdf", "ended now");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            NextButtonClickFunc();
                        }
                    });
                    this.cancel();

                }
            }
        }, 0, 1000);

    }


    private String convertTime(int songDuration) {
        int sec = (songDuration % 60);
        int min = (songDuration / 60);
        if (min < 10 && sec < 10) {
            return "0" + min + ":" + "0" + sec;
        } else if (min < 10 && sec > 9) {
            return "0" + min + ":" + sec;
        } else if (min > 9 && sec < 10) {
            return min + ":" + "0" + sec;
        } else {
            return min + ":" + sec;
        }
    }

    class TextAdapter extends BaseAdapter {

        private List<Song> Songs = new ArrayList<>();
        private List<String> data = new ArrayList<>();

        void setData(List<Song> songs) {
            Songs.clear();
            Songs.addAll(songs);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return Songs.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Song song = Songs.get(position);
            if (convertView == null) {

                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item, parent, false);
                convertView.setTag(new ViewHOlder((TextView) convertView.findViewById(R.id.itemTitle), (ImageView) convertView.findViewById(R.id.itemImage)));
            }
            ViewHOlder hOlder = (ViewHOlder) convertView.getTag();
            hOlder.SongName.setText(song.Title);
            LoadImage(song, 300, hOlder.SongImage);

            return convertView;
        }

        class ViewHOlder {
            TextView SongName;
            ImageView SongImage;

            ViewHOlder(TextView songName, ImageView imageView) {
                this.SongName = songName;
                this.SongImage = imageView;
            }
        }
    }

    class Song {
        public long Id;
        public String Name;
        public String Title;
        public String Artist;
        public int AlbumId;
        public String Data;
        public String AlbumKey;
        public Uri ImageUri;

        public Song(long thisId, String thisName, String thisTitle, String thisArtist, int thisAlbumId, String thisData, String albumKey, Uri imageUri) {
            this.Id = thisId;
            this.Name = thisName;
            this.Title = thisTitle;
            this.Artist = thisArtist;
            this.AlbumId = thisAlbumId;
            this.Data = thisData;
            this.AlbumKey = albumKey;
            this.ImageUri = imageUri;
        }
    }
}