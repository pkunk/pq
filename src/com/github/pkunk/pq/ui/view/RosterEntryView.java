package com.github.pkunk.pq.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.pkunk.pq.R;
import com.github.pkunk.pq.ui.util.UiUtils;

/**
 * User: pkunk
 * Date: 2012-02-12
 */
public class RosterEntryView extends RelativeLayout {
    
    // icons: #2694, #2620

    private ImageView killButton;
    private LinearLayout textLayout;

    private TextView firstLayer;

    private TextView secondLayer;
    private TextView thirdLayer;
    public RosterEntryView(Context context) {
        super(context);
        populateView(context);
    }

    private void populateView(Context context) {
        setBackgroundColor(UiUtils.SILVER);

        float density = getResources().getDisplayMetrics().density;
        int h_margin = (int)(6 * density);
        int v_margin = (int)(4 * density);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(h_margin, v_margin, h_margin, v_margin);
        setLayoutParams(layoutParams);
        setPadding(v_margin, v_margin, v_margin, v_margin);

        killButton = new ImageView(context);
        killButton.setId(UiUtils.generateViewId());
        LayoutParams killParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
        killParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        killParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        killButton.setLayoutParams(killParams);
        killButton.setImageResource(R.drawable.skull);
        addView(killButton);

        textLayout = new LinearLayout(context);
        LayoutParams textLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        textLayoutParams.addRule(RelativeLayout.LEFT_OF, killButton.getId());
        textLayout.setLayoutParams(textLayoutParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        addView(textLayout);


        LayoutParams textParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

        firstLayer = new TextView(context);
        firstLayer.setLayoutParams(textParams);
        firstLayer.setSingleLine();
        firstLayer.setTextColor(Color.BLACK);
        firstLayer.setTextSize(firstLayer.getTextSize() * 1.1f);
        textLayout.addView(firstLayer);

        secondLayer = new TextView(context);
        secondLayer.setLayoutParams(textParams);
        secondLayer.setSingleLine();
        secondLayer.setTextColor(Color.BLACK);
        textLayout.addView(secondLayer);

        thirdLayer = new TextView(context);
        thirdLayer.setLayoutParams(textParams);
        thirdLayer.setSingleLine();
        thirdLayer.setTextColor(Color.BLACK);
        thirdLayer.setTextSize(thirdLayer.getTextSize() * 0.8f);
        textLayout.addView(thirdLayer);
    }

    public void setText(String status1, String status2, String status3) {
        firstLayer.setText(status1);
        secondLayer.setText(status2);
        thirdLayer.setText(status3);
    }

    public View getPlayView() {
        return textLayout;
    }

    public View getKillView() {
        return killButton;
    }

}
