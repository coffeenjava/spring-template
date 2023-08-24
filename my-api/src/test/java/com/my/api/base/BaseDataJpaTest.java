package com.my.api.base;

import com.my.api.config.db.DataJpaConfig;
import com.my.api.config.db.QuerydslConfig;
import org.springframework.context.annotation.Import;

@Import({
        DataJpaConfig.class,
        QuerydslConfig.class
})
public abstract class BaseDataJpaTest implements DefaultTestProfile {
}
