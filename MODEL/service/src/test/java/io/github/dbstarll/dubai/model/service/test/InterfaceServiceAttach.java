package io.github.dbstarll.dubai.model.service.test;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.test.ClassEntity;
import io.github.dbstarll.dubai.model.service.Implementation;
import org.bson.types.ObjectId;

@Implementation(InterfaceServiceImplemental.class)
public interface InterfaceServiceAttach extends TestAttachs {
    boolean contains(ObjectId id);

    void throwException() throws Exception;

    <T> T call(T value);

    String[] call(String... values);

    String[] call(boolean bol, String... values);

    <T extends Entity> T call1(T value);

    void call2(Collection<? extends Entity> value);

    <T extends ClassEntity> T call3(T value);

    <T extends Collection<? extends Entity>> T call4(T value);
}
