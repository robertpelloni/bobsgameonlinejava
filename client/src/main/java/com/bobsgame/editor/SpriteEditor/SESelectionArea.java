package com.bobsgame.editor.SpriteEditor;

import java.awt.Color;

import com.bobsgame.editor.SelectionArea;
import com.bobsgame.editor.MultipleTileEditor.MTECanvas;
import com.bobsgame.editor.MultipleTileEditor.MTESelectionArea;
//===============================================================================================
public class SESelectionArea extends SelectionArea
{//===============================================================================================

	private SECanvas EA;

	protected int[][][] copy2;

	protected int copyWidth2, copyHeight2;

	public boolean isCopied2 = false;




	//===============================================================================================
	public SESelectionArea(SECanvas ea)
	{//===============================================================================================
		super();
		EA = ea;

		setBackground(new Color(255, 255, 255, 50));
	}


	//===============================================================================================
	public SECanvas getEditCanvas()
	{//===============================================================================================
		return EA;
	}




	//===============================================================================================
	public void copy()
	{//===============================================================================================


		if(isShowing)
		{
			copyWidth = x2 - x1;
			copyHeight = y2 - y1;
			copy = new int[copyWidth][copyHeight][1];//HYPER LAYER
			{
				for(int y = 0; y < copyHeight; y++)
				{
					for(int x = 0; x < copyWidth; x++)
					{

						copy[x][y][0] = getEditCanvas().getPixel(x1 + x, y1 + y); // Copy pixels within Selected Area

					}
				}
			}
			isCopiedOrCut = true;

		}
	}


	//===============================================================================================
	public void copykeys()
	{//===============================================================================================


		if(isShowing)
		{
			copyWidth2 = x2 - x1;
			copyHeight2 = y2 - y1;
			copy2 = new int[copyWidth2][copyHeight2][1];//HYPER LAYER
			{
				for(int y = 0; y < copyHeight2; y++)
				{
					for(int x = 0; x < copyWidth2; x++)
					{

						copy2[x][y][0] = getEditCanvas().getPixel(x1 + x, y1 + y); // Copy pixels within Selected Area

					}
				}
			}
			isCopied2 = true;

		}
	}

	//===============================================================================================
	public void cut()
	{//===============================================================================================
		copy();
		delete();
	}


	//===============================================================================================
	public void cutkeys()
	{//===============================================================================================
		copykeys();
		delete();
	}


	//===============================================================================================
	public void delete()
	{//===============================================================================================


		if(isShowing)
		{
			for(int y = 0; y < getHeight(); y++)
			{
				for(int x = 0; x < getWidth(); x++)
				{

					getEditCanvas().setPixel(x1 + x, y1 + y, 0); // Delete pixels within Selected Area

				}
			}
		}
	}


	//===============================================================================================
	public boolean paste()
	{//===============================================================================================


		if(isShowing && isCopiedOrCut && copyWidth <= getWidth() && copyHeight <= getHeight())
		{
			for(int y = 0; y < copyHeight; y++)
			{
				for(int x = 0; x < copyWidth; x++)
				{

					getEditCanvas().setPixel(x1 + x, y1 + y, copy[x][y][0]); // Paste pixels within Selected Area

				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}


	//===============================================================================================
	public boolean pastekeys()
	{//===============================================================================================

		if(isShowing && isCopied2 && copyWidth2 <= getWidth() && copyHeight2 <= getHeight())
		{
			for(int y = 0; y < copyHeight2; y++)
			{
				for(int x = 0; x < copyWidth2; x++)
				{
					getEditCanvas().setPixel(x1 + x, y1 + y, copy2[x][y][0]); // Paste pixels within Selected Area
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	//===============================================================================================
	public boolean pasteReverse()
	{//===============================================================================================
		if(isShowing && isCopiedOrCut && copyWidth <= getWidth() && copyHeight <= getHeight())
		{
			for(int y = 0; y < copyHeight; y++)
			{
				for(int x = 0; x < copyWidth; x++)
				{

					getEditCanvas().setPixel(x2 - x - 1, y1 + y, copy[x][y][0]); // Paste pixels within Selected Area

				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	//===============================================================================================
	public boolean pasteFlipped()
	{//===============================================================================================
		if(isShowing && isCopiedOrCut && copyWidth <= getWidth() && copyHeight <= getHeight())
		{
			for(int y = 0; y < copyHeight; y++)
			{
				for(int x = 0; x < copyWidth; x++)
				{

					getEditCanvas().setPixel(x1 + x, y2 - y - 1, copy[x][y][0]); // Paste pixels within Selected Area

				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}


}
