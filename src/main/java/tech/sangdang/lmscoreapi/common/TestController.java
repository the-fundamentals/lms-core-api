package tech.sangdang.lmscoreapi.common;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Profile({"local", "test"})
@RestController
public @interface TestController {}
