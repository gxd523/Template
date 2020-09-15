package com.demo.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by guoxiaodong on 2020/9/14 13:11
 */
public enum TaskManager {
    INSTANCE;
    private final Map<TaskPriority, List<Runnable>> taskListMap = new HashMap<>();
    private ExecutorService threadPool;
    private int taskIndex;
    private CountDownLatch countDownLatch;
    private int threadCount = 4;

    public void addTask(Runnable... runnable) {
        addTask(TaskPriority.DEFAULT, runnable);
    }

    public void addTask(TaskPriority priority, Runnable... runnable) {
        List<Runnable> taskList = taskListMap.get(priority);
        if (taskList == null) {
            taskList = new ArrayList<>();
            taskListMap.put(priority, taskList);
        }
        taskList.addAll(Arrays.asList(runnable));
    }

    /**
     * Task总时长为每组中执行时间最长Task相加之和
     */
    public synchronized void executeTask() {
        if (threadPool == null || threadPool.isTerminated()) {
            taskIndex = 0;
            threadPool = Executors.newFixedThreadPool(threadCount, r -> new Thread(r, "task-" + taskIndex++));
        }
        traversalTaskList(taskListMap.get(TaskPriority.FIRST));
        traversalTaskList(taskListMap.get(TaskPriority.HIGH));
        traversalTaskList(taskListMap.get(TaskPriority.DEFAULT));
        traversalTaskList(taskListMap.get(TaskPriority.LOW));
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
            // ignore
        }
        taskListMap.clear();
    }

    private void traversalTaskList(List<Runnable> taskList) {
        if (taskList == null || taskList.size() == 0) {
            return;
        }
        countDownLatch = new CountDownLatch(taskList.size());
        for (Runnable task : taskList) {
            threadPool.execute(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isTerminated() {
        return threadPool == null || threadPool.isTerminated();
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public enum TaskPriority {
        FIRST,
        HIGH,
        DEFAULT,
        LOW
    }
}