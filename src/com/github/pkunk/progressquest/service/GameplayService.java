package com.github.pkunk.progressquest.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.github.pkunk.progressquest.gameplay.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * User: pkunk
 * Date: 2011-12-24
 */
public class GameplayService extends Service {
    private static final String TAG = GameplayService.class.getCanonicalName();

    Handler handler = new Handler();

    private IBinder binder = new GameplayBinder();

    Set<GameplayServiceListener> listeners = new HashSet<GameplayServiceListener>();

    private Player player = null;

    public void setPlayer(Player player) {
        handler.removeCallbacks(updateTask);
        handler.post(updateTask);
        this.player = player;
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
                makeTurn();
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

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class GameplayBinder extends Binder {
        public GameplayService getService() {
            // Return this instance of GameplayService so clients can call public methods
            return GameplayService.this;
        }

        public void setPlayer (Player player) {
            GameplayService.this.setPlayer(player);
        }

        public Player getPlayer() {
            return GameplayService.this.getPlayer();
        }

    }

}
