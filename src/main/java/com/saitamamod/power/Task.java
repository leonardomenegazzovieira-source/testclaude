package com.saitamamod.power;

/** Trabalho de destruição que roda aos poucos, tick a tick, para não travar o servidor. */
public interface Task {
    /** @return true quando terminou e pode ser descartado */
    boolean tick();
}
