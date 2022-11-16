package de.idealo.spring.stream.binder.sns.config;

import de.idealo.spring.stream.binder.sns.properties.SnsExtendedBindingProperties;
import java.util.Optional;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;

import io.awspring.cloud.core.env.ResourceIdResolver;

import de.idealo.spring.stream.binder.sns.SnsMessageHandlerBinder;
import de.idealo.spring.stream.binder.sns.health.SnsBinderHealthIndicator;
import de.idealo.spring.stream.binder.sns.provisioning.SnsStreamProvisioner;

@Configuration
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({ SnsExtendedBindingProperties.class })
public class SnsBinderConfiguration {

    @Bean
    public SnsStreamProvisioner provisioningProvider(AmazonSNS amazonSNS, Optional<ResourceIdResolver> resourceIdResolver) {
        return new SnsStreamProvisioner(amazonSNS, resourceIdResolver.orElse(null));
    }

    @Bean
    public SnsMessageHandlerBinder snsMessageHandlerBinder(AmazonSNSAsync amazonSNS,
                                                           SnsStreamProvisioner snsStreamProvisioner,
                                                           SnsExtendedBindingProperties snsExtendedBindingProperties) {
        return new SnsMessageHandlerBinder(amazonSNS, snsStreamProvisioner, snsExtendedBindingProperties);
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
