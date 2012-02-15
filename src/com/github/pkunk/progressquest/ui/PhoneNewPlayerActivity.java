package com.github.pkunk.progressquest.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.github.pkunk.progressquest.R;
import com.github.pkunk.progressquest.gameplay.Player;
import com.github.pkunk.progressquest.gameplay.RaceClass;
import com.github.pkunk.progressquest.gameplay.Stats;
import com.github.pkunk.progressquest.gameplay.World;
import com.github.pkunk.progressquest.init.Res;
import com.github.pkunk.progressquest.ui.util.UiUtils;
import com.github.pkunk.progressquest.ui.view.StatView;
import com.github.pkunk.progressquest.util.PqUtils;
import com.github.pkunk.progressquest.util.Vfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: pkunk
 * Date: 2012-02-05
 */
public class PhoneNewPlayerActivity extends Activity {

    private static final int VIEWS_NUM = 4;

    private static final int[] TOTAL_COLORS = new int[] {Color.GRAY, UiUtils.SILVER, Color.WHITE, Color.YELLOW, Color.RED};

    private int currentView;
    private NewPlayerModel m;
    private List<StatView> statViewList;
    private List<View> stepViewList;

    private EditText nameText;
    private Button randomNameButton;

    private RadioGroup raceGroup;
    private RadioGroup roleGroup;
    
    private ViewGroup rollGroup;
    private StatView totalStatView;
    private Button rollButton;
    private Button unrollButton;

    private Button nextButton;
    private Button backButton;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ph_newplayer);
        
        m = new NewPlayerModel();
        currentView = 0;
        statViewList = new ArrayList<StatView>(Stats.BASE_STATS_NUM);
        
        populateView();
    }

    private void createNewPlayer() {
        Player player = m.generatePlayer();
        try {
            Vfs.writeToFile(this, player.getPlayerId() + Vfs.ZIP_EXT, player.savePlayer());
            Vfs.setPlayerId(this, player.getPlayerId());
            Intent intent = new Intent(this, PhoneGameplayActivity.class);
            startActivity(intent);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finish();
    }

    private void populateView() {
        initView();

        RadioButtonRaceListener raceListener = new RadioButtonRaceListener();
        List<View> raceViewList = new ArrayList<View>(Res.RACES.size());
        for (RaceClass race : Res.RACES) {
            RadioButton radioButton = UiUtils.getRadioButton(this, race.getName());
            m.addRace(race);
            radioButton.setOnClickListener(raceListener);
            raceGroup.addView(radioButton);
            raceViewList.add(radioButton);
        }

        RadioButtonRoleListener roleListener = new RadioButtonRoleListener();
        List<View> roleViewList = new ArrayList<View>(Res.KLASSES.size());
        for (RaceClass role : Res.KLASSES) {
            RadioButton radioButton = UiUtils.getRadioButton(this, role.getName());
            m.addRole(role);
            radioButton.setOnClickListener(roleListener);
            roleGroup.addView(radioButton);
            roleViewList.add(radioButton);
        }


        for (int i=0; i<Stats.BASE_STATS_NUM; i++) {
            StatView statView = new StatView(this);
            statView.setLabelText(Stats.label[i]);
            statViewList.add(statView);
            rollGroup.addView(statView);
        }

        totalStatView.setLabelText("Total");

        setListeners();

        generateRandomPlayer(raceViewList, roleViewList);
    }
    
    private void initView() {
        stepViewList = new ArrayList<View>(VIEWS_NUM);

        View nameView = findViewById(R.id.ph_np_name_view);
        stepViewList.add(nameView);

        View raceView = findViewById(R.id.ph_np_race_view);
        stepViewList.add(raceView);

        View roleView = findViewById(R.id.ph_np_role_view);
        stepViewList.add(roleView);

        View rollView = findViewById(R.id.ph_np_roll_view);
        stepViewList.add(rollView);

        nextButton = (Button)findViewById(R.id.ph_np_next);
        backButton = (Button)findViewById(R.id.ph_np_back);

        updateSteps();

        nameText = (EditText)findViewById(R.id.ph_np_name_field);
        randomNameButton = (Button)findViewById(R.id.ph_np_random_name);

        raceGroup = (RadioGroup)findViewById(R.id.ph_np_race_group);
        roleGroup = (RadioGroup)findViewById(R.id.ph_np_role_group);
                
        rollGroup = (ViewGroup)findViewById(R.id.ph_np_roll_group);

        totalStatView = (StatView)findViewById(R.id.ph_np_total);
        rollButton = (Button)findViewById(R.id.ph_np_roll);
        unrollButton  = (Button)findViewById(R.id.ph_np_unroll);
    }

    private void setListeners() {
        nameText.setOnFocusChangeListener(new TextNameListener());
        randomNameButton.setOnClickListener(new ButtonRandomNameListener());

        rollButton.setOnClickListener(new ButtonRollListener());
        unrollButton.setOnClickListener(new ButtonUnrollListener());

        nextButton.setOnClickListener(new ButtonNextListener());
        backButton.setOnClickListener(new ButtonBackListener());
    }

    private void generateRandomPlayer(List<View> raceViewList, List<View> roleViewList) {
        randomName();

        raceViewList.get(PqUtils.random(Res.RACES.size())).performClick();
        roleViewList.get(PqUtils.random(Res.KLASSES.size())).performClick();

        m.rollStats();
        updateStats();
    }

    private void randomName() {
        nameText.setText(World.generateName());
        setName();
    }
    
    private void setName() {
        m.setName(nameText.getText().toString());
    }
    
    private void updateStats() {
        unrollButton.setEnabled(m.hasOldRolls());
        
        String[] stats = m.getStats();
        for (int i=0; i<Stats.BASE_STATS_NUM; i++) {
            StatView statView = statViewList.get(i);
            statView.setValueText(stats[i]);
        }
        totalStatView.setValueText(m.totalStats());
        totalStatView.setValueColor(TOTAL_COLORS[m.totalColor()]);
    }
    
    private void updateSteps() {
        for (int i=0; i<VIEWS_NUM; i++) {
            if (currentView == i) {
                stepViewList.get(i).setVisibility(View.VISIBLE);
            } else {
                stepViewList.get(i).setVisibility(View.GONE);
            }
        }

        backButton.setText("      Back      ");

        if (currentView == 0) {
            backButton.setVisibility(View.INVISIBLE);
        } else {
            backButton.setVisibility(View.VISIBLE);
        }

        if (currentView == VIEWS_NUM - 1) {
            nextButton.setText("      Sold!      ");
        } else {
            nextButton.setText("      Next      ");
        }
    }

    private class TextNameListener implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            setName();
        }
    }

    private class ButtonRandomNameListener implements OnClickListener {
        public void onClick(View v) {
            randomName();
        }
    }

    private class RadioButtonRaceListener implements OnClickListener {
        public void onClick(View v) {
            RadioButton rb = (RadioButton) v;
            m.setRace(rb.getText().toString());
        }
    }

    private class RadioButtonRoleListener implements OnClickListener {
        public void onClick(View v) {
            RadioButton rb = (RadioButton) v;
            m.setRole(rb.getText().toString());
        }
    }

    private class ButtonRollListener implements OnClickListener {
        public void onClick(View v) {
            m.rollStats();
            updateStats();
        }
    }

    private class ButtonUnrollListener implements OnClickListener {
        public void onClick(View v) {
            m.unrollStats();
            updateStats();
        }
    }

    private class ButtonNextListener implements OnClickListener {
        public void onClick(View v) {
            if (currentView == VIEWS_NUM - 1) {
                createNewPlayer();
            } else {
                currentView += 1;
                updateSteps();
            }
        }
    }

    private class ButtonBackListener implements OnClickListener {
        public void onClick(View v) {
            currentView -= 1;
            updateSteps();
        }
    }

}
