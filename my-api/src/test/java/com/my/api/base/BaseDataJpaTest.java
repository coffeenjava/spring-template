package com.my.api.base;

import com.my.api.config.TestDataJpaConfig;
import org.springframework.context.annotation.Import;

@Import(TestDataJpaConfig.class)
public abstract class BaseDataJpaTest implements DefaultTestProfile {
}
