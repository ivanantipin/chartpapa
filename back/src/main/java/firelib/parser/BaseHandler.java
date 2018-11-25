package firelib.parser;

import java.nio.CharBuffer;


abstract class BaseHandler<T> implements ParseHandler<T> {

    final static char sep = ',';

    static final boolean eolOrEmpty(char c) {
        return ((c == '\n') || (c == '\r') || (c == '\t') || (c == ' '));
    }

    static final boolean eolOrSep(char c) {
        return ((c == '\n') || (c == '\r') || (c == sep) );
    }

    static final boolean eol(char c) {
        return ((c == '\n') || (c == '\r'));
    }

    public static int skipTillEolOrSep(CharBuffer buffer, int i) {
        while (i < buffer.limit() && !eolOrSep(buffer.get(i))) {
            i++;
        }
        return i;
    }


    public static int skipTillEol(CharBuffer buffer, int i) {
        while (i < buffer.limit() && !eol(buffer.get(i))) {
            i++;
        }
        return i;
    }

    public static int skippEolOrEmpty(CharBuffer buffer, int i) {
        while (i < buffer.limit() && eolOrEmpty(buffer.get(i))) {
            i++;
        }
        return i;
    }

    protected int skippTillChar(CharBuffer buffer, int i, char ch) {
        while (i < buffer.limit() && buffer.get(i) != ch) {
            i++;
        }
        return i;
    }
}
