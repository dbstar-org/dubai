package io.github.dbstarll.dubai.model.service.validation;

import io.github.dbstarll.dubai.model.entity.Entity;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public final class ValidationContext implements AutoCloseable {
    private static final ThreadLocal<ValidationContext> CONTEXT = ThreadLocal.withInitial(ValidationContext::new);

    /**
     * 获得一个ThreadLocal的ValidationContext实例.
     *
     * @return ValidationContext实例
     */
    public static ValidationContext get() {
        return CONTEXT.get().clear();
    }

    private final ConcurrentMap<ObjectId, Entity> entityMap;

    private ValidationContext() {
        this.entityMap = new ConcurrentHashMap<>();
    }

    private ValidationContext clear() {
        this.entityMap.clear();
        return this;
    }

    /**
     * 获取并缓存实体，供其他Validation再次使用时无需重复加载.
     *
     * @param entityId     实体Id
     * @param entityLoader 实体加载器
     * @param <E>          实体类型
     * @return 实体Id对应的实体
     */
    @SuppressWarnings("unchecked")
    public <E extends Entity> Optional<E> getEntity(final ObjectId entityId, final Function<ObjectId, E> entityLoader) {
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
