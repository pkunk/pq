package com.github.pkunk.pq.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import com.github.pkunk.pq.R;
import com.github.pkunk.pq.gameplay.Player;
import com.github.pkunk.pq.ui.util.UiUtils;
import com.github.pkunk.pq.util.Vfs;
import com.github.pkunk.pq.widget.WidgetProvider;

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
            mHandler.removeCallbacks(updateTask);
            savePlayer();
            this.mPlayer = player;
            mHandler.postDelayed(updateTask, player.getCurrentTaskTime());
        }
        updateWidget(player);
        mForceUpdateWidget = true;
    }

    public void removePlayer() {
        synchronized (PLAYER_LOCK) {
            mHandler.removeCallbacks(updateTask);
            this.mPlayer = null;
        }
        updateWidget(null);
        mForceUpdateWidget = true;
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

                Player widgetPlayer = null;
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
                    widgetPlayer = mPlayer;
                }

                mHandler.postDelayed(this, mPlayer.getCurrentTaskTime());
                notifyGameplayListeners();
                if (mForceUpdateWidget || mPlayer.isSaveGame() || mPlayer.isEquipUpdated()) {
                    updateWidget(widgetPlayer);
                }
            }
        }
    };
    
    private void updateWidget(Player player) {
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget);
        synchronized (PLAYER_LOCK) {
            view.setTextViewText(R.id.wg_status1, UiUtils.getStatus1(player));
            view.setTextViewText(R.id.wg_status2, UiUtils.getStatus2(player));
            view.setTextViewText(R.id.wg_status3, UiUtils.getStatus3(player));
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
                Vfs.writePlayerToFile(this, mPlayer.getPlayerId(), mPlayer.savePlayer());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public Player loadPlayer(String playerId) {
        Player savedPlayer = null;
        synchronized (PLAYER_LOCK) {
            try {
                Map<String, List<String>> playerSaveMap = Vfs.readPlayerFromFile(this, playerId);
                savedPlayer = Player.loadPlayer(playerSaveMap);
            } catch (IOException ioe) {
                Vfs.setPlayerId(this, null);
            }
        }
        return savedPlayer;
    }

    public void setWidgetOutdated() {
        mForceUpdateWidget = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mForceUpdateWidget = true;
        if (mPlayer == null) {
            String playerId = Vfs.getPlayerId(this);
            if (playerId != null && playerId.length() > 0) {
                try { 
                    Player player = loadPlayer(playerId);
                    setPlayer(player);
                    updateWidget(player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
