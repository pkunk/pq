package com.github.pkunk.progressquest.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.github.pkunk.progressquest.R;
import com.github.pkunk.progressquest.gameplay.Player;
import com.github.pkunk.progressquest.gameplay.Stats;
import com.github.pkunk.progressquest.gameplay.Traits;
import com.github.pkunk.progressquest.service.GameplayService;
import com.github.pkunk.progressquest.service.GameplayService.GameplayBinder;
import com.github.pkunk.progressquest.service.GameplayServiceListener;

/**
 * User: pkunk
 * Date: 2011-12-24
 */
public class TestActivity extends Activity implements GameplayServiceListener {
    private static final String TAG = TestActivity.class.getCanonicalName();

    GameplayService service;
    volatile boolean isBound = false;

    private Player player = createPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to GameplayService
        Intent intent = new Intent(this, GameplayService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (isBound) {
            TestActivity.this.service.removeGameplayListener(TestActivity.this);
            unbindService(connection);
            isBound = false;
        }
    }

    @Override
    public void onGameplay() {
        if (isBound) {
            // Call a method from the GameplayService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
            Player player = service.getPlayer();
            Toast.makeText(this, player.getCurrentTask(), Toast.LENGTH_LONG).show();
        }
    }

    private Player createPlayer() {
        Traits traits = new Traits("Tester", "Gremlin", "Dancer");
        Stats stats = new Stats(new int[]{10,11,12,13,14,15,80,60});
        Player player = Player.newPlayer(traits, stats);
        return player;
    }

     /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected");
            // We've bound to GameplayService, cast the IBinder and get GameplayService instance
            GameplayBinder binder = (GameplayBinder) service;
            TestActivity.this.service = binder.getService();
            isBound = true;
            TestActivity.this.service.addGameplayListener(TestActivity.this);
            TestActivity.this.service.setPlayer(player);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            isBound = false;
        }
    };
}
