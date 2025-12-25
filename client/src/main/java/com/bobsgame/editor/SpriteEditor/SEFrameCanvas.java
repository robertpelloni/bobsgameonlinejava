package com.bobsgame.editor.SpriteEditor;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;

import javax.swing.JComponent;

import com.bobsgame.editor.Project.Sprite.Sprite;
import com.bobsgame.shared.SpriteAnimationSequence;


//===============================================================================================
public class SEFrameCanvas extends JComponent implements MouseListener, ImageObserver
{//===============================================================================================


	protected SpriteEditor SE;



	//===============================================================================================
	public SEFrameCanvas(SpriteEditor se)
	{//===============================================================================================
		SE = se;

		addMouseListener(this);



	}

	//===============================================================================================
	public Sprite getSprite()
	{//===============================================================================================
		return SpriteEditor.getSprite();
	}


	//===============================================================================================
	public void paint(Graphics G)
	{//===============================================================================================

		super.paint(G);

		if(G!=null)
		{
			int zoom = 4;

			int w = getSprite().wP()*zoom;
			int h = getSprite().hP()*zoom;
			int f = getSprite().frames();

			setSize((w*f)+(f*1),h+40);

			G.setColor(SE.getSpritePal().getColor(0));
			G.fillRect(0,0,getWidth(),getHeight()-40);
			G.setColor(Color.BLACK);
			G.fillRect(0,getHeight()-40,getWidth(),getHeight());
			G.setFont(new Font("Tahoma",Font.PLAIN,9));

			for(int n=0;n<f;n++)
			{
				//draw sprite frame
				G.drawImage(getSprite().getFrameImage(n),((n*w)+(n*1))+1,0,w,h,this);

				//draw sprite frame hitbox
				SpriteAnimationSequence a = getSprite().getAnimationStartFrameForFrame(n);
				if(a!=null)
				{

					G.setColor(Color.RED);
					int x = ((n*w)+(n*1))+1;
					int y = 0;

					int hL = a.hitBoxFromLeftPixels1X*zoom;
					int hR = a.hitBoxFromRightPixels1X*zoom;
					int hT = a.hitBoxFromTopPixels1X*zoom;
					int hB = a.hitBoxFromBottomPixels1X*zoom;

					G.drawRect(x+hL, y+hT, ((w-hL)-hR)-1, ((h-hT)-hB)-1);
				}

				//draw divider line
				G.setColor(Color.BLACK);
				G.drawLine((n*w)+(n*1), 0, (n*w)+(n*1), h);

				//draw frame #
				G.setColor(Color.WHITE);
				G.drawString(""+n, ((n*w)+(n*1))+1, h+10);


				SpriteAnimationSequence currentFrameAnimation = getSprite().getAnimationForExactFrameOrNull(n);
				if(currentFrameAnimation!=null)
				{
					//draw frame Name
					G.setColor(Color.RED);
					G.drawString(currentFrameAnimation.frameSequenceName, ((n*w)+(n*1))+1, h+20);
				}
			}

			int currentFrame = getSprite().getSelectedFrameIndex();

			G.setColor(Color.GREEN);
			G.drawRect((currentFrame*w)+(currentFrame*1), 0, w, h);
			G.drawString(""+currentFrame, ((currentFrame*w)+(currentFrame*1))+1, h+10);
		}

	}
	//===============================================================================================
	public void scrollToFrame(int frame)
	{//===============================================================================================
		int w = getSprite().wP()*4;
		int f = getSprite().frames();

		Point p = new Point();

		p.x = ((w+1)*frame);
		p.y=0;
		//p.x = SE.frameScrollPane.getViewportSize().width;
		//p.y = SE.frameScrollPane.getViewportSize().height;

		SpriteEditor.frameScrollPane.getHorizontalScrollBar().setValue(p.x);
		SpriteEditor.frameScrollPane.getVerticalScrollBar().setValue(p.y);
	}

	//===============================================================================================
	@Override
	public void mouseClicked(MouseEvent me)
	{//===============================================================================================




	}


	//===============================================================================================
	@Override
	public void mousePressed(MouseEvent me)
	{//===============================================================================================

		int w = getSprite().wP()*4;
		int h = getSprite().hP()*4;
		int f = getSprite().frames();

		int x = me.getX();
		int y = me.getY();

		int frame = x/(w+1);

		getSprite().setFrame(frame);

		SpriteEditor.frameControlPanel.updateSpriteInfo();
		SpriteEditor.frameControlPanel.updateFrames();

		SpriteEditor.editCanvas.repaintBufferImage();
		SpriteEditor.editCanvas.repaint();



	}


	//===============================================================================================
	@Override
	public void mouseReleased(MouseEvent e)
	{//===============================================================================================


	}


	//===============================================================================================
	@Override
	public void mouseEntered(MouseEvent e)
	{//===============================================================================================


	}


	//===============================================================================================
	@Override
	public void mouseExited(MouseEvent e)
	{//===============================================================================================


	}


}
