package com.ecosia.ecosia;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.widget.RemoteViews;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


/**
 * Created by ealkan on 14/08/16.
 */

public class EcosiaWidgetProvider extends AppWidgetProvider {

    private final String ACTION_WIDGET_START = "ActionReceiverStart"; //start button widget
    private final String ACTION_WIDGET_STOP = "ActionReceiverStop"; //stop button widget as de
    private final String MEDIA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();//media file path of mobile phone not sd card
    private final String mp3LowerCasePattern = ".mp3";//lower case mp3 file extension pattern
    private final String mp3UpperCasePattern = ".MP3";//upper case mp3 file extension pattern
    private static Random random = new Random();//random number class
    public static MediaPlayer mediaPlayer = new MediaPlayer();//media player
    public static ArrayList<HashMap<String, String>> songsList;//song list
    public static int currentRandomNumber = 0;//initilaze random number


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ecosia_widget);

        Intent active = new Intent(context, EcosiaWidgetProvider.class);
        active.setAction(ACTION_WIDGET_START);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
        remoteViews.setOnClickPendingIntent(R.id.startButton, actionPendingIntent);

        active = new Intent(context, EcosiaWidgetProvider.class);
        active.setAction(ACTION_WIDGET_STOP);
        actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
        remoteViews.setOnClickPendingIntent(R.id.stopButton, actionPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        //Get songs from device and fill list
        if(songsList == null) {
            songsList = getPlayList();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) { //check receiver type start or stop button
        if (intent.getAction().equals(ACTION_WIDGET_START)) {
            if(songsList != null){
                startAudioPlayer(context);
            }else{
                songsList = getPlayList();
            }
        } else if (intent.getAction().equals(ACTION_WIDGET_STOP)) {
            stopAudioPlayer();
        }  else {
            super.onReceive(context, intent);
        }
    }

    private void startAudioPlayer(Context context){//start function
        int order = getRandomMusicOrder();
        if(order != -1){
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                stopAudioPlayer();
            }
            mediaPlayer = MediaPlayer.create(context, Uri.parse(songsList.get(order).get("songPath")));
            String currentSongName = songsList.get(order).get("songTitle") +".mp3";
            updateSongName(context,currentSongName);
            mediaPlayer.start();
        }
    }

    private void updateSongName(Context context, String currentSongName){ //notify current playing song name label
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ecosia_widget);
        ComponentName thisWidget = new ComponentName(context, EcosiaWidgetProvider.class);
        remoteViews.setTextViewText(R.id.songName, currentSongName);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    private void stopAudioPlayer(){//stop function
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private int getRandomMusicOrder(){//check song list and generate random number
        if(songsList == null) //check songlist if empty return -1
            return -1;
        int randomNumber = random.nextInt(songsList.size());
        while (currentRandomNumber == randomNumber){//prevent concurence generate same random number
            randomNumber = random.nextInt(songsList.size());
        }
        currentRandomNumber = randomNumber;
        return currentRandomNumber; //return random number
    }

    private ArrayList<HashMap<String, String>> getPlayList(){ // return songs list array
        if (MEDIA_PATH != null) {
            File home = new File(MEDIA_PATH);
            File[] listFiles = home.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }
        }
        return songsList;
    }

    private void scanDirectory(File directory) {//scan operations recursively directiory or add songs
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }
        }
    }

    private void addSongToList(File song) {
        if (song.getName().endsWith(mp3LowerCasePattern) || song.getName().endsWith(mp3UpperCasePattern)) { //control .mp3 and .MP3 extension files
            HashMap<String, String> songMap = new HashMap<String, String>();
            songMap.put("songTitle", song.getName().substring(0, (song.getName().length() - 4)));
            songMap.put("songPath", song.getPath());
            if(songsList == null){//check null control of songlist if empty generate new list
                songsList = new ArrayList<HashMap<String, String>>();
            }
            songsList.add(songMap); // Adding each song to SongList
        }
    }
}

