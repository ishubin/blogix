package net.mindengine.blogix.db.readers;

public interface Reader<T> {
    public T convert(String fileName);
}
