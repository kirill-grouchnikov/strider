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

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

import org.pushingpixels.strider.RippleConstants.Mode;
import org.pushingpixels.strider.source.OscSource;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class MouseHandler extends MouseInputAdapter {
	private boolean dragging;
	private FrameCallback dragCallback;
	private boolean dragClear;
	private boolean dragSet;
	private int dragX, dragY, dragStartX, dragStartY;

	private RipplePanel ripplePanel;

	public MouseHandler(RipplePanel ripplePanel) {
		this.ripplePanel = ripplePanel;
	}

	private void registerDragCallback(final int x, final int y) {
		this.dragCallback = new FrameCallback() {
			public void onFrameAdvance() {
				if ((ripplePanel.getSelectedSource() == null)
						&& (ripplePanel.getMode() == Mode.MODE_FUNCHOLD))
					editFuncPoint(x, y);
			}
		};
		this.ripplePanel.addCallback(this.dragCallback);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!dragging) {
			this.ripplePanel.selectSource(e);
			if (this.dragCallback != null) {
				this.ripplePanel.removeCallback(this.dragCallback);
			}
		}
		dragging = true;
		edit(e, true);
		this.ripplePanel.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (dragging)
			return;
		int x = e.getX();
		int y = e.getY();
		dragStartX = dragX = x;
		dragStartY = dragY = y;
		this.ripplePanel.selectSource(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.isPopupTrigger()) {
			// mark source to start "dying"
			OscSource selected = this.ripplePanel.getSelectedSource();
			if (selected != null)
				selected.setTimeToLive(20);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseMoved(e);
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
			return;
		dragging = true;
		edit(e, true);
	}

	private void edit(MouseEvent e, boolean toRegisterCallback) {
		int x = e.getX();
		int y = e.getY();
		OscSource selectedSource = this.ripplePanel.getSelectedSource();
		if (selectedSource != null) {
			x = x * this.ripplePanel.getWindowWidth()
					/ this.ripplePanel.getWidth();
			y = y * this.ripplePanel.getWindowHeight()
					/ this.ripplePanel.getHeight();
			if (x >= 0 && y >= 0 && x < this.ripplePanel.getWindowWidth()
					&& y < this.ripplePanel.getWindowHeight()) {
				selectedSource.setX(x + this.ripplePanel.getWindowOffsetX());
				selectedSource.setY(y + this.ripplePanel.getWindowOffsetY());
			}
			return;
		}
		if (dragX == x && dragY == y) {
			// editFuncPoint(dragX, dragY);
			// repaint();

			if (toRegisterCallback) {
				this.registerDragCallback(x, y);
			}
			this.ripplePanel.repaint();
		} else {
			// need to draw a line from old x,y to new x,y and
			// call editFuncPoint for each point on that line. yuck.
			if (Math.abs(y - dragY) > Math.abs(x - dragX)) {
				// y difference is greater, so we step along y's
				// from min to max y and calculate x for each step
				int x1 = (y < dragY) ? x : dragX;
				int y1 = (y < dragY) ? y : dragY;
				int x2 = (y > dragY) ? x : dragX;
				int y2 = (y > dragY) ? y : dragY;
				dragX = x;
				dragY = y;
				for (y = y1; y <= y2; y++) {
					x = x1 + (x2 - x1) * (y - y1) / (y2 - y1);
					editFuncPoint(x, y);
				}
			} else {
				// x difference is greater, so we step along x's
				// from min to max x and calculate y for each step
				int x1 = (x < dragX) ? x : dragX;
				int y1 = (x < dragX) ? y : dragY;
				int x2 = (x > dragX) ? x : dragX;
				int y2 = (x > dragX) ? y : dragY;
				dragX = x;
				dragY = y;
				for (x = x1; x <= x2; x++) {
					y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
					editFuncPoint(x, y);
				}
			}
		}
	}

	private void editFuncPoint(int x, int y) {
		// try {
		// throw new Exception();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		Point p = this.ripplePanel.pixelToGrid(x, y);
		int xp = p.x;
		// x * this.ripplePanel.getWindowWidth()
		// / this.ripplePanel.getWidth()
		// + this.ripplePanel.getWindowOffsetX();
		int yp = p.y;
		// y * this.ripplePanel.getWindowHeight()
		// / this.ripplePanel.getHeight()
		// + this.ripplePanel.getWindowOffsetY();
		// int gi = xp + yp * this.ripplePanel.getGridWidth();
		Mode mode = this.ripplePanel.getMode();
		if (mode == Mode.MODE_WALLS) {
			if (!dragSet && !dragClear) {
				dragClear = this.ripplePanel.hasWall(xp, yp);
				dragSet = !dragClear;
			}
			if (dragSet) {
				this.ripplePanel.setWall(xp, yp);
			} else {
				this.ripplePanel.clearWall(xp, yp);
			}
			this.ripplePanel.calcExceptions();
			this.ripplePanel.setFunc(xp, yp, 0);
			this.ripplePanel.setFunci(xp, yp, 0);
		} else if (mode == Mode.MODE_MEDIUM) {
			if (!dragSet && !dragClear) {
				dragClear = (this.ripplePanel.getMedium(xp, yp) > 0);
				dragSet = !dragClear;
			}
			this.ripplePanel.setMedium(xp, yp,
					(dragSet) ? RippleConstants.mediumMax : 0);
			this.ripplePanel.calcExceptions();
		} else {
			if (!dragSet && !dragClear) {
				dragClear = (this.ripplePanel.getFunc(xp, yp) > .1);
				dragSet = !dragClear;
			}
			this.ripplePanel.setFunc(xp, yp, (dragSet) ? 1 : -1);
			// System.out.println(System.currentTimeMillis() + ":" + xp + ":" +
			// yp
			// + ":" + this.ripplePanel.getFunc(xp, yp));
			this.ripplePanel.setFunci(xp, yp, 0);
		}
		this.ripplePanel.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			// mark source to start "dying"
			OscSource selected = this.ripplePanel.getSelectedSource();
			if (selected != null)
				selected.setTimeToLive(100);
		}

		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
			return;
		dragging = false;
		this.ripplePanel.removeCallback(this.dragCallback);
		this.dragCallback = null;
		dragSet = dragClear = false;
		this.ripplePanel.repaint();
	}
}