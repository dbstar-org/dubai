package io.github.dbstarll.dubai.model.service;

import com.mongodb.client.FindIterable;
import io.github.dbstarll.dubai.model.entity.Entity;
import io.github.dbstarll.dubai.model.service.impl.ServiceImplemental;
import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Implementation(ServiceImplemental.class)
public interface Service<E extends Entity> extends Base {
    Class<E> getEntityClass();

    long count(Bson filter);

    boolean contains(ObjectId id);

    FindIterable<E> find(Bson filter);

    E findOne(Bson filter);

    E findById(ObjectId id);

    E deleteById(ObjectId id);

    /**
     * 插入或更新一个实体.
     *
     * <p>
     * 在entity的id不为null时，执行更新操作，将lastModified更新为当前时间。
     * 在entity的id为null时，执行插入操作，生成一个新的id，并且则设置entity的dateCreated为当前时间。
     * </p>
     *
     * @param entity 需要插入或更新的实体
     * @return 返回更新后的实体
     * @deprecated Use {@link Service#save(Entity, Validate)}.
     */
    @Deprecated
    E save(E entity);

    /**
     * 插入或更新一个实体.
     *
     * <p>
     * 在entity的id不为null时，执行更新操作，将lastModified更新为当前时间。 在entity的id为null时，执行插入操作：
     * 这时如果传入的newEntityId也为null，则生成一个新的id，否则使用传入的newEntityId。
     * </p>
     *
     * @param entity      需要插入或更新的实体
     * @param newEntityId 插入时指定entity的id
     * @return 返回更新后的实体
     * @deprecated Use {@link ServiceSaver#save(Entity, ObjectId, Validate)}.
     */
    @Deprecated
    E save(E entity, ObjectId newEntityId);

    /**
     * 对实体进行校验然后保存. 有以下几种情况：
     * <ol>
     * <li>校验通过，且有被修改的内容，返回更新后的实体</li>
     * <li>校验通过，但没有被修改的内容，返回null</li>
     * <li>校验未通过，且有{@link Validate}，在{@link Validate}中填充校验结果，返回null</li>
     * <li>校验未通过，且未设置{@link Validate}，抛出ValidateException</li>
     * </ol>
     *
     * @param entity   需要插入或更新的实体
     * @param validate 校验结果容器
     * @return 返回更新后的实体，若未执行更新操作，则返回null
     * @throws ValidateException 如果校验未通过，且未设置校验结果容器，则抛出此异常
     */
    E save(E entity, Validate validate) throws ValidateException;
}
