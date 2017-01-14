/*
 * Copyright (c) 2005-2007 Strider Paul Falstad and Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of Strider, Paul Falstad and Kirill Grouchnikov nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.strider;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class RippleGlassPane extends RipplePanel {
	private Thread renderingThread;

	private boolean isMousePressed;

	private int lastGridX;

	private int lastGridY;

	private float alpha;

	private Map<Class, Boolean> ignoreMap;

	private Map<Class, Boolean> wallMap;

	private Map<Class, Boolean> mediumMap;

	public RippleGlassPane(final JRootPane rootPane) {
		super(false);
		this.setCurrSpeed(4);
		this.setResolution(300);
		this.setCurrBrightness(600);
		this.setSetup(null);
		this.reinit();
		this.setBackground(new Color(0x0, true));
		this.doSetup();
		this.calcExceptions();
		this.handleResize();
		this.setDamping(0.998);
		this.alpha = 0.9f;
		this.posColor = Color.black;
		this.negColor = Color.black;
		this.zeroColor = new Color(0x0, true);
		this.wallColor = new Color(0x0, true);
		this.medColor = new Color(0x0, true);
		this.negMedColor = new Color(0x0, true);
		this.posMedColor = new Color(0x0, true);
		this.doBorder();

		this.ignoreMap = new HashMap<Class, Boolean>();
		this.wallMap = new HashMap<Class, Boolean>();
		this.mediumMap = new HashMap<Class, Boolean>();

		// double max = RippleConstants.mediumMax / 3.0;
		// for (int j = 0; j < this.getGridSizeY() / 2; j++) {
		// for (int i = 0; i < this.getGridSizeX(); i++)
		// this.setMedium(i, j, RippleConstants.mediumMax / 2
		// + (int) (max * Math.random()));
		// }

		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			public void eventDispatched(AWTEvent event) {
				if (!isVisible())
					return;
				if (event instanceof MouseEvent) {
					MouseEvent me = (MouseEvent) event;
					Object src = me.getSource();
					if (src instanceof Component) {
						if (toIgnore((Component) src))
							return;
					}
					if (event.getID() == MouseEvent.MOUSE_PRESSED) {
						int ex = me.getXOnScreen();
						int ey = me.getYOnScreen();
						int rx = ex - getLocationOnScreen().x;
						int ry = ey - getLocationOnScreen().y;

						Point gridPoint = pixelToGrid(rx, ry);
						lastGridX = gridPoint.x;
						lastGridY = gridPoint.y;

						// System.out.println(rx + ":" + ry + "-->" + lastGridX
						// + ":" + lastGridY);
						setFunc(lastGridX, lastGridY, (getFunc(lastGridX,
								lastGridY) > .1) ? 1 : -1);
						setFunci(lastGridX, lastGridY, 0);
					}

					if (event.getID() == MouseEvent.MOUSE_PRESSED) {
						isMousePressed = true;
					}
					if (event.getID() == MouseEvent.MOUSE_RELEASED) {
						isMousePressed = false;
					}
				}
			}
		}, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

		this.renderingThread = new Thread() {
			@Override
			public void run() {
				int DELAY = 40;
				while (true) {
					long start = System.currentTimeMillis();
					if (isInitialized()) {
						synchronized (RippleGlassPane.this) {
							updateRipple();
						}
					}
					if (isMousePressed) {
						// mouse hasn't been released - continue "pumping"
						// the current wave
						setFunc(lastGridX, lastGridY, (getFunc(lastGridX,
								lastGridY) > .1) ? 1 : -1);
						setFunci(lastGridX, lastGridY, 0);
					}
					long end = System.currentTimeMillis();
					long delta = DELAY - (end - start);
					if (delta > 0) {
						try {
							sleep(delta);
						} catch (InterruptedException ie) {
						}
					}
				}
			}
		};
		this.renderingThread.start();

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				doBlank();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				this.scanHierarchy(rootPane);
				calcExceptions();
			}

			private void scanHierarchy(Component c) {
				if (!c.isShowing())
					return;
				if (!c.isVisible())
					return;

				Point tl = SwingUtilities.convertPoint(c, new Point(0, 0),
						RippleGlassPane.this);
				Point br = SwingUtilities.convertPoint(c, new Point(c
						.getWidth(), c.getHeight()), RippleGlassPane.this);
				Point gridTL = pixelToGrid(tl.x, tl.y);
				Point gridBR = pixelToGrid(br.x, br.y);
				if (isWall(c)) {
					for (int x = gridTL.x; x <= gridBR.x; x++) {
						setWall(x, gridTL.y);
						setWall(x, gridBR.y);
					}
					for (int y = gridTL.y; y <= gridBR.y; y++) {
						setWall(gridTL.x, y);
						setWall(gridBR.x, y);
					}
				}
				if (isMedium(c)) {
					for (int x = gridTL.x; x <= gridBR.x; x++) {
						for (int y = gridTL.y; y <= gridBR.y; y++) {
							setMedium(x, y, RippleConstants.mediumMax / 2);
						}
					}
				}

				if (c instanceof Container) {
					Container cont = (Container) c;
					for (int i = 0; i < cont.getComponentCount(); i++) {
						scanHierarchy(cont.getComponent(i));
					}
				}
			}
		});

		// this.addComponentListener(new ComponentAdapter() {
		// @Override
		// public void componentResized(ComponentEvent e) {
		// SwingUtilities.invokeLater(new Runnable() {
		// @Override
		// public void run() {
		// handleResize();
		// }
		// });
		// }
		// });
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setComposite(AlphaComposite.SrcOver.derive(this.alpha));
		super.paintComponent(g2d);
		g2d.dispose();
	}

	public void ignoreClicksOn(Class compClass, boolean affectHierarchy) {
		this.ignoreMap.put(compClass, affectHierarchy);
	}

	private boolean toIgnore(Component comp) {
		for (Map.Entry<Class, Boolean> ignoreEntry : this.ignoreMap.entrySet()) {
			Class igClass = ignoreEntry.getKey();
			if (igClass == comp.getClass())
				return true;
			if (igClass.isAssignableFrom(comp.getClass())
					&& ignoreEntry.getValue())
				return true;
		}
		return false;
	}

	public void markAsMedium(Class compClass, boolean affectHierarchy) {
		this.mediumMap.put(compClass, affectHierarchy);
	}

	private boolean isMedium(Component comp) {
		for (Map.Entry<Class, Boolean> mediumEntry : this.mediumMap.entrySet()) {
			Class igClass = mediumEntry.getKey();
			if (igClass == comp.getClass())
				return true;
			if (igClass.isAssignableFrom(comp.getClass())
					&& mediumEntry.getValue())
				return true;
		}
		return false;
	}

	public void markAsWall(Class compClass, boolean affectHierarchy) {
		this.wallMap.put(compClass, affectHierarchy);
	}

	private boolean isWall(Component comp) {
		for (Map.Entry<Class, Boolean> wallEntry : this.wallMap.entrySet()) {
			Class igClass = wallEntry.getKey();
			if (igClass == comp.getClass())
				return true;
			if (igClass.isAssignableFrom(comp.getClass())
					&& wallEntry.getValue())
				return true;
		}
		return false;
	}
}
