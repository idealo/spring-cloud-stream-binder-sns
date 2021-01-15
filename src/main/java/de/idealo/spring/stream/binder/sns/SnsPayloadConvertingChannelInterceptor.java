package de.idealo.spring.stream.binder.sns;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

public class SnsPayloadConvertingChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        return MessageBuilder.createMessage(new String((byte[]) message.getPayload()), message.getHeaders());
    }

}
