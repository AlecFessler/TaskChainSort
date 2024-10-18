package taskChainPlanner;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public class GraphEditingController {
    /**
     * The GraphEditingController class is responsible for translating
     * users input into operations on the graph and rerendering as needed.
     *
     * The commands are divided between those on the graph pane, on the nodes,
     * and on the edges. The commands are as follows:
     *
     * Graph Pane:
     * - Zoom in/out on scroll
     * - Insert node on double left click
     * - Pan on left drag
     *
     * Nodes:
     * - Move node on left drag
     * - Begin inserting edge on right press and drag,
     *   and end on release over another node
     * - Edit node name on double left click
     * - Remove node on double right click
     *
     * Edges:
     * - Remove edge on double right click
     *
     * A temporary edge is rendered when inserting an edge to show the user
     * where the edge will be placed. A brief red edge is rendered when an
     * invalid edge is attempted to be inserted.
     *
     * The whole graph is rendered on every change to the graph, and the
     * event handlers are reset each time subsequently. While this is not
     * efficient, it massively simplifies the implementation and is not
     * causing any noticeable performance issues as of now.
     */
    private DirectedAcyclicGraph<Task> graph;
    private Pane graphPane;
    private Group group;
    private GraphRenderer graphRenderer;
    private double scaleValue = 1.0;
    private double zoomIntensity = 0.002;
    private double[] lastMouseCoordinates = new double[2];

    private int taskCounter = 0;
    private boolean movingNode = false;
    private Task selectedTask = null;
    private boolean insertingEdge = false;
    private Task startTask = null;

    /**
     * Constructor for the GraphEditingController class.
     *
     * @param graph The graph to be edited.
     * @param graphPane The pane to render the graph on.
     */
    public GraphEditingController(DirectedAcyclicGraph<Task> graph, Pane graphPane) {
        this.graph = graph;
        this.graphPane = graphPane;
        graphPane.getChildren().clear();

        this.group = new Group();
        graphPane.getChildren().add(this.group);

        this.graphRenderer = new GraphRenderer(graph, group);
        graphRenderer.renderGraph(true);
        setupGraphEventHandlers();
        setupGraphPaneEventHandlers();
    }

    /**
     * Rerender the graph and reset the event handlers.
     */
    private void setupGraphEventHandlers() {
        Map<Task, Group> taskGroups = graphRenderer.taskGroups();
        Map<DirectedAcyclicGraph.Edge<Task>, Group> edgeLines = graphRenderer.edgeLines();
        for (Task tasks : graphRenderer.taskGroups().keySet()) {
            setupTaskEventHandlers(taskGroups.get(tasks), tasks);
        }
        for (DirectedAcyclicGraph.Edge<Task> edge : graphRenderer.edgeLines().keySet()) {
            setupEdgeEventHandlers(edge, edgeLines.get(edge));
        }
    }

    /**
     * Set up the event handlers for a task node.
     *
     * The event handlers are as follows:
     * - Remove node on double right click
     * - Edit node name on double left click
     * - Start edge insertion on right press
     * - Start moving node on left press
     * - Insert edge on right release
     *
     * @param taskGroup The group representing the task node.
     * @param task The task node.
     */
    private void setupTaskEventHandlers(Group taskGroup, Task task) {
        taskGroup.setOnMouseClicked(e -> {
            // remove node on double right click
            if (e.getButton() == MouseButton.SECONDARY && e.getClickCount() == 2) {
                graph.removeNode(task);
                graphRenderer.renderGraph(true);
                setupGraphEventHandlers();
            }
            // edit node name on double left click
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TextField nameField = graphRenderer.renderEditableTaskName(task);
                nameField.requestFocus();

                // update task name on enter
                nameField.setOnAction(event -> {
                    task.setName(nameField.getText());
                    graphRenderer.renderGraph(true);
                    setupGraphEventHandlers();
                });
                // or update task name on focus loss
                nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        task.setName(nameField.getText());
                        graphRenderer.renderGraph(true);
                        setupGraphEventHandlers();
                    }
                });
                e.consume();
            }
        });

        taskGroup.setOnMousePressed(e -> {
            // start edge insertion on right button press
            if (e.getButton() == MouseButton.SECONDARY) {
                insertingEdge = true;
                startTask = task;
            } else if (e.getButton() == MouseButton.PRIMARY) {
                // store the last coordinates for moving the node on left button press
                lastMouseCoordinates[0] = e.getSceneX();
                lastMouseCoordinates[1] = e.getSceneY();
                movingNode = true;
                selectedTask = task;
            }
        });

        taskGroup.setOnMouseReleased(e -> {
            // insert edge on right button release
            if (e.getButton() == MouseButton.SECONDARY && insertingEdge) {
                insertingEdge = false;
                Task endTask = findTaskAt(e.getSceneX(), e.getSceneY());
                graphRenderer.clearTempEdge();
                if (endTask != null && endTask != startTask) {
                    DirectedAcyclicGraph.Edge<Task> edge = graph.insertEdge(startTask, endTask);
                    if (edge != null) {
                        graphRenderer.renderGraph(true);
                        setupGraphEventHandlers();
                    } else {
                        graphRenderer.renderInvalidEdge(startTask, endTask);
                    }
                }
            }
        });
    }

    /**
     * Set up the event handlers for an edge line.
     *
     * The event handlers are as follows:
     * - Remove edge on double right click
     *
     * @param edge The edge to set up the event handlers for.
     * @param edgeLine The line representing the edge.
     */
    private void setupEdgeEventHandlers(DirectedAcyclicGraph.Edge<Task> edge, Group edgeLine) {
        edgeLine.setOnMouseClicked(e -> {
            // remove edge on double right click
            if (e.getButton() == MouseButton.SECONDARY && e.getClickCount() == 2) {
                graph.removeEdge(edge.from, edge.to);
                graphRenderer.renderGraph(true);
                setupGraphEventHandlers();
            }
        });
    }

    /**
     * Set up the event handlers for the graph pane.
     *
     * The event handlers are as follows:
     * - Zoom in/out on scroll
     * - Insert node on double left click
     * - Pan on left drag
     */
    private void setupGraphPaneEventHandlers() {
        // zoom on scroll
        graphPane.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = Math.exp(event.getDeltaY() * zoomIntensity);
            scaleValue *= zoomFactor;
            group.setScaleX(scaleValue);
            group.setScaleY(scaleValue);
        });

        graphPane.setOnMouseClicked(event -> {
            // insert node at cursor position on double left click
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Point2D mousePoint = group.sceneToLocal(event.getSceneX(), event.getSceneY());
                double x = mousePoint.getX();
                double y = mousePoint.getY();
                Task newTask = new Task(x, y);
                graph.insertNode(newTask);
                newTask.setName("New Task " + taskCounter++);

                graphRenderer.renderGraph(true);
                setupGraphEventHandlers();
            }
        });

        graphPane.setOnMousePressed(event -> {
            // store cursor position on left button press for panning
            if (event.getButton() == MouseButton.PRIMARY) {
                lastMouseCoordinates[0] = event.getX();
                lastMouseCoordinates[1] = event.getY();
            }
        });

        graphPane.setOnMouseDragged(event -> {
            // move node on left button drag
            if (movingNode && selectedTask != null) {
                double deltaX = event.getSceneX() - lastMouseCoordinates[0];
                double deltaY = event.getSceneY() - lastMouseCoordinates[1];
                selectedTask.setPos(selectedTask.x() + deltaX, selectedTask.y() + deltaY);
                lastMouseCoordinates[0] = event.getSceneX();
                lastMouseCoordinates[1] = event.getSceneY();

                graphRenderer.renderGraph(true);
                setupGraphEventHandlers();

            // pan the graph on left button drag when not moving a node
            } else if (!movingNode && event.getButton() == MouseButton.PRIMARY) {
                double deltaX = event.getX() - lastMouseCoordinates[0];
                double deltaY = event.getY() - lastMouseCoordinates[1];
                group.setTranslateX(group.getTranslateX() + deltaX);
                group.setTranslateY(group.getTranslateY() + deltaY);
                lastMouseCoordinates[0] = event.getX();
                lastMouseCoordinates[1] = event.getY();

            // render temp edge on right button drag when inserting edge
            } else if (event.getButton() == MouseButton.SECONDARY && insertingEdge && startTask != null) {
                Point2D mousePoint = group.sceneToLocal(event.getSceneX(), event.getSceneY());
                double x = mousePoint.getX();
                double y = mousePoint.getY();
                graphRenderer.renderTempEdge(startTask, x, y);
            }
        });

        graphPane.setOnMouseReleased(event -> {
            // reset edge insertion on right button release
            if (event.getButton() == MouseButton.SECONDARY && insertingEdge) {
                insertingEdge = false;
                startTask = null;
                graphRenderer.clearTempEdge();
            }
            // reset moving node on left button release
            if (event.getButton() == MouseButton.PRIMARY && movingNode) {
                movingNode = false;
                selectedTask = null;
            }
        });
    }

    /**
     * Find the task at the given scene coordinates.
     * @param sceneX The x coordinate.
     * @param sceneY The y coordinate.
     * @return The task at the given coordinates, or null if none.
     */
    private Task findTaskAt(double sceneX, double sceneY) {
        Point2D localPoint = group.sceneToLocal(sceneX, sceneY);
        for (Task task : graph.nodes()) {
            if (Math.hypot(task.x() - localPoint.getX(), task.y() - localPoint.getY()) <= graphRenderer.taskRadius()) {
                return task;
            }
        }
        return null;
    }
}
