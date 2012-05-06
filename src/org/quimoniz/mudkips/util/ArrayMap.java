package org.quimoniz.mudkips.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class ArrayMap<K, V> implements Map<K,V> {
  private Entry<K,V>[] dataArray;
  private int size = 0;
  private static final double loadFactor = 0.75; 
  private int indexOfNullKey = -1;
  private boolean resizeable;
  public ArrayMap(int initialCapacity, boolean resizeable) {
    if(resizeable) initialCapacity = (int) (initialCapacity / loadFactor + 1); 
    dataArray = (Entry<K,V>[])new Entry[initialCapacity];
  }
  public class Entry<K,V> implements java.util.Map.Entry<K,V> {
    private K key;
    private V value;
    public Entry (K key, V value) {
      this.key = key;
      this.value = value;
    }
	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V arg0) {
		V oldValue = value;
		value = arg0;
		return oldValue;
	}
	@Override
    public int hashCode() {
      return (getKey()==null ? 0 : getKey().hashCode()) ^ (getValue()==null ? 0 : getValue().hashCode());
	}
  }
  @Override
  public void clear() {
    synchronized(dataArray) {
      size = 0;
      dataArray = (Entry<K,V>[])new Entry[dataArray.length];
      indexOfNullKey = -1;
    }
  }

  @Override
  public boolean containsKey(Object arg0) {
    K key = (K) arg0;
    for(int i = 0; i<size; i++) {
      if(dataArray[i].getKey().equals(key)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsValue(Object arg0) {
    V value = (V) arg0;
    for(int i = 0; i<size; i++) {
      if(dataArray[i].getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set entrySet() {
    ArraySet<Entry<K,V>> set = new ArraySet(size, false);
    set.addAll(dataArray, size);
    return set;
  }

  @Override
  public V get(Object arg0) {
    K key = (K)arg0;
    for(int i = 0; i<size; i++) {
      if(dataArray[i].getKey().equals(key)) {
        return dataArray[i].getValue();
      }
    }
    return null;
  }

  @Override
  public boolean isEmpty() {
    return size<1;
  }

  @Override
  public Set keySet() {
    ArraySet<K> set = new ArraySet<K>(size);
    for(int i = 0; i < size; i++)
      set.add(dataArray[i].getKey());
    return set;
  }

  @Override
  public Object put(Object arg0, Object arg1) {
    K key = (K) arg0;
    V value = (V) arg1;
    if(arg0 == null) {
      if(indexOfNullKey >= 0) {
        dataArray[indexOfNullKey].setValue(value);
      } else {
        add(key, value);
        return null;
      }
    }
    for(int i=0; i<size; i++) {
      if(dataArray[i].getKey().equals(key)) {
        V oldVal = dataArray[i].getValue();
        synchronized(dataArray) {
          dataArray[i].setValue(value);
        }
        return oldVal;
      }
    }
    add(key, value);
    return null;
  }
  private void add(K key, V value) {
    if(++size > dataArray.length*loadFactor && resizeable) {
      Object[] tempDataArray = dataArray;
      int newCapacity = (int)(dataArray.length/loadFactor) + 1;
      synchronized(dataArray) {
        dataArray = new Entry[newCapacity];
        System.arraycopy(tempDataArray, 0, dataArray, 0, size-1);
        tempDataArray = null;
      }
    }
    dataArray[size-1] = new Entry<K,V>(key, value);
    if(key == null) indexOfNullKey = size; 
  }
  @Override
  public void putAll(Map arg0) {
    Set<java.util.Map.Entry> mapEntrySet = arg0.entrySet();
    for(java.util.Map.Entry curEntry : mapEntrySet) {
      put(curEntry.getKey(), curEntry.getValue());
    }
  }

  @Override
  public V remove(Object arg0) {
    for(int i = 0; i < size; i++) {
      if((arg0 == null && dataArray[i].getKey() == null) || (dataArray[i] != null && dataArray[i].getKey().equals(arg0))) {
        V oldValue = dataArray[i].getValue();
        for(int j = i + 1; j < size; j++) {
          dataArray[i - 1] = dataArray[i];
        }
        if(indexOfNullKey == i) {
          indexOfNullKey = -1;
        } else if(indexOfNullKey > i){
          indexOfNullKey--;
        }
        size--;
        return oldValue;
      }
    }
    return null;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Collection values() {
    ArrayList<V> list = new ArrayList<V>(size);
    for(int i = 0; i < size; i++) {
      list.add(dataArray[i].getValue());
    }
    return list;
  }
  @Override
  public int hashCode() {
    long sum = 0;
    for(int i = 0; i < size; i++) {
      if(dataArray[i].getValue() != null)
        sum += dataArray[i].getValue().hashCode();
    }
    sum %= 4294967296L;
    if(sum > 2147483647L)
      sum -= 4294967296L;
    return (int)sum;
  }
  public void incVal(K key) {
    for(int i = 0; i < size; i++) {
      Object val = dataArray[i].getValue();
      if(val instanceof Integer) {
        dataArray[i].setValue((V)new Integer((Integer)val + 1));
      } else if(val instanceof Double) {
        dataArray[i].setValue((V)new Double((Double)val + 1));
      } else if(val instanceof Float) {
        dataArray[i].setValue((V)new Float((Float)val + 1));
      } else if(val instanceof Long) {
        dataArray[i].setValue((V)new Long((Long)val + 1));
      } else if(val instanceof Short) {
        dataArray[i].setValue((V)new Short((short)((Short)val + 1)));
      } else if(val instanceof Byte) {
        dataArray[i].setValue((V)new Byte((byte)((Byte)val + 1)));
      } else if(val instanceof Character) {
        dataArray[i].setValue((V)new Character((char)((Character)val + 1)));
      }
    }
  }
}
