package de.tobbexiv.pruefungsleistung.camera;

import android.os.Handler;
import android.os.HandlerThread;

public class CameraBackgroundThread {
    private Handler         handler;
    private HandlerThread   thread;

    public void start() {
        thread = new HandlerThread("Camera Background Thread");
        thread.start();

        handler = new Handler(thread.getLooper());
    }

    public Handler getHandler() {
        return handler;
    }

    public void stop() {
        if (thread == null) {
            return;
        }

        thread.quitSafely();

        try {
            thread.join();
            thread = null;

            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
