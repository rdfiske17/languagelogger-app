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

package de.lmu.ifi.researchime.recycler.adapter;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.v7.widget.RecyclerView;

import de.lmu.ifi.researchime.R;

public class ScaleTranslateAnimationAdapter extends RecyclerAnimationAdapter {

    public ScaleTranslateAnimationAdapter(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    protected PropertyValuesHolder[] getAnimationProperties(Context context) {
        int translation = context.getResources().getDimensionPixelSize(R.dimen.scale_animation_y_offset);
        return new PropertyValuesHolder[]{
                PropertyValuesHolder.ofFloat("scaleX", 0.8f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1f),
                PropertyValuesHolder.ofFloat("translationY", translation, 0)};
    }
}
