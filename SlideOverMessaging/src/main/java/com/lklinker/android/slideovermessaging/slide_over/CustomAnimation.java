/*
 * Copyright 2013 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lklinker.android.slideovermessaging.slide_over;

import android.view.View;

public abstract class CustomAnimation extends Thread {
    public View view;
    private boolean running = false;

    public CustomAnimation(View v) {
        super();
        view = v;
    }

    public void setRunning(boolean run) {
        running = run;
    }

    private final static int MAX_FPS = 120;
    private final static int MAX_FRAME_SKIPS = 5;
    private final static int FRAME_PERIOD = 1000 / MAX_FPS;

    @Override
    public void run() {
        long beginTime;
        long timeDiff;
        int sleepTime;
        int framesSkipped;

        while (running) {
            try {
                beginTime = System.currentTimeMillis();
                framesSkipped = 0;

                updateView();
                view.postInvalidate();

                timeDiff = System.currentTimeMillis() - beginTime;
                sleepTime = (int) (FRAME_PERIOD - timeDiff);

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }

                while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
                    updateView();
                    sleepTime += FRAME_PERIOD;
                    framesSkipped++;
                }
            } finally {
                view.postInvalidate();
            }
        }
    }

    public abstract void updateView();
}