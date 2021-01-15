package de.idealo.spring.stream.binder.sns.provisioning;

import org.springframework.cloud.stream.provisioning.ProducerDestination;

public class SnsProducerDestination implements ProducerDestination {

    private final String name;
    private final String arn;

    public SnsProducerDestination(final String name, final String arn) {
        this.name = name;
        this.arn = arn;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNameForPartition(final int partition) {
        throw new UnsupportedOperationException("Partitioning is not supported for SQS");
    }

    public String getArn() {
        return arn;
    }
}
