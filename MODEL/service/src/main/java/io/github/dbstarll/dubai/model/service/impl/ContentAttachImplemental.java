package io.github.dbstarll.dubai.model.service.impl;

import io.github.dbstarll.dubai.model.collection.Collection;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.entity.info.Contentable;
import io.github.dbstarll.dubai.model.service.Service;
import io.github.dbstarll.dubai.model.service.attach.ContentAttach;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validation.GeneralValidation;
import io.github.dbstarll.dubai.model.service.validation.Validation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public final class ContentAttachImplemental<E extends Entity & Contentable, S extends Service<E>>
        extends CoreImplementals<E, S> implements ContentAttach<E> {
    /**
     * 构建ContentAttachImplemental.
     *
     * @param service    service
     * @param collection collection
     */
    public ContentAttachImplemental(final S service, final Collection<E> collection) {
        super(service, collection);
    }

    /**
     * contentValidation.
     *
     * @return contentValidation
     */
    @GeneralValidation(Contentable.class)
    public Validation<E> contentValidation() {
        return new AbstractBaseEntityValidation<Contentable>(Contentable.class) {
            @Override
            protected void validate(final Contentable entity, final Contentable original, final Validate validate) {
                if (ArrayUtils.isEmpty(entity.getContent())) {
                    validate.addFieldError(Contentable.FIELD_NAME_CONTENT, "内容未设置");
                }
                final String contentType = entity.getContentType();
                if (original == null || !StringUtils.equals(contentType, original.getContentType())) {
                    if (StringUtils.isBlank(contentType)) {
                        validate.addFieldError(Contentable.FIELD_NAME_CONTENT_TYPE, "内容类型未设置");
                    } else {
                        final int index = contentType.indexOf('/');
                        if (index < 0) {
                            validate.addFieldError(Contentable.FIELD_NAME_CONTENT_TYPE, "不符合格式：主类型/子类型");
                        } else if (index == 0) {
                            validate.addFieldError(Contentable.FIELD_NAME_CONTENT_TYPE, "缺少主类型");
                        } else if (index == contentType.length() - 1) {
                            validate.addFieldError(Contentable.FIELD_NAME_CONTENT_TYPE, "缺少子类型");
                        } else if (contentType.indexOf('/', index + 1) > 0) {
                            validate.addFieldError(Contentable.FIELD_NAME_CONTENT_TYPE, "只能有一个子类型");
                        }
                    }
                }
            }
        };
    }
}
