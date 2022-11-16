package de.idealo.spring.stream.binder.sns.properties;

public class SnsProducerProperties {

  /**
   * Channel name to which to send publisher confirms (acks).
   */
  private String confirmAckChannel;

  public String getConfirmAckChannel() {
    return this.confirmAckChannel;
  }

  public void setConfirmAckChannel(String confirmAckChannel) {
    this.confirmAckChannel = confirmAckChannel;
  }

}
