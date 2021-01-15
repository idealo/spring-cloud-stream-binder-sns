package de.idealo.spring.stream.binder.sns;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.aws.outbound.SnsMessageHandler;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import com.amazonaws.services.sns.AmazonSNSAsync;

import de.idealo.spring.stream.binder.sns.provisioning.SnsProducerDestination;
import de.idealo.spring.stream.binder.sns.provisioning.SnsStreamProvisioner;

public class SnsMessageHandlerBinder
        extends AbstractMessageChannelBinder<ConsumerProperties, ProducerProperties, SnsStreamProvisioner> {

    private final AmazonSNSAsync amazonSNS;

    public SnsMessageHandlerBinder(AmazonSNSAsync amazonSNS, SnsStreamProvisioner provisioningProvider) {
        super(new String[0], provisioningProvider);
        this.amazonSNS = amazonSNS;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination, ProducerProperties producerProperties, MessageChannel errorChannel) throws Exception {
        SnsProducerDestination snsDestination = (SnsProducerDestination) destination;
        SnsMessageHandler snsMessageHandler = new SnsMessageHandler(amazonSNS);
        snsMessageHandler.setTopicArn(snsDestination.getArn());
        snsMessageHandler.setFailureChannel(errorChannel);
        snsMessageHandler.setBeanFactory(getBeanFactory());
        return snsMessageHandler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group, ConsumerProperties properties) throws Exception {
        throw new UnsupportedOperationException("Consuming from SNS is not supported");
    }

    @Override
    protected void postProcessOutputChannel(MessageChannel outputChannel, ProducerProperties producerProperties) {
        ((AbstractMessageChannel) outputChannel).addInterceptor(new SnsPayloadConvertingChannelInterceptor());
    }

}
