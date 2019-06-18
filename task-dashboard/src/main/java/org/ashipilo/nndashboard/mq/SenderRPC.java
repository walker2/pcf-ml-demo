package org.ashipilo.nndashboard.mq;

import com.google.gson.Gson;
import org.ashipilo.nndashboard.job.QuartzJobFactory;
import org.ashipilo.nndashboard.job.Task;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;

public class SenderRPC {
    private static Logger logger = LoggerFactory.getLogger(Sender.class.getName());

    @Autowired
    private RabbitTemplate template;

    @Autowired
    @Qualifier("exchange")
    private DirectExchange exchange;

    @Autowired
    private Scheduler scheduler;


    public void send(Task task) throws SchedulerException {
        template.setReplyTimeout(-1);
        String routingKey;
        String jsonObject;
        if (task.getType().equals("RECOGNITION")) {
            logger.info("interface: {} send mq", task.getFiles());
            routingKey = "recognize";
            jsonObject = new Gson().toJson(task.getFiles());
        } else {
            routingKey = "relearn";
            logger.info("interface: train_buck {} valid_buck {}", task.getTrain_buck(), task.getValid_buck());

            Map<String, String> dictionary = new HashMap<>();
            dictionary.put("train_buck", task.getTrain_buck());
            dictionary.put("valid_buck", task.getValid_buck());
            jsonObject = new Gson().toJson(dictionary);
        }


        JobKey jobKey = JobKey.jobKey(task.getName(), task.getType());

        task.setExecution("RUNNING");
        updateTask(task, jobKey);


        byte[] responseObj = (byte[]) template.convertSendAndReceive(exchange.getName(), routingKey, jsonObject);
        if (responseObj != null) {
            String response = new String(responseObj);
            logger.info("Got response: {}", response);
            task.setExecution("DONE");// Set the result of the task? To do this we should make a function send(Task task)
            task.setResult(response);
            updateTask(task, jobKey);
        } else {
            logger.info("No response");
        }
    }

    private void updateTask(Task task, JobKey jobKey) throws SchedulerException {
        scheduler.deleteJob(jobKey);
        JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class)
                .withIdentity(task.getName(), task.getType())
                .withDescription(task.getDesc()).build();
        jobDetail.getJobDataMap().put("scheduleJob", task);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(task
                .getCronExp());
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(task.getName(), task.getType()).withSchedule(scheduleBuilder).build();
        scheduler.scheduleJob(jobDetail, trigger);
    }
}