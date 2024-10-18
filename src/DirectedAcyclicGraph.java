package taskChainPlanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;


public class DirectedAcyclicGraph<T> implements Cloneable {
  /*
   The graph is represented as an adjacency list,
   where the key is the node, and the value is a list
   of nodes that the key has edges to.

   Example:
   A: [B, C]
   B: [C]
   C: [D]
   D: []

   The directed acyclic property is enforced in the
   insert method, by doing a simple dfs to check if
   there there is a link from the node we are trying
   to connect to back to the node we are connecting.
  */
  public HashMap<T, ArrayList<T>> graph;

  public DirectedAcyclicGraph() {
    this.graph = new HashMap<T, ArrayList<T>>();
  }

  public int size() {
    return this.graph.size();
  }

  /**
   * Get the nodes in the graph.
   *
   * @return The nodes in the graph
   */
  public Set<T> nodes() {
    return this.graph.keySet();
  }

  /**
   * Represents an edge in the graph.
   * This is only used for the edges method
   * to provide a convenient way to determine
   * the nodes that are connected by an edge,
   * for example when removing an edge or rendering
   * the graph.
   */
  public static class Edge<T> {
    public T from;
    public T to;

    public Edge(T from, T to) {
      this.from = from;
      this.to = to;
    }
  }

  /**
   * Get the edges in the graph.
   *
   * @return The edges in the graph
   */
  public Set<Edge<T>> edges() {
    Set<Edge<T>> edges = new HashSet<Edge<T>>();
    for (T node : this.graph.keySet()) {
      for (T neighbor : this.graph.get(node)) {
        edges.add(new Edge<T>(node, neighbor));
      }
    }
    return edges;
  }

  /**
   * Get the neighbors of a node.
   *
   * @param node The node to get the neighbors of
   * @return The neighbors of the node
   */
  public ArrayList<T> get(T node) {
    return this.graph.get(node);
  }

  /**
   * Check if inserting the edge would create a cycle.
   * We do this by performing a dfs from the node we are
   * connecting to, and checking if we can reach the node
   * we are connecting from.
   *
   * @param node   The node we are trying to connect to
   * @param target The node we are trying to connect from
   */
  private boolean hasCycle(T node, T target) {
    if (node == target) {
      return true;
    }

    Stack<T> visiting = new Stack<T>();
    HashSet<T> visited = new HashSet<T>();

    visiting.push(node);
    while (!visiting.isEmpty()) {
      T current = visiting.pop();
      visited.add(current);

      for (T neighbor : this.graph.getOrDefault(current, new ArrayList<T>())) {
        if (neighbor == target) {
          return true;
        }
        if (!visited.contains(neighbor)) {
          visiting.push(neighbor);
        }
      }
    }
    return false;
  }

  /**
   * Insert an edge between two nodes.
   *
   * @param connect   The node we are connecting from
   * @param connectTo The node we are connecting to
   * @return True if the edge was inserted, false if it would create a cycle
   */
  public Edge<T> insertEdge(T connectFrom, T connectTo) {
      if (this.hasCycle(connectTo, connectFrom)) {
        return null;
      }
      if (this.graph.containsKey(connectFrom) && this.graph.get(connectFrom).contains(connectTo)) {
        return null;
      }
      this.graph.putIfAbsent(connectFrom, new ArrayList<>());
      this.graph.putIfAbsent(connectTo, new ArrayList<>());
      this.graph.get(connectFrom).add(connectTo);
      return new Edge<T>(connectFrom, connectTo);
  }

  /**
   * Remove an edge between two nodes.
   *
   * @param connectFrom The node we are connecting from
   * @param connectTo   The node we are connecting to
   */
  public void removeEdge(T connectFrom, T connectTo) {
      if (this.graph.containsKey(connectFrom)) {
          this.graph.get(connectFrom).remove(connectTo);
      }
  }

  /**
   * Insert a node into the graph.
   *
   * @param node The node to insert
   */
  public void insertNode(T node) {
    this.graph.putIfAbsent(node, new ArrayList<>());
  }

  /**
   * Remove a node from the graph.
   *
   * @param node The node to remove
   */
  public void removeNode(T node) {
    this.graph.remove(node);
    for (ArrayList<T> neighbors : this.graph.values()) {
      neighbors.remove(node);
    }
  }

  @Override
  public DirectedAcyclicGraph<T> clone() {
      try {
          DirectedAcyclicGraph<T> cloned = (DirectedAcyclicGraph<T>) super.clone();
          cloned.graph = new HashMap<>(this.graph);
          for (T key : cloned.graph.keySet()) {
              cloned.graph.put(key, new ArrayList<>(cloned.graph.get(key)));
          }
          return cloned;
      } catch (CloneNotSupportedException e) {
          throw new AssertionError();
      }
  }
}
