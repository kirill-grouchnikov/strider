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
package org.pushingpixels.strider.source;

import org.pushingpixels.strider.RippleConstants.SourceWaveform;
import org.pushingpixels.strider.RipplePanel;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class OscSource {
	private final RipplePanel ripplePanel;
	private double x;
	private double y;
	private double v;

	private int phaseDiff;

	private int freqDiff;

	private double speed;

	private SourceWaveform waveform;

	private int freqCount;

	private int frequency;

	private int timeToLive;

	private boolean isTemporary;

	public OscSource(RipplePanel ripplePanel, double xx, double yy) {
		this.ripplePanel = ripplePanel;
		this.x = xx;
		this.y = yy;
		this.phaseDiff = 0;
		this.freqDiff = 0;
		this.speed = 0;
		this.waveform = SourceWaveform.SWF_SIN;
		this.freqCount = 1;
		this.isTemporary = false;
	}

	public int getScreenX() {
		return (int) ((x - this.ripplePanel.getWindowOffsetX())
				* ripplePanel.getWidth() + ripplePanel.getWidth() / 2)
				/ this.ripplePanel.getWindowWidth();
	}

	public int getScreenY() {
		return (int) ((y - this.ripplePanel.getWindowOffsetY())
				* ripplePanel.getHeight() + ripplePanel.getHeight() / 2)
				/ this.ripplePanel.getWindowHeight();
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return x;
	}

	public void setV(double v) {
		this.v = v;
	}

	public double getV() {
		return v;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return y;
	}

	public int getPhaseDiff() {
		return phaseDiff;
	}

	public void setPhaseDiff(int phaseDiff) {
		this.phaseDiff = phaseDiff;
	}

	public int getFreqDiff() {
		return freqDiff;
	}

	public void setFreqDiff(int freqDiff) {
		this.freqDiff = freqDiff;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public SourceWaveform getWaveform() {
		return waveform;
	}

	public void setWaveform(SourceWaveform waveform) {
		this.waveform = waveform;
	}

	public int getFreqCount() {
		return freqCount;
	}

	public void setFreqCount(int freqCount) {
		this.freqCount = freqCount;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getFrequency() {
		return frequency;
	}

	/**
	 * Marks this source as having limited "life". This can be used for
	 * temporary notification sources (fire a few pulses and die).
	 * 
	 * @param ttl
	 *            Time to live (in terms of iteration cycles).
	 */
	public void setTimeToLive(int ttl) {
		this.isTemporary = true;
		this.timeToLive = ttl;
	}

	/**
	 * Decrements the time to live for this source. Has no effect is the source
	 * is not temporary.
	 */
	public void updateTimeToLive() {
		if (this.isTemporary)
			this.timeToLive--;
	}

	/**
	 * Returns a boolean indication whether this source is dead (time to live
	 * expired).
	 * 
	 * @return <code>true</code> if the source is dead (was marked as
	 *         temporary and time to live has expired), <code>false</code>
	 *         otherwise.
	 */
	public boolean isDead() {
		return this.isTemporary && (this.timeToLive <= 0);
	}
}