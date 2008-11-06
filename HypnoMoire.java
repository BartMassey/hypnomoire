/*
 * Copyright © 2008 Bart Massey
 * ALL RIGHTS RESERVED
 * [This program is licensed under the "3-clause ('new') BSD License"]
 * Please see the end of this file for license terms.
 */

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
 * @version 1.1
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
    private static final boolean debugging = false;

    private void status(String s) {
        showStatus(s);
	if (debugging)
	    System.out.println(s);
    }
    
    private void debug(String s) {
	if (debugging)
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
    
    public synchronized void init() {
	debug("Initializing");
        lag = get_double_parm("lag", 0.3);
        jump = get_double_parm("jump", 0.05);
        frame_rate = get_double_parm("frame_rate", 5.0);
	if (frame_rate > 100.0)
	    frame_rate = 100.0;
	if (frame_rate < 0.1)
	    frame_rate = 0.1;
        href = getParameter("href");
        if (href != null)
            try {
                href_url = new URL(href);
		href = href_url.toString();
            } catch (MalformedURLException ex) {
                status("HREF URL format error");
                href = null;
            }
	fg = new Color(255,255,255);
	bg = new Color(0,0,0);
	debug("In thread " + Thread.currentThread());
	debug("Initialized");
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

    private void establish_buffer() {
	if (buffer != null)
	    return;
	d = this.size();
	if (d.width == 0 || d.height == 0)
	    return;
	int ss = d.width * d.width + d.height * d.height;
	r = Math.sqrt(ss) + 1.0;
	xo = d.width / 2;
	yo = d.height / 2;
	buffer = createImage(d.width, d.height);
	bitmap = buffer.getGraphics();
    }

    public void paint(Graphics g) {
	debug("Painting");
	if (buffer == null)
	    return;
        g.drawImage(buffer, 0, 0, null);
    }

    public void update(Graphics g) {
	debug("Updating");
	if (buffer == null)
	    return;
        do_line(theta, bg);
        do_line(theta + lag, fg);
        paint(g);
        theta += jump;
        while (theta - 2 * Math.PI > 0)
	    theta -= 2 * Math.PI;
    }
    
    public synchronized void start() {
	debug("Starting");
	debug("In thread " + Thread.currentThread());
	motor = new Thread(this, "motor");
	motor.setPriority(Thread.MAX_PRIORITY);
	debug("Created " + motor);
	motor.start();
    }
    
    public synchronized void stop() {
	debug("Stopping");
	debug("In thread " + Thread.currentThread());
	motor.stop();
    }
    
    public void run() {
	debug("Running");
        while (true) {
	    Thread me = Thread.currentThread();
	    debug("In thread " + me);
	    debug("Sleeping");
	    try {
		me.sleep(round(1000.0/frame_rate));
	    } catch (InterruptedException e) {
		status("Interrupted");
	    }
	    debug("Repainting");
	    establish_buffer();
	    repaint();
        }
    }
}

/*
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the
 * following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer.
 *     * Redistributions in binary form must reproduce the
 *       above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of the copyright holder, nor the names
 *       of other affiliated organizations, nor the names
 *       of other contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
