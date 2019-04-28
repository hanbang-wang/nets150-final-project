import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

final class GraphVisualizer implements Runnable {
    private final static int NAME_THRESHOLD = 20;
    private final static int VISUALIZE_TIME_MS = 100;
    private final JFrame frame = new JFrame("Actor Network Visualizer");
    private final JLabel status = new JLabel();
    private ActorsNetwork g;
    private Graph network;
    private List<Component> comps = new LinkedList<>();

    private void init() {
        DataProcessing process = new DataProcessing();
        try {
            status.setText("Downloading data...");
            process.acquireData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String[]> casts = process.getAllCasts();
        g = new ActorsNetwork(
                casts.stream().flatMap(Arrays::stream).collect(Collectors.toSet()));
        status.setText("Processing data...");
        for (String[] clique : casts) {
            final int l = clique.length;
            for (int i = 0; i < l; ++i) {
                for (int j = i + 1; j < l; ++j) {
                    g.addEdge(clique[i], clique[j]);
                }
            }
        }
        status.setText("Network built!");
    }

    private final Set<Integer> nodesInGraph = new HashSet<>();

    private Edge addEdge(String s, String e) {
        if (s.compareTo(e) > 0) {
            final String tmp = s;
            s = e;
            e = tmp;
        }
        final Node node1 = network.addNode(s);
        node1.setAttribute("ui.label",
                s.length() <= NAME_THRESHOLD ? s : s.substring(0, NAME_THRESHOLD));
        Node node2 = network.addNode(e);
        node2.setAttribute("ui.label",
                e.length() <= NAME_THRESHOLD ? e : e.substring(0, NAME_THRESHOLD));
        Edge edge = network.addEdge(s + " - " + e, node1, node2);
        if (edge != null) {
            try {
                Thread.sleep(VISUALIZE_TIME_MS);
            } catch (InterruptedException ignored) {
            }
        }
        return edge;
    }

    private void displayNeighbors(String name) {
        final Node node = network.addNode(name);
        if (Objects.equals(node.getAttribute("ui.class"), "highlight")) {
            return;
        }
        node.setAttribute("ui.class", "highlight");
        node.setAttribute("ui.label", name);
        node.setAttribute("layout.weight", 1. / 3);

        final int id = g.getId(name);
        List<Integer> collect = g.getNeighbors(id)
                .stream()
                .sorted(Comparator.comparingInt(o -> g.getDegree(o)))
                .limit(15).collect(Collectors.toList());
        collect.forEach(e -> addEdge(name, g.getName(e)));
        collect.add(id);
        collect.forEach(e -> g.getNeighbors(e)
                .stream()
                .filter(nodesInGraph::contains)
                .forEach(i -> addEdge(g.getName(e), g.getName(i))));
        nodesInGraph.addAll(collect);
    }

    private void findLink(String start, String end) {
        if (Objects.equals(start, end)) return;
        network.forEach(n -> {
            if (Objects.equals(n.getAttribute("ui.class"), "lowlight")) {
                n.removeAttribute("ui.class");
            }
        });
        network.getEachEdge().forEach(e -> {
            if (Objects.equals(e.getAttribute("ui.class"), "lowlight")) {
                e.removeAttribute("ui.class");
                e.removeAttribute("ui.label");
            }
        });
        List<Integer> list = g.shortestPath(g.getId(start), g.getId(end));
        nodesInGraph.addAll(list);
        String last = null;
        int count = 0;
        for (Integer n : list) {
            String name = g.getName(n);
            if (last == null) {
                last = name;
                continue;
            }
            final Edge edge = addEdge(last, name);
            if (network.getNode(last).getAttribute("ui.class") == null) {
                network.getNode(last).setAttribute("ui.class", "lowlight");
            }
            edge.setAttribute("ui.class", "lowlight");
            edge.setAttribute("ui.label", String.valueOf(++count));
            last = name;
        }
        if (network.getNode(last).getAttribute("ui.class") == null) {
            network.getNode(last).setAttribute("ui.class", "lowlight");
        }
    }

    private void getDiameter() {
        Util.Pair<Integer, Integer> diameter = g.getDiameter();
        findLink(g.getName(diameter.first), g.getName(diameter.second));
    }

    private void disableAll() {
        comps.forEach(c -> c.setEnabled(false));
    }

    private void enableAll() {
        comps.forEach(c -> c.setEnabled(true));
    }

    private Component makeToolBar() {
        final JPanel toolbar = new JPanel();
        final JTextField act = new JTextField(10);
        final JLabel label = new JLabel("Actor/Actress name:");
        final JButton button = new JButton("Add to graph");
        comps.add(act);
        comps.add(button);
        toolbar.add(label);
        toolbar.add(act);
        toolbar.add(button);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String actor = act.getText();
                if (!g.exists(actor)) {
                    JOptionPane.showMessageDialog(frame,
                            "The actor/actress does not exist!");
                    return;
                }
                CallbackRunnable runnable = new CallbackRunnable(() -> displayNeighbors(actor));
                runnable.callback = () -> enableAll();
                disableAll();
                runnable.start();
            }
        });

        toolbar.add(Box.createHorizontalStrut(10));

        final JTextField startAct = new JTextField(10);
        final JTextField endAct = new JTextField(10);
        final JLabel toLabel = new JLabel("to");
        final JButton linkButton = new JButton("Find link");
        comps.add(startAct);
        comps.add(endAct);
        comps.add(linkButton);
        toolbar.add(startAct);
        toolbar.add(toLabel);
        toolbar.add(endAct);
        toolbar.add(linkButton);

        linkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                final String actor1 = startAct.getText();
                final String actor2 = endAct.getText();
                if (!g.exists(actor1) || !g.exists(actor2)) {
                    JOptionPane.showMessageDialog(frame,
                            "The actor/actress does not exist!");
                    return;
                }
                CallbackRunnable runnable = new CallbackRunnable(() -> findLink(actor1, actor2));
                runnable.callback = () -> enableAll();
                disableAll();
                runnable.start();
            }
        });

        toolbar.add(Box.createHorizontalStrut(10));

        final JButton diameterButton = new JButton("Get diameter");
        comps.add(diameterButton);
        toolbar.add(diameterButton);

        diameterButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CallbackRunnable runnable = new CallbackRunnable(() -> getDiameter());
                runnable.callback = () -> enableAll();
                disableAll();
                runnable.start();
            }
        });

        final JButton clearButton = new JButton("Clear graph");
        comps.add(clearButton);
        toolbar.add(clearButton);

        clearButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Iterator<Node> nodeIterator = network.getNodeIterator();
                while (nodeIterator.hasNext()) {
                    nodeIterator.next();
                    nodeIterator.remove();
                }
                nodesInGraph.clear();
            }
        });

        return toolbar;
    }

    private Component getVisualizer() {
        network = new SingleGraph("ActorNetwork");
        network.setStrict(false);
        Viewer viewer = new Viewer(network, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        network.setAttribute("ui.stylesheet", "url(ui.css)");
        network.setAttribute("ui.quality");
        network.setAttribute("ui.antialias");

        ViewPanel view = viewer.addDefaultView(false);
        view.addMouseWheelListener(e -> {
            e.consume();
            final double factor = Math.pow(1.25, e.getWheelRotation());
            final Camera cam = view.getCamera();
            final double zoom = cam.getViewPercent() * factor;
            if (zoom < 1) {
                final Point2 pxCenter = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
                final Point3 guClicked = cam.transformPxToGu(e.getX(), e.getY());
                final double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu / factor;
                final double x = guClicked.x + (pxCenter.x - e.getX()) / newRatioPx2Gu;
                final double y = guClicked.y - (pxCenter.y - e.getY()) / newRatioPx2Gu;
                cam.setViewCenter(x, y, 0);
                cam.setViewPercent(zoom);
            } else {
                cam.resetView();
            }
        });

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    GraphicElement element = view.findNodeOrSpriteAt(e.getX(), e.getY());
                    if (element instanceof Node) {
                        final JPopupMenu popup = new JPopupMenu();
                        JMenuItem deleteItem = new JMenuItem("Delete Node");
                        deleteItem.addActionListener(el -> {
                            final String id = element.getId();
                            final Node clickedNode = network.getNode(id);
                            List<Node> neighborNodes = new LinkedList<>();
                            Iterator<Node> neighborNodeIter = clickedNode.getNeighborNodeIterator();
                            while (neighborNodeIter.hasNext()) {
                                neighborNodes.add(neighborNodeIter.next());
                            }
                            nodesInGraph.remove(g.getId(id));
                            network.removeNode(clickedNode);
                            neighborNodes.stream()
                                    .filter(n -> n.getDegree() == 0).forEach(n -> {
                                nodesInGraph.remove(g.getId(n.getId()));
                                network.removeNode(n);
                            });
                        });
                        popup.add(deleteItem);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        view.setPreferredSize(new Dimension(1280, 800));
        return view;
    }

    @Override
    public void run() {
        frame.setLayout(new BorderLayout());
        frame.add(makeToolBar(), BorderLayout.NORTH);
        frame.add(getVisualizer(), BorderLayout.CENTER);
        frame.add(status, BorderLayout.SOUTH);

        status.setHorizontalAlignment(SwingConstants.CENTER);
        status.setFont(new Font(status.getFont().getName(), Font.PLAIN, 40));

        frame.setLocation(200, 10);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        CallbackRunnable runnable = new CallbackRunnable(this::init);
        runnable.callback = this::enableAll;
        disableAll();
        runnable.start();
    }

    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui.renderer",
                "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        SwingUtilities.invokeLater(new GraphVisualizer());
    }
}