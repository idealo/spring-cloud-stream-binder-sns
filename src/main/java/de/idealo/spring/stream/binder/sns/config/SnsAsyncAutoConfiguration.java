package de.idealo.spring.stream.binder.sns.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.awspring.cloud.autoconfigure.core.AwsClientBuilderConfigurer;
import io.awspring.cloud.autoconfigure.core.AwsClientCustomizer;
import io.awspring.cloud.autoconfigure.sns.SnsProperties;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsAsyncClientBuilder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SnsAsyncClient.class)
public class SnsAsyncAutoConfiguration {

    /**
     * Based on {@link io.awspring.cloud.autoconfigure.sns.SnsAutoConfiguration#snsClient}
     */
    @ConditionalOnMissingBean
    @Bean
    public SnsAsyncClient amazonSNS(SnsProperties properties, AwsClientBuilderConfigurer awsClientBuilderConfigurer, ObjectProvider<AwsClientCustomizer<SnsAsyncClientBuilder>> configurer) {
        return awsClientBuilderConfigurer.configure(SnsAsyncClient.builder(), properties, configurer.getIfAvailable()).build();
    }

}
