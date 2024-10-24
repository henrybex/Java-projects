
import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//to represent the LightEmAll Game World
class LightEmAll extends World {
  // a list of rows of GamePieces,
  // i.e., represents the board in row-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  // to display the current world image
  WorldScene currentWorld;
  // to randomize the selection and rotation of game pieces
  Random rand;
  // scales images by this size
  int pixelSize;

  // to make world with random seed
  LightEmAll(int pixelSize, int width, int height, int powerRow, int powerCol, Random rand) {
    this.radius = 10;
    this.pixelSize = pixelSize;
    this.width = width;
    this.height = height;
    this.powerRow = powerRow;
    this.powerCol = powerCol;
    this.rand = rand;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.currentWorld = new WorldScene(this.width * this.pixelSize, this.height * this.pixelSize);
    this.createRandomBoard();
    this.createRandomEdges();
    this.modifyBoard();
    this.rotateGamePieces();
    this.createEdges();
    this.board.get(this.powerRow).get(this.powerCol).updateConnected(this);
    this.drawBoard();
  }

  // FOR PART 1: to make world for testing purposes
  LightEmAll(int width, int height, boolean test, Random rand) {
    this.radius = 10;
    this.pixelSize = 50;
    this.width = width;
    this.height = height;
    this.powerRow = height / 2;
    this.powerCol = width / 2;
    this.rand = rand;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.currentWorld = new WorldScene(this.width * this.pixelSize, this.height * this.pixelSize);
    if (!test) {
      this.createBoard();
      this.rotateGamePieces();
      this.createEdges();
      this.board.get(this.powerRow).get(this.powerCol).updateConnected(this);
      this.drawBoard();
    }
  }

  // FOR PART 2: to make world for testing purposes
  LightEmAll(int width, int height, boolean test, Random rand, boolean part2) {
    this.radius = 10;
    this.pixelSize = 50;
    this.width = width;
    this.height = height;
    this.powerRow = 0;
    this.powerCol = 0;
    this.rand = rand;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.currentWorld = new WorldScene(this.width * this.pixelSize, this.height * this.pixelSize);
    if (!test) {
      this.createRandomBoard();
      this.createRandomEdges();
      this.modifyBoard();
      this.rotateGamePieces();
      this.createEdges();
      this.board.get(this.powerRow).get(this.powerCol).updateConnected(this);
      this.drawBoard();
    }
  }

  // to be used as default constructor
  LightEmAll(int width, int height) {
    this(50, width, height, 0, 0, new Random());
  }

  // to display last scene
  public WorldScene lastScene(String msg) {
    this.currentWorld.placeImageXY(new OverlayImage(
        new TextImage(msg, this.pixelSize / 2, FontStyle.BOLD, Color.white),
        new RectangleImage(this.pixelSize * 4, this.pixelSize * 2, OutlineMode.SOLID, Color.black)),
        (width * pixelSize) / 2, (height * pixelSize) / 2);
    return this.currentWorld;
  }

  // to return the currentWorld image
  public WorldScene makeScene() {
    return this.currentWorld;
  }

  // to update the game world
  public void onTick() {
    this.makeScene();
  }

  // to handle mouse clicks and update where power station is placed
  public void onMouseClicked(Posn pos, String buttonName) {
    int row = pos.y / this.pixelSize;
    int col = pos.x / this.pixelSize;
    if (row < this.height && row >= 0 && col < this.width && col >= 0
        && buttonName.equals("LeftButton")) {
      this.board.get(row).get(col).rotate(this);
      this.createEdges();
      this.board.get(this.powerRow).get(this.powerCol).updateConnected(this);
    }
  }

  // to handle key presses
  public void onKeyReleased(String key) {
    if (key.equals("up") && this.powerRow != 0) {
      this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
      this.powerRow--;
      this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
    } else if (key.equals("down") && this.powerRow != this.height - 1) {
      this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
      this.powerRow++;
      this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
    } else if (key.equals("left") && this.powerCol != 0) {
      this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
      this.powerCol--;
      this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
    } else if (key.equals("right") && this.powerCol != this.width - 1) {
      this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
      this.powerCol++;
      this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
    }
    this.board.get(this.powerRow).get(this.powerCol).updateConnected(this);
  }

  // to create game pieces
  public void createBoard() {
    for (int row = 0; row < this.height; row++) {
      this.board.add(new ArrayList<GamePiece>());
      for (int col = 0; col < this.width; col++) {
        if (row != this.width / 2) {
          this.board.get(row)
              .add(new GamePiece(this.pixelSize, row, col, true, true, false, false, this.radius));
        } else {
          this.board.get(row)
              .add(new GamePiece(this.pixelSize, row, col, true, true, true, true, this.radius));
        }
        this.nodes.add(this.board.get(row).get(col));
      }
    }
    this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
  }

  // to create edges between nodes
  public void createEdges() {
    for (int idx = 0; idx < this.nodes.size(); idx++) {
      this.nodes.get(idx).createEdges(this);
    }
  }

  // to rotate every game piece 1 to 4 times
  public void rotateGamePieces() {
    for (int idx = 0; idx < this.nodes.size(); idx++) {
      for (int randNum = this.rand.nextInt(4); randNum >= 0; randNum--) {
        this.nodes.get(idx).rotate(this);
      }
    }
  }

  // to draw the game board
  public void drawBoard() {
    for (int idx = 0; idx < this.nodes.size(); idx++) {
      this.updateGamePiece(this.nodes.get(idx));
    }
  }

  // to update specified game piece image
  public void updateGamePiece(GamePiece gp) {
    this.currentWorld.placeImageXY(gp.currentImg, (gp.col * this.pixelSize) + (this.pixelSize / 2),
        (gp.row * this.pixelSize) + (this.pixelSize / 2));
  }

  // to create a board of empty gamepieces
  public void createRandomBoard() {
    for (int row = 0; row < this.height; row++) {
      this.board.add(new ArrayList<GamePiece>());
      for (int col = 0; col < this.width; col++) {
        this.board.get(row)
            .add(new GamePiece(this.pixelSize, row, col, false, false, false, false, this.radius));
        this.nodes.add(this.board.get(row).get(col));
      }
    }
    this.board.get(this.powerRow).get(this.powerCol).updatePowerStation(this);
  }

  // to generate edges between all gamepieces with random weights
  // also adds each gamepieces posn to representatives for kruskal's algorithm
  // each gamepiece is by default its own representative
  public void createRandomEdges() {
    HashMap<Posn, Posn> representatives = new HashMap<Posn, Posn>();
    ArrayList<Edge> allEdges = new ArrayList<Edge>();
    for (int row = 0; row < this.height; row++) {
      for (int col = 0; col < this.width; col++) {
        if (row != this.height - 1) {
          allEdges.add(new Edge(this.board.get(row).get(col), this.board.get(row + 1).get(col),
              this.rand.nextInt(100)));
        }
        if (col != this.width - 1) {
          allEdges.add(new Edge(this.board.get(row).get(col), this.board.get(row).get(col + 1),
              this.rand.nextInt(100)));
        }
        representatives.put(new Posn(row, col), new Posn(row, col));
      }
    }
    this.kruskalAlgorithm(representatives, allEdges);
  }

  // using kruskal's algorithm, sorts all edges in increasing order by weight
  // creates a minimum spanning tree of edges where there are n - 1 edges for n
  // gamepieces
  public void kruskalAlgorithm(HashMap<Posn, Posn> representatives, ArrayList<Edge> allEdges) {
    Collections.sort(allEdges, new SortByWeight());
    for (Edge e : allEdges) {
      Posn from = new Posn(e.from.row, e.from.col);
      Posn to = new Posn(e.to.row, e.to.col);
      if (this.find(representatives, from).equals(this.find(representatives, to))) {
        // discard edge
      } else {
        this.mst.add(e);
        representatives.replace(this.find(representatives, from), this.find(representatives, to));
      }
    }
  }

  // recursively finds the given posn in a hashmap of representatives
  // used for kruskal's algorithm
  public Posn find(HashMap<Posn, Posn> reps, Posn pos) {
    if (reps.get(pos).equals(pos)) {
      return pos;
    } else {
      return this.find(reps, reps.get(pos));
    }
  }

  // cycles through every edge in the minimum spanning tree and updates the
  // booleans
  // for every game piece
  public void modifyBoard() {
    for (Edge e : this.mst) {
      int fromRow = e.from.row;
      int fromCol = e.from.col;
      int toRow = e.to.row;
      int toCol = e.to.col;
      if (fromRow < toRow) {
        this.board.get(fromRow).get(fromCol).modifyDirection("bottom");
        this.board.get(toRow).get(toCol).modifyDirection("top");
      } else if (fromCol < toCol) {
        this.board.get(fromRow).get(fromCol).modifyDirection("right");
        this.board.get(toRow).get(toCol).modifyDirection("left");
      }
    }
  }
}

//to represent a game piece in LightEmAll World
class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is connected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;
  // power amount is between 0 and 9 inclusive, 0 being least and 9 being most
  int powerAmount;
  // displays current image
  WorldImage currentImg;
  // scales images by this size
  int pixelSize;
  // contains edges for which
  ArrayList<Edge> outEdges;
  // determines the radius for the power station
  int worldRadius;

  // game piece with no power
  GamePiece(int pixelSize, int row, int col, boolean left, boolean right, boolean top,
      boolean bottom, int worldRadius) {
    this.pixelSize = pixelSize;
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = false;
    this.powerAmount = 0;
    this.worldRadius = worldRadius;
    this.currentImg = this.createImage();
    this.outEdges = new ArrayList<Edge>();
  }

  // to set the given direction to true
  public void modifyDirection(String direction) {
    if (direction.equals("left")) {
      this.left = true;
    } else if (direction.equals("right")) {
      this.right = true;
    } else if (direction.equals("top")) {
      this.top = true;
    } else if (direction.equals("bottom")) {
      this.bottom = true;
    }
  }

  // to switch powerstation and update power amount and its image
  public void updatePowerStation(LightEmAll world) {
    this.powerStation = !this.powerStation;
    if (this.powerStation) {
      this.updatePowered(world, this.worldRadius);
    }
    this.currentImg = this.createImage();
    world.updateGamePiece(this);
  }

  // to update powered with the given power amount and update image
  public void updatePowered(LightEmAll world, int powerAmount) {
    this.powerAmount = powerAmount;
    this.currentImg = this.createImage();
    world.updateGamePiece(this);
  }

  // to get corresponding image given the game piece
  public WorldImage createImage() {
    String key = "";
    if (this.left) {
      key += "L";
    }
    if (this.right) {
      key += "R";
    }
    if (this.top) {
      key += "T";
    }
    if (this.bottom) {
      key += "B";
    }
    if (this.powerStation) {
      return new OverlayImage(
          new OverlayImage(
              new StarImage(this.pixelSize / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
              new StarImage(this.pixelSize / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
          new ScaleImage(new FrameImage(
              new FromFileImage(key + (this.powerAmount * 4 - 1) / this.worldRadius + ".png"),
              Color.white), this.pixelSize / 32.0));

    } else {
      return new ScaleImage(new FrameImage(
          new FromFileImage(key + (this.powerAmount * 4 - 1) / this.worldRadius + ".png"),
          Color.white), this.pixelSize / 32.0);

    }
  }

  // to rotate image clockwise
  public void rotate(LightEmAll world) {
    boolean tempTop = this.top;
    boolean tempRight = this.right;
    boolean tempBottom = this.bottom;
    boolean tempLeft = this.left;
    this.top = tempLeft;
    this.right = tempTop;
    this.bottom = tempRight;
    this.left = tempBottom;
    this.currentImg = this.createImage();
    world.updateGamePiece(this);
  }

  // to create edges for this node based on boolean fields
  public void createEdges(LightEmAll world) {
    this.outEdges.removeAll(this.outEdges);
    if (this.top && row != 0) {
      GamePiece topGP = world.board.get(this.row - 1).get(this.col);
      if (topGP.bottom) {
        this.outEdges.add(new Edge(this, topGP, 0));
      }
    }
    if (this.bottom && row != world.height - 1) {
      GamePiece bottomGP = world.board.get(this.row + 1).get(this.col);
      if (bottomGP.top) {
        this.outEdges.add(new Edge(this, bottomGP, 0));
      }
    }
    if (this.left && col != 0) {
      GamePiece leftGP = world.board.get(this.row).get(this.col - 1);
      if (leftGP.right) {
        this.outEdges.add(new Edge(this, leftGP, 0));
      }
    }
    if (this.right && col != world.height - 1) {
      GamePiece rightGP = world.board.get(this.row).get(this.col + 1);
      if (rightGP.left) {
        this.outEdges.add(new Edge(this, rightGP, 0));
      }
    }
  }

  // to update all edges of this node using bfs with power
  public void updateConnected(LightEmAll world) {
    ArrayList<GamePiece> worklist = new ArrayList<GamePiece>(Arrays.asList(this));
    ArrayList<GamePiece> alreadySeen = new ArrayList<GamePiece>();
    int powerAmount = this.worldRadius;
    boolean finishGame = true;
    while (!worklist.isEmpty()) {
      GamePiece gp = worklist.remove(0);
      if (alreadySeen.contains(gp)) {
        // already seen do nothing
      } else {
        for (Edge e : gp.outEdges) {
          worklist.add(e.to);
          if (!alreadySeen.contains(e.to)) {
            e.to.updatePowered(world, powerAmount);
          }
        }
        alreadySeen.add(gp);
      }
    }
    for (GamePiece gamePiece : world.nodes) {
      if (!alreadySeen.contains(gamePiece)) {
        gamePiece.updatePowered(world, 0);
        finishGame = false;
      }
    }
    if (finishGame) {
      world.lastScene("You Won!");
    }
  }
}

//to represent a weighted connection between GamePieces in a graph
class Edge {
  GamePiece from;
  GamePiece to;
  int weight;

  Edge(GamePiece from, GamePiece to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }
}

class SortByWeight implements Comparator<Edge> {

  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }

}

class ExampleLightEmAll {
  LightEmAll world1;
  LightEmAll world2;
  LightEmAll world3;
  GamePiece LRTB;
  GamePiece TB;
  GamePiece LR;
  Edge edge1;
  Edge edge2;

  // to initialize examples
  void init() {
    this.world1 = new LightEmAll(3, 3, true, new Random(1));
    this.world2 = new LightEmAll(3, 3, false, new Random(1));
    this.world3 = new LightEmAll(2, 2, true, new Random(1));
    this.LRTB = new GamePiece(50, 1, 1, true, true, true, true, 10);
    this.TB = new GamePiece(50, 0, 1, false, false, true, true, 10);
    this.LR = new GamePiece(50, 2, 1, true, true, false, false, 10);
    this.edge1 = new Edge(this.TB, this.LR, 0);
    this.edge2 = new Edge(this.TB, this.LRTB, 0);
  }

  // to test makeScene in LightEmAll
  boolean testMakeScene(Tester t) {
    this.init();
    return t.checkExpect(this.world1.makeScene(), new WorldScene(150, 150))
        && t.checkExpect(this.world2.makeScene(), this.world2.currentWorld);
  }

  // to test onMouseClicked in LightEmAll
  void testOnMouseClicked(Tester t) {
    this.init();
    t.checkExpect(this.world2.board.get(2).get(0).currentImg, LR.currentImg);
    t.checkExpect(this.world2.board.get(2).get(0).left, true);
    t.checkExpect(this.world2.board.get(2).get(0).right, true);
    t.checkExpect(this.world2.board.get(2).get(0).top, false);
    t.checkExpect(this.world2.board.get(2).get(0).bottom, false);
    this.world2.onMouseClicked(new Posn(10, 102), "LeftButton");
    t.checkExpect(this.world2.board.get(2).get(0).left, false);
    t.checkExpect(this.world2.board.get(2).get(0).right, false);
    t.checkExpect(this.world2.board.get(2).get(0).top, true);
    t.checkExpect(this.world2.board.get(2).get(0).bottom, true);
    t.checkExpect(this.world2.board.get(2).get(0).currentImg,
        new ScaleImage(new FrameImage(new FromFileImage("TB" + 3 + ".png"), Color.white),
            this.TB.pixelSize / 32.0));

    t.checkExpect(this.world2.board.get(1).get(1).currentImg,
        new OverlayImage(new OverlayImage(
            new StarImage(this.LRTB.pixelSize / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
            new StarImage(this.LRTB.pixelSize / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
            new ScaleImage(new FrameImage(new FromFileImage("LRTB" + 3 + ".png"), Color.white),
                this.LRTB.pixelSize / 32.0)));
    t.checkExpect(this.world2.board.get(1).get(1).left, true);
    t.checkExpect(this.world2.board.get(1).get(1).right, true);
    t.checkExpect(this.world2.board.get(1).get(1).top, true);
    t.checkExpect(this.world2.board.get(1).get(1).bottom, true);
    this.world2.onMouseClicked(new Posn(52, 52), "LeftButton");
    t.checkExpect(this.world2.board.get(1).get(1).left, true);
    t.checkExpect(this.world2.board.get(1).get(1).right, true);
    t.checkExpect(this.world2.board.get(1).get(1).top, true);
    t.checkExpect(this.world2.board.get(1).get(1).bottom, true);
    t.checkExpect(this.world2.board.get(1).get(1).currentImg,
        new OverlayImage(new OverlayImage(
            new StarImage(this.LRTB.pixelSize / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
            new StarImage(this.LRTB.pixelSize / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
            new ScaleImage(new FrameImage(new FromFileImage("LRTB" + 3 + ".png"), Color.white),
                this.LRTB.pixelSize / 32.0)));
  }

  // to test rotateGamePieces
  void testRotateGamePieces(Tester t) {
    this.init();
    this.world1.createBoard();
    this.world1.rotateGamePieces();
    GamePiece power = new GamePiece(50, 1, 1, true, true, true, true, 10);
    power.updatePowerStation(this.world3);
    t.checkExpect(this.world1.board,
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 0, 0, false, false, true, true, 10),
                    new GamePiece(50, 0, 1, false, false, true, true, 10),
                    new GamePiece(50, 0, 2, true, true, false, false, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 1, 0, true, true, true, true, 10), power,
                    new GamePiece(50, 1, 2, true, true, true, true, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 2, 0, true, true, false, false, 10),
                    new GamePiece(50, 2, 1, false, false, true, true, 10),
                    new GamePiece(50, 2, 2, true, true, false, false, 10))))));
  }

  // to test updatePowerStation
  void testUpdatePowerStation(Tester t) {
    this.init();
    t.checkExpect(this.LR.currentImg,
        new ScaleImage(new FrameImage(
            new FromFileImage("LR" + (this.LR.powerAmount * 4 - 1) / this.LR.worldRadius + ".png"),
            Color.white), this.LR.pixelSize / 32.0));
    t.checkExpect(this.LR.powerStation, false);
    t.checkExpect(this.LR.powerAmount, 0);
    // adds power station
    this.LR.updatePowerStation(this.world3);
    t.checkExpect(this.LR.currentImg, new OverlayImage(
        new OverlayImage(
            new StarImage(this.LR.pixelSize / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
            new StarImage(this.LR.pixelSize / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
        new ScaleImage(new FrameImage(
            new FromFileImage("LR" + (this.LR.powerAmount * 4 - 1) / this.LR.worldRadius + ".png"),
            Color.white), this.LR.pixelSize / 32.0)));
    t.checkExpect(this.LR.powerStation, true);
    t.checkExpect(this.LR.powerAmount, 10);
    // removes power station
    this.LR.updatePowerStation(this.world3);
    t.checkExpect(this.LR.currentImg,
        new ScaleImage(new FrameImage(
            new FromFileImage("LR" + (this.LR.powerAmount * 4 - 1) / this.LR.worldRadius + ".png"),
            Color.white), this.LR.pixelSize / 32.0));
    t.checkExpect(this.LR.powerStation, false);

    t.checkExpect(this.LRTB.currentImg,
        new ScaleImage(new FrameImage(
            new FromFileImage(
                "LRTB" + (this.LRTB.powerAmount * 4 - 1) / this.LRTB.worldRadius + ".png"),
            Color.white), this.LRTB.pixelSize / 32.0));
    this.LRTB.updatePowerStation(this.world3);
    t.checkExpect(this.LRTB.currentImg,
        new OverlayImage(new OverlayImage(
            new StarImage(this.LRTB.pixelSize / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
            new StarImage(this.LRTB.pixelSize / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
            new ScaleImage(new FrameImage(
                new FromFileImage(
                    "LRTB" + (this.LRTB.powerAmount * 4 - 1) / this.LRTB.worldRadius + ".png"),
                Color.white), this.LRTB.pixelSize / 32.0)));
  }

  // to test updatePowered
  void testUpdatePowered(Tester t) {
    this.init();
    t.checkExpect(this.LR.powerAmount, 0);
    this.LR.updatePowered(world1, 3);
    t.checkExpect(this.LR.powerAmount, 3);

    t.checkExpect(this.LRTB.powerAmount, 0);
    this.LRTB.updatePowered(world1, 10);
    t.checkExpect(this.LRTB.powerAmount, 10);
  }

  // to test createImage
  boolean testCreateImage(Tester t) {
    this.init();
    this.LRTB.updatePowerStation(this.world3);
    return t.checkExpect(this.LR.createImage(),
        new ScaleImage(new FrameImage(
            new FromFileImage("LR" + (this.LR.powerAmount * 4 - 1) / this.LR.worldRadius + ".png"),
            Color.white), this.LR.pixelSize / 32.0))
        && t.checkExpect(this.LRTB.createImage(), new OverlayImage(new OverlayImage(
            new StarImage(this.LRTB.pixelSize / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
            new StarImage(this.LRTB.pixelSize / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
            new ScaleImage(new FrameImage(
                new FromFileImage(
                    "LRTB" + (this.LRTB.powerAmount * 4 - 1) / this.LRTB.worldRadius + ".png"),
                Color.white), this.LRTB.pixelSize / 32.0)));
  }

  // to test rotate in GamePiece
  void testRotate(Tester t) {
    this.init();
    t.checkExpect(this.LR.left, true);
    t.checkExpect(this.LR.right, true);
    t.checkExpect(this.LR.top, false);
    t.checkExpect(this.LR.bottom, false);
    t.checkExpect(this.LR.currentImg,
        new ScaleImage(new FrameImage(
            new FromFileImage("LR" + (this.LR.powerAmount * 4 - 1) / this.LR.worldRadius + ".png"),
            Color.white), this.LR.pixelSize / 32.0));
    this.LR.rotate(this.world1);
    t.checkExpect(this.LR.left, false);
    t.checkExpect(this.LR.right, false);
    t.checkExpect(this.LR.top, true);
    t.checkExpect(this.LR.bottom, true);
    t.checkExpect(this.LR.currentImg,
        new ScaleImage(new FrameImage(
            new FromFileImage("TB" + (this.LR.powerAmount * 4 - 1) / this.LR.worldRadius + ".png"),
            Color.white), this.LR.pixelSize / 32.0));

    t.checkExpect(this.LRTB.left, true);
    t.checkExpect(this.LRTB.right, true);
    t.checkExpect(this.LRTB.top, true);
    t.checkExpect(this.LRTB.bottom, true);
    t.checkExpect(this.LRTB.currentImg,
        new ScaleImage(new FrameImage(
            new FromFileImage(
                "LRTB" + (this.LR.powerAmount * 4 - 1) / this.LR.worldRadius + ".png"),
            Color.white), this.LR.pixelSize / 32.0));
    this.LRTB.rotate(this.world1);
    t.checkExpect(this.LRTB.left, true);
    t.checkExpect(this.LRTB.right, true);
    t.checkExpect(this.LRTB.top, true);
    t.checkExpect(this.LRTB.bottom, true);
    t.checkExpect(this.LRTB.currentImg,
        new ScaleImage(new FrameImage(
            new FromFileImage(
                "LRTB" + (this.LR.powerAmount * 4 - 1) / this.LR.worldRadius + ".png"),
            Color.white), this.LR.pixelSize / 32.0));
  }

  // to test createEdges in GamePiece
  void testCreateEdges(Tester t) {
    this.init();
    this.world1.createBoard();
    this.world1.rotateGamePieces();
    t.checkExpect(this.world1.board.get(0).get(0).outEdges, new ArrayList<Edge>());
    this.world1.board.get(0).get(0).createEdges(this.world1);
    t.checkExpect(this.world1.board.get(0).get(0).outEdges, new ArrayList<Edge>(Arrays
        .asList(new Edge(this.world1.board.get(0).get(0), this.world1.board.get(1).get(0), 0))));

    t.checkExpect(this.world1.board.get(2).get(1).outEdges, new ArrayList<Edge>());
    this.world1.board.get(2).get(1).createEdges(this.world1);
    t.checkExpect(this.world1.board.get(2).get(1).outEdges, new ArrayList<Edge>(Arrays
        .asList(new Edge(this.world1.board.get(2).get(1), this.world1.board.get(1).get(1), 0))));
  }

  // to test updateConnected
  void testUpdateConnected(Tester t) {
    this.init();
    this.world1.createBoard();
    this.world1.rotateGamePieces();
    this.world1.createEdges();
    t.checkExpect(this.world1.board.get(0).get(0).powerAmount, 0);
    t.checkExpect(this.world1.board.get(0).get(1).powerAmount, 0);
    t.checkExpect(this.world1.board.get(1).get(0).powerAmount, 0);
    t.checkExpect(this.world1.board.get(1).get(1).powerAmount, 10);
    t.checkExpect(this.world1.board.get(1).get(2).powerAmount, 0);
    t.checkExpect(this.world1.board.get(2).get(1).powerAmount, 0);
    this.world1.board.get(1).get(1).updateConnected(world1);
    t.checkExpect(this.world1.board.get(0).get(0).powerAmount, 10);
    t.checkExpect(this.world1.board.get(0).get(1).powerAmount, 10);
    t.checkExpect(this.world1.board.get(1).get(0).powerAmount, 10);
    t.checkExpect(this.world1.board.get(1).get(1).powerAmount, 10);
    t.checkExpect(this.world1.board.get(1).get(2).powerAmount, 10);
    t.checkExpect(this.world1.board.get(2).get(1).powerAmount, 10);

  }

  // to test draw board
  void testDrawBoard(Tester t) {
    this.init();
    t.checkExpect(this.world3.currentWorld, new WorldScene(100, 100));
    this.world3.createBoard();
    this.world3.drawBoard();
    WorldScene newWorld = this.world3.currentWorld;
    WorldImage LR = this.LR.createImage();
    WorldImage LRTB = this.LRTB.createImage();
    this.LRTB.updatePowerStation(this.world3);
    WorldImage power = this.LRTB.createImage();
    newWorld.placeImageXY(LR, 25, 25);
    newWorld.placeImageXY(LR, 25, 75);
    newWorld.placeImageXY(LRTB, 75, 25);
    newWorld.placeImageXY(LRTB, 75, 75);
    t.checkExpect(this.world3.currentWorld, newWorld);
  }

  // to test lastScene
  void testLastScene(Tester t) {
    this.init();
    t.checkExpect(this.world1.currentWorld, new WorldScene(150, 150));
    this.world1.lastScene("You Win!");
    WorldScene gameOver = new WorldScene(150, 150);
    gameOver
        .placeImageXY(new OverlayImage(new TextImage("You Win!", 25, FontStyle.BOLD, Color.white),
            new RectangleImage(200, 100, OutlineMode.SOLID, Color.black)), 75, 75);
    t.checkExpect(this.world1.currentWorld, gameOver);

  }

  // to test onTick
  void testOnTick(Tester t) {
    this.init();
    t.checkExpect(this.world1, this.world1);
    this.world1.onTick();
    t.checkExpect(this.world1, this.world1);
  }

  // to test createBoard
  void testCreateBoard(Tester t) {
    this.init();
    this.world1.createBoard();
    GamePiece power = new GamePiece(50, 1, 1, true, true, true, true, 10);
    power.updatePowerStation(this.world1);
    t.checkExpect(this.world1.board,
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 0, 0, true, true, false, false, 10),
                    new GamePiece(50, 0, 1, true, true, false, false, 10),
                    new GamePiece(50, 0, 2, true, true, false, false, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 1, 0, true, true, true, true, 10), power,
                    new GamePiece(50, 1, 2, true, true, true, true, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 2, 0, true, true, false, false, 10),
                    new GamePiece(50, 2, 1, true, true, false, false, 10),
                    new GamePiece(50, 2, 2, true, true, false, false, 10))))));
  }

  // to test onKeyReleased
  void testOnKeyReleased(Tester t) {
    this.init();
    this.world1.createBoard();
    GamePiece power = new GamePiece(50, 1, 1, true, true, true, true, 10);
    power.updatePowerStation(this.world1);
    t.checkExpect(this.world1.board,
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 0, 0, true, true, false, false, 10),
                    new GamePiece(50, 0, 1, true, true, false, false, 10),
                    new GamePiece(50, 0, 2, true, true, false, false, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 1, 0, true, true, true, true, 10), power,
                    new GamePiece(50, 1, 2, true, true, true, true, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 2, 0, true, true, false, false, 10),
                    new GamePiece(50, 2, 1, true, true, false, false, 10),
                    new GamePiece(50, 2, 2, true, true, false, false, 10))))));
    this.world1.onKeyReleased("left");
    GamePiece newPower = new GamePiece(50, 1, 0, true, true, true, true, 10);
    newPower.updatePowerStation(this.world1);
    t.checkExpect(this.world1.board,
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 0, 0, true, true, false, false, 10),
                    new GamePiece(50, 0, 1, true, true, false, false, 10),
                    new GamePiece(50, 0, 2, true, true, false, false, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(newPower, new GamePiece(50, 1, 1, true, true, true, true, 10),
                    new GamePiece(50, 1, 2, true, true, true, true, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 2, 0, true, true, false, false, 10),
                    new GamePiece(50, 2, 1, true, true, false, false, 10),
                    new GamePiece(50, 2, 2, true, true, false, false, 10))))));

  }

  // to test createEdges
  void testCreateEdgesLight(Tester t) {
    this.init();
    this.world3.createBoard();
    GamePiece power = new GamePiece(50, 1, 1, true, true, true, true, 10);
    power.updatePowerStation(this.world1);
    t.checkExpect(this.world3.board,
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 0, 0, true, true, false, false, 10),
                    new GamePiece(50, 0, 1, true, true, false, false, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 1, 0, true, true, true, true, 10), power)))));
    this.world3.createEdges();
    t.checkExpect(this.world3.board.get(0).get(0).outEdges, // edges of piece at 0,0
        new ArrayList<Edge>(Arrays.asList(
            new Edge(this.world3.board.get(0).get(0), this.world3.board.get(0).get(1), 0))));
    t.checkExpect(this.world3.board.get(0).get(1).outEdges, // edges of piece at 0,1
        new ArrayList<Edge>(Arrays.asList(
            new Edge(this.world3.board.get(0).get(1), this.world3.board.get(0).get(0), 0))));
  }

  // to test drawBoard
  void testUpdateGamePiece(Tester t) {
    this.init();
    WorldScene base = new WorldScene(100, 100);
    t.checkExpect(this.world3.currentWorld, base);
    GamePiece power = new GamePiece(50, 1, 1, true, true, true, true, 10);
    this.world3.updateGamePiece(power);
    base.placeImageXY(power.createImage(), 75, 75);
    t.checkExpect(this.world3.currentWorld, base);
  }

  // to test createRandomBoard
  void testCreateRandomBoard(Tester t) {
    this.init();
    t.checkExpect(this.world1.board, new ArrayList<ArrayList<GamePiece>>());
    this.world1.createRandomBoard();
    GamePiece power = new GamePiece(50, 1, 1, false, false, false, false, 10);
    power.updatePowerStation(this.world1);
    t.checkExpect(this.world1.board,
        new ArrayList<ArrayList<GamePiece>>(Arrays.asList(
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 0, 0, false, false, false, false, 10),
                    new GamePiece(50, 0, 1, false, false, false, false, 10),
                    new GamePiece(50, 0, 2, false, false, false, false, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 1, 0, false, false, false, false, 10), power,
                    new GamePiece(50, 1, 2, false, false, false, false, 10))),
            new ArrayList<GamePiece>(
                Arrays.asList(new GamePiece(50, 2, 0, false, false, false, false, 10),
                    new GamePiece(50, 2, 1, false, false, false, false, 10),
                    new GamePiece(50, 2, 2, false, false, false, false, 10))))));

  }

  // to test createRandomEdges
  void testCreateRandomEdges(Tester t) {
    this.init();
    t.checkExpect(this.world1.mst, new ArrayList<Edge>());
    this.world1.createRandomBoard();
    this.world1.createRandomEdges();
    GamePiece g1 = new GamePiece(50, 0, 0, false, false, false, false, 10);
    GamePiece g2 = new GamePiece(50, 0, 1, false, false, false, false, 10);
    GamePiece g3 = new GamePiece(50, 0, 2, false, false, false, false, 10);
    GamePiece g4 = new GamePiece(50, 1, 0, false, false, false, false, 10);
    GamePiece g5 = new GamePiece(50, 1, 1, false, false, false, false, 10);
    GamePiece g6 = new GamePiece(50, 1, 2, false, false, false, false, 10);
    GamePiece g7 = new GamePiece(50, 2, 0, false, false, false, false, 10);
    GamePiece g8 = new GamePiece(50, 2, 1, false, false, false, false, 10);
    GamePiece g9 = new GamePiece(50, 2, 2, false, false, false, false, 10);
    g5.updatePowerStation(world1);
    t.checkExpect(this.world1.mst,
        new ArrayList<Edge>(Arrays.asList(new Edge(g4, g7, 4), new Edge(g5, g8, 6),
            new Edge(g2, g3, 13), new Edge(g4, g5, 34), new Edge(g2, g5, 47), new Edge(g6, g9, 48),
            new Edge(g3, g6, 54), new Edge(g1, g4, 85))));
  }

  // to test implementation of Kruskal's algorithm
  void testKruskalAlgorithm(Tester t) {
    this.init();
    t.checkExpect(this.world1.mst, new ArrayList<Edge>());
    this.world1.kruskalAlgorithm(new HashMap<Posn, Posn>(), new ArrayList<Edge>());
    t.checkExpect(this.world1.mst, new ArrayList<Edge>());
    this.world1.createRandomBoard();
    this.world1.createRandomEdges();
    GamePiece g1 = new GamePiece(50, 0, 0, false, false, false, false, 10);
    GamePiece g2 = new GamePiece(50, 0, 1, false, false, false, false, 10);
    GamePiece g3 = new GamePiece(50, 0, 2, false, false, false, false, 10);
    GamePiece g4 = new GamePiece(50, 1, 0, false, false, false, false, 10);
    GamePiece g5 = new GamePiece(50, 1, 1, false, false, false, false, 10);
    GamePiece g6 = new GamePiece(50, 1, 2, false, false, false, false, 10);
    GamePiece g7 = new GamePiece(50, 2, 0, false, false, false, false, 10);
    GamePiece g8 = new GamePiece(50, 2, 1, false, false, false, false, 10);
    GamePiece g9 = new GamePiece(50, 2, 2, false, false, false, false, 10);
    g5.updatePowerStation(world1);
    t.checkExpect(this.world1.mst,
        new ArrayList<Edge>(Arrays.asList(new Edge(g4, g7, 4), new Edge(g5, g8, 6),
            new Edge(g2, g3, 13), new Edge(g4, g5, 34), new Edge(g2, g5, 47), new Edge(g6, g9, 48),
            new Edge(g3, g6, 54), new Edge(g1, g4, 85))));
  }

  // to test Find
  void testFind(Tester t) {
    this.init();
    HashMap<Posn, Posn> hm1 = new HashMap<Posn, Posn>();
    hm1.put(new Posn(0, 0), new Posn(0, 0));
    t.checkExpect(this.world1.find(hm1, new Posn(0, 0)), new Posn(0, 0));
  }

  // to test modifyBoard()
  void testModifyBoard(Tester t) {
    this.init();
    t.checkExpect(this.world3.board, new ArrayList<ArrayList<GamePiece>>());
    this.world3.createRandomBoard();
    this.world3.createRandomEdges();
    GamePiece g1 = new GamePiece(50, 0, 0, false, false, false, false, 10);
    GamePiece g2 = new GamePiece(50, 0, 1, false, false, false, false, 10);
    GamePiece g3 = new GamePiece(50, 1, 0, false, false, false, false, 10);
    GamePiece g4 = new GamePiece(50, 1, 1, false, false, false, false, 10);
    g4.updatePowerStation(world3);
    t.checkExpect(this.world3.mst, new ArrayList<Edge>(
        Arrays.asList(new Edge(g3, g4, 13), new Edge(g2, g4, 47), new Edge(g1, g3, 85))));
  }

  // to test modifyDirection()
  void testModifyDirection(Tester t) {
    GamePiece G1 = new GamePiece(50, 0, 0, false, false, false, false, 10);
    t.checkExpect(G1.right, false);
    G1.modifyDirection("right");
    t.checkExpect(G1.right, true);
  }

  // to run MinesweeperWorld
  void testBigBang(Tester t) {
    this.init();
    int width = 10;
    int height = 10;
    int cellSize = 50;
    World lightEmAll = new LightEmAll(width, height);
    int worldWidth = cellSize * width;
    int worldHeight = cellSize * height;
    double tickRate = 0.1;
    lightEmAll.bigBang(worldWidth, worldHeight, tickRate);
  }
}
