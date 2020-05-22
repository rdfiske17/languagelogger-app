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

import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.lmu.ifi.researchime.recycler.binding.ItemBinderInterface;
import de.lmu.ifi.researchime.recycler.listener.RecyclerClickHandler;
import de.lmu.ifi.researchime.recycler.listener.WeakReferenceOnListChangedCallback;

public class BindingRecyclerViewAdapter<T> extends RecyclerView.Adapter<BindingRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private WeakReferenceOnListChangedCallback onListChangedCallback;
    private final ItemBinderInterface<T> itemBinder;
    private final List<T> items;
    private @Nullable ObservableList<T> observableItems;
    private @Nullable RecyclerClickHandler<T> clickHandler;
//    private @Nullable SparseArray<ViewIdClickHandler<T>> viewIdClickHandlerMap;
    private LayoutInflater inflater;

    public BindingRecyclerViewAdapter(ItemBinderInterface<T> itemBinder, List<T> items) {
        this.itemBinder = itemBinder;
        this.items = items;
    }

    public BindingRecyclerViewAdapter(ItemBinderInterface<T> itemBinder, ObservableList<T> items) {
        this.itemBinder = itemBinder;
        this.items = items;
        this.onListChangedCallback = new WeakReferenceOnListChangedCallback(this);
        this.observableItems = items;
        this.observableItems.addOnListChangedCallback(onListChangedCallback);
    }

    private void removeListChangedCallback() {
        if (observableItems != null) {
            observableItems.removeOnListChangedCallback(onListChangedCallback);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        removeListChangedCallback();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int layoutId) {
        if (inflater == null) {
            inflater = LayoutInflater.from(viewGroup.getContext());
        }

        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutId, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final T item = items.get(position);
        viewHolder.binding.setVariable(itemBinder.getBindingVariable(item), item);
        viewHolder.binding.getRoot().setTag(item);
        viewHolder.binding.getRoot().setOnClickListener(this);
//        handleViewIdListeners(viewHolder.binding.getRoot(), item);
        viewHolder.binding.executePendingBindings();
    }

    @Override
    public int getItemViewType(int position) {
        return itemBinder.getLayoutRes(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onClick(View v) {
        if (clickHandler != null) {
            T item = (T) v.getTag();
            clickHandler.onClick(item, v);
        }
    }

    public void setClickListener(RecyclerClickHandler<T> clickHandler) {
        this.clickHandler = clickHandler;
    }

    /*
    private void handleViewIdListeners(View rootView, final T item){
        if(viewIdClickHandlerMap != null){
            for(int i = 0; i < viewIdClickHandlerMap.size(); i++) {
                int key = viewIdClickHandlerMap.keyAt(i);
                final ViewIdClickHandler<T> handler = viewIdClickHandlerMap.get(key);
                View childView = rootView.findViewById(handler.getViewId());
                if(childView != null){
                    childView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            handler.onClick(item, view);
                        }
                    });
                }
            }
        }
    }

    public void registerViewIdClickListener(ViewIdClickHandler handler){
        if(viewIdClickHandlerMap == null){
            viewIdClickHandlerMap = new SparseArray<>();
        }
        viewIdClickHandlerMap.put(handler.getViewId(), handler);
    }

    public void removeViewIdClickListener(ViewIdClickHandler handler){
        if(viewIdClickHandlerMap != null){
            viewIdClickHandlerMap.remove(handler.getViewId());
        }
    }
    */

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewDataBinding binding;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}