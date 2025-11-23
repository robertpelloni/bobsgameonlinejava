package com.bobsgame.client.engine.game.nd.bobsgame.game;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import com.bobsgame.client.GLUtils;
import com.bobsgame.client.engine.Engine;
import com.bobsgame.client.engine.EnginePart;
import com.bobsgame.client.engine.game.MiniGameEngine;
import com.bobsgame.client.engine.game.nd.bobsgame.BobsGame;
import com.bobsgame.client.engine.game.nd.bobsgame.game.Settings.Difficulty;
import com.bobsgame.client.engine.game.nd.bobsgame.game.Settings.DropLockType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.Settings.GarbageSpawnRule;
import com.bobsgame.client.engine.game.nd.bobsgame.game.Settings.ScoreType;
import com.bobsgame.client.engine.game.nd.bobsgame.game.Settings.VSGarbageRule;
import com.bobsgame.client.engine.text.BobFont;
import com.bobsgame.client.engine.text.Caption;
import com.bobsgame.client.engine.text.BobFont.BitmapFont;
import com.bobsgame.shared.BobColor;
import com.bobsgame.shared.Utils;
import com.bobsgame.shared.MapData.RenderOrder;



//===============================================================================================
public class GameLogic extends EnginePart
{//===============================================================================================



	static public int LEFT = 0;
	static public int RIGHT = 1;
	static public int MIDDLE = 2;

	int side = 0;



	ArrayList<Integer> gameTypeRandomBag = new ArrayList<Integer>();


	//===============================================================================================
	public GameLogic(MiniGameEngine g, long seed)
	{//===============================================================================================

		super((Engine)g);


		randomSeed = seed;

		if(randomSeed==-1)
		{
			randomSeed = new Random().nextLong();
			if(randomSeed<0)randomSeed*=-1;
			randomSeed%=5000;
		}


		randomGenerator = new Random(randomSeed);


		setSettings(new Settings());


		blockWidth = (int)(8 * (getWidth()/320));
		blockHeight = blockWidth;




		fillGameTypeRandomBag();

		int gameType = TETRID;

		gameTypeRandomBag.remove(Integer.valueOf(gameType));
		setGameType(gameType);//getGameTypeFromRandomBag();


		//DONE: init randomseed
		//DONE: need to initialize state, settings, RNG over network
		//DONE: single player mode
		//DONE: multiplayer mode
		//DONE: waiting for challenger


		//TODO: store "got garbage" in frameState so the actual release of garbage is exactly synced


	}




	//=========================================================================================================================
	public MiniGameEngine Engine()
	{//=========================================================================================================================
		return ((MiniGameEngine)super.Engine());
	}


	//=========================================================================================================================
	public void fillGameTypeRandomBag()
	{//=========================================================================================================================
		ArrayList<Integer> tempBag = new ArrayList<Integer>();
		for(int i=0;i<gameCount;i++)
		{
			tempBag.add(Integer.valueOf(i));
		}

		while(tempBag.size()>0)
		{
			int i = getRandomIntLessThan(tempBag.size());
			gameTypeRandomBag.add(tempBag.get(i));
			tempBag.remove(i);
		}
	}

	//=========================================================================================================================
	public int getGameTypeFromRandomBag()
	{//=========================================================================================================================
		if(gameTypeRandomBag.size()==0)fillGameTypeRandomBag();

		return gameTypeRandomBag.remove(0).intValue();
	}



	private Settings settings = null;

	public Grid grid = new Grid(this);


	public int blockWidth = 1;
	public int blockHeight = 1;

	int lockInputCountdownTicks = 0;

	boolean canPressA = false;
	boolean canPressB = false;
	boolean canPressRight = false;
	boolean canPressLeft = false;
	boolean canPressDown = false;
	boolean canPressUp = false;
	boolean canPressR = false;

	int ticksHoldingA = 0;
	int ticksHoldingB = 0;
	int ticksHoldingRight = 0;
	int ticksHoldingLeft = 0;
	int ticksHoldingDown = 0;
	int ticksHoldingUp = 0;
	int ticksHoldingR = 0;

	boolean repeatStartedA = false;
	boolean repeatStartedB = false;
	boolean repeatStartedR = false;
	boolean repeatStartedUp = false;
	boolean repeatStartedDown = false;
	boolean repeatStartedLeft = false;
	boolean repeatStartedRight = false;


	public boolean win = false;
	public boolean lose = false;

	public boolean dead = false;
	boolean startedDeathSequence = false;
	boolean startedWinSequence = false;
	boolean startedLoseSequence = false;

	public boolean credits = false;
	boolean creditScreenInitialized = false;
	boolean firstInit = true;
	boolean init = false;

	boolean extraStage1 = false;
	boolean extraStage2 = false;
	boolean extraStage3 = false;
	boolean extraStage4 = false;
	long extraStageTicksPassed = 0;


	boolean gravityThisFrame = false;

	boolean pieceSetAtBottom = false;
	boolean switchedHoldPieceAlready = false;
	boolean playingFastMusic = false;
	boolean firstDeath = false;


	int currentLineDropSpeedTicks = 0;
	int lockDelayTicksCounter = 0;
	int lineDropTicksCounter = 0;
	long spawnDelayTicksCounter = 0;
	long lineClearDelayTicksCounter = 0;
	long moveDownLineTicksCounter = 0;


	String playingMusic = null;

	//used for panel
	long stackRiseTicksCounter = 0;
	long stopStackRiseTicksCounter = 0;
	long manualStackRiseTicksCounter = 0;
	int manualStackRiseSoundToggle = 0;


	int timesToFlashScreenQueue = 0;
	long flashScreenTicksCounter = 0;
	boolean flashScreenOnOffToggle = false;



	long flashBlocksTicksCounter = 0;
	int timesToFlashBlocksQueue = 0;
	long removeBlocksTicksCounter = 0;
	ArrayList<Block> currentChainBlocks = null;
	public ArrayList<Block> disappearingBlocks = new ArrayList<Block>();



	public Piece currentPiece = null;
	Piece holdPiece = null;
	ArrayList<Piece> nextPieces = null;
	ArrayList<Piece> nextPieceSpecialBuffer = new ArrayList<Piece>();



	int lastKnownLevel = 0;
	int currentLevel = 0;


	int piecesMadeThisGame = 0;
	int lastPiecesMadeThisGame = 0;
	int blocksClearedThisGame = 0;
	int linesClearedThisGame = 0;


	int piecesMadeTotal = 0;
	int lastPiecesMadeTotal = 0;
	int blocksClearedTotal = 0;
	int linesClearedTotal = 0;


	long totalTicksPassed = 0;
	public int createdPiecesCounterForFrequencyPieces = 0;


	boolean waitingForStart = true;
	boolean waitingForReady = true;
	boolean playedReadySound = false;
	long readyTicksCounter = 0;

	long garbageValueCounter = 0;

	public boolean forceGravity = false;
	public String previousGameString = "";
	public String currentGameString = "";
	int currentGameType = 0;

	boolean mute = false;


	int currentChain=0;
	int currentCombo=0;
	int comboChainTotal=0;

	public int queuedGarbageAmountToSend = 0;

	int queuedGarbageAmountFromOtherPlayer = 0;
	int garbageWaitPieces=0;

	Block garbageBlock = null;

	boolean checkForChainAgainIfNoBlocksPopping = false;



	//=========================================================================================================================
	public static enum MovementType
	{//=========================================================================================================================
		UP,
		DOWN,
		RIGHT,
		LEFT,
		ROTATE_COUNTERCLOCKWISE,
		ROTATE_CLOCKWISE,
		ROTATE_180,
		HARD_DROP,
	}



	int gameCount = 0;

	int TETRID = gameCount++;
	int TETSOSUMI = gameCount++;//tetris dontsuetris tetsosumi
	int BOBLOB = gameCount++;//bobubobu? bubobubo? bubobob? bobu's bean avalanche (was BLOB)
	int JEWELS = gameCount++;//gems jewels genegems pillars columbines (was JEWEL)
	int GEMFIGHT_COLUMNS_TYPE_Y = gameCount++;
	int POP = gameCount++;//dancinglady, "puzzle sphere war" is apparently translation
	/** <img src="C:/Users/Administrator/workspace/_screenshots/puzzlefighter.png" />*/
	int GEMFIGHT = gameCount++;
	int MRBOB = gameCount++;//dr patent? dr patentio? quack? rockdoc? Mario's Addiction? MDMA? Drugz4Kidz? EnslavedJapaneseEngineersOnSpeed



//	int JEWELRYMASTER_FLASHPOINT = gameType++;//TODO
//	int TETSOSUMIFLASH = gameType++;//TODO
//	int BOMBLISS = gameType++;//TODO
//	int MAGICALISS = gameType++;//TODO
//	int MAGICAL_TETRIS = gameType++;//TODO
//	int METEOS = gameType++;//TODO
//	int LUMINES = gameType++;//TODO
//	int KIRBYSTARSTACK = gameType++;
/** <img src="C:/Users/Administrator/workspace/_screenshots/pacattack.png" />*/
//	int PACATTACK = gameType++;




	//swap games --------------------
	/** <img src="C:/Users/Administrator/workspace/_screenshots/damaswap.png" />*/
	int POPSWAP_TOKKAE = gameCount++;
	int PANELSWAP = gameCount++;//panel de planet puzzle attack league //TODO: check gamecube version
	int GEMFIGHT_SWAP_TYPE_Z = gameCount++;


//	int YOSHISCOOKIE = gameType++;//TODO
//	int GUNPEY = gameType++;//TODO





//	//sorting into piles games ----------------
//	int WARIOSWOODS = gameType++;//TODO
//	int KLAX = gameType++;//TODO
//	int YOSHI_HATRIS = gameType++;//TODO



//	int ZOOP = gameType++;//TODO

//	/** <img src="C:/Users/Administrator/workspace/_screenshots/flipull.png" />*/
//	int FLIPULL = gameType++;//TODO
//	int MAGICALDROP = gameType++;//money idol exchanger//TODO


//	//bubble games --------------------
//	int PUZZLEBUBBLE_BUSTAMOVE = gameType++;//TODO
//	/** <img src="C:/Users/Administrator/workspace/_screenshots/pang.png" /> */
//	int PANG_BUSTERBROS = gameType++;//TODO



//	//click color group games ----------------
//	int DRILLER = gameType++;//TODO
//	int SAMEGAME = gameType++;//TODO
//	int BEJEWELED_PUZZLEQUEST = gameType++;//TODO

//	/** <img src="C:/Users/Administrator/workspace/_screenshots/puzznic.png" />*/
//	int PUZZNIC = gameType++;//TODO
//	int POLARIUM = gameType++;//TODO


//	// more single-player type puzzles games, should do this elsewhere? --------------------------
//	int PIPEDREAM = gameType++;
//	/** <img src="C:/Users/Administrator/workspace/_screenshots/denkiblocks.png" /> */
//	int DENKIBLOX = gameType++;
//	int SOKOBAN = gameType++;



//	int DDR = gameType++;
//	int BEATMANIA = gameType++;
//	int TAIKO = gameType++;
//	int TECHNIKA = gameType++;


	//=========================================================================================================================
	public void setGameType(int gameType)
	{//=========================================================================================================================

		previousGameString = currentGameString;
		currentGameType = gameType;

		if(gameType==TETSOSUMI){Settings().tetsosumi(this);currentGameString=("Tetsosumi");}
		else if(gameType==TETRID){Settings().tetrid(this);currentGameString=("Tetrid");}
		else if(gameType==MRBOB){Settings().mrbob(this);currentGameString=("MrBob");}
		else if(gameType==BOBLOB){Settings().boblob(this);currentGameString=("Boblob");}
		else if(gameType==JEWELS){Settings().jewels(this);currentGameString=("Jewels");}
		else if(gameType==POP){Settings().pop(this);currentGameString=("Pop");}
		else if(gameType==GEMFIGHT){Settings().gemfight(this);currentGameString=("GemFight");}
		else if(gameType==GEMFIGHT_COLUMNS_TYPE_Y){Settings().gemfight_columns(this);currentGameString=("GemFightColumns");}
		else if(gameType==POPSWAP_TOKKAE){Settings().popswap(this);currentGameString=("PopSwap");}
		else if(gameType==GEMFIGHT_SWAP_TYPE_Z){Settings().gemfight_swap(this);currentGameString=("GemFightSwap");}
		else if(gameType==PANELSWAP){Settings().panelswap(this);currentGameString=("PanelSwap");}
		else {Settings().tetrid(this);currentGameString=("Tetrid");}



//		if(gameType==YOSHISCOOKIE);
//		if(gameType==METEOS);
//		if(gameType==LUMINES);
//		if(gameType==GUNPEY);
//		if(gameType==KIRBYSTARSTACK);
//		if(gameType==DRILLER);

//		if(gameType==KLAX);
//		if(gameType==WARIOSWOODS);
//		if(gameType==YOSHI_HATRIS);
//		if(gameType==ZOOP);

//		if(gameType==MAGICALDROP);
//		if(gameType==PUZZLEBUBBLE_BUSTAMOVE);
//		if(gameType==PANG_BUSTERBROS);

//		if(gameType==POLARIUM);
//		if(gameType==PIPEDREAM);

//		if(gameType==FLIPULL);
//		if(gameType==PUZZNIC);
//		if(gameType==PACATTACK);

//		if(gameType==DENKIBLOX);
//		if(gameType==SOKOBAN);



	}


	//=========================================================================================================================
	void initGame()
	{//=========================================================================================================================

		//set all variables to initial state, in case when switching games
		//settings = new Settings();


		init=true;

		//TODO: default background colors/animations/border colors for games

		//TODO: graphical meter for all timings and numbers, maybe piece fills up when locking? small meter next to piece for drop delay?

		//TODO: make all options variable and mix game() modes

		//TODO: shooter blocks don't have spawn delay, should move down at a set rate

		//TODO: maximum spawn/line clear delay


		lockDelayTicksCounter = Settings().maxLockDelayTicks;
		currentLineDropSpeedTicks = Settings().initialLineDropSpeedTicks;
		stopStackRiseTicksCounter = 1000;

		piecesMadeThisGame = 0;
		lastPiecesMadeThisGame = 0;
		blocksClearedThisGame = 0;
		linesClearedThisGame = 0;



		currentPiece = null;
		holdPiece = null;
		nextPieces = null;
		grid.randomBag = null;
		nextPieceSpecialBuffer = null;


		if(Settings().chainRule_CheckEntireLine&&Settings().chainRule_AmountPerChain==0)Settings().chainRule_AmountPerChain = gridW();


		if(Settings().randomlyFillGrid)
		{
			grid.randomlyFillEntireGridWithPlayingFieldPieces(Settings().randomlyFillGridAmount,Settings().randomlyFillGridStartY);
		}

		if(Settings().randomlyFillStack)
		{
			grid.buildRandomStackWithPlayingFieldPieces(Settings().randomlyFillStackAmount,Settings().randomlyFillStackStartY);
		}

		if(Settings().makeNewPiece)
		{
			newRandomPiece();
		}


		if(Settings().makeCursorPiece)
		{

			if(Settings().cursorPieceSize==1)
			{
				PieceType cursorPiece = new PieceType(1,Piece.get1PieceCursorRotationSet());
				BlockType cursorBlock = new BlockType();
				currentPiece = new Piece(this, grid, cursorPiece, cursorBlock);
				currentPiece.xGrid = (grid.w()/2);
				currentPiece.yGrid = 7;
			}


			if(Settings().cursorPieceSize==2)
			{
				PieceType cursorPiece = new PieceType(2,Piece.get2PieceCursorRotationSet());
				BlockType cursorBlock = new BlockType();
				currentPiece = new Piece(this, grid, cursorPiece, cursorBlock);
				currentPiece.xGrid = (grid.w()/2)-1;
				currentPiece.yGrid = 7;
			}


			if(Settings().cursorPieceSize==4)
			{
				PieceType cursorPiece = new PieceType(4,Piece.get4PieceCursorRotationSet());
				BlockType cursorBlock = new BlockType();
				currentPiece = new Piece(this, grid, cursorPiece, cursorBlock);
				currentPiece.xGrid = (grid.w()/2)-1;
				currentPiece.yGrid = 7;
			}

		}



		if(Settings().difficulty==Difficulty.EASY)
		{
			Settings().extraStage1Level=5;
			Settings().extraStage2Level=6;
			Settings().extraStage3Level=7;
			Settings().extraStage4Level=8;
			Settings().creditsLevel=9;
			difficultyCaptionText = "Difficulty: Easy";
		}
		else
		if(Settings().difficulty==Difficulty.NORMAL)
		{
			Settings().extraStage1Level=9;
			Settings().extraStage2Level=14;
			Settings().extraStage3Level=19;
			Settings().extraStage4Level=25;
			Settings().creditsLevel=99;
			difficultyCaptionText = "Difficulty: Normal";
		}
		else
		{
			Settings().extraStage1Level=20;
			Settings().extraStage2Level=30;
			Settings().extraStage3Level=40;
			Settings().extraStage4Level=50;
			Settings().creditsLevel=60;
			difficultyCaptionText = "Difficulty: Hard";
		}


		AudioManager().stopMusic(playingMusic);
		playingMusic = Settings().normalMusic;
		AudioManager().playMusic(playingMusic);


//		Writer output = null;
//		try
//		{
//			output = new BufferedWriter(new FileWriter(new File(System.getProperty("user.home")+"\\Desktop\\output.txt")));
//
//			String settingsString = Settings().toGSON();
//
//			output.write(settingsString);
//			output.close();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}

	}




	//=========================================================================================================================
	public void setGridXY(int side)
	{//=========================================================================================================================


		this.side = side;

		float screenX = 0;
		float screenY = 0;

		if(side==MIDDLE)
		{

			//let's just set block width and height

			float blockH = getHeight() / (gridH() + 7);

			blockWidth = (int)blockH;
			blockHeight = (int)blockH;


			screenX = (getWidth()/2  - ((gridW()*cellW())/2));// - cellW()*3;
			screenY = (5*cellH()); //getHeight()/2 - (gridH()*cellH())/2;

		}
		else
		//if(Engine().games.size()==2)
		{

			//split screen
			if(side==LEFT)
			{


					screenX = cellW()*5;
					screenY = getHeight()/2 - (gridH()*cellH())/2;

			}
			else
			if(side==RIGHT)
			{


					screenX = getWidth()/2 + cellW()*5;
					screenY = getHeight()/2 - (gridH()*cellH())/2;

			}

		}
//		else
//		if(Engine().games.size()>2)
//		{
//
//			//split screen
//			if(side==LEFT)
//			{
//				//if(side==BobsGame.LEFT)
//				{
//					screenX = cellW()*5;
//					screenY = getHeight()/2 - (gridH()*cellH())/2;
//				}
//			}
//			else
//			{
//
//				//TODO: scale to quarters on right side
//				//if(side==BobsGame.RIGHT)
//				{
//					screenX = getWidth()/2 + cellW()*5;
//					screenY = getHeight()/2 - (gridH()*cellH())/2;
//				}
//			}
//
//
//		}


		grid.screenX = screenX;
		grid.screenY = screenY;


		captionX = (int)(grid.screenX + (grid.w()+1)*cellW());


	}



	//=========================================================================================================================
	private void flashScreen()
	{//=========================================================================================================================

		flashScreenTicksCounter+=ticks();
		if(flashScreenTicksCounter>Settings().flashScreenSpeedTicks)
		{
			flashScreenTicksCounter=0;

			flashScreenOnOffToggle = !flashScreenOnOffToggle;

			if(flashScreenOnOffToggle==true)
			{
				timesToFlashScreenQueue--;
			}
		}
	}



	//=========================================================================================================================
	private void flashChainBlocks()
	{//=========================================================================================================================

		flashBlocksTicksCounter+=ticks();

		if(flashBlocksTicksCounter>Settings().flashBlockSpeedTicks)
		{
			flashBlocksTicksCounter=0;

			if(detectedChain())
			{
				for(int i=0;i<currentChainBlocks.size();i++)
				{
					currentChainBlocks.get(i).flashingToBeRemovedLightDarkToggle=!currentChainBlocks.get(i).flashingToBeRemovedLightDarkToggle;
				}
			}

			timesToFlashBlocksQueue--;
		}

	}


	//=========================================================================================================================
	private void removeFlashedChainBlocks()
	{//=========================================================================================================================


		int linesCleared = 0;
		int blocksCleared = 0;

		if(currentChainBlocks!=null)
		{

			for(int i=0;i<currentChainBlocks.size();i++)
			{
				Block b = currentChainBlocks.get(i);

				if(b.overrideAnySpecialBehavior==false)
				{
					if(b.blockType.makePieceTypeWhenCleared!=null)
					{

						if(nextPieceSpecialBuffer==null)nextPieceSpecialBuffer = new ArrayList<Piece>();

						Piece p = new Piece(this,grid,b.blockType.makePieceTypeWhenCleared,new BlockType());
						nextPieceSpecialBuffer.add(p);

						//DONE: sound "got bomb" "got weight" "got shooter"
						if(p.pieceType.bombPiece)
						{
							makeAnnouncementCaption("BOMB",BobColor.BLUE);
							AudioManager().playSound(Settings().gotBombSound,getVolume(),1.0f,1);
						}

						if(p.pieceType.weightPiece)
						{
							makeAnnouncementCaption("WEIGHT",BobColor.ORANGE);
							AudioManager().playSound(Settings().gotWeightSound,getVolume(),1.0f,1);
						}

						if(p.pieceType.clearEveryRowPieceIsOnIfAnySingleRowCleared)
						{
							makeAnnouncementCaption("FLASHING CLEAR",BobColor.GREEN);
							AudioManager().playSound(Settings().flashingClearSound,getVolume(),1.0f,1);
						}

						if(p.pieceType.pieceRemovalShooterPiece)
						{
							makeAnnouncementCaption("SUBTRACTOR",BobColor.RED);
							AudioManager().playSound(Settings().gotSubtractorSound,getVolume(),1.0f,1);
						}

						if(p.pieceType.pieceShooterPiece)
						{
							makeAnnouncementCaption("ADDER",BobColor.YELLOW);
							AudioManager().playSound(Settings().gotAdderSound,getVolume(),1.0f,1);
						}


					}

					if(b.blockType.clearEveryOtherLineOnGridWhenCleared)
					{

						makeAnnouncementCaption("SCANLINE CLEAR",BobColor.RED);
						AudioManager().playSound(Settings().scanlineClearSound,getVolume(),1.0f,1);

						//add every other line to clear blocks
						for(int y=gridH()-2;y>=0;y-=2)
						{
							for(int x=0;x<gridW();x++)
							{
								Block c = grid.get(x,y);
								if(c!=null)
								{
									if(currentChainBlocks.contains(c)==false)currentChainBlocks.add(c);
								}
							}
						}

						grid.shakeSmall();
					}
				}
			}





			removeBlocksTicksCounter+=ticks();

			while
			(
				currentChainBlocks.size()>0
				&&
				(
					//flash blocks slower, remove one at a time
					Settings().removingBlocksDelayTicksBetweenEachBlock==0
					||
					removeBlocksTicksCounter>Settings().removingBlocksDelayTicksBetweenEachBlock
				)
			)
			{

				removeBlocksTicksCounter=0;

				Block a = currentChainBlocks.get(0);
				//we need to pop any boxes touching this one
				ArrayList<Block> temp = grid.getConnectedBlocksUpDownLeftRight(a);
				if(temp!=null)
				{
					for(int i=0;i<temp.size();i++)
					{
						Block b = temp.get(i);

						if(b.blockType.ifConnectedUpDownLeftRightToExplodingBlockChangeIntoThisType!=null)
						{
							b.popping = true;
							b.animationFrame=0;

							checkForChainAgainIfNoBlocksPopping = true;
						}
					}
				}



				if(Settings().chainRule_CheckEntireLine)
				{
					//clear line by line and add score per line
					for(int i=0;i<currentChainBlocks.size();i++)
					{
						Block b = currentChainBlocks.get(i);

						if(b!=a&&b.yGrid==a.yGrid)
						{
							currentChainBlocks.remove(b);
							grid.deleteBlock(b);

							blocksCleared++;
							blocksClearedThisGame++;
							blocksClearedTotal++;

							i=-1;
						}
					}

					linesCleared++;
					linesClearedThisGame++;
					linesClearedTotal++;

				}


				currentChainBlocks.remove(a);
				grid.deleteBlock(a);

				blocksCleared++;
				blocksClearedThisGame++;
				blocksClearedTotal++;

			}
		}

		timesToFlashScreenQueue+=linesCleared;

		if(linesCleared==1)AudioManager().playSound(Settings().singleLineFlashingSound,getVolume(),1.0f,1);
		if(linesCleared==2)AudioManager().playSound(Settings().doubleLineFlashingSound,getVolume(),1.0f,1);
		if(linesCleared==3)AudioManager().playSound(Settings().tripleLineFlashingSound,getVolume(),1.0f,1);
		if(linesCleared>=4){AudioManager().playSound(Settings().quadLineFlashingSound,getVolume(),1.0f,1);makeAnnouncementCaption("SOSUMI!",BobColor.GREEN);}


		if(Settings().chainRule_CheckEntireLine)lineClearDelayTicksCounter+=linesCleared*Settings().lineClearDelayTicksAmountPerLine;
		else lineClearDelayTicksCounter+=blocksCleared*Settings().lineClearDelayTicksAmountPerBlock;


		currentChain = currentChainBlocks.size();

	}

	//=========================================================================================================================
	private void updateSpecialPiecesAndBlocks()
	{//=========================================================================================================================

		if(currentPiece!=null)currentPiece.update();
		if(holdPiece!=null)holdPiece.update();

		if(nextPieces!=null)
		{
			for(int i=0;i<nextPieces.size();i++)
			{
				nextPieces.get(i).update();
			}
		}


		if(nextPieceSpecialBuffer!=null)
		{
			for(int i=0;i<nextPieceSpecialBuffer.size();i++)
			{
				nextPieceSpecialBuffer.get(i).update();
			}
		}

		if(disappearingBlocks!=null)
		{
			for(int i=0;i<disappearingBlocks.size();i++)
			{
				disappearingBlocks.get(i).update();
			}
		}

	}



	//=========================================================================================================================
	private void waitForPressStart()
	{//=========================================================================================================================

		if(pressStartCaption==null)
		{

			pressStartCaption = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2)-60,-1,"PRESS START",BobFont.font_normal_16_outlined_smooth,BobColor.YELLOW,BobColor.CLEAR,RenderOrder.ABOVE_TOP,2.0f,0);

		}
		else
		{

			pressStartCaption.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - pressStartCaption.getWidth()/2;
			pressStartCaption.flashing = true;
			pressStartCaption.flashingTicksPerFlash = 500;

		}

		if(Engine.ControlsManager().BUTTON_SPACE_PRESSED)
		{

			if(pressStartCaption!=null)
			{
				pressStartCaption.deleteFadeOut();
				pressStartCaption = null;
			}

			waitingForStart=false;

		}

	}

	//=========================================================================================================================
	private void waitForReady()
	{//=========================================================================================================================
		if(playedReadySound==false)
		{

			AudioManager().playSound(Settings().readySound,getVolume(),1.0f,1);
			playedReadySound = true;

			Caption c = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2)-30,Settings().readyTicksAmount,"READY",BobFont.font_normal_16_outlined_smooth,BobColor.RED,BobColor.CLEAR,RenderOrder.ABOVE_TOP,3.0f,0);
			c.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - c.getWidth()/2;

		}


		readyTicksCounter+=Engine().engineTicksPassed();
		if(readyTicksCounter>Settings().readyTicksAmount)
		{

			readyTicksCounter = 0;
			playedReadySound = true;
			waitingForReady = false;

			AudioManager().playSound(Settings().goSound,getVolume(),1.0f,1);
			Caption c = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2),1000,"GO!",BobFont.font_normal_16_outlined_smooth,BobColor.GREEN,BobColor.CLEAR,RenderOrder.ABOVE_TOP,3.0f,0);
			c.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - c.getWidth()/2;

			c.flashing = true;
			c.flashingTicksPerFlash = 80;

		}

	}




	//=========================================================================================================================
	public void updateNormalGame(int side)
	{//=========================================================================================================================

		setGridXY(side);
		updateCaptionFadeValues();

		if(waitingForStart){waitForPressStart();return;}
		if(waitingForReady){waitForReady();return;}


		sendNetworkFrames=true;



		this.frameState = new FrameState();
		this.frameState.ticksPassed = Engine().engineTicksPassed();

		this.frameState.controlsState = new ControlsState();
		this.frameState.controlsState.BUTTON_SPACE_HELD = ControlsManager().BUTTON_SPACE_HELD;
		this.frameState.controlsState.BUTTON_LCTRL_HELD = ControlsManager().BUTTON_LCTRL_HELD;
		this.frameState.controlsState.BUTTON_LSHIFT_HELD = ControlsManager().BUTTON_LSHIFT_HELD;
		this.frameState.controlsState.BUTTON_UP_HELD = ControlsManager().BUTTON_UP_HELD;
		this.frameState.controlsState.BUTTON_LEFT_HELD = ControlsManager().BUTTON_LEFT_HELD;
		this.frameState.controlsState.BUTTON_DOWN_HELD = ControlsManager().BUTTON_DOWN_HELD;
		this.frameState.controlsState.BUTTON_RIGHT_HELD = ControlsManager().BUTTON_RIGHT_HELD;

		networkPacket.frameStates.add(this.frameState);

		if(init==false)
		{
			initGame();
		}

		processFrame();


	}


	//=========================================================================================================================
	public void updateNetworkGame()
	{//=========================================================================================================================






		if(waitingForPlayer==true){return;}


		setGridXY(RIGHT);
		updateCaptionFadeValues();




//		{
//			if(waitingForPlayerCaption==null)
//			{
//				waitingForPlayerCaption = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2)-60,-1,"Waiting For Challenger",BobFont.font_normal_8_outlined,BobColor.darkGray,BobColor.CLEAR,RenderOrder.ABOVE,1.0f,50,false, true);
//			}
//			else
//			{
//				waitingForPlayerCaption.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - waitingForPlayerCaption.getWidth()/2;
//				waitingForPlayerCaption.flashing = true;
//				waitingForPlayerCaption.flashingTicksPerFlash = 10000;
//			}
//
//			return;
//		}
//		else
//		{
//			if(waitingForPlayerCaption!=null)
//			{
//				waitingForPlayerCaption.deleteFadeOut();
//				waitingForPlayerCaption = null;
//			}
//		}






		if(waitingForNetworkFrames==true){return;}


		while(networkPacket.frameStates.size()>0)
		{



			FrameState f = networkPacket.frameStates.remove(0);
			this.frameState = f;

			if(init==false)
			{
				initGame();
			}

			if(frameState.receivedGarbageAmount>0)//this should only happen when we are the network side
			{
				gotVSGarbageFromOtherPlayer(frameState.receivedGarbageAmount);
			}

			//log.debug("Processed frame");
			processFrame();
		}
		//log.debug("Waiting for frames");

		waitingForNetworkFrames=true;



	}



	//=========================================================================================================================
	private void processFrame()
	{//=========================================================================================================================


		updateCaptions();


		if(win==true){win();return;}
		if(lose==true){lose();return;}

		if(credits==true){credits();return;}
		if(dead==true){dead();return;}





		totalTicksPassed+=ticks();

		updateSpecialPiecesAndBlocks();




		if(Settings().garbageSpawnRule!=GarbageSpawnRule.NONE)processGarbageRules();

		processQueuedGarbageSentFromOtherPlayer();

		grid.update();

		grid.scrollBackground();

		doExtraStageEffects();


		lockInputCountdownTicks-=ticks();
		if(lockInputCountdownTicks<0)lockInputCountdownTicks=0;

		lockDelayTicksCounter-=ticks();
		if(lockDelayTicksCounter<0)lockDelayTicksCounter=0;

		lineDropTicksCounter-=ticks();
		if(lineDropTicksCounter<0)lineDropTicksCounter=0;

		lineClearDelayTicksCounter-=ticks();
		if(lineClearDelayTicksCounter<0)lineClearDelayTicksCounter=0;

		spawnDelayTicksCounter-=ticks();
		if(spawnDelayTicksCounter<0)spawnDelayTicksCounter=0;







		if(Settings().stackRiseGame)
		{
			doStackRiseGame();
		}
		else
		{
			doFallingBlockGame();
		}





		moveDownLineTicksCounter+=ticks();

		if((pieceSetAtBottom == true && detectedChain()==false) || forceGravity)
		{

			if(checkForChainAgainIfNoBlocksPopping)
			{
				if(grid.areAnyBlocksPopping())return;
				else
				{
					checkForChainAgainIfNoBlocksPopping = false;
				}
			}

			boolean movedDownBlocks = moveDownBlocksOverBlankSpaces();

			if(movedDownBlocks == true)gravityThisFrame = true;

			if(movedDownBlocks == false)
			{
				forceGravity = false;

				gravityThisFrame = false;


				checkForChain();


				handleNewChain();


				checkForFastMusic();


				if(detectedChain()==false && checkForChainAgainIfNoBlocksPopping==false)
				{

					currentCombo=0;
					currentChain=0;
					comboChainTotal=0;

					if(Settings().stackRiseGame)
					{
						//don't make new piece since they are not "falling block" games
					}
					else
					{
						if(pieceSetAtBottom == true)newRandomPiece();
					}

					updateScore();//must set new game type here while there are no flashing blocks
				}
			}
		}
	}

	//=========================================================================================================================
	private void addToChainBlocks(ArrayList<Block> array)
	{//=========================================================================================================================

		if(array!=null)
		{

			if(currentChainBlocks==null) currentChainBlocks = new ArrayList<Block>();

			for(int i=0;i<array.size();i++)if(currentChainBlocks.contains(array.get(i))==false)currentChainBlocks.add(array.get(i));
		}

	}

	//=========================================================================================================================
	private boolean detectedChain()
	{//=========================================================================================================================
		if( currentChainBlocks!=null && currentChainBlocks.size()>0 )return true;

		return false;
	}

	//=========================================================================================================================
	private void checkForChain()
	{//=========================================================================================================================


		currentChainBlocks = null;

		ArrayList<BlockType> ignoreTypes = Settings().blockTypesToIgnoreWhenCheckingChainConnections;
		ArrayList<BlockType> mustContainAtLeastOneTypes = Settings().blockTypesMustContainWhenCheckingChainConnections;


		//can use this while blocks are falling to detect sticky colors
		grid.setColorConnections(ignoreTypes,null);


		if(Settings().chainRule_CheckEntireLine)
		{
			ArrayList<Block> chainBlocks = null;
			chainBlocks = grid.checkLines(ignoreTypes,mustContainAtLeastOneTypes);
			addToChainBlocks(chainBlocks);
		}


		int toRow = grid.h();
		if(Settings().stackRiseGame)toRow = grid.h()-1;

		if(Settings().chainRule_AmountPerChain>0)
		{

			ArrayList<Block> chainBlocks = null;

			for(int y=0;y<toRow;y++)
			{
				for(int x=0;x<grid.w();x++)
				{
					Block b = grid.get(x,y);

					if(b!=null&&(ignoreTypes==null||ignoreTypes.contains(b.blockType)==false))
					{
						if(Settings().chainRule_CheckRowOrColumn)chainBlocks = grid.addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfInRowOrColumnAtLeastAmount(b,chainBlocks,Settings().chainRule_AmountPerChain,0,grid.w(),0,toRow,ignoreTypes,mustContainAtLeastOneTypes);
						if(Settings().chainRule_CheckDiagonal)chainBlocks = grid.addBlocksConnectedToBlockToArrayIfNotInItAlreadyIfDiagonalAtLeastAmount(b,chainBlocks,Settings().chainRule_AmountPerChain,0,grid.w(),0,toRow,ignoreTypes,mustContainAtLeastOneTypes);
					}
				}
			}

			if(Settings().chainRule_CheckRecursiveConnections)chainBlocks = grid.checkRecursiveConnectedRowOrColumn(chainBlocks,Settings().chainRule_AmountPerChain,0,grid.w(),0,toRow,ignoreTypes,mustContainAtLeastOneTypes);

			addToChainBlocks(chainBlocks);
		}

		if(Settings().chainRule_CheckTouchingBreakerBlocksChain)
		{
			ArrayList<Block> chainBlocks = grid.checkBreakerBlocks(toRow,Settings().blockTypesToIgnoreWhenCheckingChainConnections,Settings().blockTypesMustContainWhenCheckingChainConnections);
			addToChainBlocks(chainBlocks);
		}

	}







	//need to add queuedGarbageAmountToSend to queuedGarbageAmountFromOtherPlayer when received in frame
	//need to add "ReceivedGarbageAmount" to frame state

	//=========================================================================================================================
	private void handleNewChain()
	{//=========================================================================================================================
		//if flashing blocks, show caption
		if(detectedChain())
		{

			int chainMinimum = Settings().chainRule_AmountPerChain;

			if(currentCombo==0)
			{

				currentCombo = 1;
				currentChain = currentChainBlocks.size();

				makeAnnouncementCaption("Chain: "+currentChain);


				int bonusAmount = (currentChain - chainMinimum);
				if(Settings().chainRule_CheckEntireLine)
				{
					bonusAmount = currentChain / chainMinimum;
					if(bonusAmount==1)bonusAmount=0;
				}

				if(bonusAmount>0)
				{
					makeAnnouncementCaption("Chain Bonus: "+bonusAmount, BobColor.GREEN);
					queueVSGarbage(bonusAmount);
				}

				if(side==MIDDLE || side==LEFT)BobsGame.changeBG();
				Engine().shakeSmall();

			}
			else
			{
				currentCombo++;
				currentChain = currentChainBlocks.size();
				comboChainTotal += currentChain;

				makeAnnouncementCaption("Chain: "+currentChain);

				makeAnnouncementCaption(""+currentCombo+"X Combo! Total: "+comboChainTotal, BobColor.MAGENTA);

				int bonusAmount = (currentChain - chainMinimum);
				if(bonusAmount==0)bonusAmount = 1;

				makeAnnouncementCaption("Combo Bonus: "+bonusAmount+" X "+currentCombo, BobColor.GREEN);

				queueVSGarbage(currentCombo);

				Engine().shakeHard();
			}



			//add any gray blobs touching the chain to the chain
			ArrayList<Block> addToChain = new ArrayList<Block>();
			for(int i=0;i<currentChainBlocks.size();i++)
			{
				Block a = currentChainBlocks.get(i);

				ArrayList<Block> temp = grid.getConnectedBlocksUpDownLeftRight(a);
				if(temp!=null)
				{
					for(int k=0;k<temp.size();k++)
					{
						Block b = temp.get(k);

						if(b.blockType.addToChainIfConnectedUpDownLeftRightToExplodingChainBlocks)
						{
							if(addToChain.contains(b)==false)addToChain.add(b);
						}
					}
				}

			}


			for(int i=0;i<addToChain.size();i++)
			{
				Block a = addToChain.get(i);
				if(currentChainBlocks.contains(a)==false)currentChainBlocks.add(a);
			}





			for(int i=0;i<currentChainBlocks.size();i++)
			{
				Block a = currentChainBlocks.get(i);
				a.flashingToBeRemoved=true;
			}

			if(Settings().stackRiseGame)//TODO: check stop behavior for dama and puzzlefighter
			{
				//stop
				if(currentChainBlocks.size()>3)stopStackRiseTicksCounter+=1000*currentChainBlocks.size();
			}

			AudioManager().playSound(Settings().blocksFlashingSound,getVolume(),1.0f,1);
			timesToFlashBlocksQueue = Settings().timesToFlashBlocks;
		}
	}

	//=========================================================================================================================
	private void doStackRiseGame()
	{//=========================================================================================================================

		//stack rise game lets you continue manipulating pieces while there is a chain completing, falling block games don't.
		//so notice here it does not return from flashing detected chain blocks, and that it also detects one chain at a time.


		pieceSetAtBottom = true;//we dont use this and it should be true for the check below.

		manualStackRiseTicksCounter+=ticks();

		//if not STOP, scroll playingfield up.
		boolean stop = false;
		if(stopStackRiseTicksCounter>0)
		{

			stopStackRiseTicksCounter-=ticks();
			if(stopStackRiseTicksCounter<0)stopStackRiseTicksCounter=0;
			stopCounterCaptionText = "Wait: "+stopStackRiseTicksCounter;
			stop=true;

		}

		if(timesToFlashBlocksQueue>0)
		{

			flashChainBlocks();
			stopCounterCaptionText = "Wait: Flash";
			stop=true;

		}
		else
		if(detectedChain())
		{

			removeFlashedChainBlocks();

			stopCounterCaptionText = "Wait: Pop " + currentChainBlocks.size();
			stop=true;

		}


		if(timesToFlashScreenQueue>0)
		{

			flashScreen();
			stopCounterCaptionText = "Wait: Flash";
			stop=true;

		}

		if(grid.continueSwappingBlocks())
		{

			stopCounterCaptionText = "Wait: Swap";
			stop=true;

		}

		if(stop==false)
		{

			stopCounterCaptionText = "Go!";
			stackRiseTicksCounter+=ticks();


			//stackrise 300 was default, was too slow
			//200-300 is a good speed

			//dropspeed is 128-1000

			//dropspeed/10 = 12-100

			//stackrise = minStackRise

			int minStackRise = 30;
			int maxStackRise = 300;

			int stackRiseDiff = maxStackRise - minStackRise;



			int dropSpeedDiff = Settings().initialLineDropSpeedTicks - Settings().minimumLineDropSpeedTicks;

			int currentDropSpeedDiff = (currentLineDropSpeedTicks - Settings().minimumLineDropSpeedTicks);

			int currentStackRise = minStackRise + (int)(((float)currentDropSpeedDiff / (float)dropSpeedDiff) * (float)stackRiseDiff);

			if(stackRiseTicksCounter>currentStackRise/(blockHeight/8))
			{

				stackRiseTicksCounter=0;


				boolean scrolled = grid.scrollUpStack(currentPiece, 1);
				if(scrolled==false)dead=true;

				if(dead)
				{
					stopCounterCaptionText = "Wait: Dead";
					return;
				}
			}
		}

		updateKeyInput();
	}



	// =========================================================================================================================
	private void doFallingBlockGame()
	{// =========================================================================================================================

		if(timesToFlashBlocksQueue>0)
		{
			flashChainBlocks();
			return;
		}


		if(detectedChain())
		{
			removeFlashedChainBlocks();
			return;
		}

		if(timesToFlashScreenQueue>0)
		{
			flashScreen();
		}


		//apply gravity to current piece
		if(pieceSetAtBottom==false)
		{
			if(lineDropTicksCounter==0 && spawnDelayTicksCounter==0 && lineClearDelayTicksCounter==0)
			{
				boolean moved = movePiece(MovementType.DOWN);
				if(moved)lineDropTicksCounter=currentLineDropSpeedTicks;

				if(dead)return;

				//if(currentPiece!=null&&currentPiece.y<0&&moved==false){dead=true;return;}
				//if currentPosition<0 and movePiece==false dead
			}

			updateKeyInput();
		}

	}




	//=========================================================================================================================
	public void manuallyApplyGravityWithoutChainChecking()
	{//=========================================================================================================================
		while(moveDownBlocksOverBlankSpaces()){moveDownLineTicksCounter=Settings().gravityRule_ticksToMoveDownBlocksOverBlankSpaces;}
	}



	// =========================================================================================================================
	private boolean moveDownBlocksOverBlankSpaces()
	{// =========================================================================================================================

		boolean movedDownBlocks = true;

		if(moveDownLineTicksCounter>=Settings().gravityRule_ticksToMoveDownBlocksOverBlankSpaces || Settings().moveDownAllLinesOverBlankSpacesAtOnce)
		{

			moveDownLineTicksCounter=0;

			do
			{

				if(Settings().chainRule_CheckEntireLine)
				{
					movedDownBlocks = grid.moveDownLinesAboveBlankLinesOneLine();
				}
				else
				{

					if(Settings().gravityRule_onlyMoveDownDisconnectedBlocks)
					movedDownBlocks = grid.moveDownDisconnectedBlocksAboveBlankSpacesOneLine(Settings().blockTypesToIgnoreWhenMovingDownBlocks);
					else
					movedDownBlocks = grid.moveDownAnyBlocksAboveBlankSpacesOneLine(Settings().blockTypesToIgnoreWhenMovingDownBlocks);
				}

			}
			while(Settings().moveDownAllLinesOverBlankSpacesAtOnce==true && movedDownBlocks==true);

		}

		return movedDownBlocks;

	}




	// =========================================================================================================================
	private void pieceMoved()
	{// =========================================================================================================================
		lockDelayTicksCounter=Settings().maxLockDelayTicks;

		//test one more down
		currentPiece.yGrid++;
		if(grid.doesPieceFit(currentPiece)==false)AudioManager().playSound(Settings().touchBottomSound,getVolume(),getSoundEffectSpeed(),1);
		currentPiece.yGrid--;

	}




	// =========================================================================================================================
	public boolean movePiece(MovementType move)
	{// =========================================================================================================================

		if(currentPiece==null)
		{
			//new Exception().printStackTrace();
			return false;
		}

		if(move==MovementType.ROTATE_COUNTERCLOCKWISE || move==MovementType.ROTATE_CLOCKWISE)
		{

			if(currentPiece.pieceType.pieceShooterPiece)
			{
				//so we can't repeatedly use it
				switchedHoldPieceAlready = true;

				//make new piece, not new block
				//this is so the colors get initialized and the block can be updated by getting arrayOfPiecesInGrid
				Piece p = new Piece(this, grid,new PieceType(),new BlockType(BobsGame.squareName,null,BobColor.gray));
				Block b = p.blocks.get(0);

				//set last screenXY to current piece so it appears to shoot, even though it's really just being placed directly into the grid.
				b.lastScreenX = grid.x()+currentPiece.xGrid*cellW();
				b.lastScreenY = grid.y()+currentPiece.yGrid*cellH();
				b.ticksSinceLastMovement=0;

				int xGrid = currentPiece.xGrid;
				int yGrid = currentPiece.yGrid;

				while(yGrid<gridH()-1&&grid.get(xGrid,yGrid+1)==null)
				{
					yGrid++;
				}

				p.xGrid = xGrid;
				p.yGrid = yGrid;

				grid.setPiece(p);

				AudioManager().playSound(Settings().hardDropClankSound,getVolume(),getSoundEffectSpeed(),1);

				grid.shakeSmall();

				return false;
			}


			if(currentPiece.pieceType.pieceRemovalShooterPiece)
			{

				//so we can't repeatedly use it
				switchedHoldPieceAlready = true;

				int xGrid = currentPiece.xGrid;
				int yGrid = currentPiece.yGrid;

				while(yGrid<gridH()-1&&grid.get(xGrid,yGrid)==null)
				{
					yGrid++;
				}

				Block b  = grid.get(xGrid,yGrid);

				if(b!=null)
				{
					grid.deleteBlock(b);

					//set last screenXY to current piece so it appears to shoot, even though it's really just being placed directly into the grid.
					b.lastScreenX = grid.x()+xGrid*cellW();
					b.lastScreenY = grid.y()+yGrid*cellH();
					b.xGrid = xGrid;
					b.yGrid = currentPiece.yGrid;

					AudioManager().playSound(Settings().hardDropSwishSound,getVolume(),getSoundEffectSpeed(),1);

				}

				return false;
			}


		}


		if(move==MovementType.ROTATE_COUNTERCLOCKWISE) currentPiece.rotateCCW();
		if(move==MovementType.ROTATE_CLOCKWISE) currentPiece.rotateCW();
		if(move==MovementType.UP) currentPiece.yGrid--;
		if(move==MovementType.DOWN || move==MovementType.HARD_DROP) currentPiece.yGrid++;
		if(move==MovementType.RIGHT) currentPiece.xGrid++;
		if(move==MovementType.LEFT) currentPiece.xGrid--;


		if(grid.doesPieceFit(currentPiece))
		{
			pieceMoved();
			return true;
		}
		else
		{

			if(move==MovementType.ROTATE_COUNTERCLOCKWISE||move==MovementType.ROTATE_CLOCKWISE)
			{

				if(Settings().pieceClimbingAllowed)
				{

					//try climbing if direction is pressed
					if(controlsState().BUTTON_LEFT_HELD)
					{
						int tempY = currentPiece.yGrid;
						currentPiece.xGrid--;

						for(int i=0;i<=currentPiece.getHeight();i++)
						{
							currentPiece.yGrid--;
							if(grid.doesPieceFit(currentPiece)==true)
							{
								pieceMoved();
								AudioManager().playSound(Settings().diagonalWallKickSound,getVolume(),1.0f,1);
								canPressLeft=false;
								ticksHoldingLeft = 0;
								return true;
							}
						}

						currentPiece.yGrid = tempY;
						currentPiece.xGrid++;
					}

					//try climbing if direction is pressed
					if(controlsState().BUTTON_RIGHT_HELD)
					{
						int tempY = currentPiece.yGrid;
						currentPiece.xGrid++;

						for(int i=0;i<=currentPiece.getHeight();i++)
						{
							currentPiece.yGrid--;
							if(grid.doesPieceFit(currentPiece)==true)
							{
								pieceMoved();
								AudioManager().playSound(Settings().diagonalWallKickSound,getVolume(),1.0f,1);
								canPressRight=false;
								ticksHoldingRight = 0;
								return true;
							}
						}

						currentPiece.yGrid = tempY;
						currentPiece.xGrid--;
					}
				}




				// try pushing off the wall

				boolean hittingLeft = grid.isHittingLeft(currentPiece);
				boolean hittingRight = grid.isHittingRight(currentPiece);

				if(hittingLeft==true)
				{
					//check right 1
					currentPiece.xGrid++;
					if(grid.doesPieceFit(currentPiece)==true)
					{
						pieceMoved();
						AudioManager().playSound(Settings().wallKickSound,getVolume(),1.0f,1);
						return true;
					}
					else
					{
						currentPiece.xGrid--;

						if(Settings().twoSpaceWallKickAllowed)
						{
							//check right 2
							currentPiece.xGrid+=2;
							if(grid.doesPieceFit(currentPiece)==true)
							{
								pieceMoved();
								AudioManager().playSound(Settings().doubleWallKickSound,getVolume(),1.0f,1);
								return true;
							}
							else{currentPiece.xGrid-=2;}
						}

						if(Settings().diagonalWallKickAllowed)
						{
							//check down
							currentPiece.yGrid++;
							if(grid.doesPieceFit(currentPiece)==true)
							{
								pieceMoved();
								AudioManager().playSound(Settings().diagonalWallKickSound,getVolume(),1.0f,1);
								return true;
							}
							else
							{
								currentPiece.yGrid--;

								//check downright
								currentPiece.xGrid++;
								currentPiece.yGrid++;
								if(grid.doesPieceFit(currentPiece)==true)
								{
									pieceMoved();
									AudioManager().playSound(Settings().diagonalWallKickSound,getVolume(),1.0f,1);
									return true;
								}
								else
								{
									currentPiece.xGrid--;
									currentPiece.yGrid--;
								}
							}
						}

					}
				}

				if(hittingRight==true)
				{
					//check left 1
					currentPiece.xGrid--;
					if(grid.doesPieceFit(currentPiece)==true)
					{
						pieceMoved();
						AudioManager().playSound(Settings().wallKickSound,getVolume(),1.0f,1);
						return true;
					}
					else
					{
						currentPiece.xGrid++;

						if(Settings().twoSpaceWallKickAllowed)
						{
							//check left 2
							currentPiece.xGrid-=2;
							if(grid.doesPieceFit(currentPiece)==true)
							{
								pieceMoved();
								AudioManager().playSound(Settings().doubleWallKickSound,getVolume(),1.0f,1);
								return true;
							}
							else{currentPiece.xGrid+=2;}
						}

						if(Settings().diagonalWallKickAllowed)
						{
							//check down
							currentPiece.yGrid++;
							if(grid.doesPieceFit(currentPiece)==true)
							{
								pieceMoved();
								AudioManager().playSound(Settings().diagonalWallKickSound,getVolume(),1.0f,1);
								return true;
							}
							else
							{
								currentPiece.yGrid--;

								//check downleft
								currentPiece.xGrid--;
								currentPiece.yGrid++;
								if(grid.doesPieceFit(currentPiece)==true)
								{
									pieceMoved();
									AudioManager().playSound(Settings().diagonalWallKickSound,getVolume(),1.0f,1);
									return true;
								}
								else
								{
									currentPiece.xGrid++;
									currentPiece.yGrid--;
								}
							}
						}


					}
				}

				if(Settings().floorKickAllowed)
				{

					//check up 1
					currentPiece.yGrid--;
					if(grid.doesPieceFit(currentPiece)==true)
					{
						pieceMoved();
						AudioManager().playSound(Settings().floorKickSound,getVolume(),1.0f,1);
						return true;
					}
					else
					{
						currentPiece.yGrid++;

						if(Settings().twoSpaceWallKickAllowed)
						{
							//check up 2
							currentPiece.yGrid-=2;
							if(grid.doesPieceFit(currentPiece)==true)
							{
								pieceMoved();
								AudioManager().playSound(Settings().floorKickSound,getVolume(),1.0f,1);
								return true;
							}
							else{currentPiece.yGrid+=2;}
						}
					}

				}

				//DONE: handle 180 flip if rotating and piece does not fit
				if(Settings().flip180Allowed)
				{

					//rotate in direction again
					if(move==MovementType.ROTATE_COUNTERCLOCKWISE)currentPiece.rotateCCW();
					if(move==MovementType.ROTATE_CLOCKWISE)currentPiece.rotateCW();

					if(grid.doesPieceFit(currentPiece)==true)
					{
						pieceMoved();
						AudioManager().playSound(Settings().pieceFlip180Sound,getVolume(),1.0f,1);
						return true;
					}
					else
					{
						//check up 1
						currentPiece.yGrid--;
						if(grid.doesPieceFit(currentPiece)==true)
						{
							pieceMoved();
							AudioManager().playSound(Settings().pieceFlip180Sound,getVolume(),1.0f,1);
							return true;
						}
						else
						{
							currentPiece.yGrid++;
						}

						//rotate back
						if(move==MovementType.ROTATE_COUNTERCLOCKWISE)currentPiece.rotateCW();
						if(move==MovementType.ROTATE_CLOCKWISE)currentPiece.rotateCCW();
					}

				}
			}




			if(move==MovementType.ROTATE_COUNTERCLOCKWISE) currentPiece.rotateCW();
			if(move==MovementType.ROTATE_CLOCKWISE) currentPiece.rotateCCW();
			if(move==MovementType.UP) currentPiece.yGrid++;
			if(move==MovementType.DOWN || move==MovementType.HARD_DROP) currentPiece.yGrid--;
			if(move==MovementType.RIGHT) currentPiece.xGrid--;
			if(move==MovementType.LEFT) currentPiece.xGrid++;


			if(move==MovementType.DOWN || move==MovementType.HARD_DROP)
			{


				if(move==MovementType.HARD_DROP && Settings().hardDropPunchThroughToLowestValidGridPosition)
				{

					if(currentPiece!=null)
					{
						int lastValidYPosition = currentPiece.yGrid;

						for(int y = lastValidYPosition; y<grid.h(); y++)
						{
							if(grid.doesPieceFit(currentPiece,currentPiece.xGrid,y))
							{
								lastValidYPosition = y;
							}
						}
						currentPiece.yGrid = lastValidYPosition;
					}
				}


				if(lockDelayTicksCounter==0)
				{
					setPiece();
				}

			}




			return false;

		}
	}







	// =========================================================================================================================
	private void setCurrentPieceAtTop()
	{// =========================================================================================================================
		currentPiece.xGrid=(grid.w()/2) - (currentPiece.getWidth()/2 + currentPiece.getLowestOffsetX());
		if(currentPiece.getWidth()%2==1)currentPiece.xGrid-=1;

		currentPiece.yGrid=-2;
		//currentPiece.lastX = currentPiece.xGrid;
		//currentPiece.lastY = currentPiece.yGrid;

		spawnDelayTicksCounter=Settings().spawnDelayTicksAmountPerPiece;

		lineDropTicksCounter=0;
	}







	// =========================================================================================================================
	private void setPiece()
	{// =========================================================================================================================




		if(currentPiece.pieceType.bombPiece)
		{
			//get pieces down, left, right of bomb piece

			//grid.delete all those pieces

			ArrayList<Block> explodeBlocks = new ArrayList<Block>();


			int startX = (currentPiece.xGrid - Utils.abs(currentPiece.getLowestOffsetX()))-1;
			int endX = currentPiece.xGrid + currentPiece.getWidth() + 1;

			int startY = (currentPiece.yGrid - Utils.abs(currentPiece.getLowestOffsetY()))-1;
			int endY = currentPiece.yGrid + currentPiece.getHeight() + 1;

			for(int x=startX;x<endX&&x<gridW();x++)
			{
				for(int y=startY;y<endY&&y<gridH();y++)
				{

					Block b = grid.get(x,y);
					if(b!=null&&explodeBlocks.contains(b)==false)explodeBlocks.add(b);

				}
			}


			for(int i=0;i<explodeBlocks.size();i++)
			{
				grid.deleteBlock(explodeBlocks.get(i));
			}

			AudioManager().playSound(Settings().hardDropClankSound,1.5f,0.25f,1);

			grid.shakeHard();

		}


		if(currentPiece.pieceType.weightPiece)
		{

			//set weight piece blocks lastx,y
			for(int i=0;i<currentPiece.blocks.size();i++)
			{
				Block b = currentPiece.blocks.get(i);
				b.lastScreenX = grid.x()+(currentPiece.xGrid + b.xInPiece)*cellW();
				b.lastScreenY = grid.y()+(currentPiece.yGrid + b.yInPiece)*cellH();
				b.ticksSinceLastMovement=0;
			}


			//grid.delete all blocks underneath weight
			for(int y=currentPiece.yGrid;y<gridH();y++)
			{
				for(int x=currentPiece.getLowestOffsetX();x<=currentPiece.getHighestOffsetX();x++)
				{
					Block b = grid.get(currentPiece.xGrid+x,y);
					if(b!=null)grid.deleteBlock(b);
				}
			}


			//set weight piece at bottom
			while(grid.doesPieceFit(currentPiece))
			{
				currentPiece.yGrid++;
			}
			currentPiece.yGrid--;

			AudioManager().playSound(Settings().hardDropClankSound,getVolume(),0.5f,1);

			grid.shakeHard();

		}


		grid.setPiece(currentPiece);

		if(currentPiece.yGrid<0)dead=true;

		pieceSetAtBottom=true;

		currentPiece = null;

		AudioManager().playSound(Settings().pieceSetSound,getVolume(),getSoundEffectSpeed(),1);

	}







	// =========================================================================================================================
	private void newRandomPiece()
	{// =========================================================================================================================

		pieceSetAtBottom=false;


		if(nextPieces==null)
		{
			nextPieces = new ArrayList<Piece>();

			for(int i=0;i<Settings().numberOfNextPiecesToShow;i++)
			{
				nextPieces.add(grid.getRandomPiece());
				createdPiecesCounterForFrequencyPieces++;
			}
		}

		currentPiece=nextPieces.remove(0);



		//fill last nextPiece from bag
		if(nextPieceSpecialBuffer==null)nextPieceSpecialBuffer = new ArrayList<Piece>();

		if(nextPieceSpecialBuffer.size()>0)
		{
			nextPieces.add(nextPieceSpecialBuffer.remove(0));
		}
		else
		nextPieces.add(grid.getRandomPiece());



		setCurrentPieceAtTop();

		switchedHoldPieceAlready = false;

		piecesMadeThisGame++;
		piecesMadeTotal++;

		if(garbageWaitPieces>0)
		{
			garbageWaitPieces--;
		}

		AudioManager().playSound(getRandomMakePieceSound(),getVolume(),getSoundEffectSpeed(),1);



		ArrayList<Piece> piecesOnGrid = grid.getArrayOfPiecesOnGrid();
		for(int i=0;i<piecesOnGrid.size();i++)
		{
			Piece p = piecesOnGrid.get(i);
			p.piecesSetSinceThisPieceSet++;

			for(int j=0;j<p.blocks.size();j++)
			{
				Block b = p.blocks.get(j);
				if(b.blockType.counterType)
				{
					if(b.counterCount>-1)
					{
						b.counterCount--;
					}
				}
			}
		}




		//clear lineClearDelay if special piece
		if
		(
				currentPiece.pieceType.bombPiece
				||currentPiece.pieceType.weightPiece
				||currentPiece.pieceType.pieceShooterPiece
				||currentPiece.pieceType.pieceRemovalShooterPiece
		)
		{
			lineClearDelayTicksCounter=0;
		}



		//immediately skip bomb, weight, pieceShooter if playing field is empty
		if
		(
				currentPiece.pieceType.bombPiece
				||currentPiece.pieceType.weightPiece
				||currentPiece.pieceType.pieceRemovalShooterPiece
		)
		{
			boolean bottomEmpty = true;
			for(int x=0;x<grid.w();x++)
			{
				if(grid.get(x,grid.h()-1)!=null)bottomEmpty=false;
			}

			if(bottomEmpty)
			{
				newRandomPiece();
			}
		}

	}




	//need to add queuedGarbageAmountToSend to queuedGarbageAmountFromOtherPlayer when received in frame
	//need to add "ReceivedGarbageAmount" to frame state

	//=========================================================================================================================
	public void gotVSGarbageFromOtherPlayer(int amount)
	{//=========================================================================================================================

		this.frameState.receivedGarbageAmount = amount;

		garbageWaitPieces+=3;
		if(garbageWaitPieces>4)garbageWaitPieces=4;


		queuedGarbageAmountFromOtherPlayer += amount;

		makeAnnouncementCaption("Got VS Garbage From Other Player: "+amount);

		if(garbageBlock==null)
		{
			Piece p = new Piece(this,grid,new PieceType(),grid.getRandomBlockType(Settings().garbageBlockTypes));
			garbageBlock = p.blocks.get(0);
		}

	}



	//=========================================================================================================================
	public void processQueuedGarbageSentFromOtherPlayer()
	{//=========================================================================================================================


		if(queuedGarbageAmountFromOtherPlayer>0)
		{
			if(garbageWaitPieces==0)
			{
				makeAnnouncementCaption("Processed VS Garbage: "+queuedGarbageAmountFromOtherPlayer);

				while(queuedGarbageAmountFromOtherPlayer>0)
				{
					queuedGarbageAmountFromOtherPlayer--;


					if(Settings().vsGarbageRule==VSGarbageRule.FALL_FROM_CEILING_IN_EVEN_ROWS)
					{
						makeGarbageRowFromCeiling();
						moveDownBlocksOverBlankSpaces();
					}
					if(Settings().vsGarbageRule==VSGarbageRule.RISE_FROM_FLOOR_IN_EVEN_ROWS)
					{
						makeGarbageRowFromFloor();
					}
				}
			}
		}

	}



	//=========================================================================================================================
	public void queueVSGarbage(int amount)
	{//=========================================================================================================================

		//if queued garbage, send it to the other side and negate it

		//garbage types per game?


		if(queuedGarbageAmountFromOtherPlayer>0)
		{
			if(amount>=queuedGarbageAmountFromOtherPlayer)
			{
				makeAnnouncementCaption("Negated VS Garbage: "+queuedGarbageAmountFromOtherPlayer);

				amount-=queuedGarbageAmountFromOtherPlayer;
				queuedGarbageAmountFromOtherPlayer=0;
			}
			else
			if(amount<queuedGarbageAmountFromOtherPlayer)
			{
				makeAnnouncementCaption("Negated VS Garbage: "+amount);
				queuedGarbageAmountFromOtherPlayer-=amount;
				amount=0;
			}
		}

		if(amount>0)
		{
			queuedGarbageAmountToSend+=amount;
			makeAnnouncementCaption("Sent VS Garbage: "+amount+" Total: "+queuedGarbageAmountToSend);
		}

	}



	// =========================================================================================================================
	private void processGarbageRules()
	{// =========================================================================================================================

		boolean makeGarbage = false;

		if(Settings().garbageSpawnRule==GarbageSpawnRule.TICKS)
		{
			garbageValueCounter += ticks();
			if(garbageValueCounter>Settings().garbageSpawnRuleAmount)
			{
				garbageValueCounter = 0;
				makeGarbage=true;
			}
		}
		else
		if(Settings().garbageSpawnRule==GarbageSpawnRule.PIECES_MADE)
		{
			if(piecesMadeThisGame>=garbageValueCounter+Settings().garbageSpawnRuleAmount)
			{
				garbageValueCounter = piecesMadeThisGame;
				makeGarbage=true;
			}
		}
		else
		if(Settings().garbageSpawnRule==GarbageSpawnRule.BLOCKS_CLEARED)
		{
			if(blocksClearedThisGame>=garbageValueCounter+Settings().garbageSpawnRuleAmount)
			{
				garbageValueCounter = blocksClearedThisGame;
				makeGarbage=true;
			}
		}
		else
		if(Settings().garbageSpawnRule==GarbageSpawnRule.LINES_CLEARED)
		{
			if(linesClearedThisGame>=garbageValueCounter+Settings().garbageSpawnRuleAmount)
			{
				garbageValueCounter = linesClearedThisGame;
				makeGarbage=true;
			}
		}

		if(makeGarbage)
		{
			makeGarbageRowFromFloor();
		}

	}

	// =========================================================================================================================
	private void makeGarbageRowFromFloor()
	{// =========================================================================================================================
		grid.makeGarbageRowFromFloor();
		manuallyApplyGravityWithoutChainChecking();
		forceGravity = true;
	}

	// =========================================================================================================================
	private void makeGarbageRowFromCeiling()
	{// =========================================================================================================================
		grid.makeGarbageRowFromCeiling();
		forceGravity = true;
	}






//	// =========================================================================================================================
//	private void renderBackground()
//	{// =========================================================================================================================
//
//		if(side==BobsGame.LEFT)
//		{
//			// fill screen with black
//			GLUtils.drawFilledRectXYWH(0,0,getWidth()/2,getHeight(),Settings().screenBackgroundColor.r(),Settings().screenBackgroundColor.g(),Settings().screenBackgroundColor.b(),1.0f);
//		}
//		else
//		{
//			GLUtils.drawFilledRectXYWH(getWidth()/2,0,getWidth()/2,getHeight(),Settings().screenBackgroundColor.r(),Settings().screenBackgroundColor.g(),Settings().screenBackgroundColor.b(),1.0f);
//		}
//
//		//TODO: winamp style visualizations
//
//	}




	// =========================================================================================================================
	private void renderQueuedGarbage()
	{// =========================================================================================================================

		// render caption with garbageWaitPieces

		if(queuedGarbageAmountFromOtherPlayer>0)
		{


			if(garbageWaitCaption==null)garbageWaitCaption = Engine().CaptionManager().newManagedCaption(0,0,-1,"garbageWaitCaption",BobFont.font_normal_16_outlined_smooth,BobColor.WHITE,BobColor.CLEAR,RenderOrder.ABOVE_TOP,1.0f,0);

			garbageWaitCaption.screenX = (int)(grid.screenX);
			garbageWaitCaption.screenY = (int)(grid.screenY-(cellH()));
			garbageWaitCaption.flashing = true;
			garbageWaitCaption.flashingTicksPerFlash = 500;

			garbageWaitCaption.replaceText("Wait: "+garbageWaitPieces);


			for(int i=0;i<queuedGarbageAmountFromOtherPlayer;i++)
			{
				if(garbageBlock!=null)garbageBlock.render(grid.x() + i*blockWidth, grid.y()-blockHeight, 0.5f, 1.0f, false);
			}


		}
		else
		{
			if(garbageWaitCaption!=null)
			{
				garbageWaitCaption.deleteFadeOut();
				garbageWaitCaption = null;
			}
		}


	}




	// =========================================================================================================================
	private void renderHoldPiece()
	{// =========================================================================================================================


		float holdBoxX = grid.screenX - 3*cellW();
		float holdBoxY = grid.screenY;

		if(holdCaption!=null)holdCaption.screenX = holdBoxX;
		if(holdCaption!=null)holdCaption.screenY = holdBoxY-captionYSize;

		float holdBoxW = 2*cellW();
		float holdBoxH = 2*cellH();

		GLUtils.drawFilledRectXYWH(holdBoxX,holdBoxY,holdBoxW,holdBoxH,1,1,1,1.0f);
		GLUtils.drawFilledRectXYWH(holdBoxX+1,holdBoxY+1,holdBoxW-2,holdBoxH-2,0,0,0,1.0f);

		if(holdPiece!=null)
		{
			float scale = 0.5f;

			float w = cellW();
			float h = cellH();

			float holdX = (holdBoxX+1*w*scale);
			float holdY = (holdBoxY+1*h*scale);

			//center in box depending on width
			if(holdPiece.getWidth()==3)holdX-=0.5f*w*scale;
			if(holdPiece.getWidth()==4)holdX-=1*w*scale;


			for(int b=0;b<holdPiece.blocks.size();b++)
			{
				float blockX=(holdPiece.blocks.get(b).xInPiece-holdPiece.getLowestOffsetX())*w*scale;
				float blockY=(holdPiece.blocks.get(b).yInPiece-holdPiece.getLowestOffsetY())*h*scale;

				float x = holdX + blockX;
				float y = holdY + blockY;
				holdPiece.blocks.get(b).render(x,y,1.0f,0.5f,true);
			}
		}
	}
	// =========================================================================================================================
	private boolean nextPieceEnabled()
	{// =========================================================================================================================
		if(extraStage3==false&&extraStage4==false&&Settings().nextPieceEnabled==true)return true;
		return false;
	}

	// =========================================================================================================================
	private void renderNextPiece()
	{// =========================================================================================================================
		// render nextPiece
		if(nextPieceEnabled())
		{
			if(nextPieces!=null)
			{
				if(nextPieces==null)return;

				float lastPieceX = 0;
				float startPieceX = 0;

				//wait until currentPiece has moved out of the next box before sliding the other pieces over
				if(currentPiece!=null&&currentPiece.yGrid<=0)
				{
					for(int b=0;b<currentPiece.blocks.size();b++)
					{
						float blockX=currentPiece.blocks.get(b).xInPiece*cellW();

						float x = grid.screenX + ((grid.w()/2)*cellW()) + blockX;
						if(currentPiece.getWidth()%2==1)x-=cellW();

						if(x>lastPieceX)lastPieceX = x;
					}
				}



				for(int i=0;i<nextPieces.size();i++)
				{
					Piece nextPiece = nextPieces.get(i);

					startPieceX = lastPieceX + cellW();

					for(int b=0;b<nextPiece.blocks.size();b++)
					{

						if(i==0&&(currentPiece==null||currentPiece.yGrid>0))
						{

							float blockX=nextPiece.blocks.get(b).xInPiece*cellW();
							float x =  grid.screenX + (( grid.w()/2)*cellW()) + blockX;
							if(nextPiece.getWidth()%2==1)x-=cellW();



							float blockY=nextPiece.blocks.get(b).yInPiece*cellH();
							//int aboveCurrent = 0;
							//if(currentPiece!=null&&currentPiece.y<=0)aboveCurrent = 0-currentPiece.y;

							float y =  grid.screenY - (cellH()*(nextPiece.getHeight())) + blockY;


							nextPiece.blocks.get(b).render(x,y,1.0f,1.0f,true);

							if(x>lastPieceX)lastPieceX = x;
						}
						else
						{
							float s = 0.75f;

							float blockX=nextPiece.blocks.get(b).xInPiece*cellW()*s;
							float x = startPieceX + (Math.abs(nextPiece.getLowestOffsetX())+1)*cellW()*s + blockX;


							float blockY = nextPiece.blocks.get(b).yInPiece*cellH()*s;
							float y =  grid.screenY - (cellH()*3) + blockY;


							nextPiece.blocks.get(b).render(x,y,1.0f,s,true);

							if(x>lastPieceX)lastPieceX = x;
						}
					}
				}
			}
		}

		if(nextCaption!=null)nextCaption.screenX = grid.screenX + ((grid.w()/2)*cellW());
		if(nextCaption!=null)nextCaption.screenY = grid.screenY - 4*cellH();

	}


	// =========================================================================================================================
	private void renderCurrentPiece()
	{// =========================================================================================================================
		if(currentPiece!=null)
		{

			if(Settings().stackRiseGame)
			{

			}
			else
			{
				// render ghost piece
				if(pieceSetAtBottom==false && gravityThisFrame==false)grid.renderGhostPiece(currentPiece);

			}


			// render currentPiece
			//grid.renderCurrentPiece(currentPiece);
			currentPiece.renderAsCurrentPiece();
		}
	}


	// =========================================================================================================================
	private void renderOverlays()
	{// =========================================================================================================================
		// flash screen if flashing
		if(timesToFlashScreenQueue>0)
		{
			if(flashScreenOnOffToggle==true)GLUtils.drawFilledRectXYWH(0,0,getWidth(),getHeight(),1.0f,1.0f,1.0f,0.5f);
		}
	}



	// =========================================================================================================================
	public void renderBackground()
	{// =========================================================================================================================

		//Settings().gridBorderColor = BobColor.yellow;
		//Settings().gridBorderColor = BobColor.getHSBColor(captionColorCycleHueValue,1.0f,1.0f);


		// fill in checkered background
		grid.renderBackground();

		grid.renderBorder();

	}


	// =========================================================================================================================
	public void renderBlocks()
	{// =========================================================================================================================


		renderQueuedGarbage();

		for(int i=0;i<disappearingBlocks.size();i++)
		{
			disappearingBlocks.get(i).renderDisappearing();
		}

		//render blocks
		grid.render();


		renderHoldPiece();

		renderNextPiece();

		renderCurrentPiece();


	}

	// =========================================================================================================================
	public void renderForeground()
	{// =========================================================================================================================

		grid.renderBlockOutlines();

		if(Settings().stackRiseGame)
		{
			grid.renderTransparentOverLastRow();
		}


		renderOverlays();
	}

	// =========================================================================================================================
	private void doExtraStageEffects()
	{// =========================================================================================================================




		if(currentLevel>=Settings().extraStage1Level)grid.shakeForeground();

//		if(extraStage1)
//		{
//			grid.scrollBackground();
//		}
//
//
//		if(extraStage2)
//		{
//			extraStageTicksPassed+=ticks();
//			if(extraStageTicksPassed>1000)
//			{
//				extraStageTicksPassed=0;
//				grid.setRandomPieceGrayscaleColors(currentPiece,nextPieces);
//
//			}
//
//			grid.scrollBackground();
//		}
//
//		if(extraStage3)
//		{
//			extraStageTicksPassed+=ticks();
//
//			if(extraStageTicksPassed>300)
//			{
//				extraStageTicksPassed=0;
//				grid.setRandomBlockColors();
//
//			}
//
//			grid.shakeForeground();
//		}
//
//		if(extraStage4)
//		{
//			extraStageTicksPassed+=ticks();
//
//			if(extraStageTicksPassed>200)
//			{
//				extraStageTicksPassed=0;
//				grid.setRandomMatrixBlockColors();
//
//
//			}
//
//			grid.scrollBackground();
//			grid.shakeForeground();
//		}
	}





	// =========================================================================================================================
	private String getRandomMakePieceSound()
	{// =========================================================================================================================
		int r = getRandomIntLessThan(7);
		if(r==0)return "piece1";
		if(r==1)return "piece2";
		if(r==2)return "piece3";
		if(r==3)return "piece4";
		if(r==4)return "piece5";
		if(r==5)return "piece6";
		return "piece7";

	}


	// =========================================================================================================================
	private float getSoundEffectSpeed()
	{// =========================================================================================================================
		if(Settings().useRandomSoundModulation)
		{
			return 0.5f+(float)(Math.random()*1.5f);
		}
		return 1.0f;

	}

	// =========================================================================================================================
	private float getVolume()
	{// =========================================================================================================================
		if(mute)return 0.0f;
		else return 1.0f;
	}


	// =========================================================================================================================
	private void checkForFastMusic()
	{// =========================================================================================================================


		boolean anythingAboveThreeQuarters=grid.isAnythingAboveThreeQuarters();

		if(Settings().gridRule_showWarningForFieldThreeQuartersFilled && (anythingAboveThreeQuarters||extraStage1||extraStage2||extraStage3))
		{
			if(playingFastMusic==false)
			{
				playingFastMusic=true;

				if(Settings().fastMusic==null||Settings().fastMusic.length()==0)
				{
					AudioManager().stopMusic(playingMusic);
					playingMusic = Settings().normalMusic;
					AudioManager().playMusic(playingMusic,getVolume(),1.5f,true);
				}
				else
				{
					AudioManager().stopMusic(playingMusic);
					playingMusic = Settings().fastMusic;
					AudioManager().playMusic(Settings().fastMusic,getVolume(),1.0f,true);
				}

				if(anythingAboveThreeQuarters)makeAnnouncementCaption("Uh oh, be careful!");
			}
		}
		else
		{

			if(playingFastMusic==true)
			{
				playingFastMusic=false;

				AudioManager().stopMusic(playingMusic);
				playingMusic = Settings().normalMusic;
				AudioManager().playMusic(playingMusic);
			}

		}

		return;
	}




	// =========================================================================================================================
	public void updateKeyInput()
	{// =========================================================================================================================




		//DONE: autopress buttons enabled per game()
		//DONE: timing for buttons per game()


		if(lockInputCountdownTicks>0)return;


		if(!controlsState().BUTTON_SPACE_HELD)
		{
			canPressA=true;
			ticksHoldingA=0;
			repeatStartedA=false;
		}

		if(!controlsState().BUTTON_LSHIFT_HELD)
		{
			canPressB=true;
			ticksHoldingB=0;
			repeatStartedB=false;
		}

		if(!controlsState().BUTTON_RIGHT_HELD)
		{
			canPressRight=true;
			ticksHoldingRight=0;
			repeatStartedRight=false;
		}

		if(!controlsState().BUTTON_LEFT_HELD)
		{
			canPressLeft=true;
			ticksHoldingLeft=0;
			repeatStartedLeft=false;
		}

		if(!controlsState().BUTTON_DOWN_HELD)
		{
			canPressDown=true;
			ticksHoldingDown=0;
			repeatStartedDown=false;
		}

		if(!controlsState().BUTTON_UP_HELD)
		{
			canPressUp=true;
			ticksHoldingUp=0;
			repeatStartedUp=false;
		}

		if(!controlsState().BUTTON_LCTRL_HELD)
		{
			canPressR=true;
			ticksHoldingR=0;
			repeatStartedR=false;
		}






		if(controlsState().BUTTON_SPACE_HELD&&((Settings().repeatEnabledA&&repeatStartedA==false&&ticksHoldingA>=Settings().repeatStartDelayA)||(repeatStartedA&&ticksHoldingA>=Settings().repeatDelayA)))
		{
			canPressA=true;
			ticksHoldingA=0;
			repeatStartedA=true;
		}
		else ticksHoldingA+=ticks();


		if(controlsState().BUTTON_LSHIFT_HELD&&((Settings().repeatEnabledB&&repeatStartedB==false&&ticksHoldingB>=Settings().repeatStartDelayB)||(repeatStartedB&&ticksHoldingB>=Settings().repeatDelayB)))
		{
			canPressB=true;
			ticksHoldingB=0;
			repeatStartedB=true;
		}
		else ticksHoldingB+=ticks();


		if(controlsState().BUTTON_RIGHT_HELD&&((Settings().repeatEnabledRight&&repeatStartedRight==false&&ticksHoldingRight>=Settings().repeatStartDelayRight)||(repeatStartedRight&&ticksHoldingRight>=Settings().repeatDelayRight)))
		{
			canPressRight=true;
			ticksHoldingRight=0;
			repeatStartedRight=true;
		}
		else ticksHoldingRight+=ticks();


		if(controlsState().BUTTON_LEFT_HELD&&((Settings().repeatEnabledLeft&&repeatStartedLeft==false&&ticksHoldingLeft>=Settings().repeatStartDelayLeft)||(repeatStartedLeft&&ticksHoldingLeft>=Settings().repeatDelayLeft)))
		{
			canPressLeft=true;
			ticksHoldingLeft=0;
			repeatStartedLeft=true;
		}
		else ticksHoldingLeft+=ticks();


		if(controlsState().BUTTON_DOWN_HELD&&((Settings().repeatEnabledDown&&repeatStartedDown==false&&ticksHoldingDown>=Settings().repeatStartDelayDown)||(repeatStartedDown&&ticksHoldingDown>=Settings().repeatDelayDown)))
		{
			canPressDown=true;
			ticksHoldingDown=0;
			repeatStartedDown=true;
		}
		else ticksHoldingDown+=ticks();


		if(controlsState().BUTTON_UP_HELD&&((Settings().repeatEnabledUp&&repeatStartedUp==false&&ticksHoldingUp>=Settings().repeatStartDelayUp)||(repeatStartedUp&&ticksHoldingUp>=Settings().repeatDelayUp)))
		{
			canPressUp=true;
			ticksHoldingUp=0;
			repeatStartedUp=true;
		}
		else ticksHoldingUp+=ticks();


		if(controlsState().BUTTON_LCTRL_HELD&&((Settings().repeatEnabledR&&repeatStartedR==false&&ticksHoldingR>=Settings().repeatStartDelayR)||(repeatStartedR&&ticksHoldingR>=Settings().repeatDelayR)))
		{
			canPressR=true;
			ticksHoldingR=0;
			repeatStartedR=true;
		}
		else ticksHoldingR+=ticks();





		// END CHECK IF KEY WAS ALREADY PRESSED
		// BEGIN ACTUAL KEY CHECK
		if((controlsState().BUTTON_SPACE_HELD)&&(canPressA==true))
		{
			if(Settings().stackRiseGame&&Settings().cursorPieceSize==2)
			{
				AudioManager().playSound(Settings().rotateSound,getVolume(),getSoundEffectSpeed(),1);
				grid.cursorSwapBetweenTwoBlocks(currentPiece);
			}
			else
			if(Settings().stackRiseGame&&Settings().cursorPieceSize==1)
			{
				AudioManager().playSound(Settings().rotateSound,getVolume(),getSoundEffectSpeed(),1);
				grid.cursorSwapHoldingBlockWithGrid(currentPiece);
			}
			else
			if(Settings().stackRiseGame&&Settings().cursorPieceSize==4)
			{
				AudioManager().playSound(Settings().rotateSound,getVolume(),getSoundEffectSpeed(),1);
				grid.cursorRotateBlocks(currentPiece,MovementType.ROTATE_CLOCKWISE);
			}
			else
			{
				AudioManager().playSound(Settings().rotateSound,getVolume(),getSoundEffectSpeed(),1);
				movePiece(MovementType.ROTATE_CLOCKWISE);
			}

			canPressA=false;
			ticksHoldingA=0;
		}

		if((controlsState().BUTTON_LSHIFT_HELD)&&(canPressB==true))
		{


			if(Settings().stackRiseGame&&Settings().cursorPieceSize==2)
			{
				AudioManager().playSound(Settings().rotateSound,getVolume(),getSoundEffectSpeed(),1);
				grid.cursorSwapBetweenTwoBlocks(currentPiece);
			}
			else
			if(Settings().stackRiseGame&&Settings().cursorPieceSize==1)
			{
				AudioManager().playSound(Settings().rotateSound,getVolume(),getSoundEffectSpeed(),1);
				grid.cursorSwapHoldingBlockWithGrid(currentPiece);
			}
			else
			if(Settings().stackRiseGame&&Settings().cursorPieceSize==4)
			{
				AudioManager().playSound(Settings().rotateSound,getVolume(),getSoundEffectSpeed(),1);
				grid.cursorRotateBlocks(currentPiece,MovementType.ROTATE_COUNTERCLOCKWISE);
			}
			else
			{
				AudioManager().playSound(Settings().rotateSound,getVolume(),getSoundEffectSpeed(),1);
				movePiece(MovementType.ROTATE_COUNTERCLOCKWISE);
			}

			canPressB=false;
			ticksHoldingB=0;
		}

		if((controlsState().BUTTON_RIGHT_HELD)&&(canPressRight==true))
		{
			if(Settings().stackRiseGame)
			{
				AudioManager().playSound(Settings().moveRightSound,getVolume(),getSoundEffectSpeed(),1);
				if(currentPiece.xGrid<grid.w()-currentPiece.getWidth())currentPiece.xGrid++;
			}
			else
			{
				AudioManager().playSound(Settings().moveRightSound,getVolume(),getSoundEffectSpeed(),1);
				movePiece(MovementType.RIGHT);
			}

			canPressRight=false;
			ticksHoldingRight=0;
		}


		if((controlsState().BUTTON_LEFT_HELD)&&(canPressLeft==true))
		{
			if(Settings().stackRiseGame)
			{
				AudioManager().playSound(Settings().moveLeftSound,getVolume(),getSoundEffectSpeed(),1);
				if(currentPiece.xGrid>0)currentPiece.xGrid--;
			}
			else
			{
				AudioManager().playSound(Settings().moveLeftSound,getVolume(),getSoundEffectSpeed(),1);
				movePiece(MovementType.LEFT);
			}

			canPressLeft=false;
			ticksHoldingLeft=0;
		}


		if((controlsState().BUTTON_DOWN_HELD)&&(canPressDown==true))
		{

			if(Settings().stackRiseGame)
			{
				AudioManager().playSound(Settings().moveDownSound,getVolume(),getSoundEffectSpeed(),1);
				if(currentPiece.yGrid<grid.h()-(1+currentPiece.getHeight()))currentPiece.yGrid++;
			}
			else
			{
				if(pieceSetAtBottom==false)
				{
					AudioManager().playSound(Settings().moveDownSound,getVolume(),getSoundEffectSpeed(),1);

					movePiece(MovementType.DOWN);

					if(Settings().dropLockType==DropLockType.SOFT_DROP_INSTANT_LOCK)
					{
						lockDelayTicksCounter=0;
					}

				}
			}

			canPressDown=false;
			ticksHoldingDown=0;
		}





		if((controlsState().BUTTON_UP_HELD)&&(canPressUp==true))
		{

			if(Settings().stackRiseGame)
			{
				AudioManager().playSound(Settings().moveUpSound,getVolume(),getSoundEffectSpeed(),1);
				if(currentPiece.yGrid>1)currentPiece.yGrid--;
			}
			else
			{

				if(pieceSetAtBottom==false)
				{
					AudioManager().playSound(Settings().hardDropSwishSound,getVolume(),2.0f,1);

					if(currentPiece!=null)currentPiece.setBlocksSlamming();


					while(movePiece(MovementType.HARD_DROP)==true)
					{
						if(Settings().dropLockType==DropLockType.HARD_DROP_INSTANT_LOCK)
						{
							lockDelayTicksCounter=0;
						}
					}



					grid.shakeSmall();

					AudioManager().playSound(Settings().hardDropClankSound,getVolume(),0.5f,1);
				}

			}

			canPressUp=false;
			ticksHoldingUp=0;
		}


		if((controlsState().BUTTON_LCTRL_HELD)&&(canPressR==true))
		{

			if(Settings().stackRiseGame)
			{

			}
			else
			{
				if(Settings().holdPieceEnabled==true)
				{
					if(holdPiece!=null || switchedHoldPieceAlready)
					{
						if(switchedHoldPieceAlready==false)
						{
							switchedHoldPieceAlready = true;
							Piece tempPiece = holdPiece;
							holdPiece = currentPiece;
							currentPiece = tempPiece;

							if(Settings().resetHoldPieceRotation)holdPiece.setRotation(0);

							setCurrentPieceAtTop();

							AudioManager().playSound(Settings().switchHoldPieceSound,getVolume(),1.0f,1);
						}
						else
						{
							AudioManager().playSound(Settings().cantHoldPieceSound,getVolume(),1.0f,1);
						}
					}
					else
					{
						if(currentPiece!=null)
						{
							holdPiece = currentPiece;

							if(Settings().resetHoldPieceRotation)holdPiece.setRotation(0);

							AudioManager().playSound(Settings().switchHoldPieceSound,getVolume(),getSoundEffectSpeed(),1);
							newRandomPiece();
						}
					}
				}
			}

			canPressR=false;
			ticksHoldingR=0;

		}




		if(Settings().stackRiseGame)
		{
			if(controlsState().BUTTON_LCTRL_HELD)
			{
				if(manualStackRiseTicksCounter>30/(blockHeight/8))
				{
					manualStackRiseTicksCounter=0;

					manualStackRiseSoundToggle++;
					if(manualStackRiseSoundToggle>3)
					{
						manualStackRiseSoundToggle=0;
						AudioManager().playSound(Settings().stackRiseSound,getVolume(),1.0f,1);
					}
					grid.scrollUpStack(currentPiece,1);
				}
			}
		}



		//if(BUTTON_L_HELD)
		//{
		//	if(BUTTON_Y_HELD)
		//	if(BUTTON_SELECT_PRESSED)
		//	TETRID_init_game;
		//	TETRID_current_score+=10;
		//	cheater=1;
		//}


	}


	// =========================================================================================================================
	private void win()
	{// =========================================================================================================================


		if(startedWinSequence==false)
		{
			startedWinSequence=true;

			AudioManager().playSound(Settings().winSound,getVolume(),1.0f,1);

			AudioManager().stopMusic(playingMusic);
			playingMusic = Settings().winMusic;
			AudioManager().playMusic(playingMusic);

			if(winCaption==null)
			{
				winCaption = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2)-60,-1,"WIN",BobFont.font_normal_16_outlined_smooth,BobColor.GREEN,BobColor.CLEAR,RenderOrder.ABOVE_TOP,4.0f,0);
				winCaption.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - winCaption.getWidth()/2;
				winCaption.flashing = true;
				winCaption.flashingTicksPerFlash = 500;
			}

		}

		updateSpecialPiecesAndBlocks();

	}

	// =========================================================================================================================
	private void lose()
	{// =========================================================================================================================


		if(startedLoseSequence==false)
		{
			startedLoseSequence=true;

			AudioManager().playSound(Settings().loseSound,getVolume(),1.0f,1);

			AudioManager().stopMusic(playingMusic);
			playingMusic = Settings().loseMusic;
			AudioManager().playMusic(playingMusic);

			if(loseCaption==null)
			{
				loseCaption = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2)-60,-1,"LOSE",BobFont.font_normal_16_outlined_smooth,BobColor.RED,BobColor.CLEAR,RenderOrder.ABOVE_TOP,4.0f,0);
				loseCaption.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - loseCaption.getWidth()/2;
				loseCaption.flashing = true;
				loseCaption.flashingTicksPerFlash = 500;
			}

		}

		updateSpecialPiecesAndBlocks();

	}

	// =========================================================================================================================
	private void dead()
	{// =========================================================================================================================


		if(startedDeathSequence==false)
		{
			startedDeathSequence=true;

			AudioManager().playSound(Settings().deadSound,getVolume(),1.0f,1);


			AudioManager().stopMusic(playingMusic);
			playingMusic = Settings().deadMusic;
			AudioManager().playMusic(playingMusic);

			if(firstDeath==false)
			{
				firstDeath=true;

				if(deadCaption==null)
				{
					deadCaption = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2)-60,-1,"FAILURE",BobFont.font_normal_16_outlined_smooth,BobColor.RED,BobColor.CLEAR,RenderOrder.ABOVE_TOP,3.0f,0);
					deadCaption.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - deadCaption.getWidth()/2;
					deadCaption.flashing = true;
					deadCaption.flashingTicksPerFlash = 500;
				}

			}
			else
			if(firstDeath==true)
			{
				firstDeath=false;

				if(deadCaption==null)
				{
					deadCaption = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2)-60,-1,"NEVER GIVE UP",BobFont.font_normal_16_outlined_smooth,BobColor.RED,BobColor.CLEAR,RenderOrder.ABOVE_TOP,3.0f,0);
					deadCaption.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - deadCaption.getWidth()/2;
					deadCaption.flashing = true;
					deadCaption.flashingTicksPerFlash = 500;
				}
			}
		}


		updateSpecialPiecesAndBlocks();

		grid.doDeathSequence();





	}


	// =========================================================================================================================
	private void credits()
	{// =========================================================================================================================

		if(creditScreenInitialized==false)
		{
			creditScreenInitialized=true;


			AudioManager().stopMusic(playingMusic);

			playingMusic = Settings().creditsMusic;
			AudioManager().playMusic(playingMusic);

			if(creditsCaption==null)
			{
				creditsCaption = Engine().CaptionManager().newManagedCaption(0,(int)(getHeight()/2)-60,-1,"COMPLETE!!!",BobFont.font_normal_16_outlined_smooth,BobColor.PURPLE,BobColor.CLEAR,RenderOrder.ABOVE_TOP,4.0f,0);
				creditsCaption.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - creditsCaption.getWidth()/2;
				creditsCaption.flashing = true;
				creditsCaption.flashingTicksPerFlash = 500;
			}

		}

	}










	//---------------------------------------------------
	// captions
	//----------------------------------------------------

	int captionY = -1;
	int captionX = 0;

	float captionColorCycleHueValue=0;
	boolean captionColorCycleHueInOutToggle=false;


	float captionScale = 1.0f;
	int captionYSize = (int)(12 * captionScale);


	BobColor captionTextColor = BobColor.white;
	BobColor captionBGColor = BobColor.black;
	BitmapFont captionFont = BobFont.font_small_8;



	BobColor announcementCaptionTextColor = BobColor.white;
	BobColor announcementCaptionBGColor = BobColor.clear;
	BitmapFont announcementCaptionFont = BobFont.font_normal_11_outlined;


	transient public Caption levelCaption;
	public String levelCaptionText = "levelCaptionText";

	transient public Caption gameTypeCaption;
	transient public Caption rulesCaption;

	transient public Caption difficultyCaption;
	public String difficultyCaptionText = "difficultyCaptionText";

	transient public Caption stopCounterCaption;
	public String stopCounterCaptionText = "stopCounterCaptionText";

	transient public Caption xCaption;
	transient public Caption yCaption;
	transient public Caption lineDropTicksCaption;
	transient public Caption lockDelayCaption;
	transient public Caption spawnDelayCaption;
	transient public Caption lineClearDelayCaption;
	transient public Caption gravityCaption;
	transient public Caption rotationCaption;
	transient public Caption holdCaption;
	transient public Caption nextCaption;

	transient public Caption totalLinesClearedCaption;
	transient public Caption totalBlocksClearedCaption;
	transient public Caption totalPiecesMadeCaption;

	transient public Caption linesClearedThisGameCaption;
	transient public Caption blocksClearedThisGameCaption;
	transient public Caption piecesMadeThisGameCaption;

	transient public Caption blocksInGridCaption;
	transient public Caption currentChainCaption;
	transient public Caption currentComboCaption;
	transient public Caption comboChainTotalCaption;
	transient public Caption seedCaption;

	ArrayList<Caption> infoCaptions = new ArrayList<Caption>();
	ArrayList<Caption> announcementCaptions = new ArrayList<Caption>();








	transient public Caption totalTicksPassedCaption;
	int timeCaptionStandardizedWidth = 0;

	transient public Caption pressStartCaption = null;
	transient public Caption waitingForPlayerCaption = null;
	transient public Caption creditsCaption = null;
	transient public Caption deadCaption = null;
	transient public Caption winCaption = null;
	transient public Caption loseCaption = null;
	transient public Caption garbageWaitCaption = null;






	//lines cleared
	//blocks cleared
	//dobules
	//triples
	//tetrises


	//pieces
	//blocks

	//moves total
	//moves left
	//moves right
	//rotations cw
	//rotations ccw

	//hard drops
	//soft drops
	//spins
	//tspins
	//wallkicks
	//floor kicks

	//time

	//stackrise

	//combos
	//chains

	//garbage



	//=========================================================================================================================
	private void makeAnnouncementCaption(String text)
	{//=========================================================================================================================
		makeAnnouncementCaption(text,null);
	}


	//=========================================================================================================================
	private void makeAnnouncementCaption(String text,BobColor color)
	{//=========================================================================================================================

		if(color==null)color = announcementCaptionTextColor;

		announcementCaptions.add(CaptionManager().newManagedCaption(0,0,-1,text,announcementCaptionFont,color,announcementCaptionBGColor,captionScale));

	}


//	//=========================================================================================================================
//	private void makeRandomLevelUpCaption()
//	{//=========================================================================================================================
//		int z=0;
//		z=getRandomIntLessThan(5);
//		if(z==0)makeAnnouncementCaption("Nice! Keep going!");
//		if(z==1)makeAnnouncementCaption("Not bad! Can you keep it up?");
//		if(z==2)makeAnnouncementCaption("So far so good!");
//		if(z==3)makeAnnouncementCaption("You'll make it at this rate!");
//		if(z==4)makeAnnouncementCaption("You can do it, come on!");
//
//	}




	//=========================================================================================================================
	private void updateCaptionFadeValues()
	{//=========================================================================================================================



		if(captionColorCycleHueInOutToggle==true)
		{
			captionColorCycleHueValue+=0.001f*Engine().engineTicksPassed();
			if(captionColorCycleHueValue>1.0f)
			{
				captionColorCycleHueValue=1.0f;
				captionColorCycleHueInOutToggle=false;
			}
		}
		else
		{
			captionColorCycleHueValue-=0.001f*Engine().engineTicksPassed();
			if(captionColorCycleHueValue<0.0f)
			{
				captionColorCycleHueValue=0.0f;
				captionColorCycleHueInOutToggle=true;
			}
		}

	}




	//=========================================================================================================================
	private Caption makeInfoCaption(String text)
	{//=========================================================================================================================

		Caption c = CaptionManager().newManagedCaption(0,++captionY*captionYSize,-1,text,captionFont,captionTextColor,captionBGColor,captionScale);

		if(infoCaptions.contains(c)==false)infoCaptions.add(c);

		return c;
	}


	//=========================================================================================================================
	public void deleteAllCaptions()
	{//=========================================================================================================================

		if(totalTicksPassedCaption!=null)totalTicksPassedCaption.deleteImmediately();
		if(creditsCaption!=null)creditsCaption.deleteImmediately();
		if(deadCaption!=null)deadCaption.deleteImmediately();
		if(winCaption!=null)winCaption.deleteImmediately();
		if(loseCaption!=null)loseCaption.deleteImmediately();
		if(pressStartCaption!=null)pressStartCaption.deleteImmediately();
		if(waitingForPlayerCaption!=null)waitingForPlayerCaption.deleteImmediately();
		if(garbageWaitCaption!=null)garbageWaitCaption.deleteImmediately();


		for(int i=0;i<infoCaptions.size();i++)
		{
			Caption c = infoCaptions.get(i);
			if(c!=null)c.deleteImmediately();
		}


		for(int i=0;i<announcementCaptions.size();i++)
		{
			Caption c = announcementCaptions.get(i);
			if(c!=null)c.deleteImmediately();
		}

	}


	//=========================================================================================================================
	private void updateInfoCaptionsXY()
	{//=========================================================================================================================

		int counterY = -1;
		float gridY = grid.y();

		for(int i=0;i<infoCaptions.size();i++)
		{
			Caption c = infoCaptions.get(i);

			if(c!=null)
			{
				if(c!=null)c.screenX=captionX;
				if(c!=null)c.screenY=gridY+(++counterY*captionYSize);
			}
		}
	}



	//=========================================================================================================================
	private void updateCaptions()
	{//=========================================================================================================================


		captionY = -1;

		if(levelCaption==null)levelCaption=makeInfoCaption("levelCaption");
		if(gameTypeCaption==null)gameTypeCaption=makeInfoCaption("gameTypeCaption");
		if(rulesCaption==null)rulesCaption=makeInfoCaption("rulesCaption");
		if(xCaption==null)xCaption=makeInfoCaption("xCaption");
		if(yCaption==null)yCaption=makeInfoCaption("yCaption");
		if(difficultyCaption==null)difficultyCaption=makeInfoCaption("difficultyCaption");
		if(lineDropTicksCaption==null)lineDropTicksCaption=makeInfoCaption("lineDropTicksCaption");
		if(lockDelayCaption==null)lockDelayCaption=makeInfoCaption("lockDelayCaption");
		if(spawnDelayCaption==null)spawnDelayCaption=makeInfoCaption("spawnDelayCaption");
		if(lineClearDelayCaption==null)lineClearDelayCaption=makeInfoCaption("lineClearDelayCaption");
		if(gravityCaption==null)gravityCaption=makeInfoCaption("gravityCaption");
		if(rotationCaption==null)rotationCaption=makeInfoCaption("rotationCaption");

		if(totalLinesClearedCaption==null)totalLinesClearedCaption=makeInfoCaption("totalLinesClearedCaption");
		if(totalBlocksClearedCaption==null)totalBlocksClearedCaption=makeInfoCaption("totalBlocksClearedCaption");
		if(totalPiecesMadeCaption==null)totalPiecesMadeCaption=makeInfoCaption("totalPiecesMadeCaption");

		if(linesClearedThisGameCaption==null)linesClearedThisGameCaption=makeInfoCaption("linesClearedThisGameCaption");
		if(blocksClearedThisGameCaption==null)blocksClearedThisGameCaption=makeInfoCaption("blocksClearedThisGameCaption");
		if(piecesMadeThisGameCaption==null)piecesMadeThisGameCaption=makeInfoCaption("piecesMadeThisGameCaption");

		if(blocksInGridCaption==null)blocksInGridCaption=makeInfoCaption("blocksInGridCaption");
		if(currentChainCaption==null)currentChainCaption=makeInfoCaption("currentChainCaption");
		if(currentComboCaption==null)currentComboCaption=makeInfoCaption("currentComboCaption");
		if(comboChainTotalCaption==null)comboChainTotalCaption=makeInfoCaption("comboChainTotalCaption");

		if(Settings().stackRiseGame)if(stopCounterCaption==null)stopCounterCaption=makeInfoCaption("stopCounterCaption");
		if(seedCaption==null)seedCaption=makeInfoCaption("seedCaption");



		if(totalTicksPassedCaption==null)
		{
			totalTicksPassedCaption=CaptionManager().newManagedCaption(0,0,-1,"00:00:000",BobFont.font_normal_16_outlined_smooth,captionTextColor,BobColor.CLEAR,captionScale*2.0f);
			timeCaptionStandardizedWidth = (int)totalTicksPassedCaption.getWidth();
		}

		if(holdCaption==null)holdCaption=CaptionManager().newManagedCaption(0,0,-1,"HOLD",captionFont,captionTextColor,captionBGColor,captionScale);
		if(nextCaption==null)nextCaption=CaptionManager().newManagedCaption(0,0,-1,"NEXT",captionFont,captionTextColor,captionBGColor,captionScale);


		updateInfoCaptionsXY();


		if(currentPiece!=null)
		{
			xCaption.replaceText("X: " + currentPiece.xGrid);
			yCaption.replaceText("Y: " + currentPiece.yGrid);

			rotationCaption.replaceText("Rotation: "+currentPiece.currentRotation);
		}



		if(stopCounterCaption!=null)stopCounterCaption.replaceText(stopCounterCaptionText);


		if(currentLevel==Settings().extraStage1Level)levelCaptionText = "Level: EX1";
		else if(currentLevel==Settings().extraStage2Level)levelCaptionText = "Level: EX2";
		else if(currentLevel==Settings().extraStage3Level)levelCaptionText = "Level: EX3";
		else if(currentLevel==Settings().extraStage4Level)levelCaptionText = "Level: EX4";
		else
		{
			levelCaptionText = "Level: "+currentLevel;
		}



		levelCaption.replaceText(levelCaptionText);
		gameTypeCaption.replaceText(Settings().gameTypeCaptionText);
		rulesCaption.replaceText(Settings().rulesCaptionText);
		difficultyCaption.replaceText(difficultyCaptionText);

		gravityCaption.replaceText("Gravity: "+String.format("%.3f",(16.7f/(float)currentLineDropSpeedTicks))+"G");
		lockDelayCaption.replaceText("Lock Delay: "+lockDelayTicksCounter);
		lineDropTicksCaption.replaceText("Line Drop Ticks: "+lineDropTicksCounter);
		spawnDelayCaption.replaceText("Spawn Delay Ticks: "+spawnDelayTicksCounter);
		lineClearDelayCaption.replaceText("Line Clear Delay Ticks: "+lineClearDelayTicksCounter);

		linesClearedThisGameCaption.replaceText("Lines This Game: "+linesClearedThisGame);
		blocksClearedThisGameCaption.replaceText("Blocks This Game: "+blocksClearedThisGame);
		piecesMadeThisGameCaption.replaceText("Pieces Made This Game: "+piecesMadeThisGame);

		totalLinesClearedCaption.replaceText("Lines Total: "+linesClearedTotal);
		totalBlocksClearedCaption.replaceText("Blocks Total: "+blocksClearedTotal);
		totalPiecesMadeCaption.replaceText("Pieces Made Total: "+piecesMadeTotal);

		blocksInGridCaption.replaceText("Block In Grid: "+grid.getNumberOfFilledCells());
		currentChainCaption.replaceText("Current Chain: "+currentChain);
		currentComboCaption.replaceText("Curent Combo: "+currentCombo);
		comboChainTotalCaption.replaceText("Combo Chain Total: "+comboChainTotal);
		seedCaption.replaceText("Seed: "+randomSeed);


		String ms = new DecimalFormat("000").format((int)(totalTicksPassed%1000));
		String seconds = new DecimalFormat("00").format((int)((totalTicksPassed/1000)%60));
		String minutes = new DecimalFormat("00").format((int)((totalTicksPassed/1000/60)%60));

		totalTicksPassedCaption.replaceText(minutes+":"+seconds+":"+ms);

		totalTicksPassedCaption.screenX = grid.screenX+(grid.w()*blockWidth)/2 - timeCaptionStandardizedWidth/2;
		totalTicksPassedCaption.screenY = grid.screenY+grid.h()*blockHeight+20;

		levelCaption.setTextColor(BobColor.getHSBColor(captionColorCycleHueValue,0.5f,1.0f));



		if(announcementCaptions.size()>15)
		{
			Caption c = announcementCaptions.get(0);
			c.deleteFadeOut();
		}

		for(int i=0;i<announcementCaptions.size();i++)
		{

			Caption c = announcementCaptions.get(i);



//			c.setAlphaTo(1.0f);
//			c.flashing = false;
//			c.flashingTicksPerFlash = 0;

			//c.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - c.getWidth()/2;
			//c.screenY = (int)(grid.screenY+(grid.h()*cellH()/2)) - c.getHeight() * i);

			//c.screenX = (int)(grid.screenX-(cellW()*1.5f)-c.getWidth());
			//c.screenY = (int)(grid.screenY+(cellH()*4))+c.getHeight()*i;



			int stayInCenterTicks = 1000;
			int transitionTime = 200;

			if(c.ticksAge<stayInCenterTicks)
			{
				c.setAlphaTo(1.0f);
				c.flashing = true;
				c.flashingTicksPerFlash = 500;

				c.scale = 3.25f - (announcementCaptions.size()-i)*0.25f;
				if(c.scale<2.0f)c.scale=2.0f;

				c.screenX = (int)(grid.screenX+(grid.w()*cellW()/2)) - c.getWidth()/2;
				c.screenY = (int)(grid.screenY+(grid.h()*cellH()/2)) - (c.getHeight()*(announcementCaptions.size()-i)+10);

			}
			else
			//if(c.ticksAge<stayInCenterTicks+transitionTime)
			{
				float xFrom = (grid.screenX+(grid.w()*cellW()/2)) - c.getWidth()/2;
				float yFrom = (grid.screenY+(grid.h()*cellH()/2)) - c.getHeight()/2;
				float xTo = (grid.screenX-(cellW()*1.5f)-c.getWidth());
				float yTo = (grid.screenY+(cellH()*2))+c.getHeight()*i;

				float xDiff = xFrom-xTo;
				float yDiff = yFrom-yTo;

				c.setAlphaTo(1.0f);
				c.flashing = false;
				c.flashingTicksPerFlash = 0;

				float xMod = ((float)(c.ticksAge-stayInCenterTicks) / (float)transitionTime);
				float yMod = ((float)(c.ticksAge-stayInCenterTicks) / (float)transitionTime);

				if(xMod>1.0f)xMod=1.0f;
				if(yMod>1.0f)yMod=1.0f;

				c.scale = 1.0f;
				c.screenX = xFrom - xMod*xDiff;
				c.screenY = yFrom - yMod*yDiff;

			}


			if(c.getDeleteStatus()==true&&c.getAlpha()==0.0f)
			{
				announcementCaptions.remove(i);
				i=-1;
				continue;
			}


		}

	}

	//=========================================================================================================================
	private void changeGame()
	{//=========================================================================================================================


		//TODO: play sound, delay, fix captions to show scoring type, etc


		currentPiece = null;
		holdPiece = null;
		nextPieces = null;
		grid.randomBag = null;
		nextPieceSpecialBuffer = null;


		//log.warn("Number of cells before: "+grid.getNumberOfFilledCells());


		int oldWidth = gridW();
		int oldHeight = gridH();

		Settings s = new Settings();
		setSettings(s);



		//setGameType(currentGameType+1);



		//int newGame = 0;
		//do{newGame = getRandomIntLessThan(gameCount);}while(newGame==currentGameType);
		setGameType(getGameTypeFromRandomBag());


		grid.reformat(oldWidth,oldHeight);

		grid.scrollPlayingFieldY = 0;

		//log.warn("Number of cells after reformat: "+grid.getNumberOfFilledCells());

		//force gravity first to fill in any gaps
		manuallyApplyGravityWithoutChainChecking();


		//log.warn("Number of cells after move down: "+grid.getNumberOfFilledCells());


		//go through playing field, change all blocks to acceptable playing field blocks if exist, otherwise normal pieces
		grid.replaceAllBlocksWithNewGameBlocks();


		//log.warn("Number of cells after replace: "+grid.getNumberOfFilledCells());

		//force gravity first to fill in any gaps
		manuallyApplyGravityWithoutChainChecking();

		//log.warn("Number of cells after move down again: "+grid.getNumberOfFilledCells());

		int piecesMade = piecesMadeThisGame;
		int dropSpeed = currentLineDropSpeedTicks;
		//possibly adding blocks or gaps to playing field i.e. dr bob
		init = false;
		initGame();
		currentLineDropSpeedTicks = dropSpeed;
		piecesMadeThisGame = piecesMade;
		lastPiecesMadeThisGame = piecesMade;
		//log.warn("Number of cells after init: "+grid.getNumberOfFilledCells());

		//forceBlockGravity = true;

		lockInputCountdownTicks = 500;


	}





	//=========================================================================================================================
	public void updateScore()
	{//=========================================================================================================================



		if(piecesMadeThisGame>lastPiecesMadeThisGame)
		{
			lastPiecesMadeThisGame = piecesMadeThisGame;

			if(currentLineDropSpeedTicks>Settings().minimumLineDropSpeedTicks)currentLineDropSpeedTicks*=0.98f;
			if(currentLineDropSpeedTicks<Settings().minimumLineDropSpeedTicks)currentLineDropSpeedTicks=Settings().minimumLineDropSpeedTicks;
		}



		if(Settings().scoreType == ScoreType.LINES_CLEARED)
		{

			if(Settings().scoreTypeAmountPerLevelGained>0)
			{
				if(linesClearedThisGame/Settings().scoreTypeAmountPerLevelGained>=1)currentLevel++;
			}

		}
		else
		if(Settings().scoreType == ScoreType.BLOCKS_CLEARED)
		{

			if(Settings().scoreTypeAmountPerLevelGained>0)
			{
				if(blocksClearedThisGame/Settings().scoreTypeAmountPerLevelGained>=1)currentLevel++;
			}

		}
		else
		if(Settings().scoreType == ScoreType.PIECES_MADE)
		{

			if(Settings().scoreTypeAmountPerLevelGained>0)
			{
				if(piecesMadeThisGame/Settings().scoreTypeAmountPerLevelGained>=1)currentLevel++;
			}

		}





		if(currentLevel!=lastKnownLevel)
		{
			lastKnownLevel=currentLevel;


			changeGame();

			timesToFlashScreenQueue=Settings().flashScreenTimesPerLevel;


			if(currentLevel>0)
			{
				if(currentLevel<Settings().extraStage1Level)
				makeAnnouncementCaption("Level up!");

				//makeRandomLevelUpCaption();

				grid.setRandomWholePieceColors(false,currentPiece,nextPieces);

				AudioManager().playSound(Settings().levelUpSound,getVolume(),1.0f,1);
			}


			if(currentLevel==Settings().extraStage1Level&&extraStage1==false)
			{
				extraStage1=true;
				makeAnnouncementCaption("Wow, it's the special stage!",BobColor.yellow);

				AudioManager().playSound(Settings().extraStage1Sound,getVolume(),1.0f,1);
			}


			if(currentLevel==Settings().extraStage2Level&&extraStage2==false)
			{
				extraStage2=true;
				makeAnnouncementCaption("Whoa, I've never gotten this far!",BobColor.orange);

				AudioManager().playSound(Settings().extraStage2Sound,getVolume(),1.0f,1);
			}


			if(currentLevel==Settings().extraStage3Level&&extraStage3==false)
			{
				extraStage3=true;
				makeAnnouncementCaption("Amazing!",BobColor.red);

				AudioManager().playSound(Settings().extraStage3Sound,getVolume(),1.0f,1);
			}


			if(currentLevel==Settings().extraStage4Level&&extraStage4==false)
			{
				extraStage4=true;
				makeAnnouncementCaption("What is going on?!",BobColor.magenta);

				AudioManager().playSound(Settings().extraStage4Sound,getVolume(),1.0f,1);
			}


			if(currentLevel>Settings().creditsLevel)
			{
				credits=true;
				makeAnnouncementCaption("You did it!!!",BobColor.blue);

				AudioManager().playSound(Settings().creditsSound,getVolume(),1.0f,1);
			}

		}
	}





	//=========================================================================================================================
	public Settings Settings()
	{//=========================================================================================================================
		return settings;
	}

	//=========================================================================================================================
	public void setSettings(Settings settings)
	{//=========================================================================================================================
		this.settings = settings;
	}



	//=========================================================================================================================
	public int getRandomIntLessThan(int i)
	{//=========================================================================================================================
		return randomGenerator.nextInt(i);
	}


	//=========================================================================================================================
	public int cellW()
	{//=========================================================================================================================
		return blockWidth + Settings().gridPixelsBetweenColumns;
	}

	//=========================================================================================================================
	public int cellH()
	{//=========================================================================================================================
		return blockHeight + Settings().gridPixelsBetweenRows;
	}

	//=========================================================================================================================
	public int gridW()
	{//=========================================================================================================================
		return Settings().gridWidth;
	}

	//=========================================================================================================================
	public int gridH()
	{//=========================================================================================================================
		return Settings().gridHeight;
	}


	//===============================================================================================
	public float getWidth()
	{//===============================================================================================
		return Engine().getWidth();
	}
	//===============================================================================================
	public float getHeight()
	{//===============================================================================================
		return Engine().getHeight();
	}



	//=========================================================================================================================
	public class ControlsState
	{//=========================================================================================================================
		public boolean BUTTON_SPACE_HELD = false;
		public boolean BUTTON_LCTRL_HELD = false;
		public boolean BUTTON_LSHIFT_HELD = false;
		public boolean BUTTON_UP_HELD = false;
		public boolean BUTTON_LEFT_HELD = false;
		public boolean BUTTON_DOWN_HELD = false;
		public boolean BUTTON_RIGHT_HELD = false;
	}

	//=========================================================================================================================
	public class FrameState
	{//=========================================================================================================================
		public ControlsState controlsState = new ControlsState();
		public long ticksPassed = 0;
		public int receivedGarbageAmount = 0;
	}

	// =========================================================================================================================
	public class NetworkPacket
	{// =========================================================================================================================
		public Vector<FrameState> frameStates = new Vector<FrameState>();
	}



	public NetworkPacket networkPacket = new NetworkPacket();
	public FrameState frameState = new FrameState();

	//=========================================================================================================================
	public long ticks()
	{//=========================================================================================================================
		return frameState.ticksPassed;
	}

	//=========================================================================================================================
	public ControlsState controlsState()
	{//=========================================================================================================================
		return frameState.controlsState;
	}


	public long randomSeed = -1;
	public Random randomGenerator = null;
	public boolean controlledByNetwork = false;

	public boolean waitingForPlayer = true;
	public boolean sendNetworkFrames = false;


	public boolean waitingForNetworkFrames = true;


}
