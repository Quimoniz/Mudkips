package org.quimoniz.mudkips.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArraySetIterator<E> implements Iterator<E> {
  private ArraySet<E> set;
  private int index = 0;
  public ArraySetIterator(ArraySet<E> set) {
    this.set = set;
  }
  @Override
  public boolean hasNext() {
    return (index + 1) < set.size();
  }
  @Override
  public E next() throws NoSuchElementException {
    if(hasNext()) {
      return (E)set.valueArray[++index];
    } else {
      throw new NoSuchElementException("Can't access element index="+index+", size="+set.size());
    }
  }
  @Override
  public void remove() {
    set.remove(index);
  }

}
