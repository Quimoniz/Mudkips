package org.quimoniz.mudkips.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Iterator;

public class ArraySet<E> implements Set<E> {
  protected Object[] valueArray;
  protected int size = 0;
  private static final double loadFactor = 0.75;
  private int indexOfNull = -1;
  private boolean resizeable = false;
  public ArraySet(int initialCapacity) {
    this(initialCapacity, true);
  }
  public ArraySet(int initialCapacity, boolean resizeable) {
    this.resizeable = resizeable;
    if(resizeable) initialCapacity = (int) (initialCapacity / loadFactor + 1); 
    valueArray = new Object[initialCapacity];
  }
  @Override
  public boolean add(E arg0) {
    if(arg0 == null) {
      if(indexOfNull >= 0) {
        return false;
      }
    }
    if(++size > valueArray.length*loadFactor && resizeable) {
      Object[] tempValueArray = valueArray;
      int newCapacity = (int)(valueArray.length/loadFactor) + 1;
      synchronized(valueArray) {
        valueArray = new Object[newCapacity];
        System.arraycopy(tempValueArray, 0, valueArray, 0, size-1);
        tempValueArray = null;
      }
    }
    valueArray[size-1] = arg0;
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends E> arg0) {
    boolean anyChangePerformed = false;
    for(Object curObj : arg0) {
      if(add((E)curObj)) {
        anyChangePerformed = true;
      }
    }
    return anyChangePerformed;
  }
  public boolean addAll(E[] arg0) {
    return addAll(arg0, arg0.length);
  }
  public boolean addAll(E[] arg0, int length) {
    boolean anyChangePerformed = false;
    for(int i = 0; i < length; i++) {
      if(add(arg0[i])) {
        anyChangePerformed = true;
      }
    }
    return anyChangePerformed;
  }
  @Override
  public void clear() {
    synchronized(valueArray) {
      size = 0;
      valueArray = new Object[valueArray.length];
      indexOfNull = -1;
    }
  }

  @Override
  public boolean contains(Object arg0) {
    if(arg0 == null) {
      if(indexOfNull >= 0) {
        return true;
      } else {
        return false;
      }
    }
    E value = (E)arg0;
    for(int i = 0; i<size; i++) {
      if(valueArray[i].equals(value)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> arg0) {
    for(Object curObj : arg0) {
      for(int i = 0; i<size; i++) {
        if(!valueArray[i].equals(arg0)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean isEmpty() {
    return size < 1;
  }

  @Override
  public Iterator<E> iterator() {
    return new ArraySetIterator(this);
  }

  @Override
  public boolean remove(Object arg0) {
    if(arg0 == null) {
      if(indexOfNull >= 0) {
        synchronized(valueArray) {
          for(int i = indexOfNull+1; i<size; i++) {
            valueArray[i-1] = valueArray[i];
          }
          size--;
        }
        indexOfNull = -1;
        return true;
      } else {
        return false;
      }
    }
    for(int i = 0; i<size; i++) {
      if(valueArray[i] != null && valueArray[i].equals(arg0)) {
        synchronized(valueArray) {
          for(int j = i+1; j<size; j++) {
            valueArray[j-1] = valueArray[j];
          }
        }
        if(indexOfNull > i) {
          indexOfNull--;
        }
        size--;
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> arg0) {
    boolean performedAnyChange = false;
    for(Object curObj : arg0) {
      if(remove(curObj))
        performedAnyChange = true;
    }
    return performedAnyChange;
  }
  public void remove(int position) {
    synchronized(valueArray) {
      for(int i = position + 1; i < size; i++) {
        valueArray[i - 1] = valueArray[i];
      }
      if(indexOfNull > position) {
        indexOfNull--;
      } else if(indexOfNull == position) {
        indexOfNull = -1;
      }
      size--;
    }
  }
  @Override
  public boolean retainAll(Collection<?> arg0) {
    boolean performedAnyChange = false;
    synchronized(valueArray) {
      int removalCount = 0;
      for(int i = 0; i<size; i++) {
        for(Object curObj : arg0) {
          if(!((arg0 == null && valueArray==null) || (valueArray[i]!=null && valueArray[i].equals(curObj)))) {
            removalCount++;
            performedAnyChange = true;
            if(i == indexOfNull) {
              indexOfNull = -1;
            } else if(i < indexOfNull) {
              indexOfNull--;
            }
          }
        }
        if(removalCount > 0) {
          valueArray[i - removalCount] = valueArray[i];
        }
      }
      size -= removalCount;
    }
    return performedAnyChange;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Object[] toArray() {
    Object[] outputArray = new Object[size];
    System.arraycopy(valueArray, 0, outputArray, 0, size);
    return outputArray;
  }

  @Override
  public <T> T[] toArray(T[] arg0) {
    return (T[])toArray();
  }
  @Override
  public int hashCode() {
    long sum = 0;
    for(int i = 0; i < size; i++) {
      if(valueArray[i] != null)
        sum += valueArray[i].hashCode();
    }
    sum %= 4294967296L;
    if(sum > 2147483647L)
      sum -= 4294967296L;
    return (int)sum;
  }
}
