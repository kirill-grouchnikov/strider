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
package test.setup;

import java.util.Random;

import org.pushingpixels.strider.RipplePanel;

import test.RippleFrame;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class NByNModeCombosSetup extends FrameSetup {
	private Random random;

	/**
	 * @param rippleFrame
	 */
	public NByNModeCombosSetup(RippleFrame rippleFrame, RipplePanel ripplePanel) {
		super(rippleFrame, ripplePanel);
		this.random = new Random();
	}

	@Override
	public String getName() {
		return "NxN Mode Combos";
	}

	public int getrand(int x) {
		int q = random.nextInt();
		if (q < 0)
			q = -q;
		return q % x;
	}

	@Override
	public void select() {
		int i, j;
		int y = 1;
		int maxmode = 3;
		if (this.rippleFrame.getResolution() >= 70)
			maxmode++;
		if (this.rippleFrame.getResolution() >= 100)
			maxmode++;
		int ny = this.ripplePanel.getWindowHeight() / maxmode - 2;
		int nx = this.ripplePanel.getWindowWidth() / maxmode - 2;
		int gx, gy;
		for (gx = 1; gx <= maxmode; gx++)
			for (gy = 1; gy <= maxmode; gy++) {
				int mode1x = this.getrand(4) + 1;
				int mode1y = this.getrand(4) + 1;
				int mode2x, mode2y;
				do {
					mode2x = this.getrand(4) + 1;
					mode2y = this.getrand(4) + 1;
				} while (mode1x == mode2x && mode1y == mode2y);
				int x1 = this.ripplePanel.getWindowOffsetX() + 1 + (ny + 1)
						* (gx - 1);
				int y1 = this.ripplePanel.getWindowOffsetY() + 1 + (nx + 1)
						* (gy - 1);
				for (i = 0; i != nx + 2; i++) {
					this.ripplePanel.setWall(x1 + i - 1, y1 - 1);
					this.ripplePanel.setWall(x1 + i - 1, y1 + ny);
				}
				for (j = 0; j != ny + 2; j++) {
					this.ripplePanel.setWall(x1 - 1, y1 + j - 1);
					this.ripplePanel.setWall(x1 + nx, y1 + j - 1);
				}
				for (i = 0; i != nx; i++)
					for (j = 0; j != ny; j++) {
						int gi = i + x1 + this.ripplePanel.getGridWidth()
								* (j + y1);
						this.ripplePanel.setFunc(i + x1, j + y1, (float) (Math
								.sin(mode1x * Math.PI * (i + 1) / (nx + 1))
								* Math.sin(mode1y * Math.PI * (j + 1)
										/ (ny + 1)) * .5 + Math.sin(mode2x
								* Math.PI * (i + 1) / (nx + 1))
								* Math.sin(mode2y * Math.PI * (j + 1)
										/ (ny + 1)) * .5));
						this.ripplePanel.setFunci(i + x1, j + y1, 0);
					}
			}
		this.rippleFrame.setDampingValue(1);
	}

}