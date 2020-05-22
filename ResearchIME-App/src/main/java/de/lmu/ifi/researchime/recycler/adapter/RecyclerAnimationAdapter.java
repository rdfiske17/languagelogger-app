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

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import java.util.HashSet;
import java.util.Set;

public abstract class RecyclerAnimationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;
    private boolean isFirstOnly = true;
    private boolean isAnimationEnabled = true;

    private Set<Integer> animatedPositions;

    public RecyclerAnimationAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        this.adapter = adapter;
        this.animatedPositions = new HashSet<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        adapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        adapter.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        adapter.onBindViewHolder(holder, position);
        int adapterPosition = holder.getAdapterPosition();
        if (isAnimationEnabled && (!isFirstOnly || !containsPositionAnimation(adapterPosition))) {
            setPositionAnimated(adapterPosition);
            getAnimator(holder.itemView).start();
        } else {
            stopAnimation(holder);
        }
    }

    private ObjectAnimator getAnimator(View target) {
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, getAnimationProperties(target.getContext()));
        animator.setDuration(800);
        animator.setInterpolator(new DecelerateInterpolator());
        return animator;
    }

    protected abstract PropertyValuesHolder[] getAnimationProperties(Context context);

    private void resetAnimation(View view) {
        view.setScaleY(1);
        view.setScaleX(1);
        view.setTranslationY(0);
    }

    private boolean containsPositionAnimation(int pos) {
        return animatedPositions.contains(pos);
    }

    private void setPositionAnimated(int pos) {
        animatedPositions.add(pos);
    }

    private void stopAnimation(RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView).setInterpolator(null).setStartDelay(0);
        resetAnimation(holder.itemView);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        adapter.onViewAttachedToWindow(holder);
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        adapter.onViewDetachedFromWindow(holder);
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        adapter.onViewRecycled(holder);
        super.onViewRecycled(holder);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        adapter.onDetachedFromRecyclerView(recyclerView);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return adapter.onFailedToRecycleView(holder);
    }

    @Override
    public int getItemCount() {
        return adapter.getItemCount();
    }

    public void clearAnimatedPositions() {
        animatedPositions.clear();
    }

    public void setFirstOnly(boolean firstOnly) {
        isFirstOnly = firstOnly;
    }

    @Override
    public int getItemViewType(int position) {
        return adapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return adapter.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        adapter.setHasStableIds(hasStableIds);
    }

    public void setAnimationEnabled(boolean isEnabled) {
        this.isAnimationEnabled = isEnabled;
    }
}
