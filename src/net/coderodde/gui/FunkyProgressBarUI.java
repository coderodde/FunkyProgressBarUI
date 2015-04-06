package net.coderodde.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
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

    /**
     * The minimum border thickness in pixels.
     */
    private static final int MINIMUM_BORDER_THICKNESS = 0;

    /**
     * The maximum border thickness in pixels.
     */
    private static final int MAXIMUM_BORDER_THICKNESS = 10;

    /**
     * The default border thickness in pixels.
     */
    private static final int DEFAULT_BORDER_THICKNESS = 4;
    
    /**
     * The maximum height factor of the percentage text in the area excluding
     * the border.
     */
    private static final float MAXIMUM_LENGTH_FACTOR = 0.85f;

    /**
     * The thickness of the border in pixels.
     */
    private int borderThickness;
    
    /**
     * Creates a new UI for a {@link javax.swing.JProgressBar} with default
     * attributes.
     */
    public FunkyProgressBarUI() {
        setBorderThickness(DEFAULT_BORDER_THICKNESS);
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
        final Color foregroundColor = component.getForeground();
        final Color backgroundColor = component.getBackground();
        final Color borderColor = getAverageColor(foregroundColor,
                                                  backgroundColor);
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
        g.setColor(foregroundColor);
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

        final Font font = checkFont(g, component);
        final FontMetrics fm = g.getFontMetrics(font);
        final String str = "" + (int)(100 * percentageReady) + "%";
        final int stringHeight = fm.getHeight();
        final int stringWidth = fm.stringWidth(str);

        g.setFont(checkFont(g, component));
        g.setColor(backgroundColor);
        g.setXORMode(foregroundColor);
        g.drawString(str, 
                     (WIDTH - stringWidth) / 2, 
                     ((HEIGHT + stringHeight) / 2) - fm.getDescent());
    }
    
    private Font checkFont(final Graphics g,
                           final JComponent component) {
        final int AVAILABLE_WIDTH = component.getWidth() - 2 * borderThickness;
        final int AVAILABLE_HEIGHT = 
                component.getHeight() - 2 * borderThickness;
        
        Font font = null;
        final Font componentFont = component.getFont();
        
        for (int fontSize = componentFont.getSize(); 
                 fontSize >= 0; 
                 fontSize--) {
            font = new Font(componentFont.getName(), 
                            componentFont.getStyle(), 
                            fontSize);
            
            final Rectangle2D rect = 
                    getStringRectangle(g,
                                       font,
                                       (JProgressBar) component);
            
            final int STRING_WIDTH = (int) rect.getWidth();
            final int STRING_HEIGHT = (int) rect.getHeight();
            
            if (STRING_WIDTH <= (int)(MAXIMUM_LENGTH_FACTOR * AVAILABLE_WIDTH)
                    && STRING_HEIGHT <=
                       (int)(MAXIMUM_LENGTH_FACTOR * AVAILABLE_HEIGHT)) {
                return font;
            }
        }
        
        return font;
    }
    
    private static Rectangle2D getStringRectangle(final Graphics g,
                                                  final Font font, 
                                                  final JProgressBar bar) {
        final String str = "" + (int)(100 * bar.getPercentComplete()) + "%";
        final FontMetrics fm = g.getFontMetrics(font);
        return fm.getStringBounds(str, g);
    }
    
    /**
     * The entry point to the demo program.
     * 
     * @param args ignored.
     */
    public static void main(final String... args) {
        final int MIN_VALUE = 0;
        final int MAX_VALUE = 100;
        final int AVERAGE = (MIN_VALUE + MAX_VALUE) >>> 1;
        
        // Create all the GUI components.
        final JFrame frame = new JFrame("FunkyProgressBarUI");
        final JProgressBar bar1 = new JProgressBar(MIN_VALUE, MAX_VALUE);
        final JProgressBar bar2 = new JProgressBar(MIN_VALUE, MAX_VALUE);
        final JSlider slider = new JSlider(MIN_VALUE, MAX_VALUE, AVERAGE);
        final Dimension dim = new Dimension(301, 70);
        final FunkyProgressBarUI ui = new FunkyProgressBarUI();
        final GridLayout layout = new GridLayout(3, 1, 10, 10);
        
        // Let slider to modify the second progress bar.
        slider.addChangeListener(new MySliderChangeListener(bar2));

        // Prepare the bars.
        bar1.setForeground(Color.GREEN);
        bar1.setBackground(Color.BLACK);
        bar1.setPreferredSize(dim);
        bar2.setPreferredSize(dim);
        bar2.setValue(slider.getValue());
        bar1.setFont(new Font("Times New Roman", Font.ITALIC, 40));
        bar2.setFont(new Font("Verdana", Font.BOLD, 25));

        // Make them Funky.
        bar1.setUI(ui);
        bar2.setUI(ui);

        // Constructing the GUI.
        frame.setLayout(layout);
        frame.add(bar1);
        frame.add(slider);
        frame.add(bar2);
        frame.pack();

        final Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

        // Move the frame to the center of the screen.
        frame.setLocation((screenDim.width - frame.getWidth()) / 2,
                          (screenDim.height - frame.getHeight()) / 2);

        // Almost ready.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Let the thread do its job.
        new UpdateThread(bar1, 3, 100L).start();
    }

    private Color getAverageColor(final Color color1, 
                                  final Color color2) {
        
        return new Color(ave(color1.getRed(), color2.getRed()),
                         ave(color1.getGreen(), color2.getGreen()),
                         ave(color1.getBlue(), color2.getBlue()));
    }
    
    private int ave(final int i, final int j) {
        return (i + j) / 2;
    }

    /**
     * Listens to the changes in a {@link javax.swing.JSlider} and updates a
     * {@link javax.swing.JProgressBar}.
     */
    private static final class MySliderChangeListener 
    implements ChangeListener {

        /**
         * The target <code>JProgressBar</code> to update.
         */
        private final JProgressBar bar;

        /**
         * Constructs a new listener with given target.
         * 
         * @param bar the target <code>JProgressBar</code>.
         */
        public MySliderChangeListener(final JProgressBar bar) {
            this.bar = bar;
        }

        /**
         * Updates the target <code>JProgressBar</code>.
         * 
         * @param e the event.
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            bar.setValue(((JSlider) e.getSource()).getValue());
            bar.repaint();
        }
    }

    /**
     * This thread modifies the value of a {@link javax.swing.JProgressBar}. It
     * increases the value of a bar to its maximum, after which it starts to 
     * decrease it. Once the minimum value is attained, this thread begins to
     * increase it, and so on.
     */
    private static final class UpdateThread extends Thread {

        /**
         * The minimum difference in the <code>bar</code>'s value.
         */
        private static final int MINIMUM_STEP = 1;

        /**
         * The minimum sleeping duration in milliseconds.
         */
        private static final long MINIMUM_SLEEP_DURATION = 10L;

        /**
         * The {@link javax.swing.JProgressBar} to modify.
         */
        private final JProgressBar bar;

        /**
         * The amount by which the value <code>bar</code> is changed.
         */
        private final int step;

        /**
         * The duration of sleeping in milliseconds.
         */
        private final long sleepDuration;

        /**
         * Constructs a new update thread.
         * 
         * @param bar           the target <code>JProgressBar</code>.
         * @param step          the step to use.
         * @param sleepDuration the sleep duration.
         */
        UpdateThread(final JProgressBar bar, 
                     final int step,
                     final long sleepDuration) {
            this.bar = bar;
            this.step = Math.max(step, MINIMUM_STEP);
            this.sleepDuration = Math.max(sleepDuration, 
                                          MINIMUM_SLEEP_DURATION);
        }

        /**
         * The entry point into this thread.
         */
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

        /**
         * Attempts to sleep the calling thread for <code>milliseconds</code>
         * milliseconds.
         * 
         * @param milliseconds the duration of a sleep.
         */
        private static final void trySleep(final long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            } catch (final InterruptedException ie) {

            }
        }
    }
}
