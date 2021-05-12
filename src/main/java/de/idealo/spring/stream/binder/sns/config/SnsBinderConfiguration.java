package de.idealo.spring.stream.binder.sns.config;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;

import io.awspring.cloud.autoconfigure.messaging.SnsProperties;
import io.awspring.cloud.core.env.ResourceIdResolver;

import de.idealo.spring.stream.binder.sns.SnsMessageHandlerBinder;
import de.idealo.spring.stream.binder.sns.health.SnsBinderHealthIndicator;
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

    @Configuration
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnEnabledHealthIndicator("binders")
    protected static class SnsBinderHealthIndicatorConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "snsBinderHealthIndicator")
        public SnsBinderHealthIndicator snsBinderHealthIndicator(SnsMessageHandlerBinder snsMessageHandlerBinder, BindingServiceProperties bindingServiceProperties) {
            return new SnsBinderHealthIndicator(snsMessageHandlerBinder, bindingServiceProperties);
        }
    }
}
