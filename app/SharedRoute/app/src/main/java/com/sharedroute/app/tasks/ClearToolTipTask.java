package com.sharedroute.app.tasks;

import android.view.View;
import com.sharedroute.app.MainMapActivity;
import com.sharedroute.app.views.TooltipView;

import java.util.TimerTask;

/**
* Created by cohid01 on 05/03/2015.
*/
public class ClearToolTipTask extends TimerTask {
    private final MainMapActivity mainMapActivity;

    public ClearToolTipTask(MainMapActivity mainMapActivity) {
        this.mainMapActivity = mainMapActivity;
    }

    @Override
    public void run() {
        mainMapActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TooltipView tooltipView = mainMapActivity.getTooltipView();
                tooltipView.setVisibility(View.INVISIBLE);
            }
        });
    }
}
