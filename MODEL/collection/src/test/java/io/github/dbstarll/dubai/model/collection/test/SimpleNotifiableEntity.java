package io.github.dbstarll.dubai.model.collection.test;

import io.github.dbstarll.dubai.model.entity.Table;
import io.github.dbstarll.dubai.model.entity.func.Notifiable;

@Table
public interface SimpleNotifiableEntity extends SimpleEntity, Notifiable {
}
