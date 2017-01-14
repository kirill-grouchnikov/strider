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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.pushingpixels.strider.RippleGlassPane;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.MarinerSkin;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Based on the <a href=http://www.falstad.com/ripple/">original</a> code by
 * Paul Falstad.
 * 
 * @author Kirill Grouchnikov
 * @author Paul Falstad
 */
public class TestGlassPaneFrame extends JFrame {
	private RippleGlassPane rgp;

	public TestGlassPaneFrame() {
		super("Ripple test");
		this.setLayout(new BorderLayout());

		this.add(buildColumnSpanExample(), BorderLayout.CENTER);

		JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton showRipple = new JButton("Show ripple");
		showRipple.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						rgp.setVisible(true);
					}
				});
			}
		});
		controls.add(showRipple);
		JButton hideRipple = new JButton("Hide ripple");
		hideRipple.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						rgp.setVisible(false);
					}
				});
			}
		});
		controls.add(hideRipple);

		this.add(controls, BorderLayout.SOUTH);

		this.setSize(300, 300);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.rgp = new RippleGlassPane(getRootPane());
		this.rgp.ignoreClicksOn(JTextComponent.class, true);
		this.rgp.markAsMedium(AbstractButton.class, true);
		this.rgp.markAsWall(JTextComponent.class, true);
		this.rgp.setOpaque(false);
		this.rgp.setDamping(0.996);
		this.setGlassPane(this.rgp);
	}

	public static void main(String[] args) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
                JFrame.setDefaultLookAndFeelDecorated(true);
			    SubstanceLookAndFeel.setSkin(new MarinerSkin());
				new TestGlassPaneFrame().setVisible(true);
			}
		});
	}

	/**
	 * From test samples of FormLayout.
	 */
	private JComponent buildColumnSpanExample() {
		FormLayout layout = new FormLayout("pref, 8px, 40px, 4px, 160px",
				"pref, 6px, pref, 6px, pref, 6px, pref");

		JPanel panel = new JPanel(layout);
		panel.setBorder(Borders.DIALOG_BORDER);
		CellConstraints cc = new CellConstraints();

		panel.add(new JLabel("Name:"), cc.xy(1, 1));
		panel.add(new JTextField(), cc.xyw(3, 1, 3));

		panel.add(new JLabel("Phone:"), cc.xy(1, 3));
		panel.add(new JTextField(), cc.xyw(3, 3, 3));

		panel.add(new JLabel("ZIP/City:"), cc.xy(1, 5));
		panel.add(new JTextField(), cc.xy(3, 5));
		panel.add(new JTextField(), cc.xy(5, 5));

		panel.add(new JLabel("Country:"), cc.xy(1, 7));
		panel.add(new JTextField(), cc.xyw(3, 7, 3));

		return panel;
	}

}
