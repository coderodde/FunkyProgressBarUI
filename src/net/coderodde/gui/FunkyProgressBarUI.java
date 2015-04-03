package net.coderodde.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ProgressBarUI;

/**
 * This class implements coderodde's UI for {@code javax.swing.JProgressBar}.
 * 
 * @author Rodion Efremov
 * @version 1.6
 */
public class FunkyProgressBarUI extends ProgressBarUI {

    
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.DARK_GRAY;
    private static final Color DEFAULT_BORDER_COLOR = Color.RED;
    private static final Color DEFAULT_NUMBER_COLOR = Color.BLACK;
    private static final Color DEFAULT_BAR_COLOR = Color.PINK;
    
    private static final int MINIMUM_BORDER_THICKNESS = 1;
    private static final int MAXIMUM_BORDER_THICKNESS = 10;
    private static final int DEFAULT_BORDER_THICKNESS = 4;
    
    private Color backgroundColor;
    private Color borderColor;
    private Color numberColor;
    private Color barColor;
    private int borderThickness;
    
    public FunkyProgressBarUI() {
        setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
        setBorderColor(DEFAULT_BORDER_COLOR);
        setNumberColor(DEFAULT_NUMBER_COLOR);
        setBarColor(DEFAULT_BAR_COLOR);
        setBorderThickness(DEFAULT_BORDER_THICKNESS);
    }
    
    public void setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public void setBorderColor(final Color borderColor) {
        this.borderColor = borderColor;
    }
    
    public void setNumberColor(final Color numberColor) {
        this.numberColor = numberColor;
    }
    
    public void setBarColor(final Color barColor) {
        this.barColor = barColor;
    }
    
    public void setBorderThickness(final int thickness) {
        this.borderThickness = Math.max(MINIMUM_BORDER_THICKNESS, 
                                        Math.min(MAXIMUM_BORDER_THICKNESS, 
                                                 thickness));
    }
    
    @Override
    public void paint(final Graphics g, final JComponent component) {
        update(g, component);
    }
    
    @Override
    public void update(final Graphics g, final JComponent component) {
        final int WIDTH = component.getWidth();
        final int HEIGHT = component.getHeight();
        
        //// Draw the border.
        g.setColor(borderColor);
        // Upper horizontal border.
        g.fillRect(0, 0, WIDTH, borderThickness);
        // Lower horizontal border.
        g.fillRect(0, HEIGHT - borderThickness, WIDTH, HEIGHT);
        // Left vertical border.
        g.fillRect(0, 
                   borderThickness, 
                   borderThickness, 
                   HEIGHT - 2 * borderThickness);
        // Right vertical border.
        g.fillRect(WIDTH - borderThickness,
                   borderThickness,
                   borderThickness,
                   HEIGHT - 2 * borderThickness);
        
        final double percentageReady = 
                ((JProgressBar) component).getPercentComplete();
        final int width = (int)((WIDTH - 2 * borderThickness) * 
                                 percentageReady / 2.0);
        
        //// Draw the bar and more.
        g.setColor(barColor);
        g.fillRect(borderThickness,
                   borderThickness,
                   WIDTH - 2 * borderThickness,
                   HEIGHT - 2 * borderThickness);
        
        //// Fill the exposed background.
        if (percentageReady < 1.0) {
            g.setColor(backgroundColor);
            g.fillRect(borderThickness + width,
                       borderThickness, 
                       WIDTH - 2 * borderThickness - 2 * width,
                       HEIGHT - 2 * borderThickness);
        }
    }
    
    public static void main(String[] args) {
        final int MIN_VALUE = 0;
        final int MAX_VALUE = 100;
        final int AVERAGE = (MIN_VALUE + MAX_VALUE) >>> 1;
        
        final JFrame frame = new JFrame("FunkyProgressBarUI");
        final JProgressBar bar1 = new JProgressBar(MIN_VALUE, MAX_VALUE);
        final JProgressBar bar2 = new JProgressBar(MIN_VALUE, MAX_VALUE);
        final JSlider slider = new JSlider(MIN_VALUE, MAX_VALUE, AVERAGE);
        final Dimension dim = new Dimension(301, 70);
        final ProgressBarUI ui = new FunkyProgressBarUI();
        final GridLayout layout = new GridLayout(3, 1, 10, 10);
        
        slider.addChangeListener(new MySliderChangeListener(bar2));
        
        bar1.setPreferredSize(dim);
        bar2.setPreferredSize(dim);
        bar2.setValue(slider.getValue());
        
        bar1.setUI(ui);
        bar2.setUI(ui);
        
        frame.setLayout(layout);
        frame.add(bar1);
        frame.add(slider);
        frame.add(bar2);
        frame.pack();
        
        final Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        
        frame.setLocation((screenDim.width - frame.getWidth()) / 2,
                          (screenDim.height - frame.getHeight()) / 2);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        new UpdateThread(bar1, 3, 100L).start();
    }
    
    private static final class MySliderChangeListener 
    implements ChangeListener {

        private final JProgressBar bar;

        public MySliderChangeListener(final JProgressBar bar) {
            this.bar = bar;
        }
        
        @Override
        public void stateChanged(final ChangeEvent e) {
            bar.setValue(((JSlider) e.getSource()).getValue());
            bar.repaint();
        }
    }
    
    private static final class UpdateThread extends Thread {
        
        private static final int MINIMUM_STEP = 1;
        private static final long MINIMUM_SLEEP_DURATION = 10L;
        
        private final JProgressBar bar;
        private final int step;
        private final long sleepDuration;
        
        UpdateThread(final JProgressBar bar, 
                     final int step,
                     final long sleepDuration) {
            this.bar = bar;
            this.step = Math.max(step, MINIMUM_STEP);
            this.sleepDuration = Math.max(sleepDuration, 
                                          MINIMUM_SLEEP_DURATION);
        }
        
        @Override
        public void run() {
            boolean increase = true;
            bar.setValue(bar.getMinimum());
            
            for (;;) {
                trySleep(sleepDuration);
                
                if (increase) {
                    bar.setValue(bar.getValue() + step);
                    
                    if (bar.getValue() >= bar.getMaximum()) {
                        // Change direction.
                        increase = !increase;
                    }
                } else {
                    bar.setValue(bar.getValue() - step);
                    
                    if (bar.getValue() <= bar.getMinimum()) {
                        // Change direction.
                        increase = !increase;
                    }
                }
                
                bar.repaint();
            }
        }
        
        private static final void trySleep(final long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (final InterruptedException ie) {
                
            }
        }
    }
}
