package de.idealo.spring.stream.binder.sns.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

public class SnsBindingProperties implements BinderSpecificPropertiesProvider {

    private SnsConsumerProperties consumer = new SnsConsumerProperties();

    private SnsProducerProperties producer = new SnsProducerProperties();

    @Override
    public SnsConsumerProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(final SnsConsumerProperties consumer) {
        this.consumer = consumer;
    }

    @Override
    public SnsProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(final SnsProducerProperties producer) {
        this.producer = producer;
    }

}
