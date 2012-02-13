package com.github.pkunk.progressquest.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.github.pkunk.progressquest.R;
import com.github.pkunk.progressquest.ui.view.RosterEntryView;
import com.github.pkunk.progressquest.util.Vfs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * User: pkunk
 * Date: 2012-02-12
 */
public class PhoneRosterActivity extends Activity {

    private Map<View, String> saveFilesMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ph_roster);

        String[] saveFiles = Vfs.getSaveFiles(this);
        Arrays.sort(saveFiles);

        Map<String, List<String>> statusMap = Vfs.readEntryFromFiles(this, saveFiles, "Annotation");
        ViewGroup rosterGroup = (ViewGroup) findViewById(R.id.ph_roster_saves);

        saveFilesMap = new HashMap<View, String>(saveFiles.length);

        for (String file : saveFiles) {
            RosterEntryView entryView = new RosterEntryView(this);
            String[] status = getStatus(statusMap.get(file));
            entryView.setText(status[1], status[2], status[3]);
            saveFilesMap.put(entryView, status[0]);
            entryView.setOnClickListener(new RosterListener());
            rosterGroup.addView(entryView);
        }
    }


    private String[] getStatus(List<String> strings) {
        String[] result = new String[4];
        for (String s : strings) {
            String entry[] = s.split(Vfs.EQ);
            if ("playerId".equals(entry[0])) {
                result[0] = entry[1];
            } else if ("status1".equals(entry[0])) {
                result[1] = entry[1];
            } else if ("status2".equals(entry[0])) {
                result[2] = entry[1];
            } else if ("status3".equals(entry[0])) {
                result[3] = entry[1];
            }
        }
        return result;
    }

    private void selectPlayer(String playerId) {
        SharedPreferences settings = getSharedPreferences(Vfs.SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("playerId", playerId);
        editor.commit();

        Intent intent = new Intent(this, PhoneGameplayActivity.class);
        startActivity(intent);

        finish();
    }

    private class RosterListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            selectPlayer(saveFilesMap.get(v));
        }
    }
}
