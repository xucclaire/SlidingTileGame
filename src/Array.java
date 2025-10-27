import java.lang.Iterable;
import java.util.Iterator;
import java.util.Arrays;
/*
public class Array<T> implements Iterable<T> {

    // Very simple resizing array class that supports iteration

    private T[] items;

    public Array() {
        this(8);
    }

    public Array(int size) {
        this.items = (T[]) new Object[size];
    }

    public Array(T[] items) {
        this.items = Arrays.copyOf(items, items.length);
    }

    public T get(int index) {
        if (index < items.length) {
            return this.items[index];
        } else {
            return null;
        }
    }

    public void set(int index, T value) {
        ensureCapacity(index);
        this.items[index] = value;
    }

    public int size() {
        return this.items.length;
    }

    private void ensureCapacity(int index) {
        int size = this.items.length;
        while (size <= index) size *= 2;
        this.items = Arrays.copyOf(this.items, size);
    }

    public Iterator<T> iterator() {
        return this.new ArrayIterator();
    }

    public class ArrayIterator implements Iterator<T> {

        private int current = 0;

        @Override
        public boolean hasNext() {
            return this.current < Array.this.items.length;
        }

        @Override
        public T next() {
            return Array.this.items[this.current++];
        }
    }
}*/
