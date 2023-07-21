package de.idealo.spring.stream.binder.sns.health;

import de.idealo.spring.stream.binder.sns.SnsMessageHandlerBinder;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.util.Assert;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.Topic;

import java.util.List;
import java.util.stream.Collectors;

public class SnsBinderHealthIndicator extends AbstractHealthIndicator {

    private final SnsMessageHandlerBinder snsMessageHandlerBinder;
    private final BindingServiceProperties bindingServiceProperties;

    public SnsBinderHealthIndicator(final SnsMessageHandlerBinder snsMessageHandlerBinder, final BindingServiceProperties bindingServiceProperties) {
        Assert.notNull(snsMessageHandlerBinder, "SnsMessageHandlerBinder must not be null");
        this.snsMessageHandlerBinder = snsMessageHandlerBinder;
        Assert.notNull(bindingServiceProperties, "BindingServiceProperties must not be null");
        this.bindingServiceProperties = bindingServiceProperties;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        var availableTopics = snsMessageHandlerBinder.getAmazonSNS().listTopics()
                .thenApply(ListTopicsResponse::topics)
                .thenApply(List::stream)
                .join()
                .map(Topic::topicArn)
                .map(topicArn -> topicArn.substring(topicArn.lastIndexOf(':') + 1))
                .collect(Collectors.toSet());

        var availableDeclaredTopics = bindingServiceProperties.getBindings().values().stream()
                .filter(bindingProperties -> "sns".equalsIgnoreCase(bindingProperties.getBinder()))
                .map(BindingProperties::getDestination)
                .allMatch(declaredTopic -> availableTopics.contains(declaredTopic));

        if (availableDeclaredTopics) {
            builder.up();
        } else {
            builder.down().withDetail("SNS", "topic is not reachable");
        }
    }

}
