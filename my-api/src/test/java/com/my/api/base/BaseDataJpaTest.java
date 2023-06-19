package com.my.api.base;

import com.my.api.config.DataJpaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;

@Import(DataJpaConfig.class)
public abstract class BaseDataJpaTest implements DefaultTestProfile {

//    @Autowired
    protected EntityManager entityManager;
}
