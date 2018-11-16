package firelib.parser;

import java.nio.CharBuffer;

/**
 * Created by ivan on 7/28/14.
 */
public class SkipHandler extends BaseHandler{
    @Override
    public int handle(CharBuffer buffer, Object md) {
        int i = buffer.position();
        i = skipTillEolOrSep(buffer, i);
        if (i == buffer.limit()) {
            return -1;
        }
        return i;
    }
}

