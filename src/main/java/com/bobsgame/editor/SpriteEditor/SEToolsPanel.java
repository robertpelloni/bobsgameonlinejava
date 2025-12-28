package com.bobsgame.editor.SpriteEditor;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.bobsgame.EditorMain;
import com.bobsgame.editor.SpriteEditor.Tools.EraserBrush;
import com.bobsgame.editor.SpriteEditor.Tools.FillBrush;
import com.bobsgame.editor.SpriteEditor.Tools.PixelBrush;

public class SEToolsPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	protected SpriteEditor SE;

	private JToggleButton pencilButton;
	private JToggleButton eraserButton;
	private JToggleButton fillButton;
	private ButtonGroup toolGroup;

	public SEToolsPanel(SpriteEditor se) {
		this.SE = se;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBorder(EditorMain.border);

		toolGroup = new ButtonGroup();

		pencilButton = new JToggleButton("Pencil");
		pencilButton.addActionListener(this);
		pencilButton.setSelected(true);
		toolGroup.add(pencilButton);
		add(pencilButton);

		eraserButton = new JToggleButton("Eraser");
		eraserButton.addActionListener(this);
		toolGroup.add(eraserButton);
		add(eraserButton);

		fillButton = new JToggleButton("Fill");
		fillButton.addActionListener(this);
		toolGroup.add(fillButton);
		add(fillButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == pencilButton) {
			SE.editCanvas.currentBrush = new PixelBrush();
		} else if(e.getSource() == eraserButton) {
			SE.editCanvas.currentBrush = new EraserBrush();
		} else if(e.getSource() == fillButton) {
			SE.editCanvas.currentBrush = new FillBrush();
		}
	}

}
