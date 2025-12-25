package com.bobsgame.client.engine.game.nd.bobsgame.game;

import java.util.ArrayList;

import com.bobsgame.client.engine.game.nd.bobsgame.game.Piece.BlockOffset;
import com.bobsgame.client.engine.game.nd.bobsgame.game.Piece.Rotation;
import com.bobsgame.shared.BobColor;



//=========================================================================================================================
public class PieceType
{//=========================================================================================================================


	public int numBlocks = 1;
	public int lastRotation = 0;

	public BobColor color = null;

	public ArrayList<Rotation> rotationSet = new ArrayList<Rotation>();


	public int frequencySpecialPieceTypeOnceEveryNPieces=0;
	public int randomSpecialPieceChanceOneOutOf=0;


	public boolean flashingSpecialType = false;
	public boolean clearEveryRowPieceIsOnIfAnySingleRowCleared = false;

	public int turnBackToNormalPieceAfterNPiecesLock = -1;
	public boolean disappearOnceSetInGrid = false;


	public String spriteName = null;

	public boolean bombPiece = false;
	public boolean weightPiece = false;
	public boolean pieceRemovalShooterPiece = false;
	public boolean pieceShooterPiece = false;

	public BlockType overrideBlockType = null;


	//=========================================================================================================================
	public PieceType()
	{//=========================================================================================================================

		this.numBlocks = 1;
		this.lastRotation = 0;

		BlockOffset b = new BlockOffset(0,0);
		Rotation r = new Rotation();
		r.add(b);
		rotationSet.add(r);
	}
	// =========================================================================================================================
	public PieceType(BobColor color, int numBlocks, ArrayList<Rotation> rotationSet)
	{// =========================================================================================================================

		this.color = color;
		this.numBlocks = numBlocks;
		this.rotationSet = rotationSet;
		this.lastRotation = rotationSet.size()-1;
	}

	// =========================================================================================================================
	public PieceType(BobColor color, int numBlocks, ArrayList<Rotation> rotationSet, int randomSpecialPieceChanceOneOutOf, int frequencySpecialPieceTypeOnceEveryNPieces)
	{// =========================================================================================================================

		this.color = color;
		this.numBlocks = numBlocks;
		this.rotationSet = rotationSet;
		this.lastRotation = rotationSet.size()-1;
		this.randomSpecialPieceChanceOneOutOf = randomSpecialPieceChanceOneOutOf;
		this.frequencySpecialPieceTypeOnceEveryNPieces = frequencySpecialPieceTypeOnceEveryNPieces;
	}

	// =========================================================================================================================
	public PieceType(String spriteName, BobColor color, int numBlocks, ArrayList<Rotation> rotationSet, int randomSpecialPieceChanceOneOutOf, int frequencySpecialPieceTypeOnceEveryNPieces)
	{// =========================================================================================================================

		this.spriteName = spriteName;
		this.color = color;
		this.numBlocks = numBlocks;
		this.rotationSet = rotationSet;
		this.lastRotation = rotationSet.size()-1;
		this.randomSpecialPieceChanceOneOutOf = randomSpecialPieceChanceOneOutOf;
		this.frequencySpecialPieceTypeOnceEveryNPieces = frequencySpecialPieceTypeOnceEveryNPieces;
	}

	// =========================================================================================================================
	public PieceType(int numBlocks, ArrayList<Rotation> rotationSet)
	{// =========================================================================================================================

		this.numBlocks = numBlocks;
		this.rotationSet = rotationSet;
		this.lastRotation = rotationSet.size()-1;
	}
}