package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.Table;

import java.util.Map;
import java.util.Set;

@Table
public interface SimpleEntity extends Entity {
    enum Type {
        t1, t2
    }

    Type getType();

    void setType(Type type);

    @Prop
    byte[] getBytes();

    @Prop
    void setBytes(byte[] bytes);

    void setSet(boolean set);

    boolean isSet();

    void set(String set);

    void setMore(String v1, String v2);

    String get();

    boolean is();

    Map<String, Set<String>> getTypeData();

    void setTypeData(Map<String, Set<String>> typeData);
}
