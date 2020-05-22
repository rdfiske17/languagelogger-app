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

package de.lmu.ifi.researchime.recycler.listener;

import android.databinding.ObservableList;
import android.support.v7.widget.RecyclerView;

import java.lang.ref.WeakReference;

import de.lmu.ifi.researchime.recycler.adapter.BindingRecyclerViewAdapter;

public class WeakReferenceOnListChangedCallback extends ObservableList.OnListChangedCallback {

    private final WeakReference<BindingRecyclerViewAdapter> adapterCallback;

    public WeakReferenceOnListChangedCallback(BindingRecyclerViewAdapter adapterCallback) {
        this.adapterCallback = new WeakReference<>(adapterCallback);
    }

    @Override
    public void onChanged(ObservableList sender) {
        RecyclerView.Adapter adapter = adapterCallback.get();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
        RecyclerView.Adapter adapter = adapterCallback.get();
        if (adapter != null) {
            adapter.notifyItemRangeChanged(positionStart, itemCount);
        }
    }

    @Override
    public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
        RecyclerView.Adapter adapter = adapterCallback.get();
        if (adapter != null) {
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    @Override
    public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
        RecyclerView.Adapter adapter = adapterCallback.get();
        if (adapter != null) {
            for (int i = 0; i < itemCount; i++) {
                adapter.notifyItemMoved(fromPosition + i, toPosition + i);
            }
        }
    }

    @Override
    public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
        RecyclerView.Adapter adapter = adapterCallback.get();
        if (adapter != null) {
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }
    }
}