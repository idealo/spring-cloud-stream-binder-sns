package de.idealo.spring.stream.binder.sns.provisioning;

import de.idealo.spring.stream.binder.sns.properties.SnsConsumerProperties;
import de.idealo.spring.stream.binder.sns.properties.SnsProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.messaging.core.DestinationResolver;

import com.amazonaws.services.sns.AmazonSNS;
import io.awspring.cloud.core.env.ResourceIdResolver;
import io.awspring.cloud.messaging.support.destination.DynamicTopicDestinationResolver;

public class SnsStreamProvisioner implements ProvisioningProvider<ExtendedConsumerProperties<SnsConsumerProperties>, ExtendedProducerProperties<SnsProducerProperties>> {

    private final DestinationResolver<String> destinationResolver;

    public SnsStreamProvisioner(AmazonSNS amazonSNS, ResourceIdResolver resourceIdResolver) {
        this.destinationResolver = new DynamicTopicDestinationResolver(amazonSNS, resourceIdResolver);
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name, ExtendedProducerProperties<SnsProducerProperties> properties) {
        String arn = this.destinationResolver.resolveDestination(name);
        return new SnsProducerDestination(name, arn);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group, ExtendedConsumerProperties<SnsConsumerProperties> properties) {
        throw new ProvisioningException("not supported");
    }
}
