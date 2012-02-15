package com.github.pkunk.progressquest.ui.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.pkunk.progressquest.ui.util.UiUtils;

/**
 * User: pkunk
 * Date: 2012-02-09
 */
public class StatView extends RelativeLayout {

    TextView spacer;
    TextView label;
    TextView value;

    public StatView(Context context) {
        this(context, null);
    }

    public StatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(8,8,8,8);
        setLayoutParams(params);
        this.setPadding(8,8,8,8);

        populateView(context);
    }

    private void populateView(Context context) {
        spacer = new TextView(context);
        spacer.setId(UiUtils.generateViewId());
        LayoutParams spacerParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        spacerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        spacer.setLayoutParams(spacerParams);
        addView(spacer);

        label = new TextView(context);
        LayoutParams labelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.addRule(RelativeLayout.LEFT_OF, spacer.getId());
        label.setLayoutParams(labelParams);
        label.setTypeface(Typeface.MONOSPACE);
        addView(label);

        value = new TextView(context);
        LayoutParams valueParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        valueParams.addRule(RelativeLayout.RIGHT_OF, spacer.getId());
        value.setLayoutParams(valueParams);
        value.setTypeface(Typeface.MONOSPACE);
        addView(value);
    }

    public void setTextColor(int color) {
        label.setTextColor(color);
        value.setTextColor(color);
    }

    public void setLabelText(String text) {
        label.setText(text);
    }

    public void setValueText(String text) {
        value.setText(text);
    }

    public void setValueColor(int color) {
        value.setBackgroundColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        spacer.setWidth(this.getMeasuredWidth() / 4);
    }
}
