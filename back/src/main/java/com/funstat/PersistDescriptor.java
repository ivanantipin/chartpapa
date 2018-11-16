package com.funstat;

import com.funstat.finam.Symbol;
import com.funstat.store.MdDao;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistDescriptor<T> {
    String table;
    Class<T> clazz;
    Function<T,String> keyExtractor;

    public PersistDescriptor(String table, Class<T> clazz, Function<T, String> keyExtractor) {
        this.table = table;
        this.clazz = clazz;
        this.keyExtractor = keyExtractor;
    }

    public List<T> read(MdDao mdDao){
        return mdDao.readGeneric(table, clazz);
    }

    public T readByKey(MdDao dao, String key){
        return readAsMap(dao).get(key);
    }



    public Map<String,T> readAsMap(MdDao mdDao){
        return mdDao.readGeneric(table, clazz).stream().collect(Collectors.toMap(s->keyExtractor.apply(s),s->s));
    }

    public void write(MdDao dao, List<T> data){
        dao.saveGeneric(table, data, keyExtractor);
    }

    public void writeSingle(MdDao dao, T data){
        dao.saveGeneric(table, Collections.singletonList(data),keyExtractor);
    }
}

