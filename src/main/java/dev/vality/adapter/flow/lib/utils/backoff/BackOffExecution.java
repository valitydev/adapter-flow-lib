package dev.vality.adapter.flow.lib.utils.backoff;

@FunctionalInterface
public interface BackOffExecution {
    Long nextBackOff();
}
