package net.hanysz.MM.parser;

/* the java source code generated by javacc
 * from the file mmparser.jj
 * doesn't contain a package declaration;
 * therefore the file "package_declaration.txt"
 * is automatically prepended to all source files
 * by the makefile.
 */

/* Generated By:JavaCC: Do not edit this line. MMScriptParserConstants.java */
public interface MMScriptParserConstants {

  int EOF = 0;
  int LOWER_CASE_LETTER = 9;
  int RELATIVE_TEMPO = 10;
  int REPEAT = 11;
  int BLOCK_START = 12;
  int BLOCK_END = 13;
  int PAUSE = 14;
  int BRANCH_START = 15;
  int BRANCH_END = 16;
  int ACCEL = 17;
  int VOLUME = 18;
  int PAN = 19;
  int END = 20;
  int LINEAR_ACCEL = 21;
  int MARKER = 22;
  int MARKERNAME = 23;
  int PUSH_TEMPO = 29;
  int POP_TEMPO = 30;
  int SHORT_PAUSE = 31;
  int MEDIUM_PAUSE = 32;
  int PLUS_SIGN = 33;
  int MINUS_SIGN = 34;
  int TIMES_SIGN = 35;
  int DIVIDE_SIGN = 36;
  int REAL_NUMBER = 37;
  int DIGITS = 38;

  int DEFAULT = 0;
  int WithinComment = 1;
  int WithinMarker = 2;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\r\\n\"",
    "\"\\t\"",
    "\"#\"",
    "\"\\n\"",
    "<token of kind 8>",
    "<LOWER_CASE_LETTER>",
    "\"T\"",
    "\"R\"",
    "\"(\"",
    "\")\"",
    "\"S\"",
    "\"{\"",
    "\"}\"",
    "\"A\"",
    "\"V\"",
    "\"P\"",
    "\"E\"",
    "\"L\"",
    "\"M\"",
    "<MARKERNAME>",
    "\" \"",
    "\"\\n\"",
    "\"\\r\"",
    "\"\\r\\n\"",
    "\"\\t\"",
    "\"[\"",
    "\"]\"",
    "\",\"",
    "\";\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "<REAL_NUMBER>",
    "<DIGITS>",
  };

}
