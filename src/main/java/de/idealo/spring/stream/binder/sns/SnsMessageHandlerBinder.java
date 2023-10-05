package de.idealo.spring.stream.binder.sns;

import org.springframework.cloud.stream.binder.AbstractMessageChannelBinder;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.aws.outbound.SnsMessageHandler;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.services.sns.SnsAsyncClient;

import de.idealo.spring.stream.binder.sns.properties.SnsConsumerProperties;
import de.idealo.spring.stream.binder.sns.properties.SnsExtendedBindingProperties;
import de.idealo.spring.stream.binder.sns.properties.SnsProducerProperties;
import de.idealo.spring.stream.binder.sns.provisioning.SnsProducerDestination;
import de.idealo.spring.stream.binder.sns.provisioning.SnsStreamProvisioner;

public class SnsMessageHandlerBinder
        extends AbstractMessageChannelBinder<ExtendedConsumerProperties<SnsConsumerProperties>, ExtendedProducerProperties<SnsProducerProperties>, SnsStreamProvisioner>
        implements ExtendedPropertiesBinder<MessageChannel, SnsConsumerProperties, SnsProducerProperties> {

    private final SnsAsyncClient amazonSNS;

    private final SnsExtendedBindingProperties extendedBindingProperties;

    public SnsMessageHandlerBinder(SnsAsyncClient amazonSNS,
            SnsStreamProvisioner provisioningProvider,
            SnsExtendedBindingProperties extendedBindingProperties) {
        super(new String[0], provisioningProvider);
        this.amazonSNS = amazonSNS;
        this.extendedBindingProperties = extendedBindingProperties;
    }

    public SnsAsyncClient getAmazonSNS() {
        return amazonSNS;
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination, ExtendedProducerProperties<SnsProducerProperties> producerProperties, MessageChannel errorChannel) throws Exception {
        SnsProducerDestination snsDestination = (SnsProducerDestination) destination;
        SnsMessageHandler snsMessageHandler = new SnsMessageHandler(amazonSNS);
        snsMessageHandler.setTopicArn(snsDestination.getArn());
        snsMessageHandler.setTopicArnResolver(provisioningProvider.getDestinationResolver());
        snsMessageHandler.setBeanFactory(getBeanFactory());

        if (StringUtils.hasText(producerProperties.getExtension().getConfirmAckChannel())) {
            snsMessageHandler.setOutputChannelName(producerProperties.getExtension().getConfirmAckChannel());
        }

        return snsMessageHandler;
    }

    @Override
    protected MessageProducer createConsumerEndpoint(ConsumerDestination destination, String group, ExtendedConsumerProperties<SnsConsumerProperties> properties) throws Exception {
        throw new UnsupportedOperationException("Consuming from SNS is not supported");
    }

    @Override
    protected void postProcessOutputChannel(MessageChannel outputChannel, ExtendedProducerProperties<SnsProducerProperties> producerProperties) {
        ((AbstractMessageChannel) outputChannel).addInterceptor(new SnsPayloadConvertingChannelInterceptor());
    }

    @Override
    public SnsConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.extendedBindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public SnsProducerProperties getExtendedProducerProperties(String channelName) {
        return this.extendedBindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return this.extendedBindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.extendedBindingProperties.getExtendedPropertiesEntryClass();
    }

}
