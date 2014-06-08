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

import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

public class NewMessageAnimation extends CustomAnimation {

    private float speed;
    private WindowManager manager;
    private AnimationView view;
    private int step;
    private boolean started;

    public NewMessageAnimation(AnimationView v, float speed, WindowManager manager) {
        super(v);
        this.view = v;
        this.speed = speed;
        this.manager = manager;
        this.step = 0;
        this.started = false;
    }

    @Override
    public void updateView() {
        if (!started) {
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
            started = true;
        }

        if (step == 0) {
            view.circleLength -= (speed * 2);

            if (view.circleLength <= view.maxCircleLength) {
                step++;
            }
        } else if (step == 1) {
            view.arcOffset -= speed;

            float nextText;

            if (view.firstText) {
                nextText = (-1 * view.textPaint.measureText(view.name[0]));
            } else {
                nextText = (-1 * view.textPaint.measureText(view.name[1]));
            }

            if (view.firstText && view.arcOffset <= nextText) {
                view.firstText = false;
                view.arcOffset = AnimationView.ORIG_ARC_OFFSET;
            } else if (!view.firstText && view.arcOffset <= nextText) {
                step++;
            }
        } else if (step == 2) {
            view.circleLength += (speed * 2);
            view.circleStart -= (speed * 2);

            if (view.circleLength >= 0) {
                step++;
            }
        } else {
            final NewMessageAnimation anim = this;

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    anim.setRunning(false);
                    view.circleText = false;
                    try {
                        manager.removeViewImmediate(view);
                    } catch (Exception e) {

                    }
                }
            });
        }
    }
}
