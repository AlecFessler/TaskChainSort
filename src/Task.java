package taskChainPlanner;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.lang.Math;

public class Task {
  /*
   The task class is a named task with a set of flags to indicate its state.

   The flags are:
   FLAG_READY - Ready
   FLAG_ASSIGNED - Assigned
   FLAG_COMPLETE - Complete

   The id and priority are not set at initialization, but are set by the sorting algorithm.
   The sorting algorithm is a topological sort over a directed acyclic graph (DAG) of tasks.
   The algorithm is a static method on this class because its defined in a way that only makes
   sense in the context of a graph of Task objects.
  */
  private static final int FLAG_READY = 0b00000001;
  private static final int FLAG_ASSIGNED = 0b00000010;
  private static final int FLAG_COMPLETE = 0b00000100;

  private String name;
  private int flags = 0b00000000;
  private double x;
  private double y;
  private int id;
  private int priority;

  public Task(double x, double y) {
    this.x = x;
    this.y = y;
    this.name = "New Task";
  }

  public void setName(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  public void setPos(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double x() {
    return x;
  }

  public double y() {
    return y;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int id() {
    return id;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int priority() {
    return priority;
  }

  public void setReady() {
    flags |= FLAG_READY;
  }

  public boolean isReady() {
    return (flags & FLAG_READY) == FLAG_READY;
  }

  public void resetReady() {
    flags &= ~FLAG_READY;
  }

  public void setAssigned() {
    flags |= FLAG_ASSIGNED;
  }

  public boolean isAssigned() {
    return (flags & FLAG_ASSIGNED) == FLAG_ASSIGNED;
  }

  public void resetAssigned() {
    flags &= ~FLAG_ASSIGNED;
  }

  public void setComplete() {
    flags |= FLAG_COMPLETE;
  }

  public boolean isComplete() {
    return (flags & FLAG_COMPLETE) == FLAG_COMPLETE;
  }

  public void resetComplete() {
    flags &= ~FLAG_COMPLETE;
  }

  public String taskState() {
    if (isComplete()) {
      return "Complete";
    } else if (isAssigned()) {
      return "Assigned";
    } else if (isReady()) {
      return "Ready";
    } else {
      return "Not Ready";
    }
  }

  /**
   * Performs a modified topological sort (Kahn's algorithm) on a directed acyclic graph (DAG) of tasks.
   *
   * The sorting works by iteratively identifying tasks with no dependencies, and assigns them with an
   * incremental id, and a priority equal to the maximum id of its dependencies. The tasks within each
   * iteration's set are then sorted based on the descending count of their dependants. The final result
   * is that the priority queue is primarily sorted in an order which guarantees that tasks with no
   * dependencies are at the front, secondarily by the maximum id of their dependencies, and tertiarily
   * by the descending count of their dependants. In other words, within each iteration set, the tasks
   * in front are the ones which will maximally increase the number of tasks that can be completed in
   * parallel.
   *
   * @param graph the directed acyclic graph of tasks
   * @return a priority queue of tasks sorted by priority
   */
  public static PriorityQueue<Task> sortTasks(DirectedAcyclicGraph<Task> graph) {
      DirectedAcyclicGraph<Task> graphCopy = graph.clone();

      Comparator<Task> taskComparator = (a, b) -> {
        if (a.priority() == b.priority()) {
          return Integer.compare(a.id(), b.id());
        }
        return Integer.compare(a.priority(), b.priority());
      };
      PriorityQueue<Task> sortedTasks = new PriorityQueue<>(taskComparator);

      int taskEnumerator = 0;
      int tasksLeft = graphCopy.size();
      boolean firstIteration = true;
      while (tasksLeft > 0) {
        // Identify all tasks with no dependencies
        List<Task> tasks = new ArrayList<>();
        for (Task task : graphCopy.nodes()) {
          if (graphCopy.get(task).isEmpty()) {
              if (firstIteration) {
                task.setReady();
              } else {
                task.resetReady();
              }
              tasks.add(task);
          }
        }
        tasksLeft -= tasks.size();
        firstIteration = false;

        // Count the number of dependants for each task
        int[] dependantsCounts = new int[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
          Task task = tasks.get(i);
          for (Task t : graphCopy.nodes()) {
            if (graphCopy.get(t).contains(task)) {
              dependantsCounts[i]++;
            }
          }
        }

        // Determine the descending dependants count order
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
          indices.add(i);
        }
        indices.sort((a, b) -> Integer.compare(dependantsCounts[b], dependantsCounts[a]));

        // Process tasks in order of descending dependants count
        for (int i = 0; i < tasks.size(); i++) {
          Task task = tasks.get(indices.get(i));

          // Assign an incremental id to the task
          task.setId(taskEnumerator++);

          // Assign -1 to tasks with no dependencies, or the max id of its dependencies
          int maxPriority = -1;
          for (Task dependency : graph.get(task)) { // using original graph because the copy has been modified
            maxPriority = Math.max(maxPriority, dependency.id());
          }
          task.setPriority(maxPriority);

          // Remove the task from the graph copy and add it to the sorted list
          graphCopy.removeNode(task);
          sortedTasks.offer(task);
        }
      }
      return sortedTasks;
  }

  @Override
  public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Task{name='").append(name).append('\'')
        .append(", id=").append(id)
        .append(", priority=").append(priority)
        .append(", flags=").append(Integer.toBinaryString(flags))
        .append('}');
      return sb.toString();
  }
}
