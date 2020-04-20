package net.hanysz.MM.parser;

//import net.hanysz.MM.events.*;
import java.util.*;
import net.hanysz.MM.parser.ParseException;
//import net.hanysz.MM.*;


// need to rework:
// preProcess as instance method, keep macroTable static,
// other local variables become class instance fields

public class MMPreProcessor implements net.hanysz.MM.MMConstants {

    private static  Map<String,String> macroTable
			    = new HashMap<String,String>();
    private static boolean validScript=true;
    int charPosition;
    char currentChar;
    int macroStart, macroEnd;
    StringBuilder thisMacroName, macroDef;
    String macroValue;

    /** Remove comments and expand macro definitions. */
    public boolean preProcess(StringBuilder theScript) throws ParseException {
    // return true if macros are used in the script, false otherwise
	boolean macrosUsed=false;

	// removeComments(theScript);
	// can we manage without removing comments, get the parser to do it instead?

	while ((macroStart=theScript.indexOf("\\"))!=-1) {
	    charPosition = macroStart+1;
	    thisMacroName = getMacroName(theScript);
	    skipWhiteSpace(theScript);

	    if (currentChar == '=') { // got a macro definition
		if (macroTable.containsKey(thisMacroName.toString())) {
		    error("macro \\"+thisMacroName+" is defined more than once.");
		    return false;
		}
		macroDef = getMacroDefinition(theScript);
		new MMPreProcessor().preProcess(macroDef); // handle nested macros
		macroTable.put(thisMacroName.toString(), macroDef.toString());
		macrosUsed=true;
		theScript.delete(macroStart, macroEnd);
	    } else { //otherwise replace the text
		macroValue = macroTable.get(thisMacroName.toString());
		if (macroValue == null) {
		    error("macro \\"+thisMacroName+" is used before it is defined.");
		    return false;
		} else {
		    theScript.replace(macroStart, macroEnd, macroValue);
		}
	    } // end replace text
	} //end while macros
	return macrosUsed;
    } // end preprocess


    private void removeComments(StringBuilder theScript) {
	int commentStart, commentEnd;

	while ((commentStart=theScript.indexOf("#"))!=-1) {
	    commentEnd = theScript.indexOf("\n",commentStart);
	    if (commentEnd==-1) {commentEnd = theScript.length();}
	    theScript.delete(commentStart,commentEnd);
	}
    }


    public void resetPreProcessor () { // necessary to handle multiple invocations
	macroTable = new HashMap<String,String>();
	validScript = true;
    }



    private StringBuilder getMacroName(StringBuilder theScript) {
	StringBuilder name = new StringBuilder();

	currentChar = theScript.charAt(charPosition);
	while (Character.isLetter(currentChar)
		|| Character.isDigit(currentChar)
		|| currentChar == '-'
		|| currentChar == '_') {
	    name.append(currentChar);
	    getNextChar(theScript);
	}
	macroEnd = charPosition;
	return name;
    } // end getMacroName



    private StringBuilder getMacroDefinition(StringBuilder theScript) throws ParseException {
	StringBuilder theDefinition = new StringBuilder();

	getNextChar(theScript);  // move past the = sign

	skipWhiteSpace(theScript);
	if (currentChar != '"') {
	    error("macro definition for " + thisMacroName
		    + " should begin with \", got "+currentChar);
	}
	do {
	    getNextChar(theScript);
	    if (currentChar == '#') {
		error("macro definition for \\" + thisMacroName
		    + " should end with \"");
	    }
	    theDefinition.append(currentChar);
	}
	while (currentChar != '"'); // end do-while

	//now the definition will end with the closing " -- delete it
	theDefinition.deleteCharAt(theDefinition.length()-1);

	charPosition++;
	macroEnd = charPosition;
	return theDefinition;
    }


    private void skipWhiteSpace(StringBuilder theScript) {
	while (Character.isWhitespace(currentChar)) {
	    getNextChar(theScript);
	}
    }


    // nb this is for use AFTER comments have been removed--
    // then we can guarantee that '#' won't appear,
    // so we can use it as an end-of-file marker
    private void getNextChar(StringBuilder theScript) {
	charPosition++;
	currentChar = (charPosition < theScript.length() ?
	    theScript.charAt(charPosition) : '#');
    }



    /* -- not needed? --
    public void postProcess(MMEventList eventList) {
	// put accelerandi into correct form;
	// check for empty infinite repeats
    }
    */


    public static void error(String message) throws ParseException {
	validScript=false;
	// net.hanysz.MM.MMgui.displayErrorMessage("Parsing error: "+message, "Parsing error");
	throw new ParseException(message);
    }
}
