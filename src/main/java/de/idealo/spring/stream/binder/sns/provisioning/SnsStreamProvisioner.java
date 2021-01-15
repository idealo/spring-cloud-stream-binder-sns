package de.idealo.spring.stream.binder.sns.provisioning;

import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.messaging.support.destination.DynamicTopicDestinationResolver;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.messaging.core.DestinationResolver;

import com.amazonaws.services.sns.AmazonSNS;

import de.idealo.spring.stream.binder.sns.properties.SnsProducerProperties;
import de.idealo.spring.stream.binder.sns.properties.SnsConsumerProperties;

public class SnsStreamProvisioner implements ProvisioningProvider<ExtendedConsumerProperties<SnsConsumerProperties>, ExtendedProducerProperties<SnsProducerProperties>> {

    private final DestinationResolver<String> destinationResolver;

    public SnsStreamProvisioner(AmazonSNS amazonSNS, ResourceIdResolver resourceIdResolver) {
        this.destinationResolver = new DynamicTopicDestinationResolver(amazonSNS, resourceIdResolver);
    }

    @Override
    public ProducerDestination provisionProducerDestination(final String name, final ExtendedProducerProperties<SnsProducerProperties> properties) throws ProvisioningException {
        String arn = this.destinationResolver.resolveDestination(name);
        return new SnsProducerDestination(name, arn);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(final String name, final String group, final ExtendedConsumerProperties<SnsConsumerProperties> properties) throws ProvisioningException {
        throw new ProvisioningException("not supported");
    }
}
