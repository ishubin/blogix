/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.blogix.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class EntryList<T extends Comparable<T>> implements Iterable<T> {
    private List<T> list;

    public EntryList(List<T> list) {
        this.list = list;
    }
    
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
    
    public T get(int index) {
        return this.list.get(index);
    }
    
    public List<T> asJavaList() {
        return this.list;
    }

    public Integer size() {
        return this.list.size();
    }
    
    public EntryList<T> filter(EntryFilter<T> filter) {
        List<T> filteredList = createEmptyList();
        for (T entry : this.list) {
            if (filter.applies(entry)) {
                filteredList.add(entry);
            }
        }
        return new EntryList<T>(filteredList);
    }

    /**
     * Sorts list 
     * @return
     */
    public EntryList<T> sortDesc() {
        List<T> newList = copyList();
        Collections.sort(newList, new Comparator<T>() {
            @Override
            public int compare(T paramT1, T paramT2) {
                return -(paramT1.compareTo(paramT2));
            }
        });
        return new EntryList<T>(newList);
    }

    /**
     * Returns only specified amount of items starting from first 
     * @param amount Amount of items to be given from current list
     * @return
     */
    public EntryList<T> first(int amount) {
        List<T> newList = createEmptyList();
        
        Iterator<T> it = iterator();
        while(it.hasNext() && (amount--)>0) {
            newList.add(it.next());
        }
        return new EntryList<T>(newList);
    }
    
    public EntryList<T> page(int page, int itemsOnPage) {
        List<T> newList = createEmptyList();
        
        int indexStart = (page - 1) * itemsOnPage;
        int limit = Math.min(indexStart + itemsOnPage, size());
        for (int i = indexStart; i < limit; i++) {
            newList.add(this.list.get(i));
        }
        return new EntryList<T>(newList);
    }

    /**
     * Returns count of pages for all found items
     * @param itemsPerPage Amount of items to display in one page
     * @return
     */
    public int pages(int itemsPerPage) {
        int size = size();
        if (size > 0) {
            return (int)Math.floor(((double)size - 1.0)/10.0) + 1;
        }
        else {
            return 0;
        }
    }
    
    private List<T> copyList() {
        List<T> newList = new ArrayList<T>(this.list);
        return newList;
    }

    private List<T> createEmptyList() {
        return createList(0);
    }

    private List<T> createList(int numberOfItems) {
        return new ArrayList<T>(numberOfItems);
    }
}
