package com.github.pkunk.progressquest.gameplay;

import com.github.pkunk.progressquest.util.Vfs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * User: pkunk
 * Date: 2012-01-04
 */
public class Game {
    Task task;
    int tasks;
    String bestStat;
    String bestSpell;
    String bestEquip;
    int act;
    String bestPlot;
    String bestQuest;
    Queue<PlotTask> plotQueue;
    Monster questMonster;
    int questMonsterIndex;

    private Game() {
    }

    public static Game newGame() {
        Game game = new Game();

        game.task = Task.emptyTask();
        game.tasks = 0;
        game.bestStat = null;
        game.bestSpell = "";
        game.bestEquip = "Sharp Rock";
        game.act = 0;
        game.bestPlot = "Prologue";
        game.bestQuest = "";
        game.plotQueue = new LinkedList<PlotTask>();
        game.questMonster = null;
        game.questMonsterIndex = -1;

        return game;
    }

    public List<String> saveGame() {
        List<String> result = new ArrayList<String>(20);

        result.add("task" + Vfs.EQ + task.saveTask());
        result.add("tasks" + Vfs.EQ + tasks);
        result.add("bestStat" + Vfs.EQ + bestStat);
        result.add("bestSpell" + Vfs.EQ + bestSpell);
        result.add("bestEquip" + Vfs.EQ + bestEquip);
        result.add("act" + Vfs.EQ + act);
        result.add("bestPlot" + Vfs.EQ + bestPlot);
        result.add("bestQuest" + Vfs.EQ + bestQuest);
        result.add("questMonster" + Vfs.EQ + (questMonster == null ? "" : questMonster.toString()));
        result.add("questMonsterIndex" + Vfs.EQ + questMonsterIndex);
        for (PlotTask plotTask : plotQueue) {
            result.add("plotTask" + Vfs.EQ + plotTask.savePlot());
        }
        return result;
    }

    public static Game loadGame(List<String> strings) {
        Game game = new Game();
        game.plotQueue = new LinkedList<PlotTask>();

        for (String s : strings) {
            String entry[] = s.split(Vfs.EQ);
            if ("task".equals(entry[0])) {
                game.task = Task.loadTask(entry[1]);
            } else if ("tasks".equals(entry[0])) {
                game.tasks = Integer.decode(entry[1]);
            } else if ("bestStat".equals(entry[0])) {
                game.bestStat = entry[1];
            } else if ("bestSpell".equals(entry[0])) {
                game.bestSpell = entry[1];
            } else if ("bestEquip".equals(entry[0])) {
                game.bestEquip = entry[1];
            } else if ("act".equals(entry[0])) {
                game.act = Integer.decode(entry[1]);
            } else if ("bestPlot".equals(entry[0])) {
                game.bestPlot = entry[1];
            } else if ("bestQuest".equals(entry[0])) {
                game.bestQuest = entry[1];
            } else if ("questMonster".equals(entry[0])) {
                game.questMonster = Monster.loadMonster(entry[1]);
            } else if ("questMonsterIndex".equals(entry[0])) {
                game.questMonsterIndex = Integer.decode(entry[1]);
            } else if ("plotTask".equals(entry[0])) {
                PlotTask plotTask = PlotTask.loadPlotTask(entry[1]);
                game.plotQueue.add(plotTask);
            }
        }
        return game;
    }

}
