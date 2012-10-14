package net.mindengine.blogix.db;

public interface EntryFilter<T> {

    boolean applies(T entry);
}
