package com.github.dingo;

/**
 * 
 * @Description: LifeCycle
 * @Author: Fred Feng
 * @Date: 28/12/2024
 * @Version 1.0.0
 */
public interface LifeCycle {

    default void open() {}

    default void close() {}

    boolean isOpened();

}
