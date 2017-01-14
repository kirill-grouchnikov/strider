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

import org.pushingpixels.strider.RippleConstants.SourceWaveform;
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
public class CircleSetup extends Setup {
	public CircleSetup(RipplePanel ripplePanel) {
		super(ripplePanel);
		circle = true;
	}

	boolean circle;

	@Override
	public String getName() {
		return "Circle";
	}

	@Override
	public void select() {
		int i;
		int dx = this.ripplePanel.getWindowWidth() / 2 - 2;
		double a2 = dx * dx;
		double b2 = a2 / 2;
		if (circle)
			b2 = a2;
		int cx = this.ripplePanel.getWindowWidth() / 2
				+ this.ripplePanel.getWindowOffsetX();
		int cy = this.ripplePanel.getWindowHeight() / 2
				+ this.ripplePanel.getWindowOffsetY();
		int ly = -1;
		for (i = 0; i <= dx; i++) {
			double y = Math.sqrt((1 - i * i / a2) * b2);
			int yi = (int) (y + 1.5);
			if (i == dx)
				yi = 0;
			if (ly == -1)
				ly = yi;
			for (; ly >= yi; ly--) {
				this.ripplePanel.setWall(cx + i, cy + ly);
				this.ripplePanel.setWall(cx - i, cy + ly);
				this.ripplePanel.setWall(cx + i, cy - ly);
				this.ripplePanel.setWall(cx - i, cy - ly);
				// setWall(cx-ly, cx+i);
				// setWall(cx+ly, cx+i);
			}
			ly = yi;
		}
		int c = (int) (Math.sqrt(a2 - b2));
		// walls[cx+c][cy] = walls[cx-c][cy] = true;
		// walls[cx][cy+c] = true;
		OscSource src = this.ripplePanel.createSource(0, cx - c, cy);
		src.setWaveform(SourceWaveform.SWF_PULSE);
		src.setFrequency(1);
	}

}