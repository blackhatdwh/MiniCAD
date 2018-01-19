/*
 * MiniCAD.java
 * Copyright (C) 2018 weihao <weihao@weihao-PC>
 *
 * Distributed under terms of the MIT license.
 */

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Shape;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.JSlider;
import javax.swing.JButton;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;

public class MiniCAD extends JFrame {
    JPanel selection_panel = new JPanel(new FlowLayout());
    JRadioButton select = new JRadioButton("Select");       // select a shape
    JRadioButton line = new JRadioButton("Line");       // draw a line
    JRadioButton rectangle = new JRadioButton("Rectangle");     // draw a rectangle
    JRadioButton circle = new JRadioButton("Circle");       // draw a circle
    JRadioButton text = new JRadioButton("Text");       // add some text
    ButtonGroup bg = new ButtonGroup();     // radio button group
    JTextField text_input = new JTextField(20);
    JButton delete_btn = new JButton("Delete");

    JButton color_btn = new JButton("Color");

    JButton open_btn = new JButton("Open");
    JButton save_btn = new JButton("Save");


    JSlider slider = new JSlider(0, 100);       // used to modify scale
    Canvas cvs = new Canvas();


    public MiniCAD() {
        this.setSize(1024, 700);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.selection_panel.add(open_btn);
        this.selection_panel.add(save_btn);
        this.selection_panel.add(select);
        this.selection_panel.add(line);
        this.selection_panel.add(rectangle);
        this.selection_panel.add(circle);
        this.selection_panel.add(text);
        this.selection_panel.add(text_input);
        this.selection_panel.add(color_btn);
        this.selection_panel.add(delete_btn);
        this.bg.add(select);
        this.bg.add(line);
        this.bg.add(rectangle);
        this.bg.add(circle);
        this.bg.add(text);



        this.add(selection_panel, BorderLayout.NORTH);
        this.add(cvs, BorderLayout.CENTER);
        this.add(slider, BorderLayout.PAGE_END);



        this.setVisible(true);


        // change color
        this.color_btn.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                cvs.current_color = JColorChooser.showDialog(null, "Choose Color", Color.BLACK);
                if(cvs.current_shape instanceof Text2D){
                    ((Text2D)cvs.current_shape).color = cvs.current_color;
                }
                else if(cvs.current_shape instanceof myLine2D){
                    ((myLine2D)cvs.current_shape).color = cvs.current_color;
                }
                else if(cvs.current_shape instanceof myRectangle2D){
                    ((myRectangle2D)cvs.current_shape).color = cvs.current_color;
                }
                else if(cvs.current_shape instanceof myEllipse2D){
                    ((myEllipse2D)cvs.current_shape).color = cvs.current_color;
                }
            }
        });

        // modify scale
        this.slider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e){
                if(cvs.current_shape != null){
                    if(cvs.current_shape instanceof myLine2D){
                        ((myLine2D)cvs.current_shape).setLine(
                            ((myLine2D)cvs.current_shape).getX1(),
                            ((myLine2D)cvs.current_shape).getY1(),
                            ((myLine2D)cvs.current_shape).getX1() + (cvs.original_scale_x - ((myLine2D)cvs.current_shape).getX1())*slider.getValue()/50.0,
                            ((myLine2D)cvs.current_shape).getY1() + (cvs.original_scale_y - ((myLine2D)cvs.current_shape).getY1())*slider.getValue()/50.0
                            );
                    }
                    else if(cvs.current_shape instanceof myRectangle2D){
                        ((myRectangle2D)cvs.current_shape).setRect(
                            ((myRectangle2D)cvs.current_shape).getX(),
                            ((myRectangle2D)cvs.current_shape).getY(),
                            (cvs.original_scale_x)*slider.getValue()/50.0,
                            (cvs.original_scale_y)*slider.getValue()/50.0
                            );
                    }
                    else if(cvs.current_shape instanceof myEllipse2D){
                        ((myEllipse2D)cvs.current_shape).setFrame(
                            ((myEllipse2D)cvs.current_shape).getX(),
                            ((myEllipse2D)cvs.current_shape).getY(),
                            (cvs.original_scale_x)*slider.getValue()/50.0,
                            (cvs.original_scale_x)*slider.getValue()/50.0
                            );
                    }
                    else if(cvs.current_shape instanceof Text2D){
                        ((Text2D)cvs.current_shape).fontsize = slider.getValue() / 2;
                    }
                }
                repaint();
            }
        });
        // reset slider to the default value
        this.cvs.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(cvs.current_shape == null){
                    slider.setValue(50);
                }
            }
        });
        
        // delete
        this.delete_btn.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                if(cvs.current_shape != null){
                    for(Shape s : cvs.shapes){
                        if(cvs.current_shape == s){
                            cvs.shapes.remove(cvs.shapes.indexOf(s));
                            break;
                        }
                    }
                }
                repaint();
            }
        });

        // open file
        this.open_btn.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                JFileChooser chooser = new JFileChooser();
                chooser.showOpenDialog(null);
                File f = chooser.getSelectedFile();
                try{
                    FileInputStream file = new FileInputStream(f);
                    ObjectInputStream in = new ObjectInputStream(file);
                    cvs.shapes.clear();
                    Shape restored_shape = null;
                    while((restored_shape = (Shape)in.readObject()) != null){
                        if(restored_shape instanceof myLine2D){
                            cvs.shapes.add((myLine2D)restored_shape);
                        }
                        else if(restored_shape instanceof myRectangle2D){
                            cvs.shapes.add((myRectangle2D)restored_shape);
                        }
                        else if(restored_shape instanceof myEllipse2D){
                            cvs.shapes.add((myEllipse2D)restored_shape);
                        }
                        else if(restored_shape instanceof Text2D){
                            cvs.shapes.add((Text2D)restored_shape);
                        }
                        else{
                            System.out.println("FATAL ERROR!!!");
                        }
                        
                    }
                }
                catch(Exception ex){}
                repaint();
            }
        });

        // save file
        this.save_btn.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                JFileChooser chooser = new JFileChooser();
                chooser.showSaveDialog(null);
                File f = chooser.getSelectedFile();
                try{
                    FileOutputStream file = new FileOutputStream(f);
                    ObjectOutputStream out = new ObjectOutputStream(file);
                    for(Shape s : cvs.shapes){
                        out.writeObject(s);
                    }
                    out.close();
                    file.close();
                }
                catch(Exception ex){}
            }
        });


    }



    public class Canvas extends JComponent{
        ArrayList<Shape> shapes = new ArrayList<Shape>();
        Color current_color = Color.BLACK;
        public Shape current_shape;
        double p_x, p_y;        // the current shape's pinned point
        Point2D.Double first_click;

        Point2D.Double previous_moment_cursor;

        public double original_scale_x;     // x2,y2 in Line2D, width and height in Rectangle2D and Ellipse2D
        public double original_scale_y;

        public Canvas(){
            this.addMouseListener(new MouseAdapter(){
                public void mouseClicked(MouseEvent e){
                    // select
                    if(select.isSelected()){
                        boolean found = false;
                        for(Shape s : shapes){
                            if(s instanceof myLine2D){
                                if(((myLine2D)s).ptLineDist(e.getX(), e.getY()) < 2){
                                    current_shape = s;
                                    found = true;
                                    break;
                                }
                            }
                            else if(s.contains(e.getX(), e.getY())){
                                current_shape = s;
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            current_shape = null;
                            original_scale_x = 0;
                            original_scale_y = 0;
                        }

                        if(current_shape != null){
                            if(current_shape instanceof myLine2D){
                                original_scale_x = ((myLine2D)current_shape).getX2();
                                original_scale_y = ((myLine2D)current_shape).getY2();
                            }
                            else if(current_shape instanceof Rectangle2D){
                                original_scale_x = ((myRectangle2D)current_shape).getWidth();
                                original_scale_y = ((myRectangle2D)current_shape).getHeight();
                            }
                            else if(current_shape instanceof Ellipse2D){
                                original_scale_x = ((myEllipse2D)current_shape).getWidth();
                                original_scale_y = ((myEllipse2D)current_shape).getHeight();
                            }
                        }
                        repaint();
                    }
                    // first click
                    else if(first_click == null){
                        first_click = new Point2D.Double(e.getX(), e.getY());
                        p_x = e.getX();
                        p_y = e.getY();

                        if(line.isSelected()){
                            createLine(p_x, p_y, cvs.current_color);
                        }
                        else if(rectangle.isSelected()){
                            createRectangle(p_x, p_y, cvs.current_color);
                        }
                        else if(circle.isSelected()){
                            createEllipse(p_x, p_y, cvs.current_color);
                        }
                        else if(text.isSelected()){
                            createText(text_input.getText(), p_x, p_y, cvs.current_color);
                            first_click = null;
                        }
                        repaint();
                    }
                    // second click
                    else{
                        current_shape = null;
                        first_click = null;
                        p_x = 0;
                        p_y = 0;
                        repaint();
                    }
                }
                public void mouseReleased(MouseEvent e){
                    previous_moment_cursor = null;
                    if(current_shape != null){
                        if(current_shape instanceof myLine2D){
                            original_scale_x = ((myLine2D)current_shape).getX2();
                            original_scale_y = ((myLine2D)current_shape).getY2();
                        }
                        else if(current_shape instanceof myRectangle2D){
                            original_scale_x = ((myRectangle2D)current_shape).getWidth();
                            original_scale_y = ((myRectangle2D)current_shape).getHeight();
                        }
                        else if(current_shape instanceof myEllipse2D){
                            original_scale_x = ((myEllipse2D)current_shape).getWidth();
                            original_scale_y = ((myEllipse2D)current_shape).getHeight();
                        }
                    }
                }

            });

            this.addMouseMotionListener(new MouseMotionAdapter(){
                public void mouseMoved(MouseEvent e){
                    if(first_click != null){
                        double c_x = e.getX();      // cursor x
                        double c_y = e.getY();      // cursor y
                        if(line.isSelected()){
                            ((myLine2D)current_shape).setLine(p_x, p_y, c_x, c_y);
                        }
                        else if(rectangle.isSelected()){
                            ((myRectangle2D)current_shape).setRect(Math.min(p_x, c_x), Math.min(p_y, c_y), Math.abs(p_x - c_x), Math.abs(p_y - c_y));
                        }
                        else if(circle.isSelected()){
                            double radius = first_click.distance(c_x, c_y);
                            ((myEllipse2D)current_shape).setFrame(p_x - radius, p_y - radius, 2 * radius, 2 * radius);
                        }
                        repaint();
                    }
                }
                // move
                public void mouseDragged(MouseEvent e){
                    if(current_shape != null){
                        if(previous_moment_cursor == null){
                            previous_moment_cursor = new Point2D.Double(e.getX(), e.getY());
                        }
                        if(current_shape instanceof Line2D){
                            ((myLine2D)current_shape).setLine(
                                ((myLine2D)current_shape).getX1() + e.getX() - previous_moment_cursor.getX(),
                                ((myLine2D)current_shape).getY1() + e.getY() - previous_moment_cursor.getY(),
                                ((myLine2D)current_shape).getX2() + e.getX() - previous_moment_cursor.getX(),
                                ((myLine2D)current_shape).getY2() + e.getY() - previous_moment_cursor.getY());
                        }
                        else if(current_shape instanceof Rectangle2D){
                            ((myRectangle2D)current_shape).setRect(
                                ((myRectangle2D)current_shape).getX() + e.getX() - previous_moment_cursor.getX(),
                                ((myRectangle2D)current_shape).getY() + e.getY() - previous_moment_cursor.getY(),
                                ((myRectangle2D)current_shape).getWidth(),
                                ((myRectangle2D)current_shape).getHeight());
                        }
                        else if(current_shape instanceof Ellipse2D){
                            ((myEllipse2D)current_shape).setFrame(
                                ((myEllipse2D)current_shape).getX() + e.getX() - previous_moment_cursor.getX(),
                                ((myEllipse2D)current_shape).getY() + e.getY() - previous_moment_cursor.getY(),
                                ((myEllipse2D)current_shape).getWidth(),
                                ((myEllipse2D)current_shape).getHeight());
                        }
                        else if(current_shape instanceof Text2D){
                            ((Text2D)current_shape).setLocation(
                                ((Text2D)current_shape).x + e.getX() - previous_moment_cursor.getX(),
                                ((Text2D)current_shape).y + e.getY() - previous_moment_cursor.getY());
                        }
                    }
                    previous_moment_cursor = new Point2D.Double(e.getX(), e.getY());
                    repaint();
                }

            });

        }

        public void paint(Graphics g){
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for(Shape s : shapes){
                if(s == current_shape){
                    if(s instanceof Text2D){
                        g.setFont(new Font("TimesRoman", Font.PLAIN, ((Text2D)s).fontsize));
                        g.setColor(Color.RED);
                        g.drawString(((Text2D)s).str, (int)((Text2D)s).x, (int)((Text2D)s).y);
                    }
                    else{
                        g2.setStroke(new BasicStroke(3));
                        g2.setPaint(Color.RED);
                        g2.draw(s);
                    }
                }
                else{
                    if(s instanceof Text2D){
                        g.setFont(new Font("TimesRoman", Font.PLAIN, ((Text2D)s).fontsize));
                        g.setColor(((Text2D)s).color);
                        g.drawString(((Text2D)s).str, (int)((Text2D)s).x, (int)((Text2D)s).y);
                    }
                    else{
                        g2.setStroke(new BasicStroke(1));
                        if(s instanceof myLine2D){
                            g2.setPaint(((myLine2D)s).color);
                        }
                        else if(s instanceof myRectangle2D){
                            g2.setPaint(((myRectangle2D)s).color);
                        }
                        else if(s instanceof myEllipse2D){
                            g2.setPaint(((myEllipse2D)s).color);
                        }
                        g2.draw(s);
                    }
                }
            }
        }

        public void createLine(double x, double y, Color color){
            //Line2D.Double tmp= new Line2D.Double(x, y, x, y);
            myLine2D tmp= new myLine2D(x, y, x, y, color);
            shapes.add(tmp);
            current_shape = tmp;
        }
        public void createRectangle(double x, double y, Color color){
            //Rectangle2D.Double tmp= new Rectangle2D.Double(x, y, 0, 0);
            myRectangle2D.Double tmp= new myRectangle2D(x, y, 0, 0, color);
            shapes.add(tmp);
            current_shape = tmp;
        }
        public void createEllipse(double x, double y, Color color){
            //Ellipse2D.Double tmp= new Ellipse2D.Double(x, y, 0, 0);
            myEllipse2D.Double tmp= new myEllipse2D(x, y, 0, 0, color);
            shapes.add(tmp);
            current_shape = tmp;
        }
        public void createText(String str, double x, double y, Color color){
            Text2D tmp= new Text2D(str, x, y, color);
            shapes.add((Shape)tmp);
            current_shape = (Shape)tmp;
        }
    }

    public static void main(String[] args){
        MiniCAD cad = new MiniCAD();
    }
}


class myLine2D extends Line2D.Double implements Serializable{
    Color color;
    public myLine2D(double x1, double y1, double x2, double y2, Color color){
        super(x1, y1, x2, y2);
        this.color = color;
    }
}

class myRectangle2D extends Rectangle2D.Double implements Serializable{
    Color color;
    public myRectangle2D(double x1, double y1, double w, double h, Color color){
        super(x1, y1, w, h);
        this.color = color;
    }
}

class myEllipse2D extends Ellipse2D.Double implements Serializable{
    Color color;
    public myEllipse2D(double x1, double y1, double w, double h, Color color){
        super(x1, y1, w, h);
        this.color = color;
    }
}


class Text2D implements Shape, Serializable{
    public String str;
    public double x;
    public double y;
    int fontsize = 25;
    Color color = Color.BLACK;

    public Text2D(String str, double x, double y, Color color){
        this.str = str;
        this.x = x;
        this.y = y;
        this.color = color;
    }
    public boolean contains(double x, double y, double w, double h){
        return false;
    }
    public boolean contains(double x, double y){
        Point2D tmp = new Point2D.Double(x, y);
        if(tmp.distance(this.x, this.y) < 25 && x > this.x && y < this.y){
            tmp = null;
            return true;
        }
        else{
            tmp = null;
            return false;
        }
    }
    public boolean contains(Point2D p){
        return true;
    }
    public boolean contains(Rectangle2D r){
        return true;
    }
    public Rectangle getBounds(){
        return null;
    }
    public Rectangle2D getBounds2D(){
        return null;
    }
    public PathIterator getPathIterator(AffineTransform at, double flatness){
        return null;
    }
    public PathIterator getPathIterator(AffineTransform at){
        return null;
    }
    public boolean intersects(double x, double y, double w, double h){
        return true;
    }
    public boolean intersects(Rectangle2D r){
        return true;
    }
    public void setLocation(double x, double y){
        this.x = x;
        this.y = y;
    }
}
