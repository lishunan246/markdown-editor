package com.lishunan246.mdeditor;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.RFonts;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.markdown4j.Markdown4jProcessor;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import static org.jsoup.Jsoup.parse;

/**
 * Created by lishunan on 14-12-27.
 */
public class MainWindow extends JFrame implements ActionListener, DocumentListener, CaretListener, TreeModelListener {

    protected boolean dirty=false;
    protected static String title="MarkDown Editor";

    String html;
    protected JEditorPane jEditorPane;
    protected JTextArea mdArea;
    protected HTMLEditorKit kit;
    protected JTree tree;
    protected DefaultTreeModel treeModel;
    protected DefaultMutableTreeNode top;

    MainWindow()
    {
        mdArea = new JTextArea();
        mdArea.getDocument().addDocumentListener(this);
        mdArea.addCaretListener(this);

        jEditorPane=new JEditorPane();
        jEditorPane.setEditable(false);
        kit = new HTMLEditorKit();

        top = new DefaultMutableTreeNode("document");

        treeModel = new DefaultTreeModel(top);
        treeModel.addTreeModelListener(this);

        tree = new JTree(treeModel);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setEditable(true);
        tree.setShowsRootHandles(true);

        setCSS();

        //jEditorPane.setEditorKit(kit);

        initMainWindow();
    }

    //try to load the default css
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
        setLayout(new BorderLayout());

        JSplitPane splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(mdArea));
        splitPane.setRightComponent(new JScrollPane(jEditorPane));

        add(splitPane,BorderLayout.CENTER);

        add(new JScrollPane(tree),BorderLayout.WEST);

        setTitle(title);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        initMenuBar();
        setVisible(true);

        //set after setVisible()
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
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

        menuItem=new JMenuItem("To docx");
        menuItem.setActionCommand("to docx");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem=new JMenuItem("Exit");
        menuItem.setActionCommand("exit");
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
            html = "<html>"+new Markdown4jProcessor().process(mdArea.getText())+"<html>";
            Document doc = parse(html);
            Elements h1s = doc.getElementsByTag("h1");
            System.out.println("how many h1");
            System.out.println(h1s.toArray().length);

            int c=top.getChildCount();
            System.out.println(top.getChildCount());
            while (top.getChildCount()!=0)
            {
                DefaultMutableTreeNode f = (DefaultMutableTreeNode) top.getChildAt(0);
                treeModel.removeNodeFromParent(f);
            }
            System.out.println(top.getChildCount());

            for(Element e:h1s)
            {
                DefaultMutableTreeNode node=new DefaultMutableTreeNode(e.text());
                treeModel.insertNodeInto(node,top,top.getChildCount());
                //top.add(node);
            }
            jEditorPane.setText(html);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("parse fail");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("open".equals(e.getActionCommand()))
        {
            if(dirty) {
                int i=JOptionPane.showConfirmDialog(this, "Do you want to save your work?");
                if (JOptionPane.OK_OPTION==i) {
                    saveAsMD();
                }
                else if (JOptionPane.NO_OPTION==i)
                {
                    openMD();
                }
                else if(JOptionPane.CANCEL_OPTION==i)
                {
                    return;
                }
            }
            else
            {
                openMD();
            }
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
            JOptionPane.showMessageDialog(this,"CSS will take effect when you run the program next time.");
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
                    JOptionPane.showMessageDialog(this,"CSS will take effect when you run the program next time.");
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.out.println("error copy css");
                }
            }
            //reopen();
        }
        else if ("exit".equals(e.getActionCommand()))
        {
            if(dirty) {
                int i=JOptionPane.showConfirmDialog(this, "Do you want to save your work before leaving?");
                if (JOptionPane.OK_OPTION==i) {
                    saveAsMD();
                    System.exit(0);
                }
                else if (JOptionPane.NO_OPTION==i)
                {
                    System.exit(0);
                }
                else if(JOptionPane.CANCEL_OPTION==i)
                {
                    return;
                }
            }
            else
                System.exit(0);
        }
        else if("to docx".equals(e.getActionCommand()))
        {
            try {
//        String baseURL = "file:///C:/Users/jharrop/git/docx4j-ImportXHTML/somedir/";
                String baseURL = "file:///C:/Users/jharrop/git/docx4j-ImportXHTML/";

                String unescaped = html;
//        if (stringFromFile.contains("&lt;/") ) {
//    		unescaped = StringEscapeUtils.unescapeHtml(stringFromFile);
//        }


//        XHTMLImporter.setTableFormatting(FormattingOption.IGNORE_CLASS);
//        XHTMLImporter.setParagraphFormatting(FormattingOption.IGNORE_CLASS);

                System.out.println("Unescaped: " + unescaped);

                // Setup font mapping
                RFonts rfonts = Context.getWmlObjectFactory().createRFonts();
                rfonts.setAscii("Century Gothic");
                XHTMLImporterImpl.addFontMapping("Century Gothic", rfonts);

                // Create an empty docx package
                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
                //WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new File(System.getProperty("user.dir") + "/styled.docx"));


                NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
                wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
                ndp.unmarshalDefaultNumbering();

                // Convert the XHTML, and add it into the empty docx we made
                XHTMLImporterImpl importer = new XHTMLImporterImpl(wordMLPackage);
                importer.setHyperlinkStyle("Hyperlink");
                wordMLPackage.getMainDocumentPart().getContent().addAll(importer.convert(unescaped, baseURL));

                System.out.println(XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));

//		System.out.println(
//				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getNumberingDefinitionsPart().getJaxbElement(), true, true));

                wordMLPackage.save(new java.io.File("OUT_from_XHTML.docx") );
            } catch (InvalidFormatException e1) {
                e1.printStackTrace();
            } catch (Docx4JException e1) {
                e1.printStackTrace();
            } catch (JAXBException e1) {
                e1.printStackTrace();
            }
        }
    }

    //load a md file
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
                setNotDirty();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.out.println("Fail to open file");
            }
        }
    }

    //open a dialog and save
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
                setNotDirty();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.out.println("fail to write file");
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        sync();
        setDirty();
    }

    private void setDirty() {
        dirty=true;
        updateTitle();
    }

    private void setNotDirty() {
        dirty=false;
        updateTitle();
    }

    private void updateTitle() {
        if(dirty)
        {
            setTitle("*-"+title);
        }
        else
            setTitle(title);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        sync();
        setDirty();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        try {
            //System.out.println(e.getDot());
            int line=mdArea.getLineOfOffset(e.getDot());
            //System.out.println(line);
            //jEditorPane.setCaretPosition(e.getDot());
        } catch (BadLocationException e1) {
            System.out.println("bad caret");
            e1.printStackTrace();
        }
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {

    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {

    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {

    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {

    }
}
