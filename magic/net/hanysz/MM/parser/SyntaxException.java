package net.hanysz.MM.parser;

public class SyntaxException extends Exception {


    private int errorPosition;

    public SyntaxException(String message, int errorPos) {
	super(message);
	errorPosition = errorPos;
    }

    public int getErrorPosition() {
	return errorPosition;
    }
}
