package com.lishunan246.mdeditor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.markdown4j.Markdown4jProcessor;
import com.google.common.io.Files;
import javax.swing.text.html.StyleSheet;

/**
 * Created by lishunan on 14-12-27.
 */
public class MainWindow extends JFrame implements ActionListener, DocumentListener {

    protected JEditorPane jEditorPane;
    protected JTextArea mdArea;
    protected HTMLEditorKit kit;

    MainWindow()
    {
        mdArea = new JTextArea();
        mdArea.getDocument().addDocumentListener(this);

        jEditorPane=new JEditorPane();
        jEditorPane.setEditable(false);
        kit = new HTMLEditorKit();

        setCSS();

        jEditorPane.setEditorKit(kit);

        initMainWindow();
    }

    private void setCSS() {
        File file=new File("default.css");
        if(!file.exists())
        {
            return;
        }
        URL css;
        try {
            css = file.toURI().toURL();
            StyleSheet s = new StyleSheet();
            s.importStyleSheet(css);
            kit.setStyleSheet(s);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void initMainWindow() {
        JSplitPane splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(mdArea));
        splitPane.setRightComponent(new JScrollPane(jEditorPane));
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        add(splitPane);

        setTitle("MarkDown Editor");
        setSize(800,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        initMenuBar();
        setVisible(true);
    }

    private void initMenuBar() {
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

        menu =new JMenu("CSS");
        menuBar.add(menu);

        menuItem=new JMenuItem("Set");
        menuItem.setActionCommand("set css");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem=new JMenuItem("Reset");
        menuItem.setActionCommand("reset css");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        setJMenuBar(menuBar);
    }

    private void sync() {
        try {
            jEditorPane.setText("<html>"+new Markdown4jProcessor().process(mdArea.getText())+"<html>");
        } catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("parse fail");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("open".equals(e.getActionCommand()))
        {
            openMD();
        }
        else if ("save as".equals(e.getActionCommand()))
        {
            saveAsMD();
        }
        else if("reset css".equals(e.getActionCommand()))
        {
            File file=new File("default.css");
            if(file.exists())
            {
                if (file.delete())
                {
                    System.out.println("reset!");
                }
                else
                {
                    System.out.println("cannot reset");
                }
            }
            //reopen();
        }
        else if("set css".equals(e.getActionCommand()))
        {
            JFileChooser chooser = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("CSS Files (*.css)", "css");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " +chooser.getSelectedFile().getName());
                File file=new File("default.css");
                try {
                    Files.copy(chooser.getSelectedFile(),file);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("error copy css");
                }
            }
            //reopen();
        }
    }

    private void reopen() {
        setVisible(false);
        new MainWindow();
        dispose();
    }

    private void openMD() {
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

    private void saveAsMD() {
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

    @Override
    public void insertUpdate(DocumentEvent e) {
        sync();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        sync();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
}
