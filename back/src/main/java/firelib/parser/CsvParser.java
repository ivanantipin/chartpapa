package firelib.parser;

import firelib.core.store.reader.MarketDataReader;
import firelib.core.domain.Timed;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.time.Instant;
import java.util.function.Supplier;


public class CsvParser<T extends Timed> implements MarketDataReader<T> {

    private final ParseHandler[] handlers;
    private final Supplier<T> factory;
    private final int capacity;
    private final FileChannel fileChannel;

    private final Charset charSet;
    private Instant startDt;
    private Instant endDt;

    private T currentRecord;
    //21.05.2007,094800,90.05,90.05,90.05,90.05,900,1100
    private final CharBuffer charBuffer;

    long endReadPosition = 0;
    private final CharsetDecoder charsetDecoder;
    private String fileName;

    int maxFailedRows = 5;
    int failedRows = 0;

    MutableInt poss = new MutableInt(0);


    public CsvParser(String fileName, ParseHandler<T>[] handlers, Supplier<T> factory) {
        this(fileName, handlers, factory, 20000000);
    }

    public CsvParser(String fileName, ParseHandler<T>[] handlers, Supplier<T> factory, int capacityBytes) {
        this.fileName = fileName;
        try {
            this.capacity = capacityBytes;
            charBuffer = CharBuffer.allocate(capacityBytes*2);
            this.handlers = handlers;
            this.factory = factory;
            File aFile = new File(fileName);
            fileChannel = new RandomAccessFile(aFile, "r").getChannel();
            charSet = Charset.forName("US-ASCII");
            charsetDecoder = charSet.newDecoder();
            initStartEndTimes();
            endReadPosition = buffer(0,capacity,charBuffer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private long buffer(long pos, int capacity, CharBuffer buffer) {
        MappedByteBuffer mem = null;
        try {
            long len = Math.min(fileChannel.size() - pos, capacity);
            len = Math.max(0,len);
            if(len > 0){
                mem = fileChannel.map(FileChannel.MapMode.READ_ONLY, pos, len);
                charsetDecoder.decode(mem, buffer, false);
            }
            buffer.flip();
            return len;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void initStartEndTimes() throws IOException {
        readFirst();
        readLast();
    }

    private void readLast() throws IOException {
        CharBuffer buffer = CharBuffer.allocate(2000);
        long lastPos = fileChannel.size() - 400;
        long startPos = Math.max(0, lastPos);
        buffer(startPos,1000,buffer);
        T last = null;

        if(startPos != 0){
            readLine(buffer);
        }
        last = parseLine(readLine(buffer));
        while (last != null) {
            endDt = last.time();
            last = parseLine(readLine(buffer));
        }
    }

    private CharBuffer readLine(CharBuffer buffer) {
        int pos = BaseHandler.skipTillEol(buffer, buffer.position());
        pos = BaseHandler.skippEolOrEmpty(buffer, pos);
        CharBuffer ret = buffer.subSequence(0, pos - buffer.position());
        buffer.position(pos);
        return ret;
    }

    private void readFirst() {
        CharBuffer buffer = CharBuffer.allocate(2000);
        buffer(0,1000,buffer);
        T first = parseLine(readLine(buffer));
        startDt = first.time();
    }



    @Override
    public boolean seek(Instant time) {
        long pos = roughSeekApprox(time);
        charBuffer.clear();
        endReadPosition = pos + buffer(pos,capacity,charBuffer);
        if(pos != 0){
            readLine(charBuffer);
        }

        while (read()) {
            if (time.compareTo(current().time()) <= 0) {
                return true;
            }
        }
        return false;
    }


    private long roughSeekApprox(Instant time) {
        long ppos = 0;
        int inc = 10000000;
        while (true){
            CharBuffer buffer = CharBuffer.allocate(1000);
            long len = buffer(ppos,500,buffer);
            if(len < 500){
                return Math.max(0,ppos - inc);
            }
            readLine(buffer);
            T first = parseLine(readLine(buffer));
            if (time.compareTo(first.time()) <= 0) {
                return Math.max(0,ppos - inc);
            }
            ppos += inc;
        }
    }

    @Override
    public T current() {
        return currentRecord;
    }

    @Override
    public boolean read() {
        if(closeToEnd(200)){
            charBuffer.compact();
            long nt = System.nanoTime();
            endReadPosition += buffer(endReadPosition,capacity,charBuffer);
            System.out.println("buffered in " + (System.nanoTime() - nt)/1000000.0 + " ms. " + fileName);
        }
        currentRecord = parseLine(readLine(charBuffer));
        return currentRecord != null;
    }


    boolean closeToEnd(int safeBuffer){
        return charBuffer.limit() - safeBuffer < charBuffer.position();
    }

    private T parseLine(CharBuffer line) {
        T ret = factory.get();
        for (int i = 0; i < handlers.length; i++) {
            int ep = handlers[i].handle(line, ret);
            if(ep < 0){
                return null;
            }
            line.position(ep);
        }
        return ret;
    }

    @Override
    public Instant startTime() {
        return startDt;
    }

    @Override
    public Instant endTime() {
        return endDt;
    }

    @Override
    public void close() throws Exception {
        try {
            fileChannel.close();
        } catch (IOException e) {

        }
    }

    @Override
    public boolean isReadable() {
        return true;
    }
}
