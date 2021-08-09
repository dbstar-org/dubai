package io.github.dbstarll.dubai.model.entity.test;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;

@Table
public interface InterfaceEntity extends NoTableEntity, Defunctable {
    enum Type {
        typeA, typeB
    }

    int getIntFromInterfaceEntity();

    void setIntFromInterfaceEntity(int intFromInterfaceEntity);

    String getStringFromInterfaceEntity();

    void setStringFromInterfaceEntity(String stringFromInterfaceEntity);

    void getNoReturn();

    int getIntWithParam(int value);

    int obtainInt();

    int setIntWithReturn(int value);

    void setNoParam();

    void giveInt(int value);

    void setType(Type type);

    Type getType();
}
