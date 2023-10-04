package de.idealo.spring.stream.binder.sns.provisioning;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

import io.awspring.cloud.sns.core.TopicArnResolver;
import software.amazon.awssdk.arns.Arn;

import de.idealo.spring.stream.binder.sns.properties.SnsConsumerProperties;
import de.idealo.spring.stream.binder.sns.properties.SnsProducerProperties;

public class SnsStreamProvisioner implements ProvisioningProvider<ExtendedConsumerProperties<SnsConsumerProperties>, ExtendedProducerProperties<SnsProducerProperties>> {

    private final TopicArnResolver destinationResolver;

    public SnsStreamProvisioner(TopicArnResolver topicArnResolver) {
        this.destinationResolver = topicArnResolver;
    }

    public TopicArnResolver getDestinationResolver(){
        return this.destinationResolver;
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name, ExtendedProducerProperties<SnsProducerProperties> properties) {
        Arn arn = this.destinationResolver.resolveTopicArn(name);
        return new SnsProducerDestination(name, arn.resourceAsString());
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group, ExtendedConsumerProperties<SnsConsumerProperties> properties) {
        throw new ProvisioningException("not supported");
    }
}
