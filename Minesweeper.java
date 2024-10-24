import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//to represent the game Minesweeper
class MinesweeperWorld extends World {
  WorldScene currentWorld;
  Random rand;
  ArrayList<ArrayList<Cell>> cells;
  int height;
  int width;
  int cellSize;
  int mines;
  int clickedCells;
  boolean mineClicked;

  MinesweeperWorld(int width, int height, int mines, int cellSize, Random rand) {
    this.width = width;
    this.height = height;
    this.cellSize = cellSize;
    this.mines = mines;
    this.currentWorld = new WorldScene(width * cellSize, height * cellSize);
    this.rand = rand;
    this.cells = new ArrayList<ArrayList<Cell>>();
    this.clickedCells = 0;
    this.mineClicked = false;
  }

  MinesweeperWorld(int width, int height, int mines, int cellSize) {
    this(width, height, mines, cellSize, new Random());
  }

  // to update each time a cell is clicked on
  public void updateClickedCells() {
    this.clickedCells += 1;
  }

  // to draw the initial world
  public void initCurrentWorld() {
    for (int row = 0; row < this.cells.size(); row++) {
      for (int col = 0; col < this.cells.get(row).size(); col++) {
        this.cells.get(row).get(col).placeImage(this.currentWorld,
            col * this.cellSize + (this.cellSize / 2), row * this.cellSize + (this.cellSize / 2));
      }
    }
  }

  // to place the given cell's image on the screen
  public void updateImage(Cell cell) {
    for (int row = 0; row < this.height; row++) {
      int x = this.cells.get(row).indexOf(cell);
      if (x > -1) {
        cell.placeImage(currentWorld, x * this.cellSize + (this.cellSize / 2),
            row * this.cellSize + (this.cellSize / 2));
      }
    }
  }

  // to reveal all mines when game ends
  public void revealAllMines() {
    this.mineClicked = true;
    for (ArrayList<Cell> row : this.cells) {
      for (Cell cell : row) {
        if (cell.isMine()) {
          cell.imgClicked("LeftButton", this);
        }
      }
    }
  }

  // to display the current world on the screen
  public WorldScene makeScene() {
    return this.currentWorld;
  }

  // to update the game every tick
  public void onTick() {
    if (this.cells.size() == 0) {
      this.createCells();
      this.addMines();
      this.initCurrentWorld();
    } else if (this.clickedCells == this.width * this.height - this.mines) {
      this.lastScene("You Won!");
    } else if (this.mineClicked) {
      this.lastScene("Game Over");
    }
    this.makeScene();
  }

  // to end the world
  public WorldScene lastScene(String message) {
    this.currentWorld.placeImageXY(
        new TextImage(message, this.cellSize, FontStyle.BOLD, Color.BLACK),
        (this.width * this.cellSize) / 2, (this.height * this.cellSize) / 2);
    return this.currentWorld;

  }

  // to click on a given cell and change its image
  public void onMouseClicked(Posn pos, String buttonName) {
    int col = pos.x / this.cellSize;
    int row = pos.y / this.cellSize;
    if (row < this.width && col < this.height) {
      this.cells.get(row).get(col).imgClicked(buttonName, this);
    }
  }

  // to add cells to the cells matrix given this height and this width
  // also adds all neighbors to each cell
  void createCells() {
    for (int row = 0; row < this.height; row++) {
      this.cells.add(new ArrayList<Cell>());
      for (int col = 0; col < this.width; col++) {
        this.cells.get(row).add(new NumberedCell(new ArrayList<Cell>(), this.cellSize));
        Cell cell = this.cells.get(row).get(col);
        if (row != 0) {
          cell.addNeighbor(this.cells.get(row - 1).get(col));
        }
        if (col != 0) {
          cell.addNeighbor(this.cells.get(row).get(col - 1));
        }
        if (row != 0 && col != 0) {
          cell.addNeighbor(this.cells.get(row - 1).get(col - 1));
        }
        if (row != 0 && col != this.width - 1) {
          cell.addNeighbor(this.cells.get(row - 1).get(col + 1));
        }
      }
    }
  }

  // to add mines to the this cells matrix depending on the random field
  void addMines() {
    int currentMines = 0;
    while (currentMines < this.mines) {
      int x = this.rand.nextInt(this.width - 1);
      int y = this.rand.nextInt(this.height - 1);
      ArrayList<Cell> cellRow = this.cells.get(y);
      Cell selectedCell = cellRow.get(x);
      if (!selectedCell.isMine()) {
        cellRow.set(x, new Mine(selectedCell.neighbors, this.cellSize));
        for (Cell cell : cellRow.get(x).neighbors) {
          cell.replaceNeighbor(selectedCell, cellRow.get(x));
          cell.updateNumber();
        }
        currentMines++;
      }
    }
  }
}

//to represent a cell in the game MineSweeper
abstract class Cell {
  WorldImage standardCell;
  WorldImage flaggedCell;
  WorldImage emptyCell;
  ArrayList<Cell> neighbors;
  WorldImage currentImg;
  int length;
  boolean leftClicked;
  boolean rightClicked;

  Cell(ArrayList<Cell> neighbors, int length) {
    this.neighbors = neighbors;
    this.length = length;
    this.standardCell = new OverlayImage(new RectangleImage(length, length, "outline", Color.BLACK),
        new RectangleImage(length - 1, length - 1, "solid", Color.DARK_GRAY));
    this.currentImg = this.standardCell;
    this.flaggedCell = new OverlayImage(
        new ScaleImage(new FromFileImage("flag512.png"), 0.0013 * length), this.currentImg);
    this.emptyCell = new OverlayImage(new RectangleImage(length, length, "outline", Color.BLACK),
        new RectangleImage(length - 1, length - 1, "solid", Color.GRAY));
    this.leftClicked = false;
    this.rightClicked = false;
  }

  // to change current image and flood given buttonName and world
  // updates world with new image
  abstract void imgClicked(String buttonName, MinesweeperWorld world);

  // returns true if class is mine
  abstract boolean isMine();

  // to replace a neighbor given the targetCell and cell to replace that target
  void replaceNeighbor(Cell targetCell, Cell replaceCell) {
    if (this.neighbors.contains(targetCell)) {
      this.neighbors.set(this.neighbors.indexOf(targetCell), replaceCell);
    } else {
      throw new NoSuchElementException("Target cell was not found");
    }
  }

  // to update this cell's neighbor and given cell's neighbor
  void addNeighbor(Cell cell) {
    this.neighbors.add(cell);
    cell.neighbors.add(this);
  }

  // to add 1 to a numbered cell, if mine does nothing
  void updateNumber() {
    // this is empty because it delegates to the mine class and numbered cell class
    // this allows it to be called on all cells but only affects numbered cells
  }

  // to place current image on the given world at x and y
  void placeImage(WorldScene world, int x, int y) {
    world.placeImageXY(this.currentImg, x, y);
  }
}

//to represent a mine in the game MineSweeper
class Mine extends Cell {
  WorldImage mine;

  Mine(ArrayList<Cell> neighbors, int length) {
    super(neighbors, length);
    this.mine = new OverlayImage(new RectangleImage(length, length, "outline", Color.BLACK),
        new OverlayImage(new ScaleImage(new FromFileImage("mine512.png"), 0.0016 * length),
            new RectangleImage(this.length - 1, this.length - 1, "solid", Color.GRAY)));
  }

  // returns true if class is mine
  boolean isMine() {
    return true;
  }

  // to change current image and flood given buttonName and world
  // updates world with new image
  void imgClicked(String buttonName, MinesweeperWorld world) {
    if (!this.leftClicked && buttonName.equals("LeftButton")) {
      this.currentImg = this.mine;
      this.leftClicked = true;
      world.updateImage(this);
      world.revealAllMines();
    } else if (!this.rightClicked && !this.leftClicked && buttonName.equals("RightButton")) {
      this.currentImg = this.flaggedCell;
      world.updateImage(this);
      this.rightClicked = true;
    } else if (this.rightClicked && !this.leftClicked && buttonName.equals("RightButton")) {
      this.currentImg = this.standardCell;
      world.updateImage(this);
      this.rightClicked = false;
    }
  }

}

//to represent a numbered cell in the game Minesweeper
class NumberedCell extends Cell {
  int number;

  NumberedCell(ArrayList<Cell> neighbors, int length) {
    super(neighbors, length);

    this.number = 0;
  }

  // to add 1 to a numbered cell, if mine does nothing
  void updateNumber() {
    this.number += 1;
  }

  // returns true if class is mine
  boolean isMine() {
    return false;
  }

  // to change current image and flood given buttonName and world
  // updates world with new image
  void imgClicked(String buttonName, MinesweeperWorld world) {
    if (!this.leftClicked && buttonName.equals("LeftButton")) {
      if (this.number == 0) {
        this.currentImg = this.emptyCell;
        this.leftClicked = true;
        world.updateClickedCells();
        world.updateImage(this);
        for (Cell cell : this.neighbors) {
          if (!cell.isMine()) {
            cell.imgClicked(buttonName, world);
          }
        }
      } else {
        this.currentImg = new OverlayImage(new TextImage(Integer.toString(this.number),
            this.length / 2, FontStyle.BOLD, Color.BLUE), this.emptyCell);
        this.leftClicked = true;
        world.updateClickedCells();
        world.updateImage(this);
      }
    } else if (!this.rightClicked && !this.leftClicked && buttonName.equals("RightButton")) {
      this.currentImg = this.flaggedCell;
      world.updateImage(this);
      this.rightClicked = true;
    } else if (this.rightClicked && !this.leftClicked && buttonName.equals("RightButton")) {
      this.currentImg = this.standardCell;
      world.updateImage(this);
      this.rightClicked = false;
    }
  }
}

//to represent examples and tests of minesweeper
class ExampleMinesweeper {
  MinesweeperWorld world1;
  MinesweeperWorld world2;
  MinesweeperWorld world3;
  Mine mine1;
  Mine mine2;
  NumberedCell cell1;
  NumberedCell cell2;
  NumberedCell cell3;
  NumberedCell cell4;
  NumberedCell cell5;
  NumberedCell cell6;
  NumberedCell cell7;
  NumberedCell cell8;
  NumberedCell cell9;
  ArrayList<Cell> row1;
  ArrayList<Cell> row2;
  ArrayList<Cell> row3;

  // to initialize fields
  void init() {
    this.world1 = new MinesweeperWorld(3, 3, 2, 10, new Random(1));
    this.world2 = new MinesweeperWorld(2, 2, 1, 10, new Random(1));
    // used for testing set Random seed in bigbang
    this.world3 = new MinesweeperWorld(3, 3, 2, 100, new Random(1));
    this.mine1 = new Mine(new ArrayList<Cell>(), 10);
    this.mine2 = new Mine(new ArrayList<Cell>(), 10);
    this.cell1 = new NumberedCell(new ArrayList<Cell>(), 10);
    this.cell2 = new NumberedCell(new ArrayList<Cell>(), 10);
    this.cell3 = new NumberedCell(new ArrayList<Cell>(), 10);
    this.cell4 = new NumberedCell(new ArrayList<Cell>(), 10);
    this.cell5 = new NumberedCell(new ArrayList<Cell>(), 10);
    this.cell6 = new NumberedCell(new ArrayList<Cell>(), 10);
    this.cell7 = new NumberedCell(new ArrayList<Cell>(), 10);
    this.cell8 = new NumberedCell(new ArrayList<Cell>(), 10);
    this.cell9 = new NumberedCell(new ArrayList<Cell>(), 10);
  }

  // adding neighbors for testReplaceNeighbors
  void addCellNeighbors1() {
    this.mine1.addNeighbor(this.cell1);
    this.mine1.addNeighbor(this.cell2);
    this.mine1.addNeighbor(this.cell3);
    this.cell1.addNeighbor(this.cell2);
    this.cell1.addNeighbor(this.cell3);
    this.cell2.addNeighbor(this.cell3);
    this.cell2.addNeighbor(this.cell4);
    this.cell2.addNeighbor(this.cell5);
    this.cell3.addNeighbor(this.cell4);
    this.cell3.addNeighbor(this.cell5);
    this.cell4.addNeighbor(this.cell5);
  }

  // adding cell neighbors for testCreateCells()
  void addCellNeighbors2() {
    this.cell8.addNeighbor(this.cell9);
    this.cell1.addNeighbor(this.cell8);
    this.cell2.addNeighbor(this.cell9);
    this.cell2.addNeighbor(this.cell8);
    this.cell3.addNeighbor(this.cell8);
    this.cell3.addNeighbor(this.cell2);
    this.cell3.addNeighbor(this.cell9);
    this.cell3.addNeighbor(this.cell1);
    this.cell4.addNeighbor(this.cell1);
    this.cell4.addNeighbor(this.cell3);
    this.cell4.addNeighbor(this.cell8);
    this.cell5.addNeighbor(this.cell2);
    this.cell5.addNeighbor(this.cell3);
    this.cell6.addNeighbor(this.cell3);
    this.cell6.addNeighbor(this.cell5);
    this.cell6.addNeighbor(this.cell2);
    this.cell6.addNeighbor(this.cell4);
    this.cell7.addNeighbor(this.cell4);
    this.cell7.addNeighbor(this.cell6);
    this.cell7.addNeighbor(this.cell3);

    // creating rows that represent matrix of cells
    this.row1 = new ArrayList<Cell>(Arrays.asList(this.cell9, this.cell8, this.cell1));
    this.row2 = new ArrayList<Cell>(Arrays.asList(this.cell2, this.cell3, this.cell4));
    this.row3 = new ArrayList<Cell>(Arrays.asList(this.cell5, this.cell6, this.cell7));
  }

  // adding cell neighbors for testAddMines()
  void addCellNeighbors3() {
    this.mine2.addNeighbor(this.mine1);
    this.cell1.addNeighbor(this.mine2);
    this.cell2.addNeighbor(this.mine1);
    this.cell2.addNeighbor(this.mine2);
    this.cell3.addNeighbor(this.mine2);
    this.cell3.addNeighbor(this.cell2);
    this.cell3.addNeighbor(this.mine1);
    this.cell3.addNeighbor(this.cell1);
    this.cell4.addNeighbor(this.cell1);
    this.cell4.addNeighbor(this.cell3);
    this.cell4.addNeighbor(this.mine2);
    this.cell5.addNeighbor(this.cell2);
    this.cell5.addNeighbor(this.cell3);
    this.cell6.addNeighbor(this.cell3);
    this.cell6.addNeighbor(this.cell5);
    this.cell6.addNeighbor(this.cell2);
    this.cell6.addNeighbor(this.cell4);
    this.cell7.addNeighbor(this.cell4);
    this.cell7.addNeighbor(this.cell6);
    this.cell7.addNeighbor(this.cell3);

    // updating number for cells next to mines
    this.cell1.updateNumber();
    this.cell2.updateNumber();
    this.cell2.updateNumber();
    this.cell3.updateNumber();
    this.cell3.updateNumber();
    this.cell4.updateNumber();

    // creating rows that represent matrix of cells
    this.row1 = new ArrayList<Cell>(Arrays.asList(this.mine1, this.mine2, this.cell1));
    this.row2 = new ArrayList<Cell>(Arrays.asList(this.cell2, this.cell3, this.cell4));
    this.row3 = new ArrayList<Cell>(Arrays.asList(this.cell5, this.cell6, this.cell7));
  }

  // to test addNeighbors in Cell
  void testAddNeighbors(Tester t) {
    this.init();
    t.checkExpect(this.cell1.neighbors, new ArrayList<Cell>());
    t.checkExpect(this.cell2.neighbors, new ArrayList<Cell>());
    this.cell1.addNeighbor(this.cell2);
    t.checkExpect(this.cell1.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell2)));
    t.checkExpect(this.cell2.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell1)));

    t.checkExpect(this.mine1.neighbors, new ArrayList<Cell>());
    this.mine1.addNeighbor(cell1);
    t.checkExpect(this.mine1.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell1)));
    t.checkExpect(this.cell1.neighbors, new ArrayList<Cell>(Arrays.asList(this.cell2, this.mine1)));
  }

  // to test replaceNeighbor in Cell
  void testReplaceNeighbor(Tester t) {
    this.init();
    this.addCellNeighbors1();
    t.checkExpect(this.cell1.neighbors,
        new ArrayList<Cell>(Arrays.asList(this.mine1, this.cell2, this.cell3)));
    this.cell1.replaceNeighbor(this.mine1, this.cell4);
    t.checkExpect(this.cell1.neighbors,
        new ArrayList<Cell>(Arrays.asList(this.cell4, this.cell2, this.cell3)));

    t.checkExpect(this.mine1.neighbors,
        new ArrayList<Cell>(Arrays.asList(this.cell1, this.cell2, this.cell3)));
    this.mine1.replaceNeighbor(this.cell1, this.cell5);
    t.checkExpect(this.mine1.neighbors,
        new ArrayList<Cell>(Arrays.asList(this.cell5, this.cell2, this.cell3)));

    t.checkException(new NoSuchElementException("Target cell was not found"), this.mine1,
        "replaceNeighbor", this.cell4, this.cell1);
  }

  // to test placeImage in Cell
  void testPlaceImage(Tester t) {
    this.init();
    t.checkExpect(this.world1.currentWorld, new WorldScene(30, 30));
    this.cell1.placeImage(this.world1.currentWorld, 10, 10);
    WorldScene newWorldScene = new WorldScene(30, 30);
    newWorldScene.placeImageXY(this.cell1.standardCell, 10, 10);
    t.checkExpect(this.world1.currentWorld, newWorldScene);
  }

  // to test updateNumber in Cell
  void testUpdateNumber(Tester t) {
    this.init();
    t.checkExpect(this.cell1.number, 0);
    this.cell1.updateNumber();
    t.checkExpect(this.cell1.number, 1);
    this.cell1.updateNumber();
    t.checkExpect(this.cell1.number, 2);
  }

  // to test isMine in Cell
  boolean testIsMine(Tester t) {
    this.init();
    return t.checkExpect(this.cell1.isMine(), false) && t.checkExpect(this.mine1.isMine(), true);
  }

  // to test imgClicked in Cell
  void testImgClicked(Tester t) {
    this.init();
    this.addCellNeighbors1();
    this.world1.createCells();
    this.cell2.updateNumber();
    this.cell3.updateNumber();

    // test if clicking a numbered cell reveals its number
    t.checkExpect(this.cell2.currentImg, this.cell2.standardCell);
    this.cell2.imgClicked("LeftButton", this.world1);
    t.checkExpect(this.cell2.currentImg,
        new OverlayImage(
            new TextImage(Integer.toString(1), this.cell2.length / 2, FontStyle.BOLD, Color.BLUE),
            this.cell2.emptyCell));

    // test ability to flag a cell and unflag it
    t.checkExpect(this.cell1.currentImg, this.cell1.standardCell);
    t.checkExpect(this.cell1.rightClicked, false);
    this.cell1.imgClicked("RightButton", this.world1);
    t.checkExpect(this.cell1.currentImg, this.cell1.flaggedCell);
    t.checkExpect(this.cell1.rightClicked, true);
    this.cell1.imgClicked("RightButton", this.world1);
    t.checkExpect(this.cell1.currentImg, this.cell1.standardCell);
    t.checkExpect(this.cell1.rightClicked, false);

    // test ability to click on empty cell
    t.checkExpect(this.cell1.leftClicked, false);
    this.cell1.imgClicked("LeftButton", this.world1);
    t.checkExpect(this.cell1.currentImg, this.cell1.emptyCell);
    t.checkExpect(this.cell1.leftClicked, true);
    this.cell1.imgClicked("LeftButton", this.world1);
    t.checkExpect(this.cell1.currentImg, this.cell1.emptyCell);

    // test that right clicking an already clicked cell will not change it
    this.cell1.imgClicked("RightButton", this.world1);
    t.checkExpect(this.cell1.currentImg, this.cell1.emptyCell);

    // test ability to flag and unflag mine
    t.checkExpect(this.mine1.currentImg, this.mine1.standardCell);
    t.checkExpect(this.mine1.rightClicked, false);
    this.mine1.imgClicked("RightButton", this.world1);
    t.checkExpect(this.mine1.currentImg, this.mine1.flaggedCell);
    t.checkExpect(this.mine1.rightClicked, true);
    this.mine1.imgClicked("RightButton", this.world1);
    t.checkExpect(this.mine1.currentImg, this.mine1.standardCell);
    t.checkExpect(this.mine1.rightClicked, false);

    // test that clicking on mine reveals mine
    t.checkExpect(this.mine1.leftClicked, false);
    t.checkExpect(this.mine1.currentImg, this.mine1.standardCell);
    this.mine1.imgClicked("LeftButton", this.world1);
    t.checkExpect(this.mine1.currentImg, this.mine1.mine);
    t.checkExpect(this.mine1.leftClicked, true);

    // test that revealed mine cannot be flagged
    this.mine1.imgClicked("RightButton", this.world1);
    t.checkExpect(this.mine1.currentImg, this.mine1.mine);

    // test flood effect by clicking one cell that's neighboring another
    t.checkExpect(this.cell4.currentImg, this.cell4.standardCell);
    t.checkExpect(this.cell5.currentImg, this.cell5.standardCell);
    t.checkExpect(this.cell4.leftClicked, false);
    t.checkExpect(this.cell5.leftClicked, false);
    this.cell4.imgClicked("LeftButton", this.world1);
    t.checkExpect(this.cell4.currentImg, this.cell4.emptyCell);
    t.checkExpect(this.cell5.currentImg, this.cell5.emptyCell);
    t.checkExpect(this.cell4.leftClicked, true);
    t.checkExpect(this.cell5.leftClicked, true);
  }

  // to test updateClickedCells in MinesweeperWorld
  void testUpdateClickedCells(Tester t) {
    this.init();
    t.checkExpect(this.world1.clickedCells, 0);
    this.world1.updateClickedCells();
    t.checkExpect(this.world1.clickedCells, 1);
    this.world1.updateClickedCells();
    t.checkExpect(this.world1.clickedCells, 2);
  }

  // to test initCurrentWorld in MinesweeperWorld
  void testInitCurrentWorld(Tester t) {
    this.init();
    this.world1.createCells();
    this.world1.addMines();
    t.checkExpect(this.world1.currentWorld, new WorldScene(30, 30));
    WorldScene worldAfterInit = this.world1.currentWorld;
    worldAfterInit.placeImageXY(this.mine1.currentImg, 5, 5);
    worldAfterInit.placeImageXY(this.mine2.currentImg, 5, 15);
    worldAfterInit.placeImageXY(this.cell1.currentImg, 5, 25);
    worldAfterInit.placeImageXY(this.cell2.currentImg, 15, 5);
    worldAfterInit.placeImageXY(this.cell3.currentImg, 15, 15);
    worldAfterInit.placeImageXY(this.cell4.currentImg, 15, 25);
    worldAfterInit.placeImageXY(this.cell5.currentImg, 25, 5);
    worldAfterInit.placeImageXY(this.cell6.currentImg, 25, 15);
    worldAfterInit.placeImageXY(this.cell7.currentImg, 25, 25);
    this.world1.initCurrentWorld();
    t.checkExpect(this.world1.currentWorld, worldAfterInit);
  }

  // to test createCells in MinesweeperWorld
  void testCreateCells(Tester t) {
    this.init();
    this.addCellNeighbors2();
    t.checkExpect(this.world1.cells, new ArrayList<ArrayList<Cell>>());
    this.world1.createCells();
    t.checkExpect(this.world1.cells,
        new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row1, this.row2, this.row3)));
  }

  // to test addMines in MinesweeperWorld
  void testAddMines(Tester t) {
    this.init();
    this.addCellNeighbors2();
    this.world1.createCells();
    t.checkExpect(this.world1.cells,
        new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row1, this.row2, this.row3)));
    this.init();
    this.addCellNeighbors3();
    this.world1.createCells();
    this.world1.addMines();
    t.checkExpect(this.world1.cells,
        new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row1, this.row2, this.row3)));
  }

  // to test makeScene in MinesweeperWorld
  boolean testMakeScene(Tester t) {
    this.init();
    return t.checkExpect(this.world1.makeScene(), this.world1.currentWorld)
        && t.checkExpect(this.world2.makeScene(), this.world2.currentWorld);
  }

  // to test lastScene in MinesweeperWorld
  boolean testLastScene(Tester t) {
    this.init();
    this.world1.currentWorld.placeImageXY(
        new TextImage("Game Over", this.world1.cellSize, FontStyle.BOLD, Color.BLACK),
        (this.world1.width * this.world1.cellSize) / 2,
        (this.world1.height * this.world1.cellSize) / 2);
    WorldScene newWorldScene = this.world1.currentWorld;
    this.world2.currentWorld.placeImageXY(
        new TextImage("You Won!", this.world2.cellSize, FontStyle.BOLD, Color.BLACK),
        (this.world2.width * this.world2.cellSize) / 2,
        (this.world2.height * this.world2.cellSize) / 2);
    WorldScene newWorldScene1 = this.world2.currentWorld;
    return t.checkExpect(this.world1.lastScene("Game Over"), newWorldScene)
        && t.checkExpect(this.world2.lastScene("You Won!"), newWorldScene1);
  }

  // to test onMouseClicked in MinesweeperWorld
  void testOnMouseClicked(Tester t) {
    this.init();
    this.addCellNeighbors3();
    this.world1.createCells();
    this.world1.addMines();
    this.world1.initCurrentWorld();
    // check if mine image changes if left clicked on
    t.checkExpect(this.world1.cells.get(0).get(0).currentImg, this.mine1.standardCell);
    this.world1.onMouseClicked(new Posn(5, 5), "LeftButton");
    t.checkExpect(this.world1.cells.get(0).get(0).currentImg, this.mine1.mine);

    // check that a numbered cell changes if left clicked on
    t.checkExpect(this.world1.cells.get(0).get(2).currentImg, this.cell1.standardCell);
    this.world1.onMouseClicked(new Posn(21, 5), "LeftButton");
    t.checkExpect(this.world1.cells.get(0).get(2).currentImg,
        new OverlayImage(
            new TextImage(Integer.toString(1), this.cell1.length / 2, FontStyle.BOLD, Color.BLUE),
            this.cell1.emptyCell));

    this.init();
    this.addCellNeighbors3();
    this.world1.createCells();
    this.world1.addMines();
    this.world1.initCurrentWorld();
    // check for flagging and unflagging of mines and normal cells
    t.checkExpect(this.world1.cells.get(0).get(1).currentImg, this.mine2.standardCell);
    this.world1.onMouseClicked(new Posn(11, 5), "RightButton");
    t.checkExpect(this.world1.cells.get(0).get(1).currentImg, this.mine2.flaggedCell);
    this.world1.onMouseClicked(new Posn(11, 5), "RightButton");
    t.checkExpect(this.world1.cells.get(0).get(1).currentImg, this.mine2.standardCell);

    t.checkExpect(this.world1.cells.get(1).get(1).currentImg, this.cell3.standardCell);
    this.world1.onMouseClicked(new Posn(11, 15), "RightButton");
    t.checkExpect(this.world1.cells.get(1).get(1).currentImg, this.cell3.flaggedCell);
    this.world1.onMouseClicked(new Posn(11, 15), "RightButton");
    t.checkExpect(this.world1.cells.get(1).get(1).currentImg, this.cell3.standardCell);

    // check that flood changes neighbors
    t.checkExpect(this.world1.cells.get(2).get(0).currentImg, this.cell5.standardCell);
    t.checkExpect(this.world1.cells.get(2).get(1).currentImg, this.cell6.standardCell);
    t.checkExpect(this.world1.cells.get(2).get(2).currentImg, this.cell7.standardCell);
    this.world1.onMouseClicked(new Posn(5, 24), "LeftButton");
    t.checkExpect(this.world1.cells.get(2).get(0).currentImg, this.cell5.emptyCell);
    t.checkExpect(this.world1.cells.get(2).get(1).currentImg, this.cell6.emptyCell);
    t.checkExpect(this.world1.cells.get(2).get(2).currentImg, this.cell7.emptyCell);
  }

  // to test revealAllMines in MinesweeperWorld
  void testRevealAllMines(Tester t) {
    this.init();
    this.addCellNeighbors3();
    this.world1.createCells();
    this.world1.addMines();
    this.world1.initCurrentWorld();
    t.checkExpect(this.world1.cells,
        new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row1, this.row2, this.row3)));
    t.checkExpect(this.world1.cells.get(0).get(0).currentImg, this.mine1.standardCell);
    t.checkExpect(this.world1.cells.get(0).get(1).currentImg, this.mine2.standardCell);
    t.checkExpect(this.world1.mineClicked, false);
    this.world1.revealAllMines();
    t.checkExpect(this.world1.cells.get(0).get(0).currentImg, this.mine1.mine);
    t.checkExpect(this.world1.cells.get(0).get(1).currentImg, this.mine2.mine);
    t.checkExpect(this.world1.mineClicked, true);
  }

  // to test updateImage in MinesweeperWorld
  void testUpdateImage(Tester t) {
    this.init();
    this.addCellNeighbors3();
    this.world1.createCells();
    this.world1.addMines();
    this.world1.initCurrentWorld();
    t.checkExpect(this.world1.cells.get(0).get(0).currentImg, this.mine1.standardCell);
    this.mine1.imgClicked("LeftButton", this.world1);
    this.world1.updateImage(this.mine1);
    t.checkExpect(this.world1.cells.get(0).get(0).currentImg, this.mine1.mine);
  }

  // to test onTick in MinesweeperWorld
  void testOnTick(Tester t) {
    // test that onTick() calls the correct methods on init
    this.init();
    this.addCellNeighbors3();
    t.checkExpect(this.world1.cells, new ArrayList<ArrayList<Cell>>());
    this.world1.onTick();
    t.checkExpect(this.world1.cells,
        new ArrayList<ArrayList<Cell>>(Arrays.asList(this.row1, this.row2, this.row3)));

    // test that game ends after mine is clicked
    WorldScene worldAfterMineClicked = this.world1.currentWorld;
    worldAfterMineClicked.placeImageXY(
        new TextImage("Game Over", this.world1.cellSize, FontStyle.BOLD, Color.BLACK),
        (this.world1.width * this.world1.cellSize) / 2,
        (this.world1.height * this.world1.cellSize) / 2);
    this.world1.mineClicked = true;
    this.world1.onTick();
    t.checkExpect(this.world1.currentWorld, worldAfterMineClicked);

    this.init();
    this.addCellNeighbors3();

    // test that games end after all cells are clicked
    WorldScene worldAfterAllCellsClicked = this.world1.currentWorld;
    worldAfterAllCellsClicked.placeImageXY(
        new TextImage("You Won!", this.world1.cellSize, FontStyle.BOLD, Color.BLACK),
        (this.world1.width * this.world1.cellSize) / 2,
        (this.world1.height * this.world1.cellSize) / 2);
    this.world1.clickedCells = 7;
    this.world1.onTick();
    t.checkExpect(this.world1.currentWorld, worldAfterAllCellsClicked);
  }

  // to run MinesweeperWorld
  void testBigBang(Tester t) {
    int width = 10;
    int height = 10;
    int mines = 15;
    int cellSize = 50;
    World minesweeper = new MinesweeperWorld(width, height, mines, cellSize);
    int worldWidth = cellSize * width;
    int worldHeight = cellSize * height;
    double tickRate = 0.1;
    minesweeper.bigBang(worldWidth, worldHeight, tickRate);

  }
}
