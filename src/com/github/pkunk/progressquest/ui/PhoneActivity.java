package com.github.pkunk.progressquest.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.github.pkunk.progressquest.R;
import com.github.pkunk.progressquest.gameplay.Equips;
import com.github.pkunk.progressquest.gameplay.Player;
import com.github.pkunk.progressquest.gameplay.Stats;
import com.github.pkunk.progressquest.gameplay.Traits;
import com.github.pkunk.progressquest.service.GameplayService;
import com.github.pkunk.progressquest.service.GameplayServiceListener;
import com.github.pkunk.progressquest.ui.util.TaskBarUpdater;
import com.github.pkunk.progressquest.ui.util.UiUtils;
import com.github.pkunk.progressquest.util.PqUtils;
import com.github.pkunk.progressquest.util.Roman;

import java.util.List;
import java.util.Map;

/**
 * User: pkunk
 * Date: 2012-01-25
 */
public class PhoneActivity extends Activity implements GameplayServiceListener {
    private static final String TAG = PhoneActivity.class.getCanonicalName();

    private GameplayService service;
    private volatile boolean isBound = false;

    private TaskBarUpdater taskBarUpdater;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pqphone);

        setupTabHost();
//        tabHost = getTabHost();

        tabHost.addTab(tabHost.newTabSpec("ph_tab_char").setIndicator("  Character  ").setContent(R.id.ph_tab_char));
        tabHost.addTab(tabHost.newTabSpec("ph_tab_spell").setIndicator("  Spells  ").setContent(R.id.ph_tab_spell));
        tabHost.addTab(tabHost.newTabSpec("ph_tab_equip").setIndicator("  Equip  ").setContent(R.id.ph_tab_equip));
        tabHost.addTab(tabHost.newTabSpec("ph_tab_items").setIndicator("  Inventory  ").setContent(R.id.ph_tab_items));
        tabHost.addTab(tabHost.newTabSpec("ph_tab_plot").setIndicator("  Plot  ").setContent(R.id.ph_tab_plot));
        tabHost.addTab(tabHost.newTabSpec("ph_tab_quests").setIndicator("  Quests  ").setContent(R.id.ph_tab_quests));

        if(savedInstanceState != null ){
            tabHost.setCurrentTab(savedInstanceState.getInt("tabState"));
        } else {
            tabHost.setCurrentTab(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tabState", tabHost.getCurrentTab());
    }

    private void setupTabHost() {
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to GameplayService
        Intent intent = new Intent(this, GameplayService.class);
//        startService(intent);   //todo: remove to let service die
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        taskBarUpdater = new TaskBarUpdater(this, R.id.ph_task_bar);
        taskBarUpdater.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        taskBarUpdater.cancel(true);
        // Unbind from the service
        if (isBound) {
            PhoneActivity.this.service.removeGameplayListener(PhoneActivity.this);
            unbindService(connection);
            isBound = false;
        }
    }

    @Override
    public void onGameplay() {
        if (isBound) {
            // Don't block this thread
            Player player = service.getPlayer();
            updateUi(player, false);
        }
    }

    private void updateUi(Player player, boolean force) {
        this.runOnUiThread(new UiUpdater(this, player, force));
    }

    private static Player createPlayer() {
        Traits traits = Traits.newTraits("Tester", "Gremlin", "Dancer");
        Stats stats = new Stats(new int[]{10,11,12,13,14,15,80,60});
        Player player = Player.newPlayer(traits, stats);
        return player;
    }

    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");

            GameplayService.GameplayBinder binder = (GameplayService.GameplayBinder) service;
            PhoneActivity.this.service = binder.getService();
            isBound = true;

            PhoneActivity.this.service.addGameplayListener(PhoneActivity.this);
            Player player = PhoneActivity.this.service.getPlayer();
            if (player == null) {
                try {
                    Player savedPlayer = PhoneActivity.this.service.loadPlayer("Tester");
                    PhoneActivity.this.service.setPlayer(savedPlayer);
                } catch (Exception e) {
                    Player newPlayer = createPlayer();
                    PhoneActivity.this.service.setPlayer(newPlayer);
                }
                player = PhoneActivity.this.service.getPlayer();
            }

            if (player != null) {
                updateUi(player, true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected");
            isBound = false;
        }
    };

    private class UiUpdater implements Runnable {

        private final Activity activity;
        private final Player player;
        private final boolean isForce;

        public UiUpdater(Activity activity, Player player, boolean force) {
            this.activity = activity;
            this.player = player;
            this.isForce = force;
        }

        @Override
        public void run() {

            // Statusbar
            updateStatusbar();

            // Character
            updateTraits(isForce);
            updateLevelBar();
            updateStats(isForce);

            // Spells
            updateSpellBook(isForce);

            // Inventory
            updateEquipment(isForce);
            updateEncumbranceBar();
            updateInventory(isForce);

            // Plot
            updatePlotBar();
            updatePlot(isForce);

            // Quest
            updateQuestsBar();
            updateQuests(isForce);

            // Task
            updateTaskBar();
            updateTask();
        }

        private void updateStatusbar() {
            TextView statusView = (TextView) findViewById(R.id.ph_player_status);
            Traits traits = player.getTraits();

            StringBuilder statusText = new StringBuilder();
            statusText.append(traits.getName()).append(" the ").append(traits.getRace());
            statusText.append(" (").append(player.getBestPlot()).append(")");
            statusText.append("\n");
            statusText.append("Level ").append(traits.getLevel()).append(" ").append(traits.getRole());
            statusText.append("\n");
            statusText.append(player.getBestEquip()).append(" / ");
            statusText.append(player.getBestSpell()).append(" / ");
            statusText.append(player.getBestStat());

            statusView.setText(statusText.toString());
        }

        private void updateTask() {
            TextView taskView = (TextView) findViewById(R.id.ph_task_text);
            taskView.setText(player.getCurrentTask());
        }

        private void updateTaskBar() {
            int max = player.getCurrentTaskTime();
            UiUtils.updateTextProgressBar(activity, R.id.ph_task_bar, 0, max, "0%");
        }

        private void updateTraits(boolean force) {
            if (!force && !player.isTraitsUpdated()) {
                return;
            }
            
            TableLayout traitsTable = (TableLayout)findViewById(R.id.ph_traits_table);
            traitsTable.removeAllViews();

            Traits traits = player.getTraits();
            TableRow headerTraits = UiUtils.headerRow(traitsTable.getContext(), "Trait", "Value");
            traitsTable.addView(headerTraits);
            TableRow nameRow = UiUtils.tableRow(traitsTable.getContext(), "Name", traits.getName());
            traitsTable.addView(nameRow);
            TableRow raceRow = UiUtils.tableRow(traitsTable.getContext(), "Race", traits.getRace());
            traitsTable.addView(raceRow);
            TableRow classRow = UiUtils.tableRow(traitsTable.getContext(), "Class", traits.getRole());
            traitsTable.addView(classRow);
            TableRow levelRow = UiUtils.tableRow(traitsTable.getContext(), "Level", String.valueOf(traits.getLevel()));
            traitsTable.addView(levelRow);
            TableRow emptyRow = UiUtils.tableRow(traitsTable.getContext(), "", "");
            traitsTable.addView(emptyRow);
        }

        private void updateStats(boolean force) {
            if (!force && !player.isStatsUpdated()) {
                return;
            }

            TableLayout statsTable = (TableLayout)findViewById(R.id.ph_stats_table);
            statsTable.removeAllViews();

            TableRow headerStats = UiUtils.headerRow(statsTable.getContext(), "Stat", "Value");
            statsTable.addView(headerStats);
            for (int i=0; i<Stats.STATS_NUM; i++) {
                String statName = Stats.label[i];
                String statValue = String.valueOf(player.getStats().get(i));
                TableRow row = UiUtils.tableRow(statsTable.getContext(), statName, statValue);
                statsTable.addView(row);
            }
        }

        private void updateLevelBar() {
            int current = player.getCurrentExp();
            int max = player.getMaxExp();
            int remaining = max - current;
            String text = remaining + " XP needed for next level";
            UiUtils.updateTextProgressBar(activity, R.id.ph_level_bar, current, max, text);
        }

        private void updateSpellBook(boolean force) {
            if (!force && !player.isSpellsUpdated()) {
                return;
            }

            TableLayout spellTable = (TableLayout)findViewById(R.id.ph_spell_table);
            spellTable.removeAllViews();

            TableRow header = UiUtils.headerRow(spellTable.getContext(), "Spell", "Level");
            spellTable.addView(header);

            for (Map.Entry<String,Roman> spell : player.getSpellBook().entrySet()) {
                TableRow row = UiUtils.tableRow(spellTable.getContext(), spell.getKey(), spell.getValue().toString());
                spellTable.addView(row);
            }
        }

        private void updateEquipment(boolean force) {
            if (!force && !player.isEquipUpdated()) {
                return;
            }

            TableLayout equipTable = (TableLayout)findViewById(R.id.ph_equip_table);
            equipTable.removeAllViews();

            for (int i=0; i<Equips.EQUIP_NUM; i++) {
                String equipName = Equips.label[i];
                String equipItem = player.getEquip().get(i);
                TableRow row = UiUtils.tableRow(equipTable.getContext(), equipName, equipItem);
                equipTable.addView(row);
            }
        }

        private void updateInventory(boolean force) {
            if (!force && !player.isItemsUpdated()) {
                return;
            }

            TableLayout itemsTable = (TableLayout)findViewById(R.id.ph_items_table);
            itemsTable.removeAllViews();

            TableRow header = UiUtils.headerRow(itemsTable.getContext(), "Item", "Qty  ");
            itemsTable.addView(header);

            for (Map.Entry<String,Integer> spell : player.getInventory().entrySet()) {
                TableRow row = UiUtils.tableRow(itemsTable.getContext(), spell.getKey(), spell.getValue().toString());
                itemsTable.addView(row);
            }
        }

        private void updateEncumbranceBar() {
            int current = player.getCurrentEncumbrance();
            int max = player.getMaxEncumbrance();
            StringBuilder text = new StringBuilder();
            text.append(current).append("/").append(max).append(" cubits");
            UiUtils.updateTextProgressBar(activity, R.id.ph_encum_bar, current, max, text.toString());
        }

        private void updatePlot(boolean force) {
            if (!force && !player.isPlotUpdated()) {
                return;
            }

            TableLayout plotTable = (TableLayout)findViewById(R.id.ph_plot_table);
            plotTable.removeAllViews();

            List<String> plotList = player.getPlot();
            int lastIndex = plotList.size() - 1;

            for (int i=0; i<plotList.size(); i++) {
                TableRow row = UiUtils.checkedRow(plotTable.getContext(), i!=lastIndex, plotList.get(i));
                plotTable.addView(row);
            }
        }

        private void updatePlotBar() {
            int current = player.getCurrentPlotProgress();
            int max = player.getMaxPlotProgress();
            int remaining = max - current;
            String text = PqUtils.roughTime(remaining) + "  remaining";
            UiUtils.updateTextProgressBar(activity, R.id.ph_plot_bar, current, max, text);
        }

        private void updateQuests(boolean force) {
            if (!force && !player.isQuestsUpdated()) {
                return;
            }

            TableLayout questsTable = (TableLayout)findViewById(R.id.ph_quests_table);
            questsTable.removeAllViews();

            List<String> questsList = player.getQuests();
            int lastIndex = questsList.size() - 1;

            for (int i=0; i<questsList.size(); i++) {
                TableRow row = UiUtils.checkedRow(questsTable.getContext(), i!=lastIndex, questsList.get(i));
                questsTable.addView(row);
            }
        }

        private void updateQuestsBar() {
            int current = player.getCurrentQuestProgress();
            int max = player.getMaxQuestProgress();
            StringBuilder text = new StringBuilder();
            text.append(current * 100 / max).append("% complete");
            UiUtils.updateTextProgressBar(activity, R.id.ph_quests_bar, current, max, text.toString());
        }

    }

}
