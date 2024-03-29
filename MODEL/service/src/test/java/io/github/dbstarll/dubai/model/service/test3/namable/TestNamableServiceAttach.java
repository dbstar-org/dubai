package io.github.dbstarll.dubai.model.service.test3.namable;

import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.ServiceSaver;
import io.github.dbstarll.dubai.model.service.test.TestAttachs;
import org.bson.conversions.Bson;

@Implementation(TestNamableServiceImplemental.class)
public interface TestNamableServiceAttach extends TestAttachs, ServiceSaver<TestNamableEntity> {
    void name(int minLength, int maxLength);

    Bson filter(final Bson filter);
}
