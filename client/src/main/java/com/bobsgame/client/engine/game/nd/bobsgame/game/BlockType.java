package com.bobsgame.client.engine.game.nd.bobsgame.game;

import java.util.ArrayList;

import com.bobsgame.shared.BobColor;



//=========================================================================================================================
public class BlockType
{//=========================================================================================================================



	public String sprite = null;
	public String specialSprite = null;
	public BobColor[] colors = null;
	public BobColor specialColor = null;
	public int randomSpecialBlockChanceOneOutOf=0;
	public int frequencySpecialBlockTypeOnceEveryNPieces=0;



	public boolean flashingSpecialType = false;
	public int turnBackToNormalBlockAfterNPiecesLock = -1;
	public PieceType makePieceTypeWhenCleared = null;
	public boolean clearEveryOtherLineOnGridWhenCleared = false;

	public boolean counterType = false;
	public boolean pacmanType = false;

	public BlockType ifConnectedUpDownLeftRightToExplodingBlockChangeIntoThisType = null;
	public boolean addToChainIfConnectedUpDownLeftRightToExplodingChainBlocks = false;


	static public class TurnFromBlockTypeToType
	{
		public BlockType fromType = null;
		public BlockType toType = null;

		public TurnFromBlockTypeToType(BlockType fromType, BlockType toType)
		{
			this.fromType = fromType;
			this.toType = toType;
		}
	}

	public ArrayList<TurnFromBlockTypeToType> whenSetTurnAllTouchingBlocksOfFromTypesIntoToTypeAndDisappear = new ArrayList<TurnFromBlockTypeToType>();

	public boolean removeAllBlocksOfColorOnFieldBlockIsSetOn = false;
	public boolean changeAllBlocksOfColorOnFieldBlockIsSetOnToDiamondColor = false;
	public boolean matchAnyColor = false;


	public BlockType()
	{

	}



	//=========================================================================================================================
	public BlockType(String sprite,BobColor[] colors)
	{//=========================================================================================================================

		this.sprite = sprite;
		this.colors = colors;
	}
	//=========================================================================================================================
	public BlockType(String sprite,BobColor[] colors, BobColor specialColor)
	{//=========================================================================================================================

		this.sprite = sprite;
		this.colors = colors;
		this.specialColor = specialColor;
	}

	//=========================================================================================================================
	public BlockType(String sprite, BobColor[] colors, int randomSpecialBlockChanceOneOutOf, int frequencySpecialBlockTypeOnceEveryNBlocks)
	{//=========================================================================================================================
		this.sprite = sprite;
		this.colors = colors;
		this.randomSpecialBlockChanceOneOutOf = randomSpecialBlockChanceOneOutOf;
		this.frequencySpecialBlockTypeOnceEveryNPieces = frequencySpecialBlockTypeOnceEveryNBlocks;
	}

	//=========================================================================================================================
	public BlockType(String sprite, BobColor[] colors, BobColor specialColor, int randomSpecialBlockChanceOneOutOf, int frequencySpecialBlockTypeOnceEveryNBlocks)
	{//=========================================================================================================================
		this.sprite = sprite;
		this.colors = colors;
		this.specialColor = specialColor;
		this.randomSpecialBlockChanceOneOutOf = randomSpecialBlockChanceOneOutOf;
		this.frequencySpecialBlockTypeOnceEveryNPieces = frequencySpecialBlockTypeOnceEveryNBlocks;
	}

	//=========================================================================================================================
	public BlockType(String sprite,String specialSprite,BobColor[] colors,BobColor specialColor,int randomSpecialBlockChanceOneOutOf,int frequencySpecialBlockTypeOnceEveryNBlocks)
	{//=========================================================================================================================

		this.sprite = sprite;
		this.specialSprite = specialSprite;
		this.colors = colors;
		this.specialColor = specialColor;
		this.randomSpecialBlockChanceOneOutOf = randomSpecialBlockChanceOneOutOf;
		this.frequencySpecialBlockTypeOnceEveryNPieces = frequencySpecialBlockTypeOnceEveryNBlocks;

	}



	public boolean isSpecialType()
	{
		if(randomSpecialBlockChanceOneOutOf!=0)return true;
		if(frequencySpecialBlockTypeOnceEveryNPieces!=0)return true;
		if(flashingSpecialType!=false)return true;
		if(makePieceTypeWhenCleared!=null)return true;
		if(clearEveryOtherLineOnGridWhenCleared!=false)return true;

		return false;

	}





}

