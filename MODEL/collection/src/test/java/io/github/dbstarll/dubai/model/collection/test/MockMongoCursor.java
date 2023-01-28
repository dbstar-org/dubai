package io.github.dbstarll.dubai.model.collection.test;

import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.client.MongoCursor;

import java.util.Iterator;

public class MockMongoCursor<T> implements MongoCursor<T> {
    private final Iterator<T> it;

    public MockMongoCursor(Iterator<T> it) {
        this.it = it;
    }

    @Override
    public void remove() {
        it.remove();
    }

    @Override
    public void close() {

    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public T next() {
        return it.next();
    }

    @Override
    public T tryNext() {
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public ServerCursor getServerCursor() {
        return null;
    }

    @Override
    public ServerAddress getServerAddress() {
        return null;
    }

    @Override
    public int available() {
        return it.hasNext() ? 1 : 0;
    }
}
