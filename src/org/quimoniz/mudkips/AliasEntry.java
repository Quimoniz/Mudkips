package org.quimoniz.mudkips;

import java.util.Map.Entry;

public class AliasEntry<K, V> implements Entry<K, V>,Cloneable {
  private K key;
  private V value;
  public AliasEntry(K key, V value) {
	this.key = key;
	this.value = value;
  }
  @Override public boolean equals(Object o) {
	AliasEntry<K,V> castedObject = null;
	try {
	  castedObject = (AliasEntry<K,V>) o;
	} catch(ClassCastException exc) {
	  return false;
	}
	if(castedObject != null && key.equals(castedObject.getKey()) && value.equals(castedObject.getValue()))
	  return true;
	else
	  return false;
  }
  @Override public K getKey() {
	return key;
  }
  @Override public V getValue() {
	return value;
  }
  @Override public int hashCode() {
	return key.hashCode() + value.hashCode();
  }
  @Override public V setValue(V value) {
	V oldValue = this.value;
	this.value = value;
	return oldValue;
  }
  @Override public AliasEntry<K,V> clone() {
	return new AliasEntry<K,V>(key,value);
  }
  public AliasEntry<String,String> lowerCaseStringEntry() {
	String lowerCaseKey = key.toString().toLowerCase();
	String lowerCaseValue = value.toString().toLowerCase();
	return new AliasEntry<String,String>(lowerCaseKey,lowerCaseValue);
  }
  @Override public String toString() {
	 return value + ": " + value;
  }
}
