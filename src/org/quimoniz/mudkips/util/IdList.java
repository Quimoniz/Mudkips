package org.quimoniz.mudkips.util;

import java.util.HashSet;
import java.util.LinkedList;


public class IdList {
  private boolean isWhitelisted;
  private HashSet idList = new HashSet<Integer>();
  private IdList(boolean isWhitelisted, HashSet<Integer> idList) {
    this.isWhitelisted = isWhitelisted;
    this.idList = idList;
  }
  public boolean has(int id) {
    return isWhitelisted ? idList.contains(new Integer(id)) : !idList.contains(new Integer(id)); 
  }
  public boolean hasNot(int id) {
    return isWhitelisted ? !idList.contains(new Integer(id)) : idList.contains(new Integer(id)); 
  } 
  public static IdList parse(String serializedList) {
    if(serializedList.length() > 0) {
      StringBuilder curElementBuf = new StringBuilder();
      HashSet<Integer> positiveList = new HashSet<Integer>();
      HashSet<Integer> negativeList = new HashSet<Integer>();
      boolean isWhitelisted = true;
      final char SEPARATOR = ',';
      boolean isLastChar = false;
      for(int i = 0; !isLastChar; i++) {
        char curChar = serializedList.charAt(i);
        isLastChar = (i+1) == serializedList.length();
        if(curChar == SEPARATOR || isLastChar) {
          String curElement = curElementBuf.toString();
          if(curElementBuf.length() > 0) {
            boolean isPositive = true;
            int jndex = 0;
            if(curElement.startsWith("-")) {
              isPositive = false;
              jndex++;
            } else if(curElement.startsWith("+")) {
              jndex++;
            }
            if(curElement.charAt(jndex+1) == '*') {
              if(isPositive) {
                isWhitelisted = true;
              } else {
                isWhitelisted = false;
              }
            } else {
              //TODO: parse the ID definition, such as(d for digit):
              //      dd-dd   OR  dd
              // d: 0-9
              int hyphenPos = curElement.indexOf('-');
              if(hyphenPos > 0) {
                // dd-dd
                Integer fromInt, toInt;
                fromInt = parseIntSilently(curElement.substring(jndex, hyphenPos));
                toInt = parseIntSilently(curElement.substring(hyphenPos+1));
                if(null != fromInt && null != toInt) {
                  if(fromInt > toInt) {
                    int tempIntVar = fromInt;
                    fromInt = toInt;
                    toInt = tempIntVar;
                  }
                  if(isPositive) {
                    for(; fromInt < toInt; fromInt++) positiveList.add(fromInt);
                  } else {
                    for(; fromInt < toInt; fromInt++) negativeList.add(fromInt);
                  }
                }
              } else {
                // dd
                Integer tempIntVar;
                tempIntVar = parseIntSilently(curElement.substring(jndex));
                if(tempIntVar != null) {
                  if(isPositive) {
                    positiveList.add(tempIntVar);
                  } else {
                    negativeList.add(tempIntVar);
                  }
                }
              }
            }
          }
        } else {
          curElementBuf.append(curChar);
        }
        if(isLastChar) {
          break;
        }
      }
      //TODO weighten the isWhitelisted, and the positiveList&negativeList, to build Object
      HashSet<Integer> chosenSet;
      if(isWhitelisted) {
        positiveList.removeAll(negativeList);
        chosenSet = positiveList;
      } else {
        negativeList.removeAll(positiveList);
        chosenSet = negativeList;
      }
      return new IdList(isWhitelisted, chosenSet);
    } else {
      return new IdList(true, new HashSet());
    }
  }
  private static Integer parseIntSilently(String num) {
    try {
      return Integer.parseInt(num);
    } catch(NumberFormatException exc) {
      return null;
    }
  }
}
