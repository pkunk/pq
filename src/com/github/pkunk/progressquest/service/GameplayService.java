package com.github.pkunk.progressquest.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.github.pkunk.progressquest.gameplay.Player;
import com.github.pkunk.progressquest.util.Vfs;

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

    Handler handler = new Handler();

    private IBinder binder = new GameplayBinder();

    Set<GameplayServiceListener> listeners = new HashSet<GameplayServiceListener>();

    private Player player = null;

    public void setPlayer(Player player) {
        synchronized (PLAYER_LOCK) {
            savePlayer();
            this.player = player;
            handler.removeCallbacks(updateTask);
            handler.post(updateTask);
        }
    }

    public Player getPlayer() {
        return player;
    }

    private void makeTurn () {
        player.turn();
    }

    private Runnable updateTask = new Runnable() {
        public void run() {
            Log.d(TAG, "Turn");
            if (player != null) {

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

                handler.postDelayed(this, player.getCurrentTaskTime());
                notifyGameplayListeners();
            }
        }
    };

    public void addGameplayListener(GameplayServiceListener listener) {
        listeners.add(listener);
    }

    public void removeGameplayListener(GameplayServiceListener listener) {
        listeners.remove(listener);
    }

    private void notifyGameplayListeners() {
        for (GameplayServiceListener listener : listeners) {
            listener.onGameplay();
        }
    }

    private boolean checkToSave() {
        return player.isSaveGame();
    }

    private void savePlayer() {
        synchronized (PLAYER_LOCK) {
            if (player == null) {
                return;
            }
            try {
                Vfs.writeToFile(this, player.getPlayerId() + Vfs.ZIP_EXT, player.savePlayer());
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
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateTask);
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
