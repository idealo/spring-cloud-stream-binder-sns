package de.idealo.spring.stream.binder.sns;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.DeleteTopicRequest;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import de.idealo.spring.stream.binder.sns.config.SnsAsyncAutoConfiguration;

@Testcontainers
@SpringBootTest(properties = {
        "cloud.aws.stack.auto=false",
        "cloud.aws.region.static=eu-central-1",
        "spring.cloud.stream.binders.sns.type=sns",
        "spring.cloud.stream.bindings.output-out-0.destination=topic1",
        "spring.cloud.stream.bindings.output-out-0.binder=sns",
        "spring.cloud.stream.bindings.function.definition=output"
})
class SnsBinderExceptionTest {

    @Container
    private static final LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.10"))
            .withServices(SNS, SQS)
            .withEnv("DEFAULT_REGION", "eu-central-1");

    private final static Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private SnsAsyncClient amazonSNS;

    @BeforeAll
    static void beforeAll() throws Exception {
        localStack.execInContainer("awslocal", "sns", "create-topic", "--name", "topic1");
    }

    @BeforeEach
    void deleteAllTopicsToProduceExceptions() throws ExecutionException, InterruptedException {
        amazonSNS.listTopics().get().topics().forEach(x -> amazonSNS.deleteTopic(DeleteTopicRequest.builder().topicArn(x.topicArn()).build()));
    }

    @Test
    void exceptionsWhileSendingSnsAreBeingSwallowed() {
        assertThatCode(() -> sink.tryEmitNext("hello")).doesNotThrowAnyException();
    }

    @Test
    void exceptionsWhileSendingSnsAreBeingSwallowedWhenUsingStreamBridge() {
        assertThatCode(() -> streamBridge.send("output-out-0", "hello")).doesNotThrowAnyException();
    }

    @TestConfiguration
    static class AwsConfig {

        @Bean
        SnsAsyncClient amazonSNS() {
            return SnsAsyncClient.builder()
                    .endpointOverride(localStack.getEndpointOverride(SNS))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
                    .region(Region.EU_CENTRAL_1)
                    .build();
        }

        @Bean
        SqsAsyncClient amazonSQS() {
            return SqsAsyncClient.builder()
                    .endpointOverride(localStack.getEndpointOverride(SQS))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
                    .region(Region.EU_CENTRAL_1)
                    .build();
        }

        @Bean
        public Supplier<Flux<String>> output() {
            return sink::asFlux;
        }
    }

    @SpringBootApplication
    @ComponentScan(basePackages = { "de.idealo.spring.stream.binder.sns.config" },
            excludeFilters = { @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    value = { SnsAsyncAutoConfiguration.class })
            })
    static class Application {
    }
}
