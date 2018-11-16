package com.funstat.store;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class SingletonsContainer implements AutoCloseable{

    private final ConcurrentHashMap<String,Object> singletones = new ConcurrentHashMap<>();
    private Logger log = LoggerFactory.getLogger(getClass());

    public  synchronized <T> T get(String name,
                                   final Supplier<T> provider) {
        if(!singletones.containsKey(name)){
            try {
                T t = provider.get();
                log.info("created singleton service " + name + " with type "+ t.getClass());
                Object existing = singletones.put(name, t);
                if(existing != null){
                    throw new RuntimeException("service already existed " + name + " cyclic dependency");
                }
            }catch (Exception e){
                log.error("exception " , e);
                throw new RuntimeException(e);
            }
        }
        return (T) singletones.get(name);
    }

    @Override
    public void close() throws Exception {
        singletones.forEach((n,s)->{
            if(s instanceof AutoCloseable){
                try {
                    AutoCloseable ac = (AutoCloseable) s;
                    ac.close();
                } catch (Exception e) {
                    log.error("failed to close properly singleton service " + n, e);
                }
            }
        });

    }

    static class CachedUnit{
        long timestamp;
        Object object;

        public CachedUnit(long timestamp, Object object) {
            this.timestamp = timestamp;
            this.object = object;
        }
    }

    private final ConcurrentHashMap<String,CachedUnit> expirable = new ConcurrentHashMap<>();


    public synchronized <T> T getWithExpiration(String name, final Supplier<T> provider, long minsToExpire){
        CachedUnit ret = expirable.computeIfAbsent(name, (k) -> {
            return new CachedUnit(System.currentTimeMillis(), provider.get());
        });
        if( (System.currentTimeMillis() - ret.timestamp)/60_000 > minsToExpire){
            try{
                expirable.put(name, new CachedUnit(System.currentTimeMillis(), provider.get()));
            }catch (RuntimeException e){
                log.error("failed to create service, returning previos version", e);
                return (T) expirable.get(name).object;
            }
        }
        return (T) ret.object;
    }
}
