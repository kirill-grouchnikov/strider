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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
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
public class RipplePanel extends JPanel {
	private BufferedImage dbimage;
	private int pixels[];

	// public Dimension winSize;
	private int gridSizeX;
	private int gridSizeY;
	private int gridSizeXY;
	protected int gridWidth;
	private int windowWidth = 50;
	private int windowHeight = 50;
	private int windowOffsetX = 0;
	private int windowOffsetY = 0;
	private int windowBottom = 0;
	private int windowRight = 0;

	private double dampcoef;
	private double movingSourcePos = 0;
	private double brightMult = 1;
	private double func[];
	private double funci[];
	private double damp[];
	private boolean walls[];
	private boolean exceptional[];
	private int medium[];

	private OscSource selectedSource;
	private double time;

	protected Color wallColor, posColor, negColor, zeroColor, medColor,
			posMedColor, negMedColor, sourceColor;

	private boolean isPaused;

	private boolean isFixedEdges;

	private Mode mode;

	private int currSpeed;

	private int currBrightness;

	// private Map<Integer, OscSource> sources;

	private List<OscSource> sources;

	private List<FrameCallback> callbackList;

	public RipplePanel(boolean hasMouseHandler) {
		super();
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				handleResize();
				repaint(100);
			}
		});
		if (hasMouseHandler) {
			MouseInputAdapter mia = new MouseHandler(this);
			this.addMouseMotionListener(mia);
			this.addMouseListener(mia);
		}

		this.isPaused = false;
		this.isFixedEdges = true;
		// this.sources = new HashMap<Integer, OscSource>();
		this.sources = new ArrayList<OscSource>();
		this.callbackList = new ArrayList<FrameCallback>();

		wallColor = new Color(128, 128, 128);
		posColor = new Color(0, 255, 0);
		negColor = new Color(255, 0, 0);
		zeroColor = new Color(0, 0, 0);
		posMedColor = new Color(0, 255, 255);
		negMedColor = new Color(255, 0, 255);
		medColor = new Color(0, 0, 255);
		sourceColor = Color.blue;
	}

	public void doBlank() {
		for (int i = 0; i < gridSizeXY; i++)
			func[i] = funci[i] = 0.0;
	}

	public void doBlankWalls() {
		for (int i = 0; i < gridSizeXY; i++) {
			walls[i] = false;
		}
		calcExceptions();
	}

	public void doBlankMedium() {
		for (int i = 0; i < gridSizeXY; i++) {
			medium[i] = 0;
		}
		calcExceptions();
	}

	public void doBorder() {
		for (int x = 0; x < gridSizeX; x++) {
			setWall(x, windowOffsetY);
			setWall(x, windowBottom);
		}
		for (int y = 0; y < gridSizeY; y++) {
			setWall(windowOffsetX, y);
			setWall(windowRight, y);
		}
		calcExceptions();
	}

	public boolean hasWall(int x, int y) {
		return walls[x + gridWidth * y];
	}

	public void setWall(int x, int y) {
		walls[x + gridWidth * y] = true;
	}

	public void clearWall(int x, int y) {
		walls[x + gridWidth * y] = false;
	}

	public void setMedium(int x, int y, int q) {
		medium[x + gridWidth * y] = q;
	}

	public int getMedium(int x, int y) {
		return medium[x + gridWidth * y];
	}

	public void doSetup() {
		resetTime();
		doBlank();
		doBlankWalls();
		doBlankMedium();
		// setSetup(setup);
		sources.clear();
		calcExceptions();
		this.setDamping(1.0);
	}

	public void calcExceptions() {
		int x, y;
		// if walls are in place on border, need to extend that through
		// hidden area to avoid "leaks"
		for (x = 0; x != gridSizeX; x++)
			for (y = 0; y < windowOffsetY; y++) {
				walls[x + gridWidth * y] = walls[x + gridWidth * windowOffsetY];
				walls[x + gridWidth * (gridSizeY - y - 1)] = walls[x
						+ gridWidth * (gridSizeY - windowOffsetY - 1)];
			}
		for (y = 0; y < gridSizeY; y++)
			for (x = 0; x < windowOffsetX; x++) {
				walls[x + gridWidth * y] = walls[windowOffsetX + gridWidth * y];
				walls[gridSizeX - x - 1 + gridWidth * y] = walls[gridSizeX
						- windowOffsetX - 1 + gridWidth * y];
			}
		// generate exceptional array, which is useful for doing
		// special handling of elements
		for (x = 1; x < gridSizeX - 1; x++)
			for (y = 1; y < gridSizeY - 1; y++) {
				int gi = x + gridWidth * y;
				exceptional[gi] = walls[gi - 1] || walls[gi + 1]
						|| walls[gi - gridWidth] || walls[gi + gridWidth]
						|| walls[gi] || medium[gi] != medium[gi - 1]
						|| medium[gi] != medium[gi + 1];
				if ((x == 1 || x == gridSizeX - 2)
						&& medium[gi] != medium[gridSizeX - 1 - x + gridWidth
								* (y + 1)]
						|| medium[gi] != medium[gridSizeX - 1 - x + gridWidth
								* (y - 1)])
					exceptional[gi] = true;
			}
		// put some extra exceptions at the corners to ensure tadd2, sinth,
		// etc get calculated
		exceptional[1 + gridWidth] = exceptional[gridSizeX - 2 + gridWidth] = exceptional[1
				+ (gridSizeY - 2) * gridWidth] = exceptional[gridSizeX - 2
				+ (gridSizeY - 2) * gridWidth] = true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(this.dbimage, 0, 0, null);
	}

	public synchronized void handleResize() {
		Dimension d = this.getSize();
		this.pixels = null;
		if (d.width == 0)
			return;

		this.dbimage = new BufferedImage(d.width, d.height,
				BufferedImage.TYPE_INT_ARGB);
		this.pixels = ((DataBufferInt) this.dbimage.getRaster().getDataBuffer())
				.getData();
	}

	public synchronized void reinit() {
		// this.isInitializing = true;
		// sourceCount = -1;
		// System.err.println("reinit " + gridSizeX + " " + gridSizeY + "\n");
		gridSizeXY = gridSizeX * gridSizeY;
		gridWidth = gridSizeY;
		func = new double[gridSizeXY];
		// System.err.println(func.length);
		funci = new double[gridSizeXY];
		damp = new double[gridSizeXY];
		exceptional = new boolean[gridSizeXY];
		medium = new int[gridSizeXY];
		walls = new boolean[gridSizeXY];
		int i, j;
		for (i = 0; i != gridSizeXY; i++)
			damp[i] = 1f; // (float) dampcoef;
		for (i = 0; i != windowOffsetX; i++)
			for (j = 0; j != gridSizeX; j++)
				damp[i + j * gridWidth] = damp[gridSizeX - 1 - i + gridWidth
						* j] = damp[j + gridWidth * i] = damp[j
						+ (gridSizeY - 1 - i) * gridWidth] = (float) (.999 - (windowOffsetX - i) * .002);
		// this.rippleFrame.doSetup();
		// this.isInitializing = false;
		// System.err.println("Reinit finished");
	}

	public void selectSource(MouseEvent me) {
		int x = me.getX();
		int y = me.getY();
		for (OscSource src : sources) {
			int sx = src.getScreenX();
			int sy = src.getScreenY();
			int r2 = (sx - x) * (sx - x) + (sy - y) * (sy - y);
			if (RippleConstants.sourceRadius * RippleConstants.sourceRadius > r2) {
				selectedSource = src;
				return;
			}
		}
		selectedSource = null;
	}

	// private int computeColor(int gix, double c) {
	// double h = func[gix] * brightMult;
	// if (c < 0)
	// c = 0;
	// if (c > 1)
	// c = 1;
	// c = .5 + c * .5;
	// double redness = (h < 0) ? -h : 0;
	// double grnness = (h > 0) ? h : 0;
	// if (redness > 1)
	// redness = 1;
	// if (grnness > 1)
	// grnness = 1;
	// if (grnness < 0)
	// grnness = 0;
	// if (redness < 0)
	// redness = 0;
	// double grayness = (1 - (redness + grnness)) * c;
	// double grayness2 = grayness;
	// if (medium[gix] > 0) {
	// double mm = 1 - (medium[gix] * (1 / 255.01));
	// grayness2 *= mm;
	// }
	// double gray = .6;
	// int ri = (int) ((c * redness + gray * grayness2) * 255);
	// int gi = (int) ((c * grnness + gray * grayness2) * 255);
	// int bi = (int) ((gray * grayness) * 255);
	// return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
	// }

	private void drawPlaneSource(int x1, int y1, int x2, int y2, float v,
			double w) {
		if (y1 == y2) {
			if (x1 == windowOffsetX)
				x1 = 0;
			if (x2 == windowOffsetX)
				x2 = 0;
			if (x1 == windowOffsetX + windowWidth - 1)
				x1 = gridSizeX - 1;
			if (x2 == windowOffsetX + windowWidth - 1)
				x2 = gridSizeX - 1;
		}
		if (x1 == x2) {
			if (y1 == windowOffsetY)
				y1 = 0;
			if (y2 == windowOffsetY)
				y2 = 0;
			if (y1 == windowOffsetY + windowHeight - 1)
				y1 = gridSizeY - 1;
			if (y2 == windowOffsetY + windowHeight - 1)
				y2 = gridSizeY - 1;
		}

		/*
		 * double phase = 0; if (sourceChooser.getSelectedIndex() ==
		 * SRC_1S1F_PLANE_PHASE) phase =
		 * (auxBar.getValue()-15)*3.8*freqBar.getValue()*freqMult;
		 */

		// need to draw a line from x1,y1 to x2,y2
		if (x1 == x2 && y1 == y2) {
			func[x1 + gridWidth * y1] = v;
			funci[x1 + gridWidth * y1] = 0;
		} else if (Math.abs(y2 - y1) > Math.abs(x2 - x1)) {
			// y difference is greater, so we step along y's
			// from min to max y and calculate x for each step
			double sgn = Math.signum(y2 - y1);
			int x, y;
			for (y = y1; y != y2 + sgn; y += sgn) {
				x = x1 + (x2 - x1) * (y - y1) / (y2 - y1);
				double ph = sgn * (y - y1) / (y2 - y1);
				int gi = x + gridWidth * y;
				func[gi] = v;
				// (phase == 0) ? v :
				// (float) (Math.sin(w+ph));
				funci[gi] = 0;
			}
		} else {
			// x difference is greater, so we step along x's
			// from min to max x and calculate y for each step
			double sgn = Math.signum(x2 - x1);
			int x, y;
			for (x = x1; x != x2 + sgn; x += sgn) {
				y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
				double ph = sgn * (x - x1) / (x2 - x1);
				int gi = x + gridWidth * y;
				func[gi] = v;
				// (phase == 0) ? v :
				// (float) (Math.sin(w+ph));
				funci[gi] = 0;
			}
		}
	}

	// filter out high-frequency noise
	private int filterCount;

	private void filterGrid() {
		int x, y;
		if (this.isFixedEdges)
			return;
		// if (!this.sources.isEmpty() && this.rippleFrame.getFrequency() > 23)
		// return;
		// if (sourceFreqCount >= 2)// && this.sourceFreqDiff > 23)
		// return;
		if (++filterCount < 10)
			return;
		filterCount = 0;
		for (y = windowOffsetY; y < windowBottom; y++)
			for (x = windowOffsetX; x < windowRight; x++) {
				int gi = x + y * gridWidth;
				if (walls[gi])
					continue;
				if (func[gi - 1] < 0 && func[gi] > 0 && func[gi + 1] < 0
						&& !walls[gi + 1] && !walls[gi - 1])
					func[gi] = (func[gi - 1] + func[gi + 1]) / 2;
				if (func[gi - gridWidth] < 0 && func[gi] > 0
						&& func[gi + gridWidth] < 0 && !walls[gi - gridWidth]
						&& !walls[gi + gridWidth])
					func[gi] = (func[gi - gridWidth] + func[gi + gridWidth]) / 2;
				if (func[gi - 1] > 0 && func[gi] < 0 && func[gi + 1] > 0
						&& !walls[gi + 1] && !walls[gi - 1])
					func[gi] = (func[gi - 1] + func[gi + 1]) / 2;
				if (func[gi - gridWidth] > 0 && func[gi] < 0
						&& func[gi + gridWidth] > 0 && !walls[gi - gridWidth]
						&& !walls[gi + gridWidth])
					func[gi] = (func[gi - gridWidth] + func[gi + gridWidth]) / 2;
			}
	}

	private void plotPixel(int x, int y, int pix) {
		if (x < 0 || x >= this.getWidth())
			return;
		if (y < 0 || y >= this.getHeight())
			return;

		// try {
		pixels[x + y * this.getWidth()] = pix;
		// } catch (Exception e) {
		// }
	}

	// draw a circle the slow and dirty way
	private void plotSource(OscSource src, int xx, int yy) {
		int rad = RippleConstants.sourceRadius;
		int j;
		int col = (sourceColor.getRed() << 16) | (sourceColor.getGreen() << 8)
				| (sourceColor.getBlue()) | 0xFF000000;
		if (src == this.selectedSource)
			col ^= 0xFFFFFF;
		for (j = 0; j <= rad; j++) {
			int k = (int) (Math.sqrt(rad * rad - j * j) + .5);
			plotPixel(xx + j, yy + k, col);
			plotPixel(xx + k, yy + j, col);
			plotPixel(xx + j, yy - k, col);
			plotPixel(xx - k, yy + j, col);
			plotPixel(xx - j, yy + k, col);
			plotPixel(xx + k, yy - j, col);
			plotPixel(xx - j, yy - k, col);
			plotPixel(xx - k, yy - j, col);
			plotPixel(xx, yy + j, col);
			plotPixel(xx, yy - j, col);
			plotPixel(xx + j, yy, col);
			plotPixel(xx - j, yy, col);
		}
	}

	private void draw2dView() {
		int ix = 0;
		int i, j, k, l;

		int cvw = this.getWidth();
		int cvh = this.getHeight();

		// System.out.println(func.length);
		for (j = 0; j != windowHeight; j++) {
			ix = cvw * (j * cvh / windowHeight);
			int j2 = j + windowOffsetY;
			int gi = j2 * gridWidth + windowOffsetX;
			int y = j * cvh / windowHeight;
			int y2 = (j + 1) * cvh / windowHeight;
			for (i = 0; i != windowWidth; i++, gi++) {
				int x = i * cvw / windowWidth;
				int x2 = (i + 1) * cvw / windowWidth;
				int i2 = i + windowOffsetX;
				double dy = func[gi] * brightMult;
				if (dy < -1)
					dy = -1;
				if (dy > 1)
					dy = 1;
				int col = 0;
				int colR = 0, colG = 0, colB = 0;
				int colAlpha = 0;
				if (walls[gi]) {
					colR = wallColor.getRed();
					colG = wallColor.getGreen();
					colB = wallColor.getBlue();
					colAlpha = wallColor.getAlpha();
				} else if (dy < 0) {
					double d1 = -dy;
					double d2 = 1 - d1;
					double d3 = medium[gi] * (1 / 255.01);
					double d4 = 1 - d3;
					double a1 = d1 * d4;
					double a2 = d2 * d4;
					double a3 = d1 * d3;
					double a4 = d2 * d3;
					colR = (int) (negColor.getRed() * a1 + zeroColor.getRed()
							* a2 + negMedColor.getRed() * a3 + medColor
							.getRed()
							* a4);
					colG = (int) (negColor.getGreen() * a1
							+ zeroColor.getGreen() * a2
							+ negMedColor.getGreen() * a3 + medColor.getGreen()
							* a4);
					colB = (int) (negColor.getBlue() * a1 + zeroColor.getBlue()
							* a2 + negMedColor.getBlue() * a3 + medColor
							.getBlue()
							* a4);
					colAlpha = (int) (negColor.getAlpha() * a1
							+ zeroColor.getAlpha() * a2
							+ negMedColor.getAlpha() * a3 + medColor.getAlpha()
							* a4);
				} else {
					double d1 = dy;
					double d2 = 1 - dy;
					double d3 = medium[gi] * (1 / 255.01);
					double d4 = 1 - d3;
					double a1 = d1 * d4;
					double a2 = d2 * d4;
					double a3 = d1 * d3;
					double a4 = d2 * d3;
					colR = (int) (posColor.getRed() * a1 + zeroColor.getRed()
							* a2 + posMedColor.getRed() * a3 + medColor
							.getRed()
							* a4);
					colG = (int) (posColor.getGreen() * a1
							+ zeroColor.getGreen() * a2
							+ posMedColor.getGreen() * a3 + medColor.getGreen()
							* a4);
					colB = (int) (posColor.getBlue() * a1 + zeroColor.getBlue()
							* a2 + posMedColor.getBlue() * a3 + medColor
							.getBlue()
							* a4);
					colAlpha = (int) (posColor.getAlpha() * a1
							+ zeroColor.getAlpha() * a2
							+ posMedColor.getAlpha() * a3 + medColor.getAlpha()
							* a4);
				}
				col = (colAlpha << 24) | (colR << 16) | (colG << 8) | (colB);
				for (k = 0; k != x2 - x; k++, ix++)
					for (l = 0; l != y2 - y; l++)
						pixels[ix + l * cvw] = col;
			}
		}
		int intf = (gridSizeY / 2 - windowOffsetY) * cvh / windowHeight;
		for (OscSource src : this.sources) {
			int xx = src.getScreenX();
			int yy = src.getScreenY();
			plotSource(src, xx, yy);
		}
	}

	private boolean moveRight = true;
	private boolean moveDown = true;

	public synchronized void updateRipple() {
		double tadd = 0;
		if (!this.isPaused) {
			int val = 5; // speedBar.getValue();
			tadd = val * .05;
		}
		int i, j;

		// System.out.println(this.gridSizeX + ":" + this.gridSizeY + ":" +
		// this.func.length + ":" + this.isInitializing);
		// boolean stopFunc = /* dragging && */(this.selectedSource == null)
		// && (this.mode == Mode.MODE_SETFUNC);
		// if (this.isPaused)
		// stopFunc = true;
		int iterCount = this.currSpeed;
		if (!this.isPaused) {
			/*
			 * long sysTime = System.currentTimeMillis(); if (sysTime-secTime >=
			 * 1000) { framerate = frames; steprate = steps; frames = 0; steps =
			 * 0; secTime = sysTime; } lastTime = sysTime;
			 */
			int iter;
			int mxx = gridSizeX - 1;
			int mxy = gridSizeY - 1;
			for (iter = 0; iter != iterCount; iter++) {
				int jstart, jend, jinc;
				if (moveDown) {
					// we process the rows in alternate directions
					// each time to avoid any directional bias.
					jstart = 1;
					jend = mxy;
					jinc = 1;
					moveDown = false;
				} else {
					jstart = mxy - 1;
					jend = 0;
					jinc = -1;
					moveDown = true;
				}
				moveRight = moveDown;
				float sinhalfth = 0;
				float sinth = 0;
				float scaleo = 0;
				int curMedium = -1;
				for (j = jstart; j != jend; j += jinc) {
					int istart, iend, iinc;
					if (moveRight) {
						iinc = 1;
						istart = 1;
						iend = mxx;
						moveRight = false;
					} else {
						iinc = -1;
						istart = mxx - 1;
						iend = 0;
						moveRight = true;
					}
					int gi = j * gridWidth + istart;
					int giEnd = j * gridWidth + iend;
					for (; gi != giEnd; gi += iinc) {
						// calculate equilibrum point of this
						// element's oscillation
						double previ = func[gi - 1];
						double nexti = func[gi + 1];
						double prevj = func[gi - gridWidth];
						double nextj = func[gi + gridWidth];
						double basis = (nexti + previ + nextj + prevj) * .25f;
						if (exceptional[gi]) {
							if (curMedium != medium[gi]) {
								curMedium = medium[gi];
								double tadd2 = tadd
										* (1 - (RippleConstants.mediumMaxIndex / RippleConstants.mediumMax)
												* curMedium);
								sinhalfth = (float) Math.sin(tadd2 / 2);
								sinth = (float) (Math.sin(tadd2) * dampcoef);
								scaleo = (float) (1 - Math.sqrt(4 * sinhalfth
										* sinhalfth - sinth * sinth));
							}
							if (walls[gi])
								continue;
							int count = 4;
							if (this.isFixedEdges) {
								if (walls[gi - 1])
									previ = 0;
								if (walls[gi + 1])
									nexti = 0;
								if (walls[gi - gridWidth])
									prevj = 0;
								if (walls[gi + gridWidth])
									nextj = 0;
							} else {
								if (walls[gi - 1])
									previ = walls[gi + 1] ? func[gi]
											: func[gi + 1];
								if (walls[gi + 1])
									nexti = walls[gi - 1] ? func[gi]
											: func[gi - 1];
								if (walls[gi - gridWidth])
									prevj = walls[gi + gridWidth] ? func[gi]
											: func[gi + gridWidth];
								if (walls[gi + gridWidth])
									nextj = walls[gi - gridWidth] ? func[gi]
											: func[gi - gridWidth];
							}
							basis = (nexti + previ + nextj + prevj) * .25f;
						}
						// what we are doing here (aside from damping)
						// is rotating the point (func[gi], funci[gi])
						// an angle tadd about the point (basis, 0).
						// Rather than call atan2/sin/cos, we use this
						// faster method using some precomputed info.
						double a = 0;
						double b = 0;
						if (damp[gi] == 1f) {
							a = func[gi] - basis;
							b = funci[gi];
						} else {
							a = (func[gi] - basis) * damp[gi];
							b = funci[gi] * damp[gi];
						}
						func[gi] = basis + a * scaleo - b * sinth;
						funci[gi] = b * scaleo + a * sinth;
					}
				}
				time += tadd;
				// System.out.println(this.setup.getName() + ":" +
				// this.sourceCount);
				if (!this.sources.isEmpty()) {
					Map<Integer, Double> wMap = new HashMap<Integer, Double>();
					for (int si = 0; si < this.sources.size(); si++) {
						OscSource currSrc = this.sources.get(si);
						double w = currSrc.getFrequency() * time
								* RippleConstants.freqMult;
						double wVal = w;
						// switch (auxFunction) {
						// case AUX_FREQ:
						int freqDiff = currSrc.getFreqDiff();
						if (freqDiff > 0) {
							wVal += wVal * freqDiff * time
									* RippleConstants.freqMult;
						}
						// break;
						// case AUX_PHASE:
						wVal = wVal + (currSrc.getPhaseDiff() - 1)
								* (Math.PI / 29);
						// break;
						// }
						wMap.put(si, wVal);
					}
					// double v = 0;
					Map<Integer, Double> vMap = new HashMap<Integer, Double>();
					// double v2 = 0;
					for (int si = 0; si < this.sources.size(); si++) {
						double vVal = 0;
						OscSource currSrc = this.sources.get(si);
						double w = wMap.get(si);
						switch (currSrc.getWaveform()) {
						case SWF_SIN:
							vVal = Math.cos(w);
							if (this.sources.size() >= 2)
								vVal = Math.cos(wMap.get(si));
							else if (currSrc.getFreqCount() == 2)
								vVal = (vVal + Math.cos(wMap.get(si))) * .5;
							break;
						case SWF_SQUARE:
							w %= Math.PI * 2;
							vVal = (w < Math.PI) ? 1 : -1;
							break;
						case SWF_PULSE:
							w %= Math.PI * 2;
							double pulselen = Math.PI / 4;
							double pulselen2 = currSrc.getFrequency() * .2;
							if (pulselen2 < pulselen)
								pulselen = pulselen2;
							vVal = (w > pulselen) ? 0 : Math.sin(w * Math.PI
									/ pulselen);

							break;
						}
						currSrc.updateTimeToLive();
						vMap.put(si, vVal);
					}
					for (j = 0; j < this.sources.size(); j++) {
						sources.get(j).setV(vMap.get(j));
					}

					for (int si = 0; si < this.sources.size(); si++) {
						OscSource currSrc = this.sources.get(si);
						double srcSpeed = currSrc.getSpeed();
						if (srcSpeed == 0)
							continue;

						double currY = currSrc.getY();
						// double sy;

						double delta = tadd * srcSpeed;
						// System.out.println(delta);
						double newY = currY + delta;

						if (newY >= (this.gridSizeY - this.windowOffsetY)) {
							newY = this.gridSizeY - this.windowOffsetY - 1;
							currSrc.setSpeed(-srcSpeed);
						}
						if (newY < windowOffsetY) {
							newY = windowOffsetY;
							currSrc.setSpeed(-srcSpeed);
						}
						//
						// currY += tadd * .4 * srcSpeed;
						// double wm = currY;
						// int h = windowHeight - 3;
						// wm %= h * 2;
						// sy = (int) wm;
						// if (sy > h)
						// sy = 2 * h - sy;
						// //sy += windowOffsetY + 1;
						currSrc.setY(newY);
					}
					// if (sourceMoving) {
					// int sy;
					// movingSourcePos += tadd * .02 * this.sourceSpeed;
					// double wm = movingSourcePos;
					// int h = windowHeight - 3;
					// wm %= h * 2;
					// sy = (int) wm;
					// if (sy > h)
					// sy = 2 * h - sy;
					// sy += windowOffsetY + 1;
					// sources.get(0).setY(sy);
					// }
					for (i = 0; i < this.sources.size(); i++) {
						OscSource src = sources.get(i);
						this.setFunc((int) src.getX(), (int) src.getY(), src
								.getV());
						this.setFunci((int) src.getX(), (int) src.getY(), 0);
					}
				}
				// System.out.println("Update frame");
				for (FrameCallback callback : callbackList)
					callback.onFrameAdvance();
				// System.out.println(this.callbackList.size() + " callbacks");
				filterGrid();

				Set<OscSource> deadSources = new HashSet<OscSource>();
				for (OscSource src : this.sources) {
					if (src.isDead())
						deadSources.add(src);
				}
				for (OscSource deadSrc : deadSources)
					this.sources.remove(deadSrc);
			}
		}

		brightMult = Math.exp(this.currBrightness / 100. - 5.);
		draw2dView();

		// realg.drawImage(dbimage, 0, 0, this);

		// if (!this.isPaused) {
		// if (dragging && selectedSource == -1
		// && this.mode == Mode.MODE_FUNCHOLD)
		// editFuncPoint(dragX, dragY);
		// repaint();
		// }
		repaint();
	}

	public void addMedium() {
		int i, j;
		for (i = 0; i != gridSizeX; i++)
			for (j = gridSizeY / 2; j != gridSizeY; j++)
				medium[i + j * gridWidth] = RippleConstants.mediumMax;
	}

	public void setupMode(int x, int y, int sx, int sy, int nx, int ny) {
		int i, j;
		for (i = 0; i != sx; i++)
			for (j = 0; j != sy; j++) {
				int gi = i + x + gridWidth * (j + y);
				func[gi] = (float) (Math.sin(Math.PI * nx * (i + 1) / (sx + 1)) * Math
						.sin(Math.PI * ny * (j + 1) / (sy + 1)));
				funci[gi] = 0;
			}
	}

	public void setupAcousticMode(int x, int y, int sx, int sy, int nx, int ny) {
		int i, j;
		for (i = 0; i != sx; i++)
			for (j = 0; j != sy; j++) {
				int gi = i + x + gridWidth * (j + y);
				func[gi] = (float) (Math.cos(Math.PI * nx * i / (sx - 1)) * Math
						.cos(Math.PI * ny * j / (sy - 1)));
				funci[gi] = 0;
			}
	}

	public void setResolution(int resolution) {
		windowWidth = windowHeight = resolution;
		windowOffsetX = windowOffsetY = 20;
		gridSizeX = windowWidth + windowOffsetX * 2;
		gridSizeY = windowHeight + windowOffsetY * 2;
		windowBottom = windowOffsetY + windowHeight - 1;
		windowRight = windowOffsetX + windowWidth - 1;
	}

	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}

	public void setFixedEdges(boolean isFixedEdges) {
		this.isFixedEdges = isFixedEdges;
	}

	public synchronized void setMode(Mode mode) {
		this.mode = mode;
	}

	public Mode getMode() {
		return this.mode;
	}

	public void setSetup(FrameCallback callback) {
		this.sources.clear();
		this.callbackList.clear();
		if (callback != null)
			this.callbackList.add(callback);
	}

	public synchronized void addCallback(FrameCallback callback) {
		this.callbackList.add(callback);
	}

	public synchronized void removeCallback(FrameCallback callback) {
		this.callbackList.remove(callback);
	}

	public void resetTime() {
		this.time = 0;
	}

	public int getGridSizeX() {
		return gridSizeX;
	}

	public void setGridSizeY(int gridSizeY) {
		this.gridSizeY = gridSizeY;
	}

	public int getGridSizeY() {
		return gridSizeY;
	}

	public int getWindowWidth() {
		return windowWidth;
	}

	public int getWindowHeight() {
		return windowHeight;
	}

	public int getWindowOffsetX() {
		return windowOffsetX;
	}

	public int getWindowOffsetY() {
		return windowOffsetY;
	}

	public int getWindowBottom() {
		return windowBottom;
	}

	public int getWindowRight() {
		return windowRight;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public double getFunc(int x, int y) {
		return this.func[x + gridWidth * y];
	}

	public void setFunc(int x, int y, double val) {
		// System.out.println(x + ":" + y + "->" + val);
		this.func[x + gridWidth * y] = val;
	}

	public double getFunci(int x, int y) {
		return this.funci[x + gridWidth * y];
	}

	public void setFunci(int x, int y, double val) {
		this.funci[x + gridWidth * y] = val;
	}

	public Point pixelToGrid(int x, int y) {
		int xp = x * this.getWindowWidth() / this.getWidth()
				+ this.getWindowOffsetX();
		int yp = y * this.getWindowHeight() / this.getHeight()
				+ this.getWindowOffsetY();
		return new Point(xp, yp);
	}

	public void setDamping(double value) {
		this.dampcoef = value;
	}

	public boolean isInitialized() {
		return (this.pixels != null);
	}

	public OscSource getSelectedSource() {
		return this.selectedSource;
	}

	public OscSource createSource(int sourceIndex, double x, double y) {
		OscSource source = new OscSource(this, x, y);
		this.sources.add(sourceIndex, source);
		return source;
	}

	public void setCurrBrightness(int currBrightness) {
		this.currBrightness = currBrightness;
	}

	public void setCurrSpeed(int currSpeed) {
		this.currSpeed = currSpeed;
	}
}