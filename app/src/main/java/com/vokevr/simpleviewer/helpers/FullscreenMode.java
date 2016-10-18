package com.vokevr.simpleviewer.helpers;

import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.Window;

public class FullscreenMode {
    private static final int NAVIGATION_BAR_TIMEOUT_MS = 2000;
    private final Window window;

    public FullscreenMode(Window window) {
        this.window = window;
    }

    public void goFullscreen() {
        this.setFullscreenModeFlags();
        this.setImmersiveStickyModeCompat();
    }

    private void setImmersiveStickyModeCompat() {
        if(Build.VERSION.SDK_INT < 19) {
            final Handler handler = new Handler();
            this.window.getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                public void onSystemUiVisibilityChange(int visibility) {
                    if((visibility & 2) == 0) {
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                FullscreenMode.this.setFullscreenModeFlags();
                            }
                        }, 2000L);
                    }

                }
            });
        }

    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus) {
            this.setFullscreenModeFlags();
        }

    }

    private void setFullscreenModeFlags() {
        this.window.getDecorView().setSystemUiVisibility(5894);
    }
}