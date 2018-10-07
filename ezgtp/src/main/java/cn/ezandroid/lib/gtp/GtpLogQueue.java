package cn.ezandroid.lib.gtp;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 固定长度队列
 *
 * @author gary
 */
public class GtpLogQueue<E> implements Queue<E> {

    // 队列长度
    private int mLimit;

    private Queue<E> mQueue = new LinkedList<E>();

    public GtpLogQueue(int limit) {
        this.mLimit = limit;
    }

    /**
     * 入队
     *
     * @param e
     */
    @Override
    public boolean offer(E e) {
        if (mQueue.size() >= mLimit) {
            //如果超出长度,入队时,先出队
            mQueue.poll();
        }
        return mQueue.offer(e);
    }

    /**
     * 出队
     *
     * @return
     */
    @Override
    public E poll() {
        return mQueue.poll();
    }

    /**
     * 获取队列
     *
     * @return
     */
    public Queue<E> getQueue() {
        return mQueue;
    }

    /**
     * 获取限制大小
     *
     * @return
     */
    public int getLimit() {
        return mLimit;
    }

    @Override
    public boolean add(E e) {
        return mQueue.add(e);
    }

    @Override
    public E element() {
        return mQueue.element();
    }

    @Override
    public E peek() {
        return mQueue.peek();
    }

    @Override
    public boolean isEmpty() {
        return mQueue.size() == 0;
    }

    @Override
    public int size() {
        return mQueue.size();
    }

    @Override
    public E remove() {
        return mQueue.remove();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return mQueue.addAll(c);
    }

    @Override
    public void clear() {
        mQueue.clear();
    }

    @Override
    public boolean contains(Object o) {
        return mQueue.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return mQueue.containsAll(c);
    }

    @Override
    public Iterator<E> iterator() {
        return mQueue.iterator();
    }

    @Override
    public boolean remove(Object o) {
        return mQueue.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return mQueue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return mQueue.retainAll(c);
    }

    @Override
    public Object[] toArray() {
        return mQueue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return mQueue.toArray(a);
    }
}
