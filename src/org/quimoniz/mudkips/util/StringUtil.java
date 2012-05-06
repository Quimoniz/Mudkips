package org.quimoniz.mudkips.util;

public class StringUtil {
  /*
   * @param: origin the string wherein to replace, replacer where to replace %<char> tags, replacement the stuff that will replace it
   */
  public static String replaceAll(String origin, char [] replacer, String [] replacements) {
    StringBuilder buf = new StringBuilder();
    //modReplace: %modulus + char
    boolean modReplace = false;
    for(int i = 0; i < origin.length(); i++) {
      char c = origin.charAt(i);
      if(modReplace) {
        int j = 0;
        for(; j < replacer.length; j++)
          if(replacer[j] == c)
            break;
        if(j == replacer.length || j >= replacements.length) {
          buf.append("%"+c);
        } else {
          buf.append(replacements[j]);
        }
        modReplace = false;
      } else if(c == '%') {
        modReplace = true;
      } else {
        buf.append(c);
      }
    }
   return buf.toString();
  }
  public static String replaceAll(String origin, char replacer, String replacement) {
    StringBuilder buf = new StringBuilder();
    //modReplace: %modulus + char
    boolean modReplace = false;
    for(int i = 0; i < origin.length(); i++) {
      char c = origin.charAt(i);
      if(modReplace) {
        if(c != replacer) {
          buf.append("%"+c);
        } else {
          buf.append(replacement);
        }
        modReplace = false;
      } else if(c == '%') {
        modReplace = true;
      } else {
        buf.append(c);
      }
    }
   return buf.toString();
  }
  public static boolean isInteger(String val) {
    char currentChar;
    boolean canHaveMinus = true;
    for(int i = 0; i < val.length(); i++) {
      currentChar = val.charAt(i);
      if(!(currentChar >= 48 && currentChar <= 57)) {
        if(currentChar == 45 && canHaveMinus) {
          canHaveMinus = false;
        } else {
          return false;
        }        
      } else {
        canHaveMinus = false;
      }
    }
    return true;
  }
  public static boolean isRealNumber(String val) {
    //Just going with java.lang.Double.valueOf(String) implementation
    final String Digits     = "(\\p{Digit}+)";
    final String HexDigits  = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally 
    // signed decimal integer.
    final String Exp        = "[eE][+-]?"+Digits;
    final String fpRegex    =
            ("[\\x00-\\x20]*"+  // Optional leading "whitespace"
            "[+-]?(" + // Optional sign character
            "NaN|" +           // "NaN" string
            "Infinity|" +      // "Infinity" string

            // A decimal floating-point string representing a finite positive
            // number without a leading sign has at most five basic pieces:
            // Digits . Digits ExponentPart FloatTypeSuffix
            // 
            // Since this method allows integer-only strings as input
            // in addition to strings of floating-point literals, the
            // two sub-patterns below are simplifications of the grammar
            // productions from the Java Language Specification, 2nd 
            // edition, section 3.10.2.

            // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
            "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

            // . Digits ExponentPart_opt FloatTypeSuffix_opt
            "(\\.("+Digits+")("+Exp+")?)|"+

            // Hexadecimal strings
            "((" +
            // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
            "(0[xX]" + HexDigits + "(\\.)?)|" +

            // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
            "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

            ")[pP][+-]?" + Digits + "))" +
            "[fFdD]?))" +
            "[\\x00-\\x20]*");// Optional trailing "whitespace"
            
    if (java.util.regex.Pattern.matches(fpRegex, val)) {
      return true;
    } else {
     return false;
    }
  }
  public static String concatenate(String[] concat, String delim, int offset) {
    StringBuilder concatenateBuf = new StringBuilder(concat.length*4);
    int i = 0;
    for(int j = offset; j < concat.length; j++) {
      if(i > 0)
        concatenateBuf.append(delim + concat[j]);
      else concatenateBuf.append(concat[j]);
      i ++;
    }
    return concatenateBuf.toString();
  }
  public static String fillUp(String originalString, int offset, char fillUp, int fillCount) {
    if(fillCount < 1)
      return originalString;
    StringBuilder buf = new StringBuilder(originalString.length() + fillCount);
    if(offset > originalString.length()) {
      buf.append(originalString);
    } else {
      buf.append(originalString.substring(0, offset));
    }
    for(int i = 0; i < fillCount; i++) {
      buf.append(fillUp);
    }
    if(offset < originalString.length()) {
      buf.append(originalString.substring(offset));
    }
    return buf.toString();
  }
  public static int count(String originString, String search) {
	if(originString == null)
	  return 0;
	int count = 0, offset = 0;
	while((offset = originString.indexOf(search, offset)) >= 0) {
	  count ++;
	  offset += search.length();
	}
	return count;
  }
  public static long parseDuration(String durationString) {
    long [] multiplier     = new long [] {1000L,60L*1000,60L*60*1000,60L*60*24*1000,60L*60*24*7*1000,60L*60*24*365*1000};
    char[] multiplierChar = new char[] { 's',    'm',       'h',          'd',            'w',              'y'};
    StringBuilder curNum = new StringBuilder();
    long duration = 0;
    for(int i = 0; i < durationString.length(); i++) {
      char curChar = durationString.charAt(i);
      if(curChar > 47 && curChar < 58) {
        curNum.append(curChar);
      } else {
        for(int j = 0; j < multiplierChar.length; j++) {
          if(curChar == multiplierChar[j]) {
            try {
              duration += multiplier[j] * Integer.parseInt(curNum.toString());
            } catch(NumberFormatException exc) { }
            break;
          }
        }
        curNum = new StringBuilder();
      }
    }
    if(curNum.length() > 0) {
      try {
        duration += multiplier[0] * Integer.parseInt(curNum.toString());
      } catch(NumberFormatException exc) { }
    }
    return duration; 
  }
  public static String describeDuration(long milliseconds, String language) {
    if(language.indexOf("en") == 0) { /*TODO: Implement some ratio, like 80% for stopping showing more detailed time descruption, like instead of 1year 2 months, 5 days, 3hours, 9 minutes, 4 seconds, it would just write 1year,2months*/
      String[][] durationNames = new String[][] {{"year","years"},{"month","months"},{"week","weeks"},{"day","days"},{"hour","hours"},{"minute","minutes"},{"second","seconds"},{"ms","ms"}};
      long[] durationUnits = new long[] {365L*86400*1000, 30L*86400*1000, 7L*86400*1000, 86400L*1000, 3600L*1000, 60L*1000, 1000L, 1L};
      long millisecondsRemainder = milliseconds;
      StringBuilder buf = new StringBuilder();
      boolean isFirstAppend = true;
      double skipRatio = 0.9;
      for(int i =0; (i<durationNames.length && i<durationUnits.length); i++) {
        int occurence = (int) Math.floor(millisecondsRemainder / durationUnits[i]);
        if(occurence >= 2) {
          if(isFirstAppend) {
            isFirstAppend = false;
          } else {
            buf.append(' ');
          }
          buf.append(occurence);
          buf.append(' ');
          buf.append(durationNames[i][1]);
          millisecondsRemainder -= occurence * durationUnits[i];
        } else if(occurence == 1) {
          if(isFirstAppend) {
            isFirstAppend = false;
          } else {
            buf.append(' ');
          }
          buf.append(occurence);
          buf.append(' ');
          buf.append(durationNames[i][0]);
          millisecondsRemainder -= durationUnits[i];
        }
        double inversedRemainder = milliseconds-millisecondsRemainder;
        if(inversedRemainder > 0 && (inversedRemainder/milliseconds) > skipRatio) {
          break;
        }
      }
      return buf.toString();
    }
    return null;
  }
  public static String[] separate(String originString, char delim) {
    if(originString == null) return null;
    java.util.LinkedList<String> list = new java.util.LinkedList<String>();
    int delimPos = -1, lastDelimPos = 0;
    while((delimPos = originString.indexOf(delim, delimPos)) >= 0) {
      list.add(originString.substring(lastDelimPos, delimPos));
      lastDelimPos = ++delimPos;
    }
    if(originString.length() > lastDelimPos)
      list.add(originString.substring(lastDelimPos));
    return list.toArray(new String[0]);
  }
  public static String escapeFileName(String name) {
    char [] otherCharacters = new char[] {'.','_','-'};
    StringBuilder buf = new StringBuilder(name.length()+name.length()/2);
    for(int i = 0; i < name.length(); i++) {
      char curChar = name.charAt(i);
      if((curChar>96 && curChar<123) || (curChar>64 && curChar<91) || (curChar>47 && curChar<57)) {
        buf.append(curChar);
      } else {
        boolean matchedOtherCharacters = false;
        for(int j = 0; j < otherCharacters.length; j++) {
          if(curChar == otherCharacters[j]) {
            matchedOtherCharacters = true;
            break;
          }
        }
        if(matchedOtherCharacters) {
          buf.append(curChar);
        } else {
          buf.append('%');
          buf.append(Integer.toString(curChar,16).toUpperCase());
        }
      }
    }
    return buf.toString();
  }
}
