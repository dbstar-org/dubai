package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import org.bson.types.ObjectId;

public interface ServiceSaver<E extends Entity> {
    /**
     * 对实体进行校验然后保存. 有以下几种情况：
     * <ol>
     * <li>校验通过，且有被修改的内容，返回更新后的实体</li>
     * <li>校验通过，但没有被修改的内容，返回null</li>
     * <li>校验未通过，且有{@link Validate}，在{@link Validate}中填充校验结果，返回null</li>
     * <li>校验未通过，且未设置{@link Validate}，抛出ValidateException</li>
     * </ol>
     *
     * @param entity      需要插入或更新的实体
     * @param newEntityId 插入时指定entity的id
     * @param validate    校验结果容器
     * @return 返回更新后的实体，若未执行更新操作，则返回null
     * @throws ValidateException 如果校验未通过，且未设置校验结果容器，则抛出此异常
     */
    E save(E entity, ObjectId newEntityId, Validate validate) throws ValidateException;
}
