package org.ashipilo.nndashboard.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.util.List;

/**
 * analog rabbit MQ consuming
 * receiver should be in business system
 */
@RabbitListener(queues = "TaskQueue")
public class Receiver {

	private static Logger logger = LoggerFactory.getLogger(Receiver.class.getName());
	
	@RabbitHandler
    public void process(String objects) {

		logger.info("Receiver : " + objects);
    }
	
}
