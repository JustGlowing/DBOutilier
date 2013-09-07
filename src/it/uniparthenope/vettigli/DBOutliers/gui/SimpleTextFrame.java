package it.uniparthenope.vettigli.DBOutliers.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SimpleTextFrame {
    private JFrame frame;
    private JPanel panel;
    private JTextArea area;
    private JScrollPane spane;

    public SimpleTextFrame(String title) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new JPanel();
        area = new JTextArea("Starting...\n", 22, 32);
        spane = new JScrollPane(area);
        spane.setAutoscrolls(true);
        panel.add(spane);
        frame.add(panel);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void addText(String str) {
        area.append(str + "\n");
        area.setCaretPosition(area.getText().length());
    }
}
