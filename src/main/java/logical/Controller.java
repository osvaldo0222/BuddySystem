package logical;
import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;
import java.util.List;

public class Controller extends Thread {
    private List<Node> heap;
    private Client client;
    private JFrame frameVisual;
    private JScrollPane mainPanel;
    private JProgressBar jProgressBar;
    private int memorySize;
    private int memoryUsage;
    private DefaultListModel<String> logs;
    private JList jList;
    private static Controller controller = null;

    private Controller() {
        heap = new ArrayList<>();
        client = new Client("127.0.0.1", 4567);
        String result;
        do {
            JFrame frame = new JFrame("InputDialog Example #2");
            result = JOptionPane.showInputDialog(
                    frame,
                    "Digite el tamaño del espacio de memoria",
                    "Tamaño",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (result == null) {
                System.exit(0);
            }
        } while(result == null || result.trim().equals("") || !result.trim().matches("[0-9]+") || Integer.parseInt(result.trim()) <= 0 || !twoPower(Integer.parseInt(result.trim())));
        memorySize = Integer.parseInt(result);
        client.write(result);
        String heapString = "";
        while (heapString.contains("[SERVER]") || heapString.trim().equals("")) {
            heapString = client.readLine();
            System.out.println(heapString);
        }
        String[] info = heapString.split(",");
        for (String aux : info) {
            String[] mem = aux.split(":");
            if (!mem[0].trim().equals("") && !mem[1].trim().equals("")) {
                Node node = new Node(Integer.parseInt(mem[0].trim()), Integer.parseInt(mem[1].trim()), Integer.parseInt(mem[1].trim()));
                heap.add(Integer.parseInt(mem[0].trim()), node);
            }
        }
        startVisual();
    }

    private void startVisual() {
        frameVisual = new JFrame("Buddy Algorithm");
        frameVisual.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameVisual.setExtendedState(frameVisual.getExtendedState() | JFrame.MAXIMIZED_BOTH );
        frameVisual.pack();
        refreshVisual();
        frameVisual.setResizable(false);
        frameVisual.setVisible(true);
    }

    public static Controller getInstance() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    private int leftChild(int index) {
        return (2 * index) + 1;
    }

    private int rightChild(int index) {
        return (2 * index) + 2;
    }

    private int parent(int index) {
        return (index -1)/2;
    }

    private void refreshVisual() {
        mainPanel = new JScrollPane();
        frameVisual.getContentPane().setLayout(new BorderLayout());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainPanel.setSize(new Dimension(screenSize.width, screenSize.height));
        frameVisual.add(mainPanel, BorderLayout.CENTER);
        mainPanel.setLayout(null);
        JLabel jLabel = new JLabel("Memory Map");
        jLabel.setBounds((mainPanel.getWidth()/2) - 40, 10, 100, 15);
        mainPanel.add(jLabel);
        float width = mainPanel.getWidth() - 20;
        int height_acum = 40;
        int height = 30;
        int x = 10;
        int y = 30;
        int prevValue = 0;
        for (Node aux : heap) {
            if (aux.getInitValue() == 1) {
                continue;
            }
            if (prevValue == aux.getInitValue()) {
                x += width;
            } else if(prevValue != 0) {
                x = 10;
                y += 30;
                width /= 2;
                height_acum += height;
            }
            prevValue = aux.getInitValue();
            aux.getMemory().setBounds(x, y, (int) width, height);
            mainPanel.add(aux.getMemory());
        }
        y += 30;
        JPanel jPanel = new JPanel();
        JLabel label = new JLabel("Memory Usage");
        label.setBounds((mainPanel.getWidth()/2) - 40, y, 100, 15);
        jPanel.add(label);
        jProgressBar = new JProgressBar();
        jProgressBar.setValue(0);
        jProgressBar.setStringPainted(true);
        jPanel.add(jProgressBar);
        jPanel.setSize(new Dimension(mainPanel.getWidth() - 20, 60));
        jPanel.setBounds(10, y + 15, mainPanel.getWidth() - 20, 60);
        jProgressBar.setPreferredSize(new Dimension(mainPanel.getWidth() - 20, 20));
        height_acum += jPanel.getHeight();
        mainPanel.add(jPanel);
        logs = new DefaultListModel<>();
        jList = new JList(logs);
        jList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jList.setLayoutOrientation(JList.VERTICAL_WRAP);
        jList.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane();
        listScroller.setViewportView(jList);
        jList.setAutoscrolls(true);
        listScroller.setPreferredSize(new Dimension(mainPanel.getWidth() - 20, mainPanel.getHeight() - height_acum - 110));
        listScroller.setBounds(10, y + 80, mainPanel.getWidth() - 20, mainPanel.getHeight() - height_acum - 110);
        listScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(listScroller);
    }

    private void calculate(String[] command){
        int index = Integer.parseInt(command[1].split(":")[0].trim());
        int parent = parent(index);
        int memory = Integer.parseInt(command[1].split(":")[1].trim());
        String[] divide = command[0].split("\\(");
        divide[1] = divide[1].replaceFirst("\\)", "");
        //divide[0] tiene el comando
        //divide[1] tiene la memoria que el proceso pidio
        String comando = "";
        switch (divide[0].toLowerCase()) {
            case "alloc":
                heap.get(index).setCurrentValue(heap.get(index).getCurrentValue() - memory);
                heap.get(index).getMemory().setText(heap.get(index).getCurrentValue() + "");
                heap.get(index).setStatus("allocated");
                heap.get(index).getMemory().setBackground(Color.RED);
                memoryUsage += memory;
                comando = "SO@Final>Se pidierón " + divide[1] + " bytes se otorgó un espacio de " + memory + " bytes";
                break;
            case "free":
                heap.get(index).setCurrentValue(heap.get(index).getCurrentValue() + memory);
                heap.get(index).getMemory().setText(heap.get(index).getCurrentValue() + "");
                heap.get(index).setStatus("free");
                heap.get(index).getMemory().setBackground(Color.GREEN);
                memoryUsage -= memory;
                comando = "SO@Final>Se liberarón " + memory + " bytes";
                break;
        }
        logs.addElement(comando);
        float porcentage = (float) ((float)(memorySize - memoryUsage)/(float)memorySize) * 100;
        jProgressBar.setValue(100 - (int) porcentage);
        if (index != 0) {
            int left = leftChild(parent), right = rightChild(parent);
            if (heap.get(left).getStatus().equalsIgnoreCase("allocated") && heap.get(right).getStatus().equalsIgnoreCase("allocated")) {
                heap.get(parent).setStatus("allocated");
                heap.get(parent).getMemory().setBackground(Color.RED);
            } else {
                heap.get(parent).setStatus("free");
                heap.get(parent).getMemory().setBackground(Color.GREEN);
            }
        }

    }

    public boolean twoPower(int n) {
        return n>0 && (n&n-1)==0;
    }

    @Override
    public void run() {
        while (true) {
            String frame = client.readLine();
            if (!frame.trim().equals("") && frame.contains("@")) {
                calculate(frame.split("@"));
            }
        }
    }
}
