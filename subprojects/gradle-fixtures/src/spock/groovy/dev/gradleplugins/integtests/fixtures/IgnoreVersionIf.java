package dev.gradleplugins.integtests.fixtures;

import groovy.lang.Closure;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface IgnoreVersionIf {
    Class<Closure> value();
}
