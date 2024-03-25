package com.github.alantr7.bukkitplugin.annotations.processor.processing;

public class ProcessChain {

    boolean isCanceled = false;

    public boolean isCanceled() {
        return isCanceled;
    }

    public void cancel() {
        isCanceled = true;
    }

}
