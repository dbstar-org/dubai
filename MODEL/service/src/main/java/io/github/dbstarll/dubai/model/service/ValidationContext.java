package io.github.dbstarll.dubai.model.service;

import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apache.commons.lang3.Validate.notNull;

public final class ValidationContext implements AutoCloseable {
    private static final ThreadLocal<ValidationContext> CONTEXT = ThreadLocal.withInitial(ValidationContext::new);

    static ValidationContext get() {
        return CONTEXT.get();
    }

    /**
     * 获取并缓存实体，供其他Validation再次使用时无需重复加载.
     *
     * @param entityId     实体Id
     * @param entityLoader 实体加载器
     * @param <E>          实体类型
     * @return 实体Id对应的实体
     */
    public static <E extends Entity> Optional<E> getEntity(final ObjectId entityId,
                                                           final Function<ObjectId, E> entityLoader) {
        return get().loadEntity(entityId, notNull(entityLoader, "entityLoader is null"));
    }

    /**
     * 获取并缓存实体，供其他Validation再次使用时无需重复加载.
     *
     * @param entityId 实体Id
     * @param <E>      实体类型
     * @return 实体Id对应的实体
     */
    public static <E extends Entity> Optional<E> getEntity(final ObjectId entityId) {
        return get().loadEntity(entityId, null);
    }

    /**
     * 获取并缓存实体，供其他Validation再次使用时无需重复加载.
     *
     * @param entityId        实体Id
     * @param serviceSupplier 提供实体对应的服务
     * @param <E>             实体类型
     * @return 实体Id对应的实体
     */
    public static <E extends Entity> Optional<E> getEntity(final ObjectId entityId,
                                                           final Supplier<Service<E>> serviceSupplier) {
        notNull(serviceSupplier, "serviceSupplier is null");
        return getEntity(entityId, notNull(serviceSupplier.get(), "service is null")::findById);
    }

    private final ConcurrentMap<ObjectId, Entity> entityMap;

    private ValidationContext() {
        this.entityMap = new ConcurrentHashMap<>();
    }

    ValidationContext clear() {
        this.entityMap.clear();
        return this;
    }

    @SuppressWarnings("unchecked")
    private <E extends Entity> Optional<E> loadEntity(final ObjectId entityId,
                                                      final Function<ObjectId, E> entityLoader) {
        if (entityId == null) {
            return Optional.empty();
        } else if (entityLoader == null) {
            return Optional.ofNullable((E) this.entityMap.get(entityId));
        } else {
            return Optional.ofNullable((E) this.entityMap.computeIfAbsent(entityId, entityLoader));
        }
    }

    @Override
    public void close() {
        clear();
        CONTEXT.remove();
    }
}
