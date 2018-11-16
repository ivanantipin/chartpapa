package com.funstat.store;

import java.util.List;
import java.util.function.Function;

public interface GenericDao {
    <T> void saveGeneric(String type, List<T> obj, Function<T, String> keyMapper);

    <T> List<T> readGeneric(String tableName, Class<T> clazz);
}
