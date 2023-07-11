package de.idealo.spring.stream.binder.sns.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;

import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.AuthorizationErrorException;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.Topic;

import de.idealo.spring.stream.binder.sns.SnsMessageHandlerBinder;

@ExtendWith(MockitoExtension.class)
class SnsBinderHealthIndicatorTest {

    @Mock
    private SnsMessageHandlerBinder snsMessageHandlerBinder;

    @Mock
    private SnsAsyncClient amazonSNS;

    @Mock
    private BindingServiceProperties bindingServiceProperties;

    @InjectMocks
    private SnsBinderHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        lenient().when(snsMessageHandlerBinder.getAmazonSNS()).thenReturn(amazonSNS);
    }

    @Test
    void reportsTrueWhenAllTopicsCanBeListed() {
        when(amazonSNS.listTopics()).thenReturn(CompletableFuture.completedFuture(ListTopicsResponse.builder().topics(Topic.builder().topicArn("blablabla:somemorebla:topicName").build()).build()));
        final BindingProperties binderProperties = new BindingProperties();
        binderProperties.setDestination("topicName");
        binderProperties.setBinder("sns");
        when(bindingServiceProperties.getBindings()).thenReturn(Collections.singletonMap("doesn't matter", binderProperties));

        Health.Builder builder = new Health.Builder();

        healthIndicator.doHealthCheck(builder);

        assertThat(builder.build().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void reportsTrueWhenMoreTopicsThenDestinationsArePresent() {
        when(amazonSNS.listTopics()).thenReturn(
                CompletableFuture.completedFuture(ListTopicsResponse.builder().topics(
                                Topic.builder().topicArn("blablabla:somemorebla:topicName1").build(),
                                Topic.builder().topicArn("blablabla:somemorebla:topicName2").build())
                        .build())
        );
        final BindingProperties binderProperties = new BindingProperties();
        binderProperties.setDestination("topicName1");
        binderProperties.setBinder("sns");
        when(bindingServiceProperties.getBindings()).thenReturn(Collections.singletonMap("doesn't matter", binderProperties));

        Health.Builder builder = new Health.Builder();

        healthIndicator.doHealthCheck(builder);

        assertThat(builder.build().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void filtersOutNonSnsBinders() {
        when(amazonSNS.listTopics()).thenReturn(
                CompletableFuture.completedFuture(ListTopicsResponse.builder().topics(
                                Topic.builder().topicArn("blablabla:somemorebla:topicName1").build(),
                                Topic.builder().topicArn("blablabla:somemorebla:topicName2").build())
                        .build())
        );
        Map<String, BindingProperties> bindings = new HashMap<>();
        final BindingProperties binderPropertiesSns = new BindingProperties();
        binderPropertiesSns.setDestination("topicName1");
        binderPropertiesSns.setBinder("sns");
        bindings.put("doesn't matter", binderPropertiesSns);

        final BindingProperties binderPropertiesKafka = new BindingProperties();
        binderPropertiesKafka.setDestination("topicName2");
        binderPropertiesKafka.setBinder("kafka");
        bindings.put("still doesn't matter", binderPropertiesKafka);
        when(bindingServiceProperties.getBindings()).thenReturn(bindings);

        Health.Builder builder = new Health.Builder();

        healthIndicator.doHealthCheck(builder);

        assertThat(builder.build().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void reportsFalseWhenAnExpectedTopicIsNotPresent() {
        when(amazonSNS.listTopics()).thenReturn(CompletableFuture.completedFuture(ListTopicsResponse.builder().topics(Topic.builder().topicArn("blablabla:somemorebla:wrongTopicName").build()).build()));
        final BindingProperties binderProperties = new BindingProperties();
        binderProperties.setDestination("topicName");
        binderProperties.setBinder("sns");
        when(bindingServiceProperties.getBindings()).thenReturn(Collections.singletonMap("doesn't matter", binderProperties));

        Health.Builder builder = new Health.Builder();

        healthIndicator.doHealthCheck(builder);

        Health health = builder.build();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("SNS");
    }

    @Test
    void checkThatHealthIndicatorDontCatchAnyExceptions() {
        when(amazonSNS.listTopics()).thenThrow(AuthorizationErrorException.class);

        AuthorizationErrorException thrown = assertThrows(
                "Expected doHealthCheck(new Health.Builder()) to throw, but it didn't",
                AuthorizationErrorException.class,
                () -> healthIndicator.doHealthCheck(new Health.Builder())
        );
    }
}
