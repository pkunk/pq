package com.github.pkunk.pq.gameplay;

import com.github.pkunk.pq.util.Vfs;

/**
 * User: pkunk
 * Date: 2012-01-15
 */
public final class PlotTask {
    private final String description;
    private final int time;
    private final boolean isPlot;

    public PlotTask(String description, int time) {
        this(description, time, false);
    }

    public PlotTask(String description, int time, boolean plot) {
        this.description = description;
        this.time = time;
        isPlot = plot;
    }

    public String getDescription() {
        return description;
    }

    public int getTime() {
        return time;
    }

    public boolean isPlot() {
        return isPlot;
    }
    
    public String savePlot() {
        return description + Vfs.SEPARATOR + time + Vfs.SEPARATOR + isPlot;
    }
    
    public static PlotTask loadPlotTask(String string) {
        String[] strings = string.split(Vfs.SEPARATOR);
        String description = strings[0];
        int time = Integer.valueOf(strings[1]);
        boolean isPlot = Boolean.parseBoolean(strings[2]);
        return new PlotTask(description, time, isPlot);
    }
}
