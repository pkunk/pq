package com.github.pkunk.progressquest.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import com.github.pkunk.progressquest.R;
import com.github.pkunk.progressquest.gameplay.Player;
import com.github.pkunk.progressquest.ui.util.UiUtils;
import com.github.pkunk.progressquest.util.Vfs;
import com.github.pkunk.progressquest.widget.WidgetProvider;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: pkunk
 * Date: 2011-12-24
 */
public class GameplayService extends Service {
    private static final String TAG = GameplayService.class.getCanonicalName();

    private static final Object PLAYER_LOCK = new Object();

    private Handler mHandler = new Handler();

    private IBinder mBinder = new GameplayBinder();

    private boolean mForceUpdateWidget = false;

    Set<GameplayServiceListener> mListeners = new HashSet<GameplayServiceListener>();

    private Player mPlayer = null;

    public void setPlayer(Player player) {
        synchronized (PLAYER_LOCK) {
            savePlayer();
            this.mPlayer = player;
            mHandler.removeCallbacks(updateTask);
            mHandler.postDelayed(updateTask, player.getCurrentTaskTime());
        }
    }

    public Player getPlayer() {
        return mPlayer;
    }

    private void makeTurn() {
        mPlayer.turn();
    }

    private Runnable updateTask = new Runnable() {
        public void run() {
            Log.d(TAG, "Turn");
            if (mPlayer != null) {

                synchronized (PLAYER_LOCK) {
                    makeTurn();
                    if (checkToSave()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                savePlayer();
                            }
                        }).start();
                    }
                }

                mHandler.postDelayed(this, mPlayer.getCurrentTaskTime());
                notifyGameplayListeners();
                if (mForceUpdateWidget || mPlayer.isSaveGame() || mPlayer.isEquipUpdated()) {
                    updateWidget();
                }
            }
        }
    };
    
    private void updateWidget() {
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget);
        synchronized (PLAYER_LOCK) {
            view.setTextViewText(R.id.wg_status1, UiUtils.getStatus1(mPlayer));
            view.setTextViewText(R.id.wg_status2, UiUtils.getStatus2(mPlayer));
            view.setTextViewText(R.id.wg_status3, UiUtils.getStatus3(mPlayer));
        }
        ComponentName thisWidget = new ComponentName(this, WidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, view);
        mForceUpdateWidget = false;
    }

    public void addGameplayListener(GameplayServiceListener listener) {
        mListeners.add(listener);
    }

    public void removeGameplayListener(GameplayServiceListener listener) {
        mListeners.remove(listener);
    }

    private void notifyGameplayListeners() {
        for (GameplayServiceListener listener : mListeners) {
            listener.onGameplay();
        }
    }

    private boolean checkToSave() {
        return mPlayer.isSaveGame();
    }

    private void savePlayer() {
        synchronized (PLAYER_LOCK) {
            if (mPlayer == null) {
                return;
            }
            try {
                Vfs.writeToFile(this, mPlayer.getPlayerId() + Vfs.ZIP_EXT, mPlayer.savePlayer());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public Player loadPlayer(String playerName) throws IOException {
        Player savedPlayer;
        synchronized (PLAYER_LOCK) {
            Map<String, List<String>> playerSaveMap = Vfs.readFromFile(this, playerName + Vfs.ZIP_EXT);
            savedPlayer = Player.loadPlayer(playerSaveMap);
        }
        return savedPlayer;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mForceUpdateWidget = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(updateTask);
        savePlayer();
        super.onDestroy();
    }

    public class GameplayBinder extends Binder {
        public GameplayService getService() {
            // Return this instance of GameplayService so clients can call public methods
            return GameplayService.this;
        }
    }

}
