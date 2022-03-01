package de.idealo.spring.stream.binder.sns.properties;

public class SnsProducerProperties {

    /**
     * Determines whether a message is sent synchronously.
     *
     * Default is false
     */
    boolean sync;

    public void setSync(final boolean sync) {
        this.sync = sync;
    }

    public boolean isSync() {
        return sync;
    }
}
