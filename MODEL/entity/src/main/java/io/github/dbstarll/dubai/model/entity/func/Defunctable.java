package io.github.dbstarll.dubai.model.entity.func;

import io.github.dbstarll.dubai.model.entity.FunctionalBase;
import io.github.dbstarll.dubai.model.entity.InfoBase;

public interface Defunctable extends InfoBase, FunctionalBase {
    String FIELD_NAME_DEFUNCT = "defunct";

    boolean isDefunct();

    void setDefunct(boolean defunct);
}
