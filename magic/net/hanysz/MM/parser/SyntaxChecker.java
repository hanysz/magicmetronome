package net.hanysz.MM.parser;
/* Check the syntax of the script before handing it over to the parser,
 * in the hope of generating some user-friendly error messages!
 */

import java.util.*;

public class SyntaxChecker {
    public static void checkScript(String theScript) throws SyntaxException {
	checkLegalChars(theScript);
	checkBalancedParentheses(theScript);
    }

    private static void checkLegalChars(String theScript) {
	System.out.println("supposed to check for illegal characters, "
	+"but this method hasn't yet been implemented!");
    }

    private static void checkBalancedParentheses(String theScript) {
	System.out.println("do the parentheses balance?");
    }
}

