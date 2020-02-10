package arlp.mlcs.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

public class GrowableQueue<T> implements List<T> {
  private transient Object[] datas;
  private AtomicInteger size = new AtomicInteger(0);
  private static int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  public GrowableQueue(int initialCapacity) {
    datas = new Object[initialCapacity];
  }

  public GrowableQueue() {
    this(10);
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public boolean isEmpty() {
    return size.get() == 0;
  }

  @Override
  public boolean add(T e) {
    int index = size.getAndIncrement();
    ensureCapacity(index);
    datas[index] = e;
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    for (T t : c) {
      add(t);
    }
    return true;
  }

  @Override
  public void clear() {
    int len = size.get();
    for (int i = 0; i < len; i++) {
      datas[i] = null;
    }
    size.set(0);
  }

  private void ensureCapacity(int minCapacity) {
    if (minCapacity >= datas.length) {
      synchronized (this) {
        // double check
        if (minCapacity >= datas.length) {
          // overflow-conscious code
          int oldCapacity = datas.length;
          int newCapacity = oldCapacity + (oldCapacity >> 1);
          if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
          if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity);
          // minCapacity is usually close to size, so this is a win:
          datas = Arrays.copyOf(datas, newCapacity);
        }
      }
    }
  }

  private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) // overflow
      throw new OutOfMemoryError();
    return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get(int index) {
    return (T) datas[index];
  }

  @Override
  public Object[] toArray() {
    throw new RuntimeException("not supported");
  }

  @SuppressWarnings("hiding")
  @Override
  public <T> T[] toArray(T[] a) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean contains(Object o) {
    throw new RuntimeException("not supported");
  }

  @Override
  public Iterator<T> iterator() {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean remove(Object o) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new RuntimeException("not supported");
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new RuntimeException("not supported");
  }

  @Override
  public T set(int index, T element) {
    throw new RuntimeException("not supported");
  }

  @Override
  public void add(int index, T element) {
    throw new RuntimeException("not supported");
  }

  @Override
  public T remove(int index) {
    throw new RuntimeException("not supported");
  }

  @Override
  public int indexOf(Object o) {
    throw new RuntimeException("not supported");
  }

  @Override
  public int lastIndexOf(Object o) {
    throw new RuntimeException("not supported");
  }

  @Override
  public ListIterator<T> listIterator() {
    throw new RuntimeException("not supported");
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    throw new RuntimeException("not supported");
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    throw new RuntimeException("not supported");
  }

}
