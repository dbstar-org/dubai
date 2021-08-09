package io.github.dbstarll.dubai.model.service.test;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.entity.test.InterfaceEntity;
import io.github.dbstarll.dubai.model.service.EntityService;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@EntityService
public class ClassService implements TestServices<InterfaceEntity> {
    @Override
    public Class<InterfaceEntity> getEntityClass() {
        return InterfaceEntity.class;
    }

    @Override
    public long count(Bson filter) {
        return 0;
    }

    @Override
    public boolean contains(ObjectId id) {
        return false;
    }

    @Override
    public FindIterable<InterfaceEntity> find(Bson filter) {
        return null;
    }

    @Override
    public InterfaceEntity findOne(Bson filter) {
        return null;
    }

    @Override
    public InterfaceEntity findById(ObjectId id) {
        return null;
    }

    @Override
    public InterfaceEntity deleteById(ObjectId id) {
        return null;
    }

    @Override
    public InterfaceEntity save(InterfaceEntity entity) {
        return null;
    }

    @Override
    public InterfaceEntity save(InterfaceEntity entity, ObjectId newEntityId) {
        return null;
    }

    @Override
    public InterfaceEntity save(InterfaceEntity entity, Validate validate) throws ValidateException {
        return null;
    }

    @Override
    public void callTest(InterfaceEntity entity) {
    }
}
