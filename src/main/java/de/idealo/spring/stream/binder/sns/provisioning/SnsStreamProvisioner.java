package de.idealo.spring.stream.binder.sns.provisioning;

import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.support.destination.DynamicTopicDestinationResolver;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.messaging.core.DestinationResolver;

import com.amazonaws.services.sns.AmazonSNS;

public class SnsStreamProvisioner implements ProvisioningProvider<ConsumerProperties, ProducerProperties> {

    private final DestinationResolver<String> destinationResolver;

    public SnsStreamProvisioner(AmazonSNS amazonSNS, ResourceIdResolver resourceIdResolver) {
        this.destinationResolver = new DynamicTopicDestinationResolver(amazonSNS, resourceIdResolver);
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name, ProducerProperties properties) {
        String arn = this.destinationResolver.resolveDestination(name);
        return new SnsProducerDestination(name, arn);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group, ConsumerProperties properties) {
        throw new ProvisioningException("not supported");
    }
}
