public class Queue<T> {

    // A simple queue class implemented as a resizing array.

    private T[] queue;
    private int head;
    private int tail;

    public Queue() {
        this(8);
    }

    public Queue(int size) {
        this.queue = (T[]) new Object[size];
        this.head = 0;
        this.tail = 0;
    }

    public boolean isEmpty() {
        return this.head == this.tail;
    }

    public int size() {
        return this.tail - this.head;
    }

    public int cap() {
        return this.queue.length;
    }

    private boolean isFull() {
        return this.size() == this.queue.length;
    }

    public T head() {
        assert !this.isEmpty();
        return this.queue[this.head % this.queue.length];
    }

    public T tail() {
        assert !this.isEmpty();
        return this.queue[this.tail % this.queue.length];
    }

    public void enqueue(T item) {
        ensureCapacity();
        this.queue[this.tail++ % this.queue.length] = item;
    }

    public T dequeue() {
        assert !this.isEmpty();
        return this.queue[this.head++ % this.queue.length];
    }

    private void ensureCapacity() {
        if (this.isFull()) {
            int k = 0;
            T[] old = this.queue;
            this.queue = (T[]) new Object[2 * this.queue.length];
            for (int i = this.head; i <= this.tail; i++) {
                this.queue[k++] = old[i % old.length];
            }
            this.head = 0;
            this.tail = k-1;
        }
    }
}
