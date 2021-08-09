package io.github.dbstarll.dubai.model.service.attach;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Contentable;
import io.github.dbstarll.dubai.model.service.Implementation;
import io.github.dbstarll.dubai.model.service.impl.ContentAttachImplemental;

@Implementation(ContentAttachImplemental.class)
public interface ContentAttach<E extends Entity & Contentable> extends CoreAttachs {
}
