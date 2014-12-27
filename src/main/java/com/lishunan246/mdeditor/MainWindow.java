package com.lishunan246.mdeditor;

import javax.swing.*;

/**
 * Created by lishunan on 14-12-27.
 */
public class MainWindow extends JFrame{

    MainWindow()
    {
        setTitle("MarkDown Editor");
        setSize(800,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(true);
    }
}
