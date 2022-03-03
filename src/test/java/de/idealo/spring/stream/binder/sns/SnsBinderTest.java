package de.idealo.spring.stream.binder.sns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

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
class SnsBinderTest {

    @Container
    private static final LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.12.10"))
            .withServices(SNS, SQS)
            .withEnv("DEFAULT_REGION", "eu-central-1");

    private final static Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Autowired
    private AmazonSNSAsync amazonSNS;

    @Autowired
    private AmazonSQSAsync amazonSQS;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @BeforeAll
    static void beforeAll() throws Exception {
        localStack.execInContainer("awslocal", "sns", "create-topic", "--name", "topic1");
        localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", "queue1");
    }

    @Test
    void canTestHealth() {
        assertThat(healthEndpoint.health().getStatus()).isEqualTo(Status.UP);
        assertThat(healthEndpoint.healthForPath("snsBinder").getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void canPublish() {

        String queueUrl = amazonSQS.getQueueUrl("queue1").getQueueUrl();
        String topicArn = amazonSNS.listTopics().getTopics().get(0).getTopicArn();
        amazonSNS.subscribe(topicArn, "sqs", queueUrl);

        sink.tryEmitNext("hello");

        await().untilAsserted(() -> {
            ReceiveMessageResult message = amazonSQS.receiveMessage(queueUrl);
            assertThat(message.getMessages()).hasSize(1);
            assertThat(message.getMessages().get(0).getBody()).contains("hello");
        });
    }

    @TestConfiguration
    static class AwsConfig {

        @Bean
        AmazonSNSAsync amazonSNS() {
            return AmazonSNSAsyncClientBuilder.standard()
                    .withEndpointConfiguration(localStack.getEndpointConfiguration(SNS))
                    .withCredentials(localStack.getDefaultCredentialsProvider())
                    .build();
        }

        @Bean
        AmazonSQSAsync amazonSQS() {
            return AmazonSQSAsyncClientBuilder.standard()
                    .withEndpointConfiguration(localStack.getEndpointConfiguration(SQS))
                    .withCredentials(localStack.getDefaultCredentialsProvider())
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
