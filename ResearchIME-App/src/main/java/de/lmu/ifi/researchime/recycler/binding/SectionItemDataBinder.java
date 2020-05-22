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

package de.lmu.ifi.researchime.recycler.binding;

import android.support.annotation.LayoutRes;

public class SectionItemDataBinder<T> extends ItemBinderBase<T> {

    private Class itemClass;

    public SectionItemDataBinder(Class itemClass, int bindingVariable, @LayoutRes int layoutId) {
        super(bindingVariable, layoutId);
        this.itemClass = itemClass;
    }

    public boolean canHandle(T superModel) {
        return itemClass.isAssignableFrom(superModel.getClass());
    }
}
