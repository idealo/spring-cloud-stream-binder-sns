package de.idealo.spring.stream.binder.sns.config;

import static io.awspring.cloud.core.config.AmazonWebserviceClientConfigurationUtils.GLOBAL_CLIENT_CONFIGURATION_BEAN_NAME;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;

import io.awspring.cloud.autoconfigure.messaging.SnsProperties;
import io.awspring.cloud.context.annotation.ConditionalOnMissingAmazonClient;
import io.awspring.cloud.core.config.AmazonWebserviceClientFactoryBean;
import io.awspring.cloud.core.region.RegionProvider;
import io.awspring.cloud.core.region.StaticRegionProvider;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AmazonSNSAsync.class)
public class SnsAsyncAutoConfiguration {

    private final AWSCredentialsProvider awsCredentialsProvider;

    private final RegionProvider regionProvider;

    private final ClientConfiguration clientConfiguration;

    SnsAsyncAutoConfiguration(ObjectProvider<AWSCredentialsProvider> awsCredentialsProvider,
            ObjectProvider<RegionProvider> regionProvider, SnsProperties properties,
            @Qualifier(GLOBAL_CLIENT_CONFIGURATION_BEAN_NAME) ObjectProvider<ClientConfiguration> globalClientConfiguration,
            @Qualifier("snsClientConfiguration") ObjectProvider<ClientConfiguration> snsClientConfiguration) {
        this.awsCredentialsProvider = awsCredentialsProvider.getIfAvailable();
        this.regionProvider = properties.getRegion() == null ? regionProvider.getIfAvailable()
                : new StaticRegionProvider(properties.getRegion());
        this.clientConfiguration = snsClientConfiguration.getIfAvailable(globalClientConfiguration::getIfAvailable);
    }

    @ConditionalOnMissingAmazonClient(AmazonSNSAsync.class)
    @Bean
    public AmazonWebserviceClientFactoryBean<AmazonSNSAsyncClient> amazonSNS(SnsProperties properties) {
        AmazonWebserviceClientFactoryBean<AmazonSNSAsyncClient> clientFactoryBean = new AmazonWebserviceClientFactoryBean<>(
                AmazonSNSAsyncClient.class, this.awsCredentialsProvider, this.regionProvider, this.clientConfiguration);
        Optional.ofNullable(properties.getEndpoint()).ifPresent(clientFactoryBean::setCustomEndpoint);
        return clientFactoryBean;
    }

}
