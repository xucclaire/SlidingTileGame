/*

public class Stack<T> {

    // A simple stack class implemented as a resizing array.

    private Array<T> stack;
    private int sp;

    public Stack() {
        this(8);
    }

    public Stack(int size) {
        this.stack = new Array<>(size);
        this.sp = 0;
    }

    public boolean isEmpty() {
        return this.sp == 0;
    }

    public int size() {
        return this.sp;
    }

    public T top() {
        assert !this.isEmpty();
        return this.stack.get(this.sp-1);
    }

    public void push(T item) {
        this.stack.set(this.sp++, item);
    }

    public T pop() {
        assert !this.isEmpty();
        return this.stack.get(--this.sp);
    }
}*/
