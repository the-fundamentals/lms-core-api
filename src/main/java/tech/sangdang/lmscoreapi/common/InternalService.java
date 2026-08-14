package tech.sangdang.lmscoreapi.common;

import java.lang.annotation.*;
import org.springframework.stereotype.Service;

@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface InternalService {}
