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

import org.pushingpixels.strider.FrameCallback;
import org.pushingpixels.strider.RipplePanel;
import org.pushingpixels.strider.setup.Setup;
import org.pushingpixels.strider.source.OscSource;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class Doppler2Setup extends Setup implements FrameCallback {
	/**
	 * @param rippleFrame
	 */
	public Doppler2Setup(RipplePanel ripplePanel) {
		super(ripplePanel);
	}

	@Override
	public String getName() {
		return "Doppler Effect 2";
	}

	double wall;
	int dir;
	int waiting;

	@Override
	public void select() {
		wall = this.ripplePanel.getGridSizeY() / 2.;
		dir = 1;
		waiting = 0;
		OscSource src = this.ripplePanel.createSource(0, this.ripplePanel
				.getWindowOffsetX() + 1,
				this.ripplePanel.getWindowOffsetY() + 1);
		src.setFrequency(13);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.ripple.FrameCallback#eachFrame()
	 */
	public void onFrameAdvance() {
		if (waiting > 0) {
			waiting--;
			return;
		}
		int w1 = (int) wall;
		wall += dir * .04;
		int w2 = (int) wall;
		if (w1 != w2) {
			int i;
			for (i = this.ripplePanel.getWindowOffsetX()
					+ this.ripplePanel.getWindowWidth() / 3; i <= this.ripplePanel
					.getGridSizeX() - 1; i++) {
				this.ripplePanel.clearWall(i, w1);
				this.ripplePanel.setWall(i, w2);
				int gi = i + w1 * this.ripplePanel.getGridWidth();
				if (w2 < w1) {
					this.ripplePanel.setFunc(i, w1, 0);
					this.ripplePanel.setFunci(i, w1, 0);
					// this.rippleFrame.func[gi] = this.rippleFrame.funci[gi] =
					// 0;
				} else if (w1 > 1) {
					this.ripplePanel.setFunc(i, w1, this.ripplePanel.getFunc(i,
							w1 - 1) / 2);
					// this.rippleFrame.func[gi] = this.rippleFrame.func[gi
					// - this.rippleFrame.getGridWidth()] / 2;
					this.ripplePanel.setFunci(i, w1, this.ripplePanel.getFunci(
							i, w1 - 1) / 2);
					// this.rippleFrame.funci[gi] = this.rippleFrame.funci[gi
					// - this.rippleFrame.getGridWidth()] / 2;
				}
			}
			int w3 = (w2 - this.ripplePanel.getWindowOffsetY()) / 2
					+ this.ripplePanel.getWindowOffsetY();
			for (i = this.ripplePanel.getWindowOffsetY(); i < w3; i++)
				this.ripplePanel
						.setWall(this.ripplePanel.getGridSizeX() / 2, i);
			this.ripplePanel.clearWall(this.ripplePanel.getGridSizeX() / 2, i);
			this.ripplePanel.calcExceptions();
		}
		if (w2 == this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight() / 4
				|| w2 == this.ripplePanel.getWindowOffsetY()
						+ this.ripplePanel.getWindowHeight() * 3 / 4) {
			dir = -dir;
			waiting = 1000;
		}
	}

}