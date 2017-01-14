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

import org.pushingpixels.strider.RippleConstants;
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
public class TempGradient1Setup extends Setup {
	/**
	 * @param rippleFrame
	 */
	public TempGradient1Setup(RipplePanel ripplePanel) {
		super(ripplePanel);
	}

	@Override
	public String getName() {
		return "Temperature Gradient 1";
	}

	@Override
	public void select() {
		int j1 = this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight() / 2;
		int j2 = this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight() * 3 / 4;
		int j3 = this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight() * 7 / 8;
		for (int j = 0; j < this.ripplePanel.getGridSizeY(); j++) {
			int m;
			if (j < j1)
				m = 0;
			else if (j > j2)
				m = RippleConstants.mediumMax;
			else
				m = RippleConstants.mediumMax * (j - j1) / (j2 - j1);
			for (int i = 0; i < this.ripplePanel.getGridSizeX(); i++)
				this.ripplePanel.setMedium(i, j, m);
		}
		for (int i = j3; i < this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight(); i++)
			this.ripplePanel.setWall(this.ripplePanel.getGridSizeX() / 2, i);

		OscSource src = this.ripplePanel.createSource(0, this.ripplePanel
				.getWindowOffsetX() + 2, this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight() - 2);
		src.setFrequency(5);
	}

}