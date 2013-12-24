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

public class HaloFadeAnimation extends CustomAnimation {

    private HaloView view;
    private boolean fadeIn;

    public HaloFadeAnimation(HaloView v, boolean fadeIn) {
        super(v);

        this.view = v;
        this.fadeIn = fadeIn;
    }

    @Override
    public void updateView() {

        if (fadeIn) {
            view.haloAlpha -= 2;
            view.haloNewAlpha += 2;
        } else {
            view.haloAlpha += 2;
            view.haloNewAlpha -= 2;
        }

        boolean stop = false;

        if (fadeIn && view.haloAlpha <= 0) {
            view.haloAlpha = 0;
            view.haloNewAlpha = 255;
            stop = true;
        } else if (!fadeIn && view.haloAlpha >= 255) {
            view.haloAlpha = 255;
            view.haloNewAlpha = 0;
            stop = true;
        }

        if (stop) {
            setRunning(false);
            view.animating = false;
        }
    }
}