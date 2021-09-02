package com.brian.api.common.model;

public interface BaseEntity extends BaseModel {

    default void setCreator(Integer id) {}
    default void setUpdater(Integer id) {}
}