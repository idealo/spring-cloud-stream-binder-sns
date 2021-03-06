package de.idealo.spring.stream.binder.sns.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.AuthorizationErrorException;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;

import de.idealo.spring.stream.binder.sns.SnsMessageHandlerBinder;

@ExtendWith(MockitoExtension.class)
class SnsBinderHealthIndicatorTest {

    @Mock
    private SnsMessageHandlerBinder snsMessageHandlerBinder;

    @Mock
    private AmazonSNSAsync amazonSNS;

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
        when(amazonSNS.listTopics()).thenReturn(new ListTopicsResult().withTopics(new Topic().withTopicArn("blablabla:somemorebla:topicName")));
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
        when(amazonSNS.listTopics()).thenReturn(new ListTopicsResult().withTopics(new Topic().withTopicArn("blablabla:somemorebla:topicName1")), new ListTopicsResult().withTopics(new Topic().withTopicArn("blablabla:somemorebla:topicName2")));
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
        when(amazonSNS.listTopics()).thenReturn(new ListTopicsResult().withTopics(new Topic().withTopicArn("blablabla:somemorebla:topicName1")), new ListTopicsResult().withTopics(new Topic().withTopicArn("blablabla:somemorebla:topicName2")));
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
        when(amazonSNS.listTopics()).thenReturn(new ListTopicsResult().withTopics(new Topic().withTopicArn("blablabla:somemorebla:wrongTopicName")));
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
    void reportsFalseIfCannotListTopics() {
        when(amazonSNS.listTopics()).thenThrow(AuthorizationErrorException.class);

        Health.Builder builder = new Health.Builder();

        healthIndicator.doHealthCheck(builder);

        Health health = builder.build();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("SNS");
    }
}
