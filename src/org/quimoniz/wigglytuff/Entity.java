package org.quimoniz.wigglytuff;

import java.util.LinkedList;
import java.util.HashMap;

public class Entity {
  public boolean empty = false;
  public StringBuilder buf = new StringBuilder();
  public Entity parent = null;
  public String name = "";
  private boolean inParantheses = false;
  private boolean inBracers = false;
  private LinkedList<Entity> paranthesesEntities;// = new LinkedList<Entity>();
  private LinkedList<Entity> bracersEntities;// = new LinkedList<Entity>();
  public EntityState state = EntityState.IN_NAME; // 0 parsing name, 1 parsing paranthese parameters, 2 after closing parantheses, 3 parsing bracers parameters, 4 after closing bracers
  public enum BracketType {  BRACERS, PARANTHESES }
  public enum EntityState { IN_NAME, IN_PARANTHESES, AFTER_PARANTHESES, IN_BRACERS, AFTER_BRACERS, FINISHED};
  
  public Entity() {
    
  }
  public Entity(Entity parentEntity) {
    parent = parentEntity;
  }
  public void addChar( char c) {
    empty = false;
	buf.append(c);
  }
  public boolean isNumber() {
    boolean algebraicSignOccured = false;
	boolean pointOccured = false;
	boolean hasNumbers = false;
    for(int i = 0; i < buf.length(); i++) {
	  char curChar = buf.charAt(i);
      if(curChar < 48 || curChar > 57) { //not 0-9
	    if((curChar <=13 && curChar >= 8) || curChar == 32) { // is Whitespace character (backspace, horizontal tab, line feed, vertical tab, form feed, carriage return, space)
	      continue;
	    } else if (!algebraicSignOccured && (curChar == 45 || curChar == 43)) {
		  algebraicSignOccured = true;
		} else if(!pointOccured && curChar == 46) {
		  pointOccured = true;
		} else
		  return false;
	  } else
	    hasNumbers = true;
	}
	return hasNumbers;
  }
  public long getLong() throws NumberFormatException{
    return Long.parseLong( buf.toString() );    
  }
  public int getInt() throws NumberFormatException {
    return Integer.parseInt( buf.toString() );    
  }
  public short getShort() throws NumberFormatException {
	return Short.parseShort( buf.toString() );
  }
  public Entity [] getParantheseParameters() {
    return paranthesesEntities==null? new Entity[0] : paranthesesEntities.toArray(new Entity[0]);
  }
  public Entity [] getBracerParameters() {
    return bracersEntities==null? new Entity[0] : bracersEntities.toArray(new Entity[0]);
  }
  
  public void openParantheses() {
    paranthesesEntities = new LinkedList<Entity>();
	state = EntityState.IN_PARANTHESES;
  }
  public void openBracers() {
    bracersEntities = new LinkedList<Entity>();
	state = EntityState.IN_BRACERS;
  }
  public void addEntity(Entity ent) {
    if(state == EntityState.IN_PARANTHESES) {
	  paranthesesEntities.add(ent);
	} else if(state == EntityState.IN_BRACERS) {
	  bracersEntities.add(ent);
	}
  }
  public void finish() {
    this.state = EntityState.FINISHED;
	this.name = buf.toString();
  }
  public boolean isEmpty() {
    if((this.state == EntityState.IN_NAME && buf.length() < 1) || (buf.length() < 1 && paranthesesEntities.size() < 1 && bracersEntities.size() < 1))
	  return true;
	else
	  return false;
  }
  public String toString() {
    if(buf != null) {
      return buf.toString();
    } else {
      return name;
    }
  }
}
