package org.quimoniz.wigglytuff;

//actually we could as well go with a InputStreamReader, makes interpreting of chars more elegant
//speaking of charsets, we will use ISO-8859-1 why? BECAUSE BERNERS-LEE CHOOSE IT!!!
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;

public class ReaderStreamParser implements Runnable {
private InputStream inStream = null;
private Thread t = null;
public ReaderStreamParser(InputStream inStream) {
  this.inStream = inStream;
}
public void start() {
  if(t == null) {
	  t = new Thread(this);
	  t.start();
	}
}
public void stop() {
  t = null;
}
public enum ParserState { DEFAULT, IN_STRING, IN_COMMENT }
public void run() {
  final int CHAR_BUFFER_SIZE = 1000;
  byte[] charBuf = new byte[CHAR_BUFFER_SIZE];

	Entity rootEntity = new Entity();
	//rootEntity.state = Entity.EntityState.IN_BRACERS;
	rootEntity.openBracers();
	Entity currentEntity = rootEntity;
	
	char lastChar = '\0', curChar = '\0';
	ParserState parserState = ParserState.DEFAULT;
	int collonCount = 1, lineCount = 1; // limit are 2^32 collons or rows
	long index = 0; // limit is 2^64 bytes
	boolean string_PrependingBackslash = false;
	//ToDo: define abort condition of loop!
  while(Thread.currentThread() == t) {
	  int bytesRead = -2;
	  try {
      bytesRead = inStream.read(charBuf, 0, CHAR_BUFFER_SIZE);
	  } catch(Exception exc) {
	    handleException (exc);
	  }
	  if(bytesRead > 0) {
	    for(int jndex = 0; jndex < bytesRead; jndex++) {
		  lastChar = curChar;
		  curChar = (char) charBuf[jndex];
		  index++;
		  if(curChar == '\n') {
		    collonCount = 1;
			lineCount++;
	      } else {
		    collonCount++;
		  }
		  /* DEBUG
		  if(currentEntity.buf.length() > 0)
		    System.out.print(currentEntity.buf.charAt(0));
		   else
		    System.out.print('-');
	      */
		  if(parserState == ParserState.DEFAULT) {//DEBUG: System.out.print(curChar);
	        if(lastChar == '/' && curChar == '*') {
			  parserState = ParserState.IN_COMMENT;
			  continue;
		    } else if(curChar == '"') {
			  parserState = ParserState.IN_STRING;
			  currentEntity = new Entity(currentEntity);
			  continue;
			} else {
			  // a-z 0-9 A-Z + - .
			  if((curChar >= 97 && curChar <= 122) || (curChar >=48 && curChar <= 57) || (curChar >= 65 && curChar <= 90) || curChar == 43 || curChar == 45 || curChar == 46) {
			    if(currentEntity.state == Entity.EntityState.IN_NAME) {
			      currentEntity.addChar(curChar);
				} else {
				  if(currentEntity.state == Entity.EntityState.AFTER_PARANTHESES || currentEntity.state == Entity.EntityState.AFTER_BRACERS || currentEntity.state == Entity.EntityState.FINISHED) {
				    if(currentEntity.parent != null) {
				      finishElement(currentEntity);
					  currentEntity = currentEntity.parent;
					}
				  }
				  currentEntity = new Entity(currentEntity);
				  currentEntity.addChar(curChar);
				}
			  } else if(curChar == '(') {
			    currentEntity.openParantheses();
			  } else if(curChar == 123) { //opening curly bracket
			    currentEntity.openBracers();
			  } // 125: closing curly bracket
			    else if(curChar == 125 || curChar == ',' || curChar == ')') {
			    // current entity will be closed
				if(currentEntity.parent != null) {
			      currentEntity.parent.addEntity(currentEntity);
				}
				if(curChar == ')') {
				  if(currentEntity.state == Entity.EntityState.IN_PARANTHESES) {
				    currentEntity.state = Entity.EntityState.AFTER_PARANTHESES;
				  } else {
				    boolean tooManyClosingParantheses = false;
				    while(currentEntity.state != Entity.EntityState.IN_PARANTHESES) {
					  if(currentEntity.parent != null) {
					    finishElement(currentEntity);
				        currentEntity = currentEntity.parent;
					  } else {
					     tooManyClosingParantheses = true;
					     break;
					   }
					}
					if(!tooManyClosingParantheses) {
					  currentEntity.state = Entity.EntityState.AFTER_PARANTHESES;
					} else {
					   fatalError("Too many closing Parantheses(at line " + lineCount + " collon " + collonCount + ")");
					 }
				  }
				}
				// 125: closing curly bracket
				if(curChar == 125) {
				  if(currentEntity.state == Entity.EntityState.IN_BRACERS) {
				    if(currentEntity.parent != null) {
				      currentEntity.state = Entity.EntityState.AFTER_BRACERS;
					}
				  } else {
				    boolean tooManyClosingBracers = false;
				    while(currentEntity.state != Entity.EntityState.IN_BRACERS) {
					  if(currentEntity.parent != null) {
					    finishElement(currentEntity);
				        currentEntity = currentEntity.parent;
					  } else {
					     tooManyClosingBracers = true;
					     break;
					   }
					}
					if(!tooManyClosingBracers) {
					  currentEntity.state = Entity.EntityState.AFTER_BRACERS;
					} else {
					   //fatalError("Too many closing Bracers(at line " + lineCount + " collon " + collonCount + ")");
					 }
				  }
				}
				if(curChar == ',') {
				  //finishElement(currentEntity);
				  //currentEntity = currentEntity.parent; // this behaviour, is not thought through: it is very likely to bug around
				                                        // and yep, it bugged about, causing element(a,,) to fail (empty element delimited by commata)
				  if(currentEntity.state == Entity.EntityState.IN_NAME || currentEntity.state == Entity.EntityState.AFTER_PARANTHESES || currentEntity.state == Entity.EntityState.AFTER_BRACERS) {
					if(currentEntity.parent != null) { // placing a commata at the beginning of stream will not break it anymore (NullPointerException)
					  finishElement(currentEntity);
					  currentEntity = currentEntity.parent;
					}
				  } else if(currentEntity.state == Entity.EntityState.FINISHED) {
				    currentEntity = currentEntity.parent;
				  }
				}
			  } else if(curChar == 32 || curChar == 13 || curChar == 10){
			  } else {
			    System.out.println("Illegal character '" + curChar + "'(" + ((int)curChar) + "), at line " + lineCount + ": " + collonCount);
			  }
			}
		  }
		  else if(parserState == ParserState.IN_COMMENT) {
		    if(lastChar == '*' && curChar == '/') {
			  parserState = ParserState.DEFAULT;
			}
		  }
		  else if(parserState == ParserState.IN_STRING) {
		    if(string_PrependingBackslash) {
			  char escapedChar = 0;
			  boolean couldEscape = false;
			  try {
			    escapedChar = unescapeCharacter(curChar);
			    couldEscape = true;
		      } catch(IllegalArgumentException exc) {
			    handleException(exc);
			  }
              if(couldEscape) currentEntity.addChar(escapedChar);
			  string_PrependingBackslash = false;
			} else {
			  //backslash
			  if(curChar == 92) {
			    string_PrependingBackslash = true;
			  } else if(curChar == '"') {
			    parserState = ParserState.DEFAULT;
			  } else {
			    currentEntity.addChar(curChar);
			  }
			}
		  }
	    }
	  } //end of stream reached
	    else if(bytesRead == -1) {
		  while(currentEntity.parent != null) {
		    finishElement(currentEntity);
			currentEntity = currentEntity.parent;
		  }
		  finalizeStream();
		  break;
	  }
	  try {
	    t.sleep(16);
    } catch(InterruptedException exc) {
	    handleException(exc);
	  }
	}
}
public HashMap<String, EntityReadListener> liveListeners = new HashMap<String, EntityReadListener>();
public LinkedList<Runnable> finalizerMethod = new LinkedList<Runnable>();
public void addListener(String entityNameFor, EntityReadListener eListener) {
  liveListeners.put(entityNameFor.toLowerCase(), eListener);
}
public void addFinalizerMethod(Runnable finalizerMethod) {
  this.finalizerMethod.add(finalizerMethod);
}
private void finishElement(Entity entity) {
  entity.finish();
	EntityReadListener listener = liveListeners.get(entity.name.toLowerCase());
	if(listener != null) {
	  try {
	    listener.entityFinished(entity);
	  } catch(Exception exc) {
	    handleException(exc);
	  }
	}
}
private void finalizeStream() {
  for(Runnable currentFinalizer : finalizerMethod) {
    if(currentFinalizer != null)
      try {
        currentFinalizer.run();
      } catch(Exception exc) {
        handleException(exc);
      }
  }
}
public static char unescapeCharacter(char escape) throws IllegalArgumentException {
  switch(escape) {
    case '0':
      return '\0';
    case 'a':
      return (char) 7;
    case 'b':
      return '\b';
    case 't':
  	  return '\t';
    case 'n':
	  return '\n';
    case 'v':
      return (char)11;
    case 'f':
      return (char)12;
    case 'r':
      return '\r';
    case 'e':
      return (char)27;
    case '"':
      return '"';
    case 92: // Backslash
      return 92;
//ToDo: Add other character escapes here - any more needed?
    default:
	    throw new IllegalArgumentException("'" + escape + "' is not an escape character.");
		//return '\0';
	}
}
public static String escapeCharacters(String text) {
  StringBuilder escapedText = new StringBuilder(text.length() + 2);
  for(char curChar : text.toCharArray()) {
    switch(curChar) {
      case 0:
        escapedText.append("\\0");
        break;
      case 7:
        escapedText.append("\\a");
        break;
      case 8:
        escapedText.append("\\b");
        break;
      case 9:
        escapedText.append("\\t");
        break;
      case 10:
        escapedText.append("\\n");
        break;
      case 11:
        escapedText.append("\\v");
        break;
      case 12:
        escapedText.append("\\f");
        break;
      case 13:
        escapedText.append("\\r");
        break;
      case 27:
        escapedText.append("\\e");
        break;
      case 34:
        escapedText.append("\\\"");
        break;
      case 92:
       escapedText.append("\\\\");
       break;
      default:
        escapedText.append(curChar);
        break;
    }
  }
  return escapedText.toString();
}
private void handleException(Exception exc) {
  StringBuilder buf = new StringBuilder(exc.getStackTrace().length * 10);
	buf.append(exc.toString());
	if(exc.getStackTrace().length > 0) {
	  buf.append("\n  ");
	  buf.append(exc.getStackTrace()[0].toString()); 
    for(int i = 1; i < exc.getStackTrace().length; i++) {
	    buf.append("\n  ");
	    buf.append(exc.getStackTrace()[i].toString());
	  }
	}
	System.out.println(buf.toString());
}
private void fatalError(String description) {
  int randomNumber = (int) (Math.random()*1000);
	if(randomNumber < 10)
	  randomNumber = (int) (Math.pow(10,9) + Math.random()*Math.pow(10,9));
  System.out.println("Fatal Error #" + randomNumber + ": " + description);
  System.exit(0);
}
}
