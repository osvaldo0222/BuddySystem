package logical;

import javax.swing.*;
import java.awt.*;

public class Node {
    private int heapPos;
    private int initValue;
    private int currentValue;
    private int width;
    private String status;
    private JButton memory;

    public Node(int heapPos, int initValue, int currentValue) {
        this.heapPos = heapPos;
        this.initValue = initValue;
        this.currentValue = currentValue;
        this.status = "free";
        this.memory = new JButton();
        this.memory.setText(currentValue + "");
        this.memory.setBackground(Color.GREEN);
    }

    public int getHeapPos() {
        return heapPos;
    }

    public void setHeapPos(int heapPos) {
        this.heapPos = heapPos;
    }

    public int getInitValue() {
        return initValue;
    }

    public void setInitValue(int initValue) {
        this.initValue = initValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JButton getMemory() {
        return memory;
    }

    public void setMemory(JButton memory) {
        this.memory = memory;
    }
}
