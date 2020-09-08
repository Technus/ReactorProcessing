package com.github.technus.processingReactor;

import java.util.function.Function;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;

public class Utility {
    @SuppressWarnings("rawtypes")
    private static final Function NO_OPERATION = o -> o;
    public static boolean DEBUG = isDebug();

    private Utility() {
    }

    public static boolean isDebug(){
        return getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp");
    }

    @SuppressWarnings("unchecked")
    public static <T,R> Function<T,R> noOperation(){
        return (Function<T, R>) NO_OPERATION;
    }
}
