import java.lang.*;
import java.applet.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;

/**
 * A simple (not to say lame) graphics demo in Java.
 * The default parameters work well for about 500x500.
 * @param jump is the angle increment.
 * @param lag is the lag angle for the follow line.
 * @param frame_rate is the frames per second.
 * @param href is a URL to jump to on mouse up in the applet.
 * @see java.applet.Applet, java.lang.Runnable
 * @author Bart Massey
 * Some ideas borrowed from:
 *	The Java Handbook
 *	Patrick Naughton
 *	McGraw Hill 1996
 *	ISBN 0-07-882199-1
 * @version 1.0
 */
public class HypnoMoire extends Applet implements Runnable {
    private Dimension d;
    private double r;
    private double theta;
    private double lag;
    private double jump;
    private double frame_rate;
    private Thread motor;
    private Graphics bitmap;
    private Image buffer;
    private int xo;
    private int yo;
    private Color fg;
    private Color bg;
    private String href;
    private URL href_url;
    private boolean initialized = false;

    private void status(String s) {
        showStatus(s);
        System.out.println(s);
    }
    
    private double get_double_parm(String name, double iv) {
        Double parm = new Double(iv);
        String parm_str = getParameter(name);
        if (parm_str != null) {
            try {
                parm = new Double(parm_str);
            } catch(NumberFormatException e) {
                status("parameter " + name + " not a number");
            }
        }
        return parm.doubleValue();
    }
    
    public void init() {
        lag = get_double_parm("lag", 0.3);
        jump = get_double_parm("jump", 0.05);
        frame_rate = get_double_parm("frame_rate", 10.0);
        href = getParameter("href");
        if (href != null)
            try {
                URL base = getDocumentBase();
                href_url = new URL(base, href);
		href = href_url.toString();
            } catch (MalformedURLException ex) {
                status("HREF URL format error");
                href = null;
            }
        d = this.size();
        int ss = d.width * d.width + d.height * d.height;
        r = Math.sqrt(ss) + 1.0;
        xo = d.width / 2;
        yo = d.height / 2;
	fg = new Color(255,255,255);
	bg = new Color(0,0,0);
        buffer = createImage(d.width, d.height);
        bitmap = buffer.getGraphics();
	initialized = true;
    }

    private static int round(double d) {
        Long l = new Long(Math.round(d));
        return l.intValue();
    }

    private void do_line(double t, Color c) {
        int x1 = round(r * Math.cos(t));
        int y1 = round(r * Math.sin(t));
        bitmap.setColor(c);
        bitmap.drawLine(xo, yo, xo + x1, yo + y1);
    }

    public boolean mouseUp(Event e, int x, int y) {
        if (href != null) {
            getAppletContext().showDocument(href_url);
            return true;
        }
        return super.mouseUp(e, x, y);
    }

    public boolean mouseEnter(Event e, int x, int y) {
        if (href != null) {
            showStatus(href);
            return true;
        }
        return super.mouseEnter(e, x, y);
    }

    public void paint(Graphics g) {
        g.drawImage(buffer, 0, 0, null);
    }

    public void update(Graphics g) {
        do_line(theta, bg);
        do_line(theta + lag, fg);
        paint(g);
        theta += jump;
        while (theta - 2 * Math.PI > 0)
	    theta -= 2 * Math.PI;
    }
    
    public void start() {
        motor = new Thread(this);
        motor.setPriority(Thread.MIN_PRIORITY);
        motor.start();
    }

    public void stop() {
        motor.stop();
    }

    public void run() {
        while (true) {
            try {
                motor.sleep(round(1000.0 / frame_rate), 0);
            } catch (InterruptedException e) {
                status("interrupted");
            }
	    if (initialized)
                repaint();
        }
    }
}