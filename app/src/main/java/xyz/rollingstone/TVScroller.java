package xyz.rollingstone;

import android.graphics.Color;
import android.widget.TextView;

/**
 * Created by Deeprom on 14/10/2558.
 */
public class TVScroller {
    private TextView pastpastTextView;
    private TextView pastTextView;
    private TextView currentTextView;
    private TextView nextTextView;
    private TextView nextnextTextView;

    public TVScroller(TextView pastpastTextView, TextView pastTextView, TextView currentTextView, TextView nextTextView, TextView nextnextTextView) {
        this.pastpastTextView = pastpastTextView;
        this.pastTextView = pastTextView;
        this.currentTextView = currentTextView;
        this.nextTextView = nextTextView;
        this.nextnextTextView = nextnextTextView;
    }


}
