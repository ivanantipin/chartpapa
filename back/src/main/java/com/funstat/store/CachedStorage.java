package com.funstat.store;

import com.funstat.domain.InstrId;
import com.funstat.domain.Ohlc;
import firelib.common.interval.Interval;

import java.util.List;

public class CachedStorage implements MdStorage{

    private MdStorage delegate;

    SingletonsContainer container = new SingletonsContainer();

    public CachedStorage(MdStorage delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<Ohlc> read(InstrId instrId, String interval) {
        Interval iv = Interval.valueOf(interval);
        return container.getWithExpiration(instrId.toString() + "/" + interval, ()->{
            return delegate.read(instrId,interval);
        }, iv.getDuration().toMinutes()/2);

    }

    @Override
    public void save(String code, String source, String interval, List<firelib.domain.Ohlc> data) {
        throw new RuntimeException("not implemented");

    }

    @Override
    public List<InstrId> getMeta() {
        return delegate.getMeta();
    }

    @Override
    public void updateSymbolsMeta() {
        delegate.updateSymbolsMeta();
    }
}
