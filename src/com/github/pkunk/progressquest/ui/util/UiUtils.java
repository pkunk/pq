package com.github.pkunk.progressquest.ui.util;

import android.app.Activity;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;
import com.github.pkunk.progressquest.ui.view.TextProgressBar;

/**
 * User: pkunk
 * Date: 2012-01-28
 */
public class UiUtils {

    public static TableRow headerRow(Context context, String... titles) {
        TableRow row = new TableRow(context);
        for (String title : titles) {
            TextView textView = new TextView(row.getContext());
            textView.setText(title);
            row.addView(textView);
        }
        return row;
    }

    public static TableRow tableRow(Context context, String... text) {
        TableRow row = new TableRow(context);
        for (String title : text) {
            TextView textView = new TextView(row.getContext());
            textView.setText(title);
            row.addView(textView);
        }
        return row;
    }

    public static TableRow checkedRow(Context context, boolean  checked, String text) {
        TableRow row = new TableRow(context);

        CheckBox checkBox = new CheckBox(context);
        checkBox.setClickable(false);
        checkBox.setChecked(checked);
        row.addView(checkBox);

        TextView textView = new TextView(row.getContext());
        textView.setText(text);
        row.addView(textView);

        return row;
    }

    public static void updateTextProgressBar(Activity activity, int resId, int current, int max, String text) {
        TextProgressBar bar = (TextProgressBar) activity.findViewById(resId);
        bar.setMax(max);
        bar.setProgress(current);
        bar.setText(text);
    }

}
