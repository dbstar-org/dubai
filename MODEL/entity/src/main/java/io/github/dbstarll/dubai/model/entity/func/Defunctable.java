package io.github.dbstarll.dubai.model.entity.func;

import io.github.dbstarll.dubai.model.entity.FunctionalBase;
import io.github.dbstarll.dubai.model.entity.InfoBase;

/**
 * 预置了实体是否失效(伪删除)的信息和功能类实体，功能类实体不应该由用户设置，而是系统内置设置.
 */
public interface Defunctable extends InfoBase, FunctionalBase {
    String FIELD_NAME_DEFUNCT = "defunct";

    /**
     * 检测实体是否失效.
     *
     * @return 是否失效
     */
    boolean isDefunct();

    /**
     * 设置是否失效.
     *
     * @param defunct 是否失效
     */
    void setDefunct(boolean defunct);
}
