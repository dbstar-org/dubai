package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import org.bson.types.ObjectId;

public interface ServiceDeleter<E extends Entity> {
    /**
     * 对实体进行校验然后删除. 有以下几种情况：
     * <ol>
     * <li>实体未找到，返回null</li>
     * <li>校验通过，返回被删除的实体</li>
     * <li>校验未通过，且有{@link Validate}，在{@link Validate}中填充校验结果，返回null</li>
     * <li>校验未通过，且未设置{@link Validate}，抛出ValidateException</li>
     * </ol>
     *
     * @param id       待删除的实体id
     * @param validate 校验结果容器
     * @return 返回被删除的实体，若未执行删除操作，则返回null
     * @throws ValidateException 如果校验未通过，且未设置校验结果容器，则抛出此异常
     */
    E deleteById(ObjectId id, Validate validate) throws ValidateException;
}
