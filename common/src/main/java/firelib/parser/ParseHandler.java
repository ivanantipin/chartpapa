package firelib.parser;

import java.nio.CharBuffer;


public interface ParseHandler<T> {
    int handle(CharBuffer buffer, T md);
}
