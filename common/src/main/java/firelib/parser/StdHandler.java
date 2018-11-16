package firelib.parser;

import java.nio.CharBuffer;
import java.util.function.Function;

class StdHandler<T,V> extends BaseHandler<T> {

    private IModifier<T, V> consumer;
    private Function<CharSequence, V> parser;

    public StdHandler(IModifier<T, V> consumer, Function<CharSequence, V> parser){
        this.consumer = consumer;
        this.parser = parser;
    }

    @Override
    public int handle(CharBuffer buffer, T md) {
        int i = buffer.position();
        i = skipTillEolOrSep(buffer, i);
        if (i == buffer.limit()) {
            return -1;
        }
        CharBuffer seq = buffer.subSequence(0, i - buffer.position());
        consumer.apply(md, parser.apply(seq));
        return i;
    }

}
