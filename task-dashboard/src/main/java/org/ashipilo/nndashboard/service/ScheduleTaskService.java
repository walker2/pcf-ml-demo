package org.ashipilo.nndashboard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.codepipeline.model.JobData;
import org.ashipilo.nndashboard.job.QuartzJobFactory;
import org.ashipilo.nndashboard.job.Task;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;

@Service
public class ScheduleTaskService {

    private final Scheduler scheduler;

    @Autowired
    public ScheduleTaskService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public List<Task> getAllTaskList() {
        List<Task> jobList = new ArrayList<>();
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeySet = scheduler.getJobKeys(matcher);
            for (JobKey jobKey : jobKeySet) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    Task task = new Task();
                    this.wrapScheduleTask(task, scheduler, jobKey, trigger);
                    jobList.add(task);
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return jobList;
    }


    public List<Task> getRunningTaskList() throws SchedulerException {
        List<JobExecutionContext> executingJobList = scheduler.getCurrentlyExecutingJobs();
        List<Task> jobList = new ArrayList<>(executingJobList.size());
        for (JobExecutionContext executingJob : executingJobList) {
            Task task = new Task();
            JobDetail jobDetail = executingJob.getJobDetail();
            JobKey jobKey = jobDetail.getKey();
            Trigger trigger = executingJob.getTrigger();
            this.wrapScheduleTask(task, scheduler, jobKey, trigger);
            jobList.add(task);
        }
        return jobList;
    }


    public void saveOrUpdate(Task task) throws Exception {
        Preconditions.checkNotNull(task, "job is null");
        if (StringUtils.isEmpty(task.getId())) {
            addTask(task);
        } else {
            updateTaskCronExpression(task);
        }
    }

    private void addTask(Task task) throws Exception {
        checkNotNull(task);
        Preconditions.checkNotNull(StringUtils.isEmpty(task.getCronExp()), "CronExpression is null");

        TriggerKey triggerKey = TriggerKey.triggerKey(task.getName(), task.getType());
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if (trigger != null) {
            throw new Exception("job already exists!");
        }
        task.setInterfaceName("");

        task.setId(String.valueOf(QuartzJobFactory.jobList.size() + 1));
        task.setInterfaceName("interface" + task.getId());
        task.setExecution("STOPPED");
        QuartzJobFactory.jobList.add(task);

        JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class)
                .withIdentity(task.getName(), task.getType())
                .withDescription(task.getDesc()).build();
        jobDetail.getJobDataMap().put("scheduleJob", task);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(task
                .getCronExp());
        trigger = TriggerBuilder.newTrigger().withIdentity(task.getName(), task.getType()).withSchedule(scheduleBuilder).build();
        scheduler.scheduleJob(jobDetail, trigger);
    }


    public void pauseTask(Task task) throws SchedulerException {
        checkNotNull(task);
        JobKey jobKey = JobKey.jobKey(task.getName(), task.getType());
        scheduler.pauseJob(jobKey);
    }

    public void resumeTask(Task task) throws SchedulerException {
        checkNotNull(task);
        JobKey jobKey = JobKey.jobKey(task.getName(), task.getType());
        scheduler.resumeJob(jobKey);
    }

    public void deleteTask(Task task) throws SchedulerException {
        checkNotNull(task);
        JobKey jobKey = JobKey.jobKey(task.getName(), task.getType());
        /*
        JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
        jobDataMap.remove("scheduleJob");
        jobDataMap.put("scheduleJob", xx);
        Task yy = (Task) scheduler.getJobDetail(jobKey).getJobDataMap().get("scheduleJob");*/
        scheduler.deleteJob(jobKey);
    }

    public void runTaskOnce(Task task) throws SchedulerException {
        checkNotNull(task);
        JobKey jobKey = JobKey.jobKey(task.getName(), task.getType());
        scheduler.triggerJob(jobKey);
    }


    private void updateTaskCronExpression(Task task) throws SchedulerException {
        checkNotNull(task);
        Preconditions.checkNotNull(StringUtils.isEmpty(task.getCronExp()), "CronExpression is null");

        TriggerKey triggerKey = TriggerKey.triggerKey(task.getName(), task.getType());
        CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(task.getCronExp());
        cronTrigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(cronScheduleBuilder).build();
        scheduler.rescheduleJob(triggerKey, cronTrigger);
    }

    private void wrapScheduleTask(Task task, Scheduler scheduler, JobKey jobKey, Trigger trigger) {
        try {
            task.setName(jobKey.getName());
            task.setType(jobKey.getGroup());

            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            Task job = (Task) jobDetail.getJobDataMap().get("scheduleJob");
            task.setDesc(job.getDesc());
            task.setId(job.getId());
            task.setExecution(job.getExecution());
            task.setFiles(job.getFiles());
            task.setResult(job.getResult());

            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            task.setStatus(triggerState.name());
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                String cronExpression = cronTrigger.getCronExpression();
                task.setCronExp(cronExpression);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private void checkNotNull(Task task) {
        Preconditions.checkNotNull(task, "job is null");
        Preconditions.checkNotNull(StringUtils.isEmpty(task.getName()), "jobName is null");
        Preconditions.checkNotNull(StringUtils.isEmpty(task.getType()), "jobGroup is null");
    }


    public SchedulerMetaData getMetaData() throws SchedulerException {
        SchedulerMetaData metaData = scheduler.getMetaData();
        return metaData;
    }


}
