package de.idealo.spring.stream.binder.sns.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.AbstractExtendedBindingProperties;
import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

@ConfigurationProperties("spring.cloud.stream.sns")
public class SnsExtendedBindingProperties extends AbstractExtendedBindingProperties<SnsConsumerProperties, SnsProducerProperties, SnsBindingProperties> {

    private static final String DEFAULTS_PREFIX = "spring.cloud.stream.sns.default";

    @Override
    public String getDefaultsPrefix() {
        return DEFAULTS_PREFIX;
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return SnsBindingProperties.class;
    }
}
