package firelib.parser;

import java.nio.CharBuffer;


public class IncHandler<T> extends BaseHandler<T> {
    @Override
    public int handle(CharBuffer buffer, T md) {
        return Math.min(buffer.position() + 1,buffer.limit()) ;
    }
}
