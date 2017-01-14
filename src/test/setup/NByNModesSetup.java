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

import org.pushingpixels.strider.RipplePanel;

import test.RippleFrame;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class NByNModesSetup extends FrameSetup {
	/**
	 * @param rippleFrame
	 */
	public NByNModesSetup(RippleFrame rippleFrame, RipplePanel ripplePanel) {
		super(rippleFrame, ripplePanel);
	}

	@Override
	public String getName() {
		return "NxN Modes";
	}

	@Override
	public void select() {
		int y = 1;
		int maxmode = 3;
		if (this.rippleFrame.getResolution() >= 70)
			maxmode++;
		if (this.rippleFrame.getResolution() >= 100)
			maxmode++;
		int ny = this.ripplePanel.getWindowHeight() / maxmode - 2;
		int nx = this.ripplePanel.getWindowWidth() / maxmode - 2;
		for (int modex = 1; modex <= maxmode; modex++)
			for (int modey = 1; modey <= maxmode; modey++) {
				int x1 = this.ripplePanel.getWindowOffsetX() + 1 + (ny + 1)
						* (modey - 1);
				int y1 = this.ripplePanel.getWindowOffsetY() + 1 + (nx + 1)
						* (modex - 1);
				for (int i = 0; i < nx + 2; i++) {
					this.ripplePanel.setWall(x1 + i - 1, y1 - 1);
					this.ripplePanel.setWall(x1 + i - 1, y1 + ny);
				}
				for (int j = 0; j < ny + 2; j++) {
					this.ripplePanel.setWall(x1 - 1, y1 + j - 1);
					this.ripplePanel.setWall(x1 + nx, y1 + j - 1);
				}
				this.ripplePanel.setupMode(x1, y1, nx, ny, modex, modey);
			}
		this.rippleFrame.setDampingValue(1);
	}

}