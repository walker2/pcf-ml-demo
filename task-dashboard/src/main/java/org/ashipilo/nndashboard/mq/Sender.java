package org.ashipilo.nndashboard.mq;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class Sender {

    private static Logger logger = LoggerFactory.getLogger(Sender.class.getName());

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void send(List<String> objects) {
        logger.info("interface: {} send mq", objects);

        String jsonObject = new Gson().toJson(objects);
        amqpTemplate.convertAndSend("TaskQueue", jsonObject);
    }

}
