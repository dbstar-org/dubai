package io.github.dbstarll.dubai.model.service.attach;

import com.mongodb.client.result.UpdateResult;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Sourceable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.impl.SourceAttachImplemental;
import org.bson.types.ObjectId;

import java.util.Map;

@Implementation(SourceAttachImplemental.class)
public interface SourceAttach<E extends Entity & Sourceable> extends CoreAttachs {
    UpdateResult mergeSource(String source, ObjectId from, ObjectId to);

    UpdateResult updateSource(ObjectId entityId, Map<String, ObjectId> sources);

    UpdateResult removeSource(ObjectId entityId, Map<String, ObjectId> sources);
}