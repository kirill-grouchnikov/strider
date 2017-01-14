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
import org.pushingpixels.strider.source.OscSource;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class TempGradient4Setup extends TempGradient3Setup {
	/**
	 * @param rippleFrame
	 */
	public TempGradient4Setup(RipplePanel ripplePanel) {
		super(ripplePanel);
	}

	@Override
	public String getName() {
		return "Temperature Gradient 4";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.ripple.setup.TempGradient3Setup#select()
	 */
	@Override
	public void select() {
		int j1 = this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight() / 2
				- this.ripplePanel.getWindowHeight() / 5;
		int j2 = this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight() / 2
				+ this.ripplePanel.getWindowHeight() / 5;
		int j3 = this.ripplePanel.getGridSizeY() / 2;
		for (int j = 0; j < this.ripplePanel.getGridSizeY(); j++) {
			int m;
			if (j < j1 || j > j2)
				m = 0;
			else if (j > j3)
				m = RippleConstants.mediumMax * (j2 - j) / (j2 - j3);
			else
				m = RippleConstants.mediumMax * (j - j1) / (j3 - j1);
			for (int i = 0; i < this.ripplePanel.getGridSizeX(); i++)
				this.ripplePanel.setMedium(i, j, m);
		}
		OscSource src = this.ripplePanel.createSource(0, this.ripplePanel
				.getWindowOffsetX() + 2, this.ripplePanel.getWindowOffsetY()
				+ this.ripplePanel.getWindowHeight() / 4);
		src.setFrequency(5);
	}

}