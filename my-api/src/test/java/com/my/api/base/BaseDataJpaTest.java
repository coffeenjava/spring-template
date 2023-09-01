package com.my.api.base;

import com.my.api.config.DataJpaConfig;
import com.my.api.config.QuerydslConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({
        DataJpaConfig.class,
        QuerydslConfig.class
})
@DataJpaTest
public abstract class BaseDataJpaTest implements DefaultTestProfile {
}
