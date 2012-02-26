package com.github.pkunk.pq.gameplay;

import com.github.pkunk.pq.util.Vfs;

/**
 * User: pkunk
 * Date: 2012-01-04
 */
public final class Task {
    private final TaskType taskType;
    private final Monster monster;

    public static Task emptyTask () {
        return new Task(TaskType.EMPTY, null);
    }

    public static Task killTask (Monster monster) {
        return new Task(TaskType.KILL, monster);
    }

    public static Task buyingTask () {
        return new Task(TaskType.BUYING, null);
    }

    public static Task marketTask () {
        return new Task(TaskType.MARKET, null);
    }

    public static Task sellTask () {
        return new Task(TaskType.SELL, null);
    }

    public static Task headingTask () {
        return new Task(TaskType.HEADING, null);
    }

    private Task(TaskType taskType, Monster monster) {
        if (taskType == null) {
            throw new IllegalArgumentException("TaskType cannot be null");
        }
        if (taskType == TaskType.KILL && monster == null) {
            throw new IllegalArgumentException("You cannot kill null");
        }
        
        this.taskType = taskType;
        this.monster = taskType == TaskType.KILL ? monster : null;
    }

    public boolean isKill() {
        return taskType == TaskType.KILL;
    }

    public boolean isBuying() {
        return taskType == TaskType.BUYING;
    }

    public boolean isMarket() {
        return taskType == TaskType.MARKET;
    }

    public boolean isSell() {
        return taskType == TaskType.SELL;
    }

    public boolean isHeading() {
        return taskType == TaskType.HEADING;
    }

    public Monster getMonster() {
        return monster;
    }

    public String saveTask() {
        String result = taskType.toString();
        if (monster != null) {
            result += Vfs.SEPARATOR + monster.saveMonster();
        }
        return result;
    }

    public static Task loadTask(String string) {
        String[] strings = string.split(Vfs.SEPARATOR);
        TaskType type = TaskType.valueOf(strings[0]);
        if (type == TaskType.KILL) {
            String name = strings[1];
            int level = Integer.valueOf(strings[2]);
            String loot = strings[3];
            Monster monster = new Monster(name, level, loot);
            return new Task(type, monster);
        } else {
            return new Task(type, null);
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + taskType.hashCode();
        result = 31 * result + (monster == null ? 0 : monster.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task) {
            return taskType.equals(((Task) obj).taskType)
             && (monster == null ? ((Task) obj).monster == null
                                 : monster.equals(((Task) obj).monster));
        }
        return false;
    }
}
