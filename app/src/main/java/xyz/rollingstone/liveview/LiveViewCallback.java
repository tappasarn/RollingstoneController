package xyz.rollingstone.liveview;

import android.graphics.Bitmap;
import android.os.Handler;

public interface LiveViewCallback {

    void setLiveViewData(Bitmap imageData);
    Handler getLiveViewHandler();

}
