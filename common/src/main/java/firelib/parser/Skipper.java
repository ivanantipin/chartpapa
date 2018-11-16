package firelib.parser;

import java.nio.CharBuffer;

/**
 * Created by ivan on 8/15/14.
 */
@FunctionalInterface
interface Skipper{
    int skip(CharBuffer buffer, int i);
}
