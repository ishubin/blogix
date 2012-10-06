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
        List<T> newList = createList();
        
        Iterator<T> it = iterator();
        while(it.hasNext() && (amount--)>0) {
            newList.add(it.next());
        }
        return new EntryList<T>(newList);
    }
    
    private List<T> copyList() {
        List<T> newList = new ArrayList<T>(this.list);
        return newList;
    }

    private List<T> createList() {
        return createList(0);
    }

    private List<T> createList(int numberOfItems) {
        return new ArrayList<T>(numberOfItems);
    }

    public EntryList<T> page(int page, int itemsOnPage) {
        List<T> newList = createList();
        
        int indexStart = (page - 1) * itemsOnPage;
        int limit = Math.min(indexStart + itemsOnPage, size());
        for (int i = indexStart; i < limit; i++) {
            newList.add(this.list.get(i));
        }
        return new EntryList<T>(newList);
    }
}
