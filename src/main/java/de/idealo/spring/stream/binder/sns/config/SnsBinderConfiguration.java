package de.idealo.spring.stream.binder.sns.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;

import de.idealo.spring.stream.binder.sns.SnsMessageHandlerBinder;
import de.idealo.spring.stream.binder.sns.provisioning.SnsStreamProvisioner;

@Configuration
@ConditionalOnMissingBean(Binder.class)
public class SnsBinderConfiguration {

    @Bean
    public SnsStreamProvisioner provisioningProvider(AmazonSNS amazonSNS, ResourceIdResolver resourceIdResolver) {
        return new SnsStreamProvisioner(amazonSNS, resourceIdResolver);
    }

    @Bean
    public SnsMessageHandlerBinder snsMessageHandlerBinder(AmazonSNSAsync amazonSNS, SnsStreamProvisioner snsStreamProvisioner) {
        return new SnsMessageHandlerBinder(amazonSNS, snsStreamProvisioner);
    }

}
