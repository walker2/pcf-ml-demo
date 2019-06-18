package org.ashipilo.nndashboard.controller;

import java.util.List;

import org.ashipilo.nndashboard.job.Task;
import org.ashipilo.nndashboard.service.ScheduleTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class IndexController {

    private final ScheduleTaskService scheduleTaskService;

    @Autowired
    public IndexController(ScheduleTaskService scheduleTaskService) {
        this.scheduleTaskService = scheduleTaskService;
    }

    @RequestMapping("/")
    public String index(Model model) {
        List<Task> taskList = scheduleTaskService.getAllTaskList();
        model.addAttribute("tasks", taskList);
        return "index";
    }

}
