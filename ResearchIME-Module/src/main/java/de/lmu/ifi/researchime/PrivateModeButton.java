/*
 * Copyright (C) 2016 - 2018 ResearchIME Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lmu.ifi.researchime;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;

class PrivateModeButton {
    private ImageButton button;
    private Drawable drawableActive;
    private Drawable drawableInactive;

    public PrivateModeButton(ImageButton button, Drawable active, Drawable inactive) {
        this.button = button;
        this.drawableActive = active;
        this.drawableInactive = inactive;
    }

    public void setActive(boolean active) {
        button.setImageDrawable(active ? drawableActive : drawableInactive);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        button.setOnClickListener(onClickListener);
    }

    public void setVisible(boolean isVisible){
        button.setVisibility(isVisible ? View.VISIBLE: View.GONE);
    }
}
