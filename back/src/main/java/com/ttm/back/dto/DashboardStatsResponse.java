package com.ttm.back.dto;

import com.ttm.back.model.TaskStatus;

import java.util.Map;

public class DashboardStatsResponse {

    private long totalProjects;
    private long totalOpenTasks;
    private Map<TaskStatus, Long> tasksByStatus;
    private long overdueTasks;
    private long myAssignedOpenTasks;

    public DashboardStatsResponse(long totalProjects, long totalOpenTasks, Map<TaskStatus, Long> tasksByStatus,
                                   long overdueTasks, long myAssignedOpenTasks) {
        this.totalProjects = totalProjects;
        this.totalOpenTasks = totalOpenTasks;
        this.tasksByStatus = tasksByStatus;
        this.overdueTasks = overdueTasks;
        this.myAssignedOpenTasks = myAssignedOpenTasks;
    }

    public long getTotalProjects() {
        return totalProjects;
    }

    public long getTotalOpenTasks() {
        return totalOpenTasks;
    }

    public Map<TaskStatus, Long> getTasksByStatus() {
        return tasksByStatus;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public long getMyAssignedOpenTasks() {
        return myAssignedOpenTasks;
    }
}
