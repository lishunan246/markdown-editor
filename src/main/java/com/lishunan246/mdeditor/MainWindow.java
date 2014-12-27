package com.lishunan246.mdeditor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.markdown4j.Markdown4jProcessor;
import com.google.common.io.Files;

/**
 * Created by lishunan on 14-12-27.
 */
public class MainWindow extends JFrame implements ActionListener, DocumentListener {

    protected JLabel htmlArea;
    protected JTextArea mdArea;

    MainWindow()
    {
        setTitle("MarkDown Editor");
        setSize(800,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JMenuBar menuBar=new JMenuBar();
        JMenu menu=new JMenu("File");
        menuBar.add(menu);

        JMenuItem menuItem=new JMenuItem("Open");
        menuItem.setActionCommand("open");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem=new JMenuItem("Save as");
        menuItem.setActionCommand("save as");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        mdArea = new JTextArea();
        mdArea.getDocument().addDocumentListener(this);

        htmlArea = new JLabel();
        htmlArea.setVerticalAlignment(SwingConstants.TOP);

        JSplitPane splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(mdArea));
        splitPane.setRightComponent(new JScrollPane(htmlArea));
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        add(splitPane);

        setJMenuBar(menuBar);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("open".equals(e.getActionCommand()))
        {
            JFileChooser chooser = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("Markdown Files (*.md)", "md");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " +chooser.getSelectedFile().getName());

                try {
                    String string = Files.toString(chooser.getSelectedFile(), Charset.defaultCharset());
                    mdArea.setText(string);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("Fail to open file");
                }
            }
        }
        else if ("save as".equals(e.getActionCommand()))
        {
            JFileChooser chooser=new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("Markdown Files (*.md)", "md");
            chooser.setFileFilter(filter);
            if(chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION)
            {
                String path=chooser.getSelectedFile().getAbsolutePath();
                System.out.println(path);

                if(!path.endsWith(".md"))
                {
                    path+=".md";
                }
                try {
                    FileWriter writer=new FileWriter(path);
                    writer.write(mdArea.getText());
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("fail to write file");
                }
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        //System.out.println("insert");

        sync();
    }

    private void sync() {
        try {
            htmlArea.setText("<html>"+new Markdown4jProcessor().process(mdArea.getText())+"<html>");
        } catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("parse fail");
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        //System.out.println("remove");
        sync();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        //System.out.println("change");
    }
}
