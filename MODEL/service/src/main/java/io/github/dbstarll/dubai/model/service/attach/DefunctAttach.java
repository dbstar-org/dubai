package io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.func.Defunctable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.impl.DefunctAttachImplemental;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Implementation(DefunctAttachImplemental.class)
public interface DefunctAttach<E extends Entity & Defunctable> extends CoreAttachs {
    Bson filterByDefunct(boolean defunct);

    boolean contains(ObjectId id, Boolean defunct);

    FindIterable<E> find(Bson filter, Boolean defunct);

    E findById(ObjectId id, Boolean defunct);

    long count(Bson filter, Boolean defunct);
}
