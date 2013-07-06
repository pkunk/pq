package com.github.pkunk.pq.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.github.pkunk.pq.R;
import com.github.pkunk.pq.init.Res;
import com.github.pkunk.pq.service.GameplayService;
import com.github.pkunk.pq.ui.view.RosterEntryView;
import com.github.pkunk.pq.util.Vfs;

import java.util.*;


/**
 * User: pkunk
 * Date: 2012-02-12
 */
public class PhoneRosterActivity extends Activity {
    private static final String TAG = PhoneRosterActivity.class.getCanonicalName();

    private Map<View, String> playViewsMap;
    private Map<View, String> killViewsMap;
    private Map<String, RosterEntryView> rosterEntriesMap;
    private Map<String, String> namesMap;

    private String playerIdToKill;

    private GameplayService service;
    private volatile boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ph_roster);

        View newPlayerView = findViewById(R.id.ph_roster_newplayer);
        newPlayerView.setOnClickListener(new NewPlayerListener());

        populateView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, GameplayService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        super.onStop();
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");

            GameplayService.GameplayBinder binder = (GameplayService.GameplayBinder) service;
            PhoneRosterActivity.this.service = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");
            isBound = false;
        }
    };

    private void populateView() {
        String[] saveFiles = Vfs.getPlayersSaveFiles(this);
        Arrays.sort(saveFiles);

        Map<String, List<String>> statusMap = Vfs.readEntryFromFiles(this, saveFiles, "Annotation");
        ViewGroup rosterGroup = (ViewGroup) findViewById(R.id.ph_roster_saves);

        playViewsMap = new WeakHashMap<View, String>(saveFiles.length);
        killViewsMap = new WeakHashMap<View, String>(saveFiles.length);
        rosterEntriesMap = new HashMap<String, RosterEntryView>(saveFiles.length);
        namesMap = new HashMap<String, String>(saveFiles.length);

        PlayListener playListener = new PlayListener();
        KillListener killListener = new KillListener();

        for (String file : saveFiles) {
            RosterEntryView entryView = new RosterEntryView(this);
            PlayerAnnotation player = getStatus(statusMap.get(file));
            entryView.setText(player.status1, player.status2, player.status3);
            playViewsMap.put(entryView.getPlayView(), player.playerId);
            killViewsMap.put(entryView.getKillView(), player.playerId);
            rosterEntriesMap.put(player.playerId, entryView);
            namesMap.put(player.playerId, player.name);
            entryView.getPlayView().setOnClickListener(playListener);
            entryView.getKillView().setOnClickListener(killListener);
            rosterGroup.addView(entryView);
        }
    }

    private void removeRosterEntry(String playerId) {
        RosterEntryView view = rosterEntriesMap.get(playerId);
        playViewsMap.remove(view.getPlayView());
        killViewsMap.remove(view.getKillView());
        rosterEntriesMap.remove(playerId);
        namesMap.remove(playerId);
        ((ViewGroup)view.getParent()).removeView(view);
    }

    private PlayerAnnotation getStatus(List<String> strings) {
        PlayerAnnotation result = new PlayerAnnotation();
        for (String s : strings) {
            String entry[] = s.split(Vfs.EQ);
            if ("playerId".equals(entry[0])) {
                result.playerId = entry[1];
            } else if ("status1".equals(entry[0])) {
                result.status1 = entry[1];
            } else if ("status2".equals(entry[0])) {
                result.status2 = entry[1];
            } else if ("status3".equals(entry[0])) {
                result.status3 = entry[1];
            } else if ("name".equals(entry[0])) {
                result.name = entry[1];
            }
        }
        return result;
    }

    private void createNewPlayer() {
        Intent intent = new Intent(this, PhoneNewPlayerActivity.class);
        if (namesMap.isEmpty()) {
            finish();
        }
        startActivity(intent);
    }

    private void selectPlayer(String playerId) {
        Vfs.setPlayerId(this, playerId);
        Intent intent = new Intent(this, PhoneGameplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
    private void killPlayer() {
        String currentPlayerId = Vfs.getPlayerId(this);

        if (playerIdToKill.equals(currentPlayerId)) {
            Vfs.setPlayerId(this, null);
            service.removePlayer();
        }
        Vfs.deletePlayerFiles(this, playerIdToKill);
        removeRosterEntry(playerIdToKill);
    }

    private AlertDialog killConfirmationDialog() {
        String playerName;
        playerName = namesMap.get(playerIdToKill);
        if (playerName == null) {
            playerName = "Hero";
        }
        String confirmationText = "Terminate " + Res.MERITS.pick() + " " + playerName + "?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(confirmationText)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        killPlayer();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    private static class PlayerAnnotation {
        private String playerId = null;
        private String status1 = "";
        private String status2 = "";
        private String status3 = "";
        private String name = null;
    }

    private class PlayListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            selectPlayer(playViewsMap.get(v));
        }
    }

    private class KillListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            playerIdToKill =  killViewsMap.get(v);
            killConfirmationDialog().show();
        }
    }

    private class NewPlayerListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            createNewPlayer();
        }
    }
}
