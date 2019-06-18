package org.ashipilo.nndashboard.controller;

import java.io.IOException;
import java.util.List;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.ashipilo.nndashboard.job.Task;
import org.ashipilo.nndashboard.s3.S3Wrapper;
import org.ashipilo.nndashboard.common.Message;
import org.ashipilo.nndashboard.service.ScheduleTaskService;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RestfulController {

    private static Logger logger = LoggerFactory.getLogger(RestfulController.class);

    private final ScheduleTaskService scheduleTaskService;

    private final S3Wrapper s3Wrapper;

    @Autowired
    public RestfulController(ScheduleTaskService scheduleTaskService, S3Wrapper s3Wrapper) {
        this.scheduleTaskService = scheduleTaskService;
        this.s3Wrapper = s3Wrapper;
    }


    @RequestMapping("/metaData")
    public Object metaData() throws SchedulerException {
        SchedulerMetaData metaData = scheduleTaskService.getMetaData();
        return metaData;
    }

    @RequestMapping("/getAllJobs")
    public Object getAllJobs() throws SchedulerException {
        List<Task> jobList = scheduleTaskService.getAllTaskList();
        return jobList;
    }

    @RequestMapping("/getRunningJobs")
    public Object getRunningJobs() throws SchedulerException {
        List<Task> jobList = scheduleTaskService.getRunningTaskList();
        return jobList;
    }

    @RequestMapping(value = "/pauseJob", method = {RequestMethod.GET, RequestMethod.POST})
    public Object pauseJob(Task job) {
        logger.info("params, job = {}", job);
        Message message = Message.failure();
        try {
            scheduleTaskService.pauseTask(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            logger.error("pauseTask ex:", e);
        }
        return message;
    }

    @RequestMapping(value = "/resumeJob", method = {RequestMethod.GET, RequestMethod.POST})
    public Object resumeJob(Task job) {
        logger.info("params, job = {}", job);
        Message message = Message.failure();
        try {
            scheduleTaskService.resumeTask(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            logger.error("resumeTask ex:", e);
        }
        return message;
    }


    @RequestMapping(value = "/deleteJob", method = {RequestMethod.GET, RequestMethod.POST})
    public Object deleteJob(Task job) {
        logger.info("params, job = {}", job);
        Message message = Message.failure();
        try {
            scheduleTaskService.deleteTask(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            logger.error("deleteTask ex:", e);
        }
        return message;
    }

    @RequestMapping(value = "/runJob", method = {RequestMethod.GET, RequestMethod.POST})
    public Object runJob(Task job) {
        logger.info("params, job = {}", job);
        Message message = Message.failure();
        try {
            scheduleTaskService.runTaskOnce(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            logger.error("runJob ex:", e);
        }
        return message;
    }


    @RequestMapping(value = "/saveOrUpdate", method = {RequestMethod.GET, RequestMethod.POST})
    public Object saveOrUpdate(Task job) {
        logger.info("params, job = {}", job);
        Message message = Message.failure();
        try {
            scheduleTaskService.saveOrUpdate(job);
            message = Message.success();
        } catch (Exception e) {
            message.setMsg(e.getMessage());
            logger.error("updateCron ex:", e);
        }
        return message;
    }

    @RequestMapping(value = "s3/list", method = RequestMethod.GET)
    public List<S3ObjectSummary> list(@RequestParam String bucket) throws IOException {
        return s3Wrapper.list(bucket);
    }

    @RequestMapping(value = "s3/list_buckets", method = RequestMethod.GET)
    public List<Bucket> list() throws IOException {
        return s3Wrapper.list_buckets();
    }

}
