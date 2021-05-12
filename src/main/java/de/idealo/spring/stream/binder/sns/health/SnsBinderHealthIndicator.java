package de.idealo.spring.stream.binder.sns.health;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.util.Assert;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;

import de.idealo.spring.stream.binder.sns.SnsMessageHandlerBinder;

public class SnsBinderHealthIndicator extends AbstractHealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SnsBinderHealthIndicator.class);

    private final SnsMessageHandlerBinder snsMessageHandlerBinder;
    private final BindingServiceProperties bindingServiceProperties;
    private String topicArn;

    public SnsBinderHealthIndicator(final SnsMessageHandlerBinder snsMessageHandlerBinder, final BindingServiceProperties bindingServiceProperties) {
        Assert.notNull(snsMessageHandlerBinder, "SnsMessageHandlerBinder must not be null");
        this.snsMessageHandlerBinder = snsMessageHandlerBinder;
        Assert.notNull(bindingServiceProperties, "BindingServiceProperties must not be null");
        this.bindingServiceProperties = bindingServiceProperties;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        boolean allListenersRunning = true;

        final List<String> topicList = bindingServiceProperties.getBindings().values().stream().map(bindingProperties -> bindingProperties.getDestination()).collect(toList());

        if (!topicsAreReachable(topicList)) {
            builder.down().withDetail("SNS", "topic is not reachable");
            allListenersRunning = false;
        }

        if (allListenersRunning) {
            builder.up();
        }
    }

    private boolean topicsAreReachable(final List<String> expectedTopicList) {
        try {
            final ListTopicsResult listTopicsResult = this.snsMessageHandlerBinder.getAmazonSNS().listTopics();
            final List<String> actualTopicList = listTopicsResult.getTopics().stream().map(topic -> extractTopicName(topic.getTopicArn())).collect(toList());

            Collections.sort(expectedTopicList);
            Collections.sort(actualTopicList);
            return expectedTopicList.equals(actualTopicList);
        } catch (SdkClientException e) {
            LOGGER.error("SNS is not reachable", e);
            return false;
        }
    }

    private String extractTopicName(final String topicArn) {
        return topicArn.substring(topicArn.lastIndexOf(':') + 1);
    }
}
