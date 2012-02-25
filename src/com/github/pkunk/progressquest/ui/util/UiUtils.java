package com.github.pkunk.progressquest.ui.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import com.github.pkunk.progressquest.R;
import com.github.pkunk.progressquest.gameplay.Player;
import com.github.pkunk.progressquest.gameplay.Traits;
import com.github.pkunk.progressquest.ui.view.TextProgressBar;
import com.github.pkunk.progressquest.util.PqUtils;

/**
 * User: pkunk
 * Date: 2012-01-28
 */
public class UiUtils {

    public static final int SILVER = 0xFFE7E7E7;

    public static TableRow getHeaderRow(Context context, String... titles) {
        TableRow row = new TableRow(context);
        for (String title : titles) {
            TextView textView = new TextView(row.getContext());
            textView.setText(title);
            row.addView(textView);
        }
        row.setBackgroundColor(0xFFCCCCCC);
        return row;
    }

    public static TableRow getTableRow(Context context, String... text) {
        TableRow row = new TableRow(context);
        for (String title : text) {
            TextView textView = new TextView(row.getContext());
            textView.setText(" " + title);
            row.addView(textView);
        }
        return row;
    }

    public static TableRow getCheckedRow(Context context, boolean checked, String text) {
        TableRow row = new TableRow(context);

        CheckBox checkBox = new CheckBox(context);
        checkBox.setButtonDrawable(R.drawable.pq_checkbox);
        checkBox.setClickable(false);
        checkBox.setChecked(checked);
        row.addView(checkBox);

        TextView textView = new TextView(row.getContext());
        textView.setText(text);
        row.addView(textView);

        return row;
    }

    public static RadioButton getRadioButton(Context context, String text) {
        RadioButton radioButton = new RadioButton(context);
        radioButton.setButtonDrawable(R.drawable.pq_radio);
        radioButton.setText(text);
        radioButton.setTextColor(Color.BLACK);
        return radioButton;
    }

    public static void updateTextProgressBar(Activity activity, int resId, int current, int max, String text) {
        TextProgressBar bar = (TextProgressBar) activity.findViewById(resId);
        bar.setMax(max);
        bar.setProgress(current);
        bar.setText(text);
    }

    public static String getStatus1(Player player) {
        if (player == null) {
            return "Progress Quest";
        }
        Traits traits = player.getTraits();
        StringBuilder statusText = new StringBuilder();
        statusText.append(traits.getName()).append(" the ").append(traits.getRace());
        statusText.append(" (").append(player.getBestPlot()).append(")");
        return statusText.toString();
    }

    public static String getStatus2(Player player) {
        if (player == null) {
            return "Click to start";
        }
        Traits traits = player.getTraits();
        StringBuilder statusText = new StringBuilder();
        statusText.append("Level ").append(traits.getLevel()).append(" ").append(traits.getRole());
        return statusText.toString();
    }

    public static String getStatus3(Player player) {
        if (player == null) {
            return "";
        }
        StringBuilder statusText = new StringBuilder();
        statusText.append(player.getBestEquip()).append(" / ");
        if (player.getBestSpell().length() > 0) {
            statusText.append(player.getBestSpell()).append(" / ");
        }
        statusText.append(player.getBestStat());
        return statusText.toString();
    }

    public static Dialog aboutDialog(Context context) {
        Dialog dialog = new Dialog(context, R.style.About);
        dialog.setContentView(R.layout.about);
        return dialog;
    }

    public static int generateViewId() {
        return PqUtils.random(Integer.MAX_VALUE);
    }

}
