package org.ashipilo.nndashboard.job;

import java.util.Collections;
import java.util.List;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import org.ashipilo.nndashboard.mq.Sender;
import org.ashipilo.nndashboard.mq.SenderRPC;
import org.ashipilo.nndashboard.service.SampleService;

public class QuartzJobFactory implements Job {

    @Autowired
    private SampleService sampleService;
    
    @Autowired
    private SenderRPC sender;



    public static List<Task> jobList = Lists.newArrayList();

    static {
        /* Add example task */
        Task job = new Task();
        job.setId(String.valueOf(0));
        job.setName("Sample job");
        job.setType("RECOGNITION");
        job.setDesc("Sample image recognition job");
        job.setStatus("PLANNED");
        job.setCronExp("0 0 0 1 1 ? *"); // 1st January
        job.setFiles(Collections.singletonList("dog.jpg"));
        job.setInterfaceName("interface_0");
        job.setExecution("STOPPED");
        jobList.add(job);
    }

    // simulate data from db
    public static List<Task> getInitAllJobs() {
        return jobList;
    }


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Task task = (Task) jobExecutionContext.getMergedJobDataMap().get("scheduleJob");

        String jobName = task.getName();

        // execute task inner quartz system
        // spring bean can be @Autowired
        sampleService.hello(jobName);

        // use rabbit MQ to asynchronously notify the task execution in business system
        try {
            sender.send(task);
        }
        catch (SchedulerException se) {
            System.out.println(se.toString());
        }
    }

}
