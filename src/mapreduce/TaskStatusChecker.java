package mapreduce;

import java.util.Map;

public class TaskStatusChecker implements Runnable {

  private final long ALIVE_CYCLE; // 8 seconds

  private TaskTracker taskTracker;

  public TaskStatusChecker(TaskTracker taskTracker) {
    this.taskTracker = taskTracker;
    ALIVE_CYCLE = Long.parseLong(Utility.getParam("ALIVE_CYCLE"));
  }

  public void run() {
    Map<Integer, TaskProgress> taskStatus = taskTracker.getTaskStatus();
    synchronized (taskStatus) {
      for (TaskProgress taskProgress : taskStatus.values()) {
        long curTime = System.currentTimeMillis();
        if ((curTime - taskProgress.getTimestamp() > ALIVE_CYCLE)
                && taskProgress.getStatus() != TaskMeta.TaskStatus.SUCCEED) {
          taskProgress.setStatus(TaskMeta.TaskStatus.FAILED);
        }
      }
    }
  }

}
