package de.idealo.spring.stream.binder.sns.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.aws.core.env.ResourceIdResolver;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;

import de.idealo.spring.stream.binder.sns.SnsMessageHandlerBinder;
import de.idealo.spring.stream.binder.sns.provisioning.SnsStreamProvisioner;

@Configuration
@ConditionalOnMissingBean(Binder.class)
public class SnsBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean(AmazonSNSAsync.class)
    public AmazonSNSAsync amazonSNS(AWSCredentialsProvider awsCredentialsProvider, RegionProvider regionProvider) {
        AmazonSNSAsyncClientBuilder builder = AmazonSNSAsyncClientBuilder.standard();
        builder.setCredentials(awsCredentialsProvider);
        builder.setRegion(regionProvider.getRegion().getName());
        return builder.build();
    }

    @Bean
    public SnsStreamProvisioner provisioningProvider(AmazonSNS amazonSNS, ResourceIdResolver resourceIdResolver) {
        return new SnsStreamProvisioner(amazonSNS, resourceIdResolver);
    }

    @Bean
    public SnsMessageHandlerBinder snsMessageHandlerBinder(AmazonSNSAsync amazonSNS, SnsStreamProvisioner snsStreamProvisioner) {
        return new SnsMessageHandlerBinder(amazonSNS, snsStreamProvisioner);
    }

}
