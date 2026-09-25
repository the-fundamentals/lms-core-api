package tech.sangdang.lmscoreapi.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

// Same profile gate as @TestController; separate annotation because OpenAPI tag `_Dev` generates DevApi
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Profile({"local", "test"})
@RestController
public @interface DevController {}
