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
package test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pushingpixels.strider.FrameCallback;
import org.pushingpixels.strider.RippleConstants.Mode;
import org.pushingpixels.strider.RipplePanel;
import org.pushingpixels.strider.setup.Setup;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import test.setup.BeatsSetup;
import test.setup.BigModeSetup;
import test.setup.CircleSetup;
import test.setup.CoupledCavitiesSetup;
import test.setup.DipoleSourceSetup;
import test.setup.DispersionSetup;
import test.setup.Doppler2Setup;
import test.setup.DopplerSetup;
import test.setup.DoubleSourceSetup;
import test.setup.EllipseSetup;
import test.setup.HexapoleSetup;
import test.setup.LateralQuadrupoleSetup;
import test.setup.LinearQuadrupoleSetup;
import test.setup.Multi12Setup;
import test.setup.NByNAcoModesSetup;
import test.setup.NByNModeCombosSetup;
import test.setup.NByNModesSetup;
import test.setup.OctupoleSetup;
import test.setup.OneByNModeCombosSetup;
import test.setup.OneByNModesSetup;
import test.setup.OneByOneModesSetup;
import test.setup.QuadrupleSourceSetup;
import test.setup.RoomResonanceSetup;
import test.setup.SingleSourceSetup;
import test.setup.SlowMediumSetup;
import test.setup.SonicBoomSetup;
import test.setup.TempGradient1Setup;
import test.setup.TempGradient2Setup;
import test.setup.TempGradient3Setup;
import test.setup.TempGradient4Setup;
import test.setup.ZeroByNModesSetup;
import test.setup.ZeroByOneModesSetup;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class RippleFrame extends JFrame {

	private JSlider dampingBar;
	private JSlider speedBar;
	// private JSlider freqBar;
	private JSlider resBar;
	private JSlider brightnessBar;

	private Setup setup;

	private Thread renderingThread;

	private RipplePanel ripplePanel;
	private JCheckBox fixedEndsCheck;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				RippleFrame rf = new RippleFrame();
				rf.init();
				rf.startEngine();
				rf.setVisible(true);
			}
		});
	}

	public RippleFrame() {
		super("Ripple Tank");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void startEngine() {
		this.renderingThread = new Thread() {
			@Override
			public void run() {
				int DELAY = 10;
				while (true) {
					long start = System.currentTimeMillis();
					if (ripplePanel.isInitialized()) {
						synchronized (ripplePanel) {
							ripplePanel.updateRipple();
						}
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
	}

	private void init() {
		ripplePanel = new RipplePanel(true);

		this.setLayout(new BorderLayout());
		this.add(ripplePanel, BorderLayout.CENTER);

		FormLayout lm = new FormLayout("right:pref, 4dlu, fill:pref:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(lm);

		final JComboBox setupChooser = new JComboBox();
		setupChooser.addItem(new SingleSourceSetup(this.ripplePanel));
		setupChooser.addItem(new DoubleSourceSetup(this.ripplePanel));
		setupChooser.addItem(new QuadrupleSourceSetup(this.ripplePanel));
		setupChooser.addItem(new DipoleSourceSetup(this.ripplePanel));
		setupChooser.addItem(new LateralQuadrupoleSetup(this.ripplePanel));
		setupChooser.addItem(new LinearQuadrupoleSetup(this.ripplePanel));
		setupChooser.addItem(new HexapoleSetup(this.ripplePanel));
		setupChooser.addItem(new OctupoleSetup(this.ripplePanel));
		setupChooser.addItem(new Multi12Setup(this.ripplePanel));
		setupChooser.addItem(new DopplerSetup(this, this.ripplePanel));
		setupChooser.addItem(new Doppler2Setup(this.ripplePanel));
		setupChooser.addItem(new SonicBoomSetup(this, this.ripplePanel));
		setupChooser.addItem(new BigModeSetup(this, this.ripplePanel));
		setupChooser.addItem(new OneByOneModesSetup(this, this.ripplePanel));
		setupChooser.addItem(new OneByNModesSetup(this, this.ripplePanel));
		setupChooser.addItem(new NByNModesSetup(this, this.ripplePanel));
		setupChooser.addItem(new OneByNModeCombosSetup(this, this.ripplePanel));
		setupChooser.addItem(new NByNModeCombosSetup(this, this.ripplePanel));
		setupChooser.addItem(new ZeroByOneModesSetup(this, this.ripplePanel));
		setupChooser.addItem(new ZeroByNModesSetup(this, this.ripplePanel));
		setupChooser.addItem(new NByNAcoModesSetup(this, this.ripplePanel));
		setupChooser.addItem(new CoupledCavitiesSetup(this, this.ripplePanel));
		setupChooser.addItem(new BeatsSetup(this.ripplePanel));
		setupChooser.addItem(new SlowMediumSetup(this.ripplePanel));
		setupChooser.addItem(new CircleSetup(this.ripplePanel));
		setupChooser.addItem(new EllipseSetup(this.ripplePanel));
		setupChooser.addItem(new RoomResonanceSetup(this, this.ripplePanel));
		setupChooser.addItem(new TempGradient1Setup(this.ripplePanel));
		setupChooser.addItem(new TempGradient2Setup(this.ripplePanel));
		setupChooser.addItem(new TempGradient3Setup(this.ripplePanel));
		setupChooser.addItem(new TempGradient4Setup(this.ripplePanel));
		setupChooser.addItem(new DispersionSetup(this, this.ripplePanel));
		setupChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setup = (Setup) setupChooser.getSelectedItem();
						doSetup();
					}
				});
			}
		});

		setupChooser.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, ((Setup) value)
						.getName(), index, isSelected, cellHasFocus);
			}
		});
		builder.append("Setup", setupChooser);

		final JComboBox modeChooser = new JComboBox();
		modeChooser.addItem(Mode.MODE_SETFUNC);
		modeChooser.addItem(Mode.MODE_WALLS);
		modeChooser.addItem(Mode.MODE_MEDIUM);
		modeChooser.addItem(Mode.MODE_FUNCHOLD);
		modeChooser.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, ((Mode) value)
						.name(), index, isSelected, cellHasFocus);
			}
		});
		modeChooser.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				ripplePanel.setMode((Mode) modeChooser.getSelectedItem());
			}
		});
		builder.append("Mouse mode", modeChooser);

		JButton blankButton = new JButton("Clear waves");
		blankButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ripplePanel.doBlank();
				ripplePanel.repaint();
			}
		});
		builder.append(blankButton, new JLabel(""));

		JButton blankWallsButton = new JButton("Clear walls");
		blankWallsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ripplePanel.doBlankWalls();
				ripplePanel.repaint();
			}
		});
		builder.append(blankWallsButton, new JLabel(""));

		JButton blankMediumButton = new JButton("Clear medium");
		blankMediumButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ripplePanel.doBlankMedium();
				ripplePanel.repaint();
			}
		});
		builder.append(blankMediumButton, new JLabel(""));

		JButton borderButton = new JButton("Add border walls");
		borderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ripplePanel.doBorder();
				ripplePanel.repaint();
			}
		});
		builder.append(borderButton, new JLabel(""));

		final JCheckBox stoppedCheck = new JCheckBox("Stopped");
		stoppedCheck.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				ripplePanel.setPaused(stoppedCheck.isSelected());
				ripplePanel.repaint();
			}
		});
		builder.append(stoppedCheck, new JLabel(""));
		this.fixedEndsCheck = new JCheckBox("Fixed Edges", true);
		this.fixedEndsCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ripplePanel.setFixedEdges(fixedEndsCheck.isSelected());
			}
		});
		builder.append(fixedEndsCheck, new JLabel(""));

		builder.append("Simulation Speed", speedBar = new JSlider(1, 100, 1));
		speedBar.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ripplePanel.setCurrSpeed(speedBar.getValue());
			}
		});
		ripplePanel.setCurrSpeed(speedBar.getValue());

		// controlPanel.add(new JLabel("Resolution", JLabel.CENTER));
		builder.append("Resolution", resBar = new JSlider(5, 250, 100));
		resBar.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (resBar.getValueIsAdjusting())
					return;
				synchronized (ripplePanel) {
					setResolution();
					ripplePanel.reinit();
					doSetup();
				}
			}
		});
		setResolution();

		this.dampingBar = new JSlider(90, 100, 99);
		this.dampingBar.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ripplePanel.setDamping(dampingBar.getValue() / 100.0);
			}
		});
		builder.append("Damping", this.dampingBar);

		// builder.append("Source Frequency", freqBar = new JSlider(1, 30, 15));
		// freqBar.addChangeListener(new ChangeListener() {
		// public void stateChanged(ChangeEvent e) {
		// ripplePanel.setFrequency(freqBar.getValue());
		// }
		// });

		builder.append("Brightness", brightnessBar = new JSlider(1, 1200, 600));
		brightnessBar.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ripplePanel.setCurrBrightness(brightnessBar.getValue());
			}
		});
		ripplePanel.setCurrBrightness(brightnessBar.getValue());

		this.add(builder.getPanel(), BorderLayout.EAST);

		ripplePanel.setDamping(dampingBar.getValue() / 100.0);
		setup = (Setup) setupChooser.getSelectedItem();
		ripplePanel
				.setSetup((setup instanceof FrameCallback) ? (FrameCallback) setup
						: null);
		ripplePanel.reinit();
		ripplePanel.setBackground(Color.black);

		this.setSize(800, 540);
		this.doSetup();
		this.ripplePanel.handleResize();
		this.setLocationRelativeTo(null);
	}

	public void setResolution() {
		ripplePanel.setResolution(this.getResolution());
	}

	public void setResolution(int x) {
		resBar.setValue(x);
		setResolution();
		ripplePanel.reinit();
		this.doSetup();
	}

	void doSetup() {
		synchronized (ripplePanel) {
			ripplePanel
					.setSetup((setup instanceof FrameCallback) ? (FrameCallback) setup
							: null);
			ripplePanel.doSetup();

			if (resBar.getValue() < 32)
				setResolution(32);

			this.dampingBar.setValue(99);
			// setFrequency(5);

			setup.select();
			ripplePanel.calcExceptions();
		}
	}

	public void setDampingValue(double value) {
		this.dampingBar.setValue((int) (100.0 * value));
	}

	// public int getFrequency() {
	// return this.freqBar.getValue();
	// }
	//
	// public void setFrequency(int x) {
	// freqBar.setValue(x);
	// ripplePanel.setFrequency(x);
	// }

	public int getResolution() {
		return this.resBar.getValue();
	}

	public void setFixedEdges(boolean val) {
		this.fixedEndsCheck.setSelected(val);
	}
}
