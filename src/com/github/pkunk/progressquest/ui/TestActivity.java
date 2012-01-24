package com.github.pkunk.progressquest.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import com.github.pkunk.progressquest.R;
import com.github.pkunk.progressquest.gameplay.Equips;
import com.github.pkunk.progressquest.gameplay.Player;
import com.github.pkunk.progressquest.gameplay.Stats;
import com.github.pkunk.progressquest.gameplay.Traits;
import com.github.pkunk.progressquest.service.GameplayService;
import com.github.pkunk.progressquest.service.GameplayService.GameplayBinder;
import com.github.pkunk.progressquest.service.GameplayServiceListener;
import com.github.pkunk.progressquest.util.PqUtils;
import com.github.pkunk.progressquest.util.Roman;

import java.util.Map;

/**
 * User: pkunk
 * Date: 2011-12-24
 */
public class TestActivity extends Activity implements GameplayServiceListener {
    private static final String TAG = TestActivity.class.getCanonicalName();

    private GameplayService service;
    private volatile boolean isBound = false;

    private TaskBarUpdater taskBarUpdater;

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
        startService(intent);   //todo: remove to let service die
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        taskBarUpdater = new TaskBarUpdater();
        taskBarUpdater.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        taskBarUpdater.cancel(true);
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
//            Toast.makeText(this, player.getCurrentTask(), Toast.LENGTH_LONG).show();
            updateUi(player);
        }
    }

    private void updateUi(Player player) {
        this.runOnUiThread(new UiUpdater(player));
    }

    private static Player createPlayer() {
        Traits traits = new Traits("Tester", "Gremlin", "Dancer");
        Stats stats = new Stats(new int[]{10,11,12,13,14,15,80,60});
        Player player = Player.newPlayer(traits, stats);
        return player;
    }

     /** Defines callbacks for service binding, passed to bindService() */
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected");
            // We've bound to GameplayService, cast the IBinder and get GameplayService instance
            GameplayBinder binder = (GameplayBinder) service;
            TestActivity.this.service = binder.getService();
            isBound = true;
            TestActivity.this.service.addGameplayListener(TestActivity.this);
            Player player = TestActivity.this.service.getPlayer();
            if (player == null) {
                TestActivity.this.service.setPlayer(createPlayer());
                player = TestActivity.this.service.getPlayer();
            }
            if (player != null) {
                updateUi(player);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            isBound = false;
        }
    };

    private class TaskBarUpdater extends AsyncTask {
        @Override
        protected Object doInBackground(Object... params) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            TextProgressBar taskBar = (TextProgressBar) findViewById(R.id.taskBar);
            taskBar.incrementProgressBy(100);
            taskBar.setText((taskBar.getProgress()*100/taskBar.getMax()) + "%");
        }
    }

    private class UiUpdater implements Runnable {

        private final Player player;

        public UiUpdater(Player player) {
            this.player = player;
        }

        @Override
        public void run() {

            // Task
            updateTask();
            updateTaskBar();

            // Character
            updateTraits();
            updateStats();
            updateLevelBar();

            // Spells
            updateSpellBook();

            // Inventory
            updateEquipment();
            updateInventory();
            updateEncumbranceBar();

            // Plot
            updatePlot();
            updatePlotBar();

            // Quest
            updateQuests();
            updateQuestsBar();
        }

        private void updateTask() {
            TextView taskView = (TextView) findViewById(R.id.task);
            taskView.setText(player.getCurrentTask());
        }

        private void updateTaskBar() {
            TextProgressBar taskBar = (TextProgressBar) findViewById(R.id.taskBar);
            int max = player.getCurrentTaskTime();
            taskBar.setMax(max);
            taskBar.setProgress(0);
            taskBar.setText("0%");
        }

        private void updateTraits() {
            StringBuilder builder = new StringBuilder();
            builder.append("Trait").append("\n");
            builder.append("Name").append("\t\t\t").append(player.getTraits().getName()).append("\n");
            builder.append("Race").append("\t\t\t").append(player.getTraits().getRace()).append("\n");
            builder.append("Class").append("\t\t\t").append(player.getTraits().getRole()).append("\n");
            builder.append("Level").append("\t\t\t").append(player.getTraits().getLevel()).append("\n");
            TextView traitsView = (TextView) findViewById(R.id.traits);
            traitsView.setText(builder);
        }

        private void updateStats() {
            StringBuilder builder = new StringBuilder();
            builder.append("Stat").append("\n");
            for (int i=0; i<Stats.STATS_NUM; i++) {
                builder.append(Stats.label[i]).append("\t\t\t").append(player.getStats().get(i)).append("\n");
            }
            TextView statsView = (TextView) findViewById(R.id.stats);
            statsView.setText(builder);
        }

        private void updateLevelBar() {
            TextProgressBar levelBar = (TextProgressBar) findViewById(R.id.levelBar);
            int current = player.getCurrentExp();
            int max = player.getMaxExp();
            int remaining = max - current;
            levelBar.setMax(max);
            levelBar.setProgress(current);
            levelBar.setText(remaining + " XP needed for next level");
        }

        private void updateSpellBook() {
            StringBuilder builder = new StringBuilder();
            builder.append("Spell").append("\n");
            for (Map.Entry<String,Roman> spell : player.getSpellBook().entrySet()) {
                builder.append(spell.getKey()).append("\t\t\t").append(spell.getValue().getRoman()).append("\n");
            }
            TextView spellbookView = (TextView) findViewById(R.id.spellbook);
            spellbookView.setText(builder);
        }

        private void updateEquipment() {
            StringBuilder builder = new StringBuilder();
            for (int i=0; i< Equips.EQUIP_NUM; i++) {
                builder.append(Equips.label[i]).append("\t\t\t").append(player.getEquip().get(i)).append("\n");
            }
            TextView equipmentView = (TextView) findViewById(R.id.equipment);
            equipmentView.setText(builder);
        }

        private void updateInventory() {
            StringBuilder builder = new StringBuilder();
            builder.append("Item").append("\n");
            for (Map.Entry<String,Integer> spell : player.getInventory().entrySet()) {
                builder.append(spell.getKey()).append("\t\t\t").append(spell.getValue()).append("\n");
            }
            TextView inventoryView = (TextView) findViewById(R.id.inventory);
            inventoryView.setText(builder);
        }

        private void updateEncumbranceBar() {
            TextProgressBar encumbranceBar = (TextProgressBar) findViewById(R.id.encumbranceBar);
            int current = player.getCurrentEncumbrance();
            int max = player.getMaxEncumbrance();
            encumbranceBar.setMax(max);
            encumbranceBar.setProgress(current);
            StringBuilder builder = new StringBuilder();
            builder.append(current).append("/").append(max).append(" cubits");
            encumbranceBar.setText(builder.toString());
        }

        private void updatePlot() {
            StringBuilder builder = new StringBuilder();
            for (String plot : player.getPlot()) {
                builder.append(plot).append("\n");
            }
            TextView plotView = (TextView) findViewById(R.id.plot);
            plotView.setText(builder);
        }

        private void updatePlotBar() {
            TextProgressBar plotBar = (TextProgressBar) findViewById(R.id.plotBar);
            int current = player.getCurrentPlotProgress();
            int max = player.getMaxPlotProgress();
            int remaining = max - current;
            plotBar.setMax(max);
            plotBar.setProgress(current);
            plotBar.setText(PqUtils.roughTime(remaining) + "  remaining");
        }

        private void updateQuests() {
            StringBuilder builder = new StringBuilder();
            for (String quest : player.getQuests()) {
                builder.append(quest).append("\n");
            }
            TextView questsView = (TextView) findViewById(R.id.quests);
            questsView.setText(builder);
        }

        private void updateQuestsBar() {
            TextProgressBar plotBar = (TextProgressBar) findViewById(R.id.questsBar);
            int current = player.getCurrentQuestProgress();
            int max = player.getMaxQuestProgress();
            plotBar.setMax(max);
            plotBar.setProgress(current);
            StringBuilder builder = new StringBuilder();
            builder.append(current*100/max).append("% complete");
            plotBar.setText(builder.toString());
        }
    }
}
