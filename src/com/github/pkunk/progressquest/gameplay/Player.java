package com.github.pkunk.progressquest.gameplay;

import android.util.Log;
import com.github.pkunk.progressquest.init.Res;
import com.github.pkunk.progressquest.ui.util.UiUtils;
import com.github.pkunk.progressquest.util.PqUtils;
import com.github.pkunk.progressquest.util.ResList;
import com.github.pkunk.progressquest.util.Roman;
import com.github.pkunk.progressquest.util.Vfs;

import java.util.*;
import java.util.Map.Entry;

/**
 * User: pkunk
 * Date: 2011-12-24
 */
public class Player {

    private String playerId;

    private Traits traits;
    private Stats stats;
    private SpellBook spellbook;
    private Equips equips;
    private Inventory inventory;
    private QuestList plots;
    private QuestList quests;
    private Game game;

    private ProgressCounter expProgress;
    private ProgressCounter plotProgress;
    private ProgressCounter questProgress;
    private ProgressCounter encumProgress;

    private String currentTask;
    private int currentTaskTime;

    private boolean isTraitsUpdated;
    private boolean isStatsUpdated;
    private boolean isSpellsUpdated;
    private boolean isEquipUpdated;
    private boolean isItemsUpdated;
    private boolean isPlotUpdated;
    private boolean isQuestsUpdated;
    private boolean saveGame;

    private Player() {
    }

    public static Player newPlayer(Traits traits, Stats stats) {
        Player player = new Player();

        player.playerId = PqUtils.getTimestamp();

        player.game = Game.newGame();
        player.traits = traits;
        player.stats = stats;
        player.spellbook = SpellBook.newSpellBook();
        player.equips = Equips.newEquips();
        player.inventory = Inventory.newInventory();
        player.plots = QuestList.newQuestList();
        player.quests = QuestList.newQuestList();

        player.currentTask = "Loading....";
        player.currentTaskTime = 2000;

        player.expProgress = new ProgressCounter(player.levelUpTime());
        player.plotProgress = new ProgressCounter(28); // initial task time + sum of plot tasks except last
        player.questProgress = new ProgressCounter(1);
        player.encumProgress = new ProgressCounter(0);
        player.recalculateEncum();

        player.hotOrNot();

        player.plots.add(player.game.bestPlot);
        player.queuePlot(new PlotTask("Experiencing an enigmatic and foreboding night vision", 10));
        player.queuePlot(new PlotTask("Much is revealed about that wise old bastard you'd underestimated", 6));
        player.queuePlot(new PlotTask("A shocking series of events leaves you alone and bewildered, but resolute", 6));
        player.queuePlot(new PlotTask("Drawing upon an unrealized reserve of determination, you set out on a long and dangerous journey", 4));
        player.queuePlot(new PlotTask("Loading", 2, true));

        player.setAllFlags();

        return player;
    }

    private void task(String taskText, int millis) {
        currentTask = taskText + "...";
        currentTaskTime = millis;
    }

    private void levelUp() {
        traits.levelUp();
        isTraitsUpdated = true;
        stats.inc(Stats.HP_MAX, Stats.CON / 3 + 1 + PqUtils.random(4));
        stats.inc(Stats.MP_MAX, Stats.INT / 3 + 1 + PqUtils.random(4));
        isStatsUpdated = true;
        winStat();
        winStat();
        winSpell();
        expProgress.reset(levelUpTime());
        hotOrNot();
        saveGame = true;
    }

    private void dequeue() {
//        while (TaskDone()) {
            if (game.task.isKill()) {
                if (game.task.getMonster().getLoot().equals("*")) {
                    winItem();
                } else if (game.task.getMonster().getLoot().length() > 0) {
                    inventory.add(game.task.getMonster().getName().toLowerCase(Locale.US) + " " + game.task.getMonster().getLoot(),1);
                    recalculateEncum();
                    isItemsUpdated = true;
                }
            } else if (game.task.isBuying()) {
                // buy some equipment
                inventory.add(Inventory.GOLD,-equipPrice());
                isItemsUpdated = true;
                winEquip();
            } else if ((game.task.isMarket()) || (game.task.isSell())) {
                if (game.task.isSell()) {
                    String item = inventory.getLastItem();
                    int amt = inventory.get(item) * traits.getLevel();
                    if (item.contains(" of ")) {
                        amt *= (1+PqUtils.randomLow(10)) * (1+PqUtils.randomLow(traits.getLevel()));
                    }
                    inventory.remove(item);
                    inventory.add(Inventory.GOLD, amt);
                    recalculateEncum();
                    isItemsUpdated = true;
                }
                if (inventory.size() > 1) {
//                    Inventory.scrollToTop();
                    String sellingItem = inventory.getLastItem();
                    task("Selling " + PqUtils.indefinite(sellingItem, inventory.get(sellingItem)),
                            1 * 1000);
                    game.task = Task.sellTask();
                    return;
                }
            }

            Task old = game.task;
            game.task = Task.emptyTask();
            if (game.plotQueue.size() > 0) {
                PlotTask plotTask = game.plotQueue.poll();
                String desc = plotTask.getDescription();
                if (plotTask.isPlot()) {
                    completeAct();
                    desc = "Loading " + game.bestPlot;
                }
                task(desc, plotTask.getTime() * 1000);
            } else if (encumProgress.done()) {
                task("Heading to market to sell loot",4 * 1000);
                game.task = Task.marketTask();
            } else if ((!old.isKill()) && (!old.isHeading())) {
                if (inventory.get(Inventory.GOLD) > equipPrice()) {
                    task("Negotiating purchase of better equipment", 5 * 1000);
                    game.task = Task.buyingTask();
                } else {
                    task("Heading to the killing fields", 4 * 1000);
                    game.task = Task.headingTask();
                }
            } else {
                int nn = traits.getLevel();
                World.MonsterTask t = World.monsterTask(game, nn);
                int InventoryLabelAlsoGameStyleTag = 3;
                nn = (int)Math.floor((2 * InventoryLabelAlsoGameStyleTag * t.level * 1000) / nn);
                task("Executing " + t.description, nn);
            }
//        }
    }

    private void winStat() {
        int toRise = -1;
        if (PqUtils.odds(1, 2))  {
            toRise = PqUtils.random(Stats.BASE_STATS_NUM);
        } else {
            // Favor the best stat so it will tend to clump
            int t = 0;
            for (int i=0; i< Stats.BASE_STATS_NUM; i++) {
                t += PqUtils.square(stats.get(i));
            }
            t = PqUtils.random(t);
            for (int i=0; i< Stats.BASE_STATS_NUM; i++) {
                t -= PqUtils.square(stats.get(i));
                if (t < 0) {
                    toRise = i;
                    break;
                }
            }
        }
        stats.inc(toRise, 1);
        if (toRise == Stats.STR) {
            recalculateEncum();
        }
        isStatsUpdated = true;
    }

    private void winSpell() {
        spellbook.addR(Res.SPELLS.get(PqUtils.randomLow(Math.min(stats.get(Stats.WIS) + traits.getLevel(),
                Res.SPELLS.size()))), 1);
        isSpellsUpdated = true;
        hotOrNot();
    }

    private void winEquip() {
        int posn = PqUtils.random(Equips.EQUIP_NUM);

        ResList<EquipItem> stuff, better, worse;

        if (posn == Equips.WEAPON) {
            stuff = Res.WEAPONS;
            better = Res.OFFENSE_ATTRIB;
            worse = Res.OFFENSE_BAD;
        } else {
            better = Res.DEFENSE_ATTRIB;
            worse = Res.DEFENSE_BAD;
            stuff = (posn == Equips.SHIELD) ? Res.SHIELDS:  Res.ARMORS;
        }
        EquipItem eq = World.pickEquip(stuff, traits.getLevel());
        String name = eq.getName();
        int qual = eq.getMod();
        int plus = traits.getLevel() - qual;
        if (plus < 0) better = worse;
        int count = 0;
        while (count < 2 && plus != 0) {
            EquipItem modifier = better.pick();
            qual = modifier.getMod();
            String modifierName = modifier.getName();
            if (name.contains(modifierName)) break; // no repeats
            if (Math.abs(plus) < Math.abs(qual)) break; // too much
            name = modifierName + " " + name;
            plus -= qual;
            ++count;
        }
        if (plus != 0) name = plus + " " + name;
        if (plus > 0) name = "+" + name;

        equips.set(posn, name);
        game.bestEquip = name;
        if (posn > 1) game.bestEquip += " " + Equips.label[posn];
        isEquipUpdated = true;
    }

    private int equipPrice() {
        return  5 * PqUtils.square(traits.getLevel()) +
                10 * traits.getLevel() +
                20;
    }

    private void winItem() {
        inventory.add(World.getSpecialItem(), 1);
        recalculateEncum();
        isItemsUpdated = true;
    }

    private void recalculateEncum() {
        int current = 0;
        int max = 10 + stats.get(Stats.STR);
        
        for (Entry<String, Integer> item : inventory.entrySet()) {
            if (!item.getKey().equals(Inventory.GOLD)) {
                current += item.getValue();
            }
        }
        encumProgress.reset(max, current);
    }

    private void completeAct() {
//        Plots.CheckAll();
        game.act += 1;
        plotProgress.reset(60 * 60 * (1 + 5 * game.act)); // 1 hr + 5/act
        game.bestPlot = "Act " + Roman.toRoman(game.act);
        plots.add(game.bestPlot);

        if (game.act > 1) {
            winItem();
            winEquip();
        }

        isPlotUpdated = true;
        saveGame = true;
    }


    private void completeQuest() {
        questProgress.reset(50 + PqUtils.randomLow(1000));
        if (quests.size() > 0) {
//            Log("Quest completed: " + game.bestQuest);
//            Quests.CheckAll();
            switch (PqUtils.random(4)) {
                case 0: winSpell(); break;
                case 1: winEquip(); break;
                case 2: winStat();  break;
                case 3: winItem();  break;
            }
        }
        while (quests.size() > 99) {
            quests.remove();
        }

        game.questMonster = null;
        String caption = "";
        switch (PqUtils.random(5)) {
            case 0:
                int level = traits.getLevel();
                int lev = 0;
                for (int i=1; i<=4; i++) {
                    int montag = PqUtils.random(Res.MONSTERS.size());
                    Monster m = Res.MONSTERS.get(montag);
                    int l = m.getLevel();
                    if (i == 1 || Math.abs(l - level) < Math.abs(lev - level)) {
                        lev = l;
                        game.questMonster = m;
                        game.questMonsterIndex = montag;
                    }
                }
                caption = "Exterminate " + PqUtils.definite(game.questMonster.getName(), 2);
                break;
            case 1:
                caption = "Seek " + PqUtils.definite(World.getInterestingItem(), 1);
                break;
            case 2:
                caption = "Deliver this " + World.getBoringItem();
                break;
            case 3:
                caption = "Fetch me " + PqUtils.indefinite(World.getBoringItem(), 1);
                break;
            case 4:
                int mlev = 0;
                level = traits.getLevel();
                for (int i = 1; i <= 2; ++i) {
                    int montag = PqUtils.random(Res.MONSTERS.size());
                    Monster m = Res.MONSTERS.get(montag);
                    int l = m.getLevel();
                    if ((i == 1) || (Math.abs(l - level) < Math.abs(mlev - level))) {
                        mlev = l;
                        game.questMonster = m;
                    }
                }
                caption = "Placate " + PqUtils.definite(game.questMonster.getName(), 2);
                game.questMonster = null;  // We're trying to placate them, after all
                break;
        }
//        if (!game.Quests) game.Quests = [];
//        while (game.Quests.length > 99) game.Quests.shift();
        quests.add(caption);
        game.bestQuest = caption;
        isQuestsUpdated = true;
//        Quests.AddUI(caption);


//        Log("Commencing quest: " + caption);
        hotOrNot();
        saveGame = true;
    }

    /**
     *
     * @return time to level up in seconds
     */
    private int levelUpTime() {
        // 20 minutes per level
        return 20 * traits.getLevel() * 60;
    }

    public void turn() {
        Log.d("TURN", currentTask);
//        timerid = null;  // Event has fired
//        if (TaskBar.done()) {
            game.tasks += 1;
//            game.elapsed += TaskBar.Max().div(1000);

//            ClearAllSelections();
            clearAllFlags();

//            if (game.kill == 'Loading....')
//                TaskBar.reset(0);  // Not sure if this is still the ticket

            // gain XP / level up
            boolean gain = game.task.isKill();
            if (gain) {
                if (expProgress.done()) {
                    levelUp();
                } else {
                    expProgress.increment(currentTaskTime / 1000);
                }
            }

            // advance quest
            if (gain && game.act >= 1) {
                if (questProgress.done() || quests.size() == 0) {
                    completeQuest();
                } else {
                    questProgress.increment(currentTaskTime / 1000);
                }
            }

            // advance plot
            if (gain || game.act == 0) {
                if (plotProgress.done())
                    interplotCinematic();
                else
                    plotProgress.increment(currentTaskTime / 1000);
            }

            dequeue();
//        } else {
//            var elapsed = timeGetTime() - lasttick;
//            if (elapsed > 100) elapsed = 100;
//            if (elapsed < 0) elapsed = 0;
//            TaskBar.increment(elapsed);
//        }

//        StartTimer();
    }

    private void clearAllFlags() {
        isTraitsUpdated = false;
        isStatsUpdated = false;
        isSpellsUpdated = false;
        isEquipUpdated = false;
        isItemsUpdated = false;
        isPlotUpdated = false;
        isQuestsUpdated = false;
        saveGame = false;
    }

    private void setAllFlags() {
        isTraitsUpdated = true;
        isStatsUpdated = true;
        isSpellsUpdated = true;
        isEquipUpdated = true;
        isItemsUpdated = true;
        isPlotUpdated = true;
        isQuestsUpdated = true;
        saveGame = true;
    }

    private void interplotCinematic() {
        switch (PqUtils.random(3)) {
            case 0:
                queuePlot(new PlotTask("Exhausted, you arrive at a friendly oasis in a hostile land", 1));
                queuePlot(new PlotTask("You greet old friends and meet new allies", 2));
                queuePlot(new PlotTask("You are privy to a council of powerful do-gooders", 2));
                queuePlot(new PlotTask("There is much to be done. You are chosen!", 1));
                break;
            case 1:
                queuePlot(new PlotTask("Your quarry is in sight, but a mighty enemy bars your path!", 1));
                String nemesisMonster = World.namedMonster(traits.getLevel() + 3);
                queuePlot(new PlotTask("A desperate struggle commences with " + nemesisMonster, 4));
                int s = PqUtils.random(3);
                for (int i=1; i<= PqUtils.random(1 + game.act + 1); i++) {
                    s += 1 + PqUtils.random(2);
                    switch (s % 3) {
                        case 0: queuePlot(new PlotTask("Locked in grim combat with " + nemesisMonster, 2)); break;
                        case 1: queuePlot(new PlotTask(nemesisMonster + " seems to have the upper hand", 2)); break;
                        case 2: queuePlot(new PlotTask("You seem to gain the advantage over " + nemesisMonster, 2)); break;
                    }
                }
                queuePlot(new PlotTask("Victory! " + nemesisMonster + " is slain! Exhausted, you lose conciousness", 3));
                queuePlot(new PlotTask("You awake in a friendly place, but the road awaits", 2));
                break;
            case 2:
                String nemesisGuy = World.impressiveGuy();
                queuePlot(new PlotTask("Oh sweet relief! You've reached the protection of the good " + nemesisGuy, 2));
                queuePlot(new PlotTask("There is rejoicing, and an unnerving encouter with " + nemesisGuy + " in private", 3));
                queuePlot(new PlotTask("You forget your " + World.getBoringItem() + " and go back to get it", 2));
                queuePlot(new PlotTask("What's this!? You overhear something shocking!", 2));
                queuePlot(new PlotTask("Could " + nemesisGuy + " be a dirty double-dealer?", 2));
                queuePlot(new PlotTask("Who can possibly be trusted with this news!? ... Oh yes, of course", 3));
                break;
        }
        queuePlot(new PlotTask("Loading", 1, true));
    }

    private void queuePlot(PlotTask plotTask) {
        game.plotQueue.add(plotTask);
    }

    private void hotOrNot() {
        // Figure out which spell is best
        if (spellbook.size() > 0) {
            int i = 1;
            int best = 0;
            Map.Entry<String,Roman> bestSpell = null;
            for (Map.Entry<String,Roman> spell : spellbook.entrySet()) {
                if (bestSpell == null ||
                        i * spell.getValue().getInt() > best * bestSpell.getValue().getInt()) {
                    bestSpell = spell;
                    best = i;
                }
                i++;
            }
            game.bestSpell = bestSpell.getKey() + " " + bestSpell.getValue();
        } else {
            game.bestSpell = "";
        }

        /// And which stat is best?
        int bestStat = 0;
        for (int i=1; i<Stats.BASE_STATS_NUM; i++) {
            if (stats.get(i) > stats.get(bestStat)) {
                bestStat = i;
            }
        }
        game.bestStat = Stats.label[bestStat] + " " + stats.get(bestStat);
    }


    // Getters

    public String getPlayerId() {
        return playerId;
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public int getCurrentTaskTime() {
        return currentTaskTime;
    }

    public Traits getTraits() {
        return traits;
    }

    public Stats getStats() {
        return stats;
    }

    public Map<String,Roman> getSpellBook() {
        return spellbook;
    }

    public List<String> getEquip() {
        return equips;
    }

    public Map<String,Integer> getInventory() {
        return inventory;
    }

    public List<String> getPlot() {
        return plots;
    }

    public List<String> getQuests() {
        return quests;
    }

    public int getCurrentExp() {
        return expProgress.getCurrent();
    }

    public int getMaxExp() {
        return expProgress.getMax();
    }

    public int getCurrentEncumbrance() {
        return encumProgress.getCurrent();
    }

    public int getMaxEncumbrance() {
        return encumProgress.getMax();
    }

    public int getCurrentPlotProgress() {
        return plotProgress.getCurrent();
    }

    public int getMaxPlotProgress() {
        return plotProgress.getMax();
    }

    public int getCurrentQuestProgress() {
        return questProgress.getCurrent();
    }

    public int getMaxQuestProgress() {
        return questProgress.getMax();
    }

    public String getBestPlot() {
        return game.bestPlot;
    }

    public String getBestEquip() {
        return game.bestEquip;
    }

    public String getBestSpell() {
        return game.bestSpell;
    }

    public String getBestStat() {
        return game.bestStat;
    }

    public boolean isTraitsUpdated() {
        return isTraitsUpdated;
    }

    public boolean isStatsUpdated() {
        return isStatsUpdated;
    }

    public boolean isSpellsUpdated() {
        return isSpellsUpdated;
    }

    public boolean isEquipUpdated() {
        return isEquipUpdated;
    }

    public boolean isItemsUpdated() {
        return isItemsUpdated;
    }

    public boolean isPlotUpdated() {
        return isPlotUpdated;
    }

    public boolean isQuestsUpdated() {
        return isQuestsUpdated;
    }

    public boolean isSaveGame() {
        return saveGame;
    }

    // Save/Load

    public Map<String, List<String>> savePlayer() {
        Map<String, List<String>> result = new HashMap<String, List<String>>();

        result.put("Player", save());
        result.put("Game", game.saveGame());
        result.put("Traits", traits.saveTraits());
        result.put("Stats", stats.saveStats());
        result.put("SpellBook", spellbook.saveSpellBook());
        result.put("Equips", equips.saveEquips());
        result.put("Inventory", inventory.saveInventory());
        result.put("Plot", plots.saveQuestList());
        result.put("Quests", quests.saveQuestList());
        result.put("Annotation", saveAnnotation());

        return result;
    }

    public static Player loadPlayer(Map<String, List<String>> map) {
        Player player = new Player();

        for (Entry<String, List<String>> entry : map.entrySet()) {
            if ("Player".equals(entry.getKey())) {
                player.load(entry.getValue());
            } else if  ("Game".equals(entry.getKey())){
                player.game = Game.loadGame(entry.getValue());
            } else if  ("Traits".equals(entry.getKey())){
                player.traits = Traits.loadTraits(entry.getValue());
            } else if  ("Stats".equals(entry.getKey())){
                player.stats = Stats.loadStats(entry.getValue());
            } else if  ("SpellBook".equals(entry.getKey())){
                player.spellbook = SpellBook.loadSpellBook(entry.getValue());
            } else if  ("Equips".equals(entry.getKey())){
                player.equips = Equips.loadEquips(entry.getValue());
            } else if  ("Inventory".equals(entry.getKey())){
                player.inventory = Inventory.loadInventory(entry.getValue());
            } else if  ("Plot".equals(entry.getKey())){
                player.plots = QuestList.loadQuestList(entry.getValue());
            } else if  ("Quests".equals(entry.getKey())){
                player.quests = QuestList.loadQuestList(entry.getValue());
            }
        }

        return player;
    }

    private List<String> save() {
        List<String> result = new ArrayList<String>();

        result.add("playerId" + Vfs.EQ + playerId);
        result.add("currentTask" + Vfs.EQ + currentTask);
        result.add("currentTaskTime" + Vfs.EQ + currentTaskTime);
        result.add("expProgressCurrent" + Vfs.EQ + expProgress.getCurrent());
        result.add("expProgressMax" + Vfs.EQ + expProgress.getMax());
        result.add("plotProgressCurrent" + Vfs.EQ + plotProgress.getCurrent());
        result.add("plotProgressMax" + Vfs.EQ + plotProgress.getMax());
        result.add("questProgressCurrent" + Vfs.EQ + questProgress.getCurrent());
        result.add("questProgressMax" + Vfs.EQ + questProgress.getMax());
        result.add("encumProgressCurrent" + Vfs.EQ + encumProgress.getCurrent());
        result.add("encumProgressMax" + Vfs.EQ + encumProgress.getMax());

        return result;
    }

    private List<String> saveAnnotation() {
        List<String> result = new ArrayList<String>();

        result.add("playerId" + Vfs.EQ + playerId);
        result.add("status1" + Vfs.EQ + UiUtils.getStatus1(this));
        result.add("status2" + Vfs.EQ + UiUtils.getStatus2(this));
        result.add("status3" + Vfs.EQ + UiUtils.getStatus3(this));
        result.add("name" + Vfs.EQ + traits.getName());

        return result;
    }

    private void load(List<String> strings) {
        int expProgressCurrent = 0;
        int expProgressMax = 0;
        int plotProgressCurrent = 0;
        int plotProgressMax = 0;
        int questProgressCurrent = 0;
        int questProgressMax = 0;
        int encumProgressCurrent = 0;
        int encumProgressMax = 0;

        for (String s : strings) {
            String entry[] = s.split(Vfs.EQ);
            if ("currentTask".equals(entry[0])) {
                currentTask = entry[1];
            } else if ("currentTaskTime".equals(entry[0])) {
                currentTaskTime = Integer.decode(entry[1]);
            } else if ("expProgressCurrent".equals(entry[0])) {
                expProgressCurrent = Integer.decode(entry[1]);
            } else if ("expProgressMax".equals(entry[0])) {
                expProgressMax = Integer.decode(entry[1]);
            } else if ("plotProgressCurrent".equals(entry[0])) {
                plotProgressCurrent = Integer.decode(entry[1]);
            } else if ("plotProgressMax".equals(entry[0])) {
                plotProgressMax = Integer.decode(entry[1]);
            } else if ("questProgressCurrent".equals(entry[0])) {
                questProgressCurrent = Integer.decode(entry[1]);
            } else if ("questProgressMax".equals(entry[0])) {
                questProgressMax = Integer.decode(entry[1]);
            } else if ("encumProgressCurrent".equals(entry[0])) {
                encumProgressCurrent = Integer.decode(entry[1]);
            } else if ("encumProgressMax".equals(entry[0])) {
                encumProgressMax = Integer.decode(entry[1]);
            } else if ("playerId".equals(entry[0])) {
                playerId = entry[1];
            }
        }

        expProgress = new ProgressCounter(0);
        plotProgress = new ProgressCounter(0);
        questProgress = new ProgressCounter(0);
        encumProgress = new ProgressCounter(0);

        expProgress.reset(expProgressMax, expProgressCurrent);
        plotProgress.reset(plotProgressMax, plotProgressCurrent);
        questProgress.reset(questProgressMax, questProgressCurrent);
        encumProgress.reset(encumProgressMax, encumProgressCurrent);
    }
}
