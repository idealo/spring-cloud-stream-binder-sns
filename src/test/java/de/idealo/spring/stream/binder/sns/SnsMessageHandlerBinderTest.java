package de.idealo.spring.stream.binder.sns;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.aws.outbound.SnsMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.sns.AmazonSNSAsync;

import de.idealo.spring.stream.binder.sns.provisioning.SnsProducerDestination;

class SnsMessageHandlerBinderTest {

    @Test
    void createSnsMessageHandlerWithExpectedProperties() throws Exception {
        AmazonSNSAsync amazonSNS = mock(AmazonSNSAsync.class);
        SnsMessageHandlerBinder snsMessageHandlerBinder = new SnsMessageHandlerBinder(amazonSNS, null);

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        snsMessageHandlerBinder.setApplicationContext(applicationContext);

        SnsProducerDestination snsProducerDestination = new SnsProducerDestination("name", "arn");
        SnsMessageHandler producerMessageHandler = (SnsMessageHandler)snsMessageHandlerBinder.createProducerMessageHandler(snsProducerDestination, null, null);

        assertThat(producerMessageHandler).isNotNull();
        assertThat((AmazonSNSAsync) ReflectionTestUtils.getField(producerMessageHandler, "amazonSns")).isEqualTo(amazonSNS);
        assertThat(((LiteralExpression) ReflectionTestUtils.getField(producerMessageHandler, "topicArnExpression")).getValue()).isEqualTo("arn");
        assertThat((MessageChannel) ReflectionTestUtils.getField(producerMessageHandler, "failureChannel")).isNull();
        assertThat((BeanFactory) ReflectionTestUtils.getField(producerMessageHandler, "beanFactory")).isEqualTo(applicationContext.getBeanFactory());
        assertThat((Boolean) ReflectionTestUtils.getField(producerMessageHandler, "sync")).isFalse();
    }
}
