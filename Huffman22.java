import tester.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//to represent a huffman data structure
class Huffman {
  ArrayList<String> letters;
  ArrayList<Integer> freq;
  // a matrix containing the code for each letter
  ArrayList<ArrayList<String>> code;
  ArrayList<Tree> trees;

  Huffman(ArrayList<String> letters, ArrayList<Integer> freq) {
    if (letters.size() != freq.size()) {
      throw new IllegalArgumentException("Lists are not same size");
    }
    else if (letters.size() < 2 || freq.size() < 2) {
      throw new IllegalArgumentException("Lists sizes are not greater than 1");
    }
    this.letters = letters;
    this.freq = freq;
    this.trees = new ArrayList<Tree>();
    this.code = new ArrayList<ArrayList<String>>();
  }

  // EFFECT: converts all of the letters and frequencies into leaves
  void createLeaves() {
    for (int idx = 0; idx < this.letters.size(); idx++) {
      this.trees.add(new Leaf(this.letters.get(idx), this.freq.get(idx)));
    }
    this.sort();
  }

  // EFFECT: initializes the code matrix by adding the letters to the first column
  void initCode() {
    for (int idx = 0; idx < this.letters.size(); idx++) {
      this.code.add(new ArrayList<String>(Arrays.asList(this.letters.get(idx))));
    }
  }

  // EFFECT: sorts the tree matrix from least frequency to highest
  void sort() {
    new ArrayUtils().sort(this.trees, new TreeCompare());
  }

  // EFFECT: merges the two lowest trees into a node and adds their corresponding
  // code to the code matrix
  void mergeTrees() {
    this.trees.add(new Node(this.trees.get(0), this.trees.get(1)));
    this.addCode();
    this.trees.remove(0);
    this.trees.remove(0);
    this.sort();

  }

  // EFFECT: adds the code to the matrix for the corresponding letter
  void addCode() {
    for (int idx = 0; idx < this.code.size(); idx++) {
      if (this.trees.get(this.trees.size() - 1).containsLeft(this.code.get(idx).get(0))) {
        this.code.get(idx).add(1, "false");
      }
      if (this.trees.get(this.trees.size() - 1).containsRight(this.code.get(idx).get(0))) {
        this.code.get(idx).add(1, "true");
      }
    }
  }

  // EFFECT: merges all trees until there is a single tree
  void mergeAll() {
    for (int idx = this.trees.size(); idx > 1; idx--) {
      this.mergeTrees();
    }
  }

  // throws an exception if the given string is not part of the list of letters
  boolean charInList(String s) {
    for (int idx = 0; idx < s.length(); idx++) {
      if (this.letters.indexOf(s.substring(idx, idx + 1)) == -1) {
        throw new IllegalArgumentException(
            "Tried to encode " + s.charAt(idx) + " but that is not part of the language.");
      }
    }
    return true;
  }

  // encodes the given string based on the code matrix
  ArrayList<Boolean> encode(String s) {
    if (this.trees.size() == 0) {
      this.createLeaves();
    }
    if (this.code.size() == 0) {
      this.initCode();
    }
    if (this.trees.size() > 1) {
      this.mergeAll();
    }
    ArrayList<Boolean> encodedMessage = new ArrayList<Boolean>();
    if (this.charInList(s)) {
      for (int stringIdx = 0; stringIdx < s.length(); stringIdx++) {
        String letter = s.substring(stringIdx, stringIdx + 1);
        for (int codeRow = 0; codeRow < this.code.size(); codeRow++) {
          ArrayList<String> selectedRow = this.code.get(codeRow);
          if (selectedRow.get(0).equals(letter)) {
            for (int codeCol = 1; codeCol < selectedRow.size(); codeCol++) {
              if (selectedRow.get(codeCol).equals("true")) {
                encodedMessage.add(true);
              }
              else {
                encodedMessage.add(false);
              }
            }
          }
        }
      }
    }
    return encodedMessage;
  }

  // decodes the given boolean list based on the single tree in tree list
  String decode(ArrayList<Boolean> encoded) {
    if (this.trees.size() == 0) {
      this.createLeaves();
    }
    if (this.trees.size() > 1) {
      this.mergeAll();
    }
    String decodedMessage = "";
    int idx = 0;
    while (idx < encoded.size()) {
      String letterAndNum = this.trees.get(0).traverse(encoded, idx);
      decodedMessage += letterAndNum.substring(0, 1);
      idx = Integer.parseInt(letterAndNum.substring(1));
    }
    return decodedMessage;
  }
}

//to represent a tree data struture
abstract class Tree {
  int freq;

  Tree(int freq) {
    this.freq = freq;
  }

  // returns the string based on the given boolean list and increments idx each
  // step
  abstract String traverse(ArrayList<Boolean> encoded, int idx);

  // returns true if this tree contains the given string
  abstract boolean contains(String s);

  // returns true if the right subtree contains the given string
  abstract boolean containsRight(String s);

  // returns true if the left subtree contains the given string
  abstract boolean containsLeft(String s);

}

class Leaf extends Tree {
  String letter;

  Leaf(String letter, int freq) {
    super(freq);
    this.letter = letter;
  }

  // returns true if this tree contains the given string
  boolean contains(String s) {
    return s.equals(this.letter);
  }

  // returns true if the right subtree contains the given string
  boolean containsRight(String s) {
    return s.equals(this.letter);
  }

  // returns true if the left subtree contains the given string
  boolean containsLeft(String s) {
    return s.equals(this.letter);
  }

  // returns the string based on the given boolean list and increments idx each
  // step
  String traverse(ArrayList<Boolean> encoded, int idx) {
    return this.letter + idx;
  }
}

class Node extends Tree {
  Tree left;
  Tree right;

  Node(Tree left, Tree right) {
    super(left.freq + right.freq);
    this.left = left;
    this.right = right;
  }

  // returns true if this tree contains the given string
  boolean contains(String s) {
    return this.right.contains(s) || this.left.contains(s);
  }

  // returns true if the right subtree contains the given string
  boolean containsRight(String s) {
    return this.right.contains(s);
  }

  // returns true if the left subtree contains the given string
  boolean containsLeft(String s) {
    return this.left.contains(s);
  }

  // returns the string based on the given boolean list and increments idx each
  // step
  String traverse(ArrayList<Boolean> encoded, int idx) {
    if (idx >= encoded.size()) {
      return "?" + idx;
    }
    if (encoded.get(idx)) {
      return this.right.traverse(encoded, idx + 1);
    }
    else {
      return this.left.traverse(encoded, idx + 1);
    }
  }
}

//to represent ArrayList utils
class ArrayUtils {

  // Effect: swaps elements of array at given indexes
  <T> void swap(ArrayList<T> arr, int index1, int index2) {
    if (index1 > arr.size() - 1 || index2 > arr.size() - 1) {
      throw new IllegalArgumentException("Given indexes are out of bounds");
    }
    T oldValueAtIndex2 = arr.get(index2);
    arr.set(index2, arr.get(index1));
    arr.set(index1, oldValueAtIndex2);
  }

  // Effect: sorts array according to comparator
  <T> void sort(ArrayList<T> arr, Comparator<T> comp) {
    for (int idx = 0; idx < arr.size(); idx++) {
      int minIdxOfVal = idx;
      for (int idx2 = idx + 1; idx2 < arr.size(); idx2++) {
        if (comp.compare(arr.get(idx2), arr.get(minIdxOfVal)) < 0) {
          minIdxOfVal = idx2;
        }
      }
      new ArrayUtils().swap(arr, idx, minIdxOfVal);
    }
  }
}

//to represent comparing the freq of two trees
class TreeCompare implements Comparator<Tree> {
  // to compare the freq of two trees
  public int compare(Tree o1, Tree o2) {
    return o1.freq - o2.freq;
  }
}

//to represent comparing integers
class IntCompare implements Comparator<Integer> {
  // to return the difference of two Integers
  public int compare(Integer o1, Integer o2) {
    return o1 - o2;
  }
}

//to represent comparing strings
class StringCompare implements Comparator<String> {
  // to return the lexicographical difference between two strings
  public int compare(String o1, String o2) {
    return o1.compareTo(o2);
  }
}

//to represent examples of huffman
class ExamplesHuffman {
  // Create examples
  ArrayList<Integer> numList1;
  ArrayList<Integer> numList2;
  ArrayList<String> letterList1;
  ArrayList<String> letterList2;
  Leaf leaf1;
  Leaf leaf2;
  Leaf leaf3;
  Node node1;
  Node node2;
  ArrayUtils arrayUtils;
  Huffman huffman1;
  Huffman huffman2;
  ArrayList<Boolean> code1;
  ArrayList<Boolean> code2;
  TreeCompare treeCompare;
  IntCompare intCompare;
  StringCompare stringCompare;

  // assign data to examples
  void init() {
    this.numList1 = new ArrayList<Integer>(Arrays.asList(4, 2, 5, 8, 1));
    this.numList2 = new ArrayList<Integer>(Arrays.asList(5, 6, 2));
    this.letterList1 = new ArrayList<String>(Arrays.asList("d", "b", "a", "c", "f"));
    this.letterList2 = new ArrayList<String>(Arrays.asList("g", "f", "r"));
    this.arrayUtils = new ArrayUtils();
    this.huffman1 = new Huffman(this.letterList1, this.numList1);
    this.huffman2 = new Huffman(this.letterList2, this.numList2);
    this.leaf1 = new Leaf("c", 8);
    this.leaf2 = new Leaf("d", 10);
    this.leaf3 = new Leaf("f", 10);
    this.node1 = new Node(this.leaf1, this.leaf2);
    this.node2 = new Node(new Leaf("g", 18), this.node1);
    this.code1 = new ArrayList<Boolean>(Arrays.asList(true, true, false, true, false));
    this.code2 = new ArrayList<Boolean>(Arrays.asList(false));
    this.treeCompare = new TreeCompare();
    this.intCompare = new IntCompare();
    this.stringCompare = new StringCompare();
  }

  // call methods on huffman to create single tree and initialize code
  void initHuffman() {
    this.huffman1.createLeaves();
    this.huffman1.initCode();
    this.huffman1.mergeAll();
  }

  // to test constructor
  boolean testConstructorExceptions(Tester t) {
    return t.checkConstructorException(new IllegalArgumentException("Lists are not same size"),
        "Huffman", this.letterList1, this.numList2)
        && t.checkConstructorException(
            new IllegalArgumentException("Lists sizes are not greater than 1"), "Huffman",
            new ArrayList<String>(Arrays.asList("d")), new ArrayList<Integer>(Arrays.asList(2)));

  }

  // to test sort in ArrayUtils
  void testSort(Tester t) {
    this.init();
    t.checkExpect(this.numList1, new ArrayList<Integer>(Arrays.asList(4, 2, 5, 8, 1)));
    this.arrayUtils.sort(this.numList1, new IntCompare());
    t.checkExpect(this.numList1, new ArrayList<Integer>(Arrays.asList(1, 2, 4, 5, 8)));

    t.checkExpect(this.letterList1, new ArrayList<String>(Arrays.asList("d", "b", "a", "c", "f")));
    this.arrayUtils.sort(this.letterList1, new StringCompare());
    t.checkExpect(this.letterList1, new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "f")));

    t.checkExpect(this.huffman1, new Huffman(letterList1, numList1));
    this.huffman1.sort();
    t.checkExpect(this.huffman1,
        new Huffman(new ArrayList<String>(Arrays.asList("a", "b", "c", "d", "f")),
            new ArrayList<Integer>(Arrays.asList(1, 2, 4, 5, 8))));

  }

  // to test swap in ArrayUtils
  void testSwap(Tester t) {
    this.init();
    t.checkExpect(this.numList1, new ArrayList<Integer>(Arrays.asList(4, 2, 5, 8, 1)));
    this.arrayUtils.swap(this.numList1, 1, 2);
    t.checkExpect(this.numList1, new ArrayList<Integer>(Arrays.asList(4, 5, 2, 8, 1)));
  }


  // to test createLeaves in Huffman
  void testCreateLeaves(Tester t) {
    this.init();
    t.checkExpect(this.huffman1.trees, new ArrayList<Tree>());
    this.huffman1.createLeaves();
    t.checkExpect(this.huffman1.trees, new ArrayList<Tree>(Arrays.asList(new Leaf("f", 1),
        new Leaf("b", 2), new Leaf("d", 4), new Leaf("a", 5), new Leaf("c", 8))));
    
    t.checkExpect(this.huffman2.trees, new ArrayList<Tree>());
    this.huffman2.createLeaves();
    t.checkExpect(this.huffman2.trees, new ArrayList<Tree>(Arrays.asList(new Leaf("r", 2),
        new Leaf("g", 5), new Leaf("f", 6))));
  }

  // to test initCode in Huffman
  void testInitCode(Tester t) {
    this.init();
    t.checkExpect(this.huffman1.code, new ArrayList<ArrayList<String>>());
    this.huffman1.initCode();
    t.checkExpect(this.huffman1.code,
        new ArrayList<ArrayList<String>>(Arrays.asList(new ArrayList<String>(Arrays.asList("d")),
            new ArrayList<String>(Arrays.asList("b")), new ArrayList<String>(Arrays.asList("a")),
            new ArrayList<String>(Arrays.asList("c")), new ArrayList<String>(Arrays.asList("f")))));
    
    t.checkExpect(this.huffman2.code, new ArrayList<ArrayList<String>>());
    this.huffman2.initCode();
    t.checkExpect(this.huffman2.code,
        new ArrayList<ArrayList<String>>(Arrays.asList(new ArrayList<String>(Arrays.asList("g")),
            new ArrayList<String>(Arrays.asList("f")), new ArrayList<String>(Arrays.asList("r")))));
  }

  // to test mergeTrees in Huffman
  void testMergeTrees(Tester t) {
    this.init();
    this.huffman1.createLeaves();
    t.checkExpect(this.huffman1.trees, new ArrayList<Tree>(Arrays.asList(new Leaf("f", 1),
        new Leaf("b", 2), new Leaf("d", 4), new Leaf("a", 5), new Leaf("c", 8))));
    this.huffman1.mergeTrees();
    t.checkExpect(this.huffman1.trees,
        new ArrayList<Tree>(Arrays.asList(new Node(new Leaf("f", 1), new Leaf("b", 2)),
            new Leaf("d", 4), new Leaf("a", 5), new Leaf("c", 8))));
    
    this.huffman2.createLeaves();
    t.checkExpect(this.huffman2.trees, new ArrayList<Tree>(Arrays.asList(new Leaf("r", 2),
        new Leaf("g", 5), new Leaf("f", 6))));
    this.huffman2.mergeTrees();
    t.checkExpect(this.huffman2.trees, 
        new ArrayList<Tree>(Arrays.asList(new Leaf("f", 6), 
            new Node(new Leaf("r", 2), new Leaf("g", 5)))));
  }

  // to test addCode in Huffman
  void testAddCode(Tester t) {
    this.init();
    this.huffman1.createLeaves();
    this.huffman1.initCode();
    t.checkExpect(this.huffman1.code,
        new ArrayList<ArrayList<String>>(Arrays.asList(new ArrayList<String>(Arrays.asList("d")),
            new ArrayList<String>(Arrays.asList("b")), new ArrayList<String>(Arrays.asList("a")),
            new ArrayList<String>(Arrays.asList("c")), new ArrayList<String>(Arrays.asList("f")))));
    this.huffman1.mergeTrees(); // mergeTrees calls on addCode as a helper
    t.checkExpect(this.huffman1.code,
        new ArrayList<ArrayList<String>>(Arrays.asList(new ArrayList<String>(Arrays.asList("d")),
            new ArrayList<String>(Arrays.asList("b", "true")),
            new ArrayList<String>(Arrays.asList("a")), new ArrayList<String>(Arrays.asList("c")),
            new ArrayList<String>(Arrays.asList("f", "false")))));
    
    this.huffman2.createLeaves();
    this.huffman2.initCode();
    t.checkExpect(this.huffman2.code,
        new ArrayList<ArrayList<String>>(Arrays.asList(new ArrayList<String>(Arrays.asList("g")),
        new ArrayList<String>(Arrays.asList("f")), new ArrayList<String>(Arrays.asList("r")))));
    this.huffman2.mergeTrees();
    t.checkExpect(this.huffman2.code, 
        new ArrayList<ArrayList<String>>(Arrays.asList(
            new ArrayList<String>(Arrays.asList("g", "true")),
        new ArrayList<String>(Arrays.asList("f")),
        new ArrayList<String>(Arrays.asList("r", "false")))));
  }

  // to test mergeAll in Huffman
  void testMergeAll(Tester t) {
    this.init();
    this.huffman1.initCode();
    this.huffman1.createLeaves();
    t.checkExpect(this.huffman1.trees, new ArrayList<Tree>(Arrays.asList(new Leaf("f", 1),
        new Leaf("b", 2), new Leaf("d", 4), new Leaf("a", 5), new Leaf("c", 8))));
    t.checkExpect(this.huffman1.code,
        new ArrayList<ArrayList<String>>(Arrays.asList(new ArrayList<String>(Arrays.asList("d")),
            new ArrayList<String>(Arrays.asList("b")), new ArrayList<String>(Arrays.asList("a")),
            new ArrayList<String>(Arrays.asList("c")), new ArrayList<String>(Arrays.asList("f")))));

    this.huffman1.mergeAll();

    t.checkExpect(this.huffman1.trees,
        new ArrayList<Tree>(Arrays.asList(new Node(new Leaf("c", 8), new Node(new Leaf("a", 5),
            new Node(new Node(new Leaf("f", 1), new Leaf("b", 2)), new Leaf("d", 4)))))));
    t.checkExpect(this.huffman1.code,
        new ArrayList<ArrayList<String>>(
            Arrays.asList(new ArrayList<String>(Arrays.asList("d", "true", "true", "true")),
                new ArrayList<String>(Arrays.asList("b", "true", "true", "false", "true")),
                new ArrayList<String>(Arrays.asList("a", "true", "false")),
                new ArrayList<String>(Arrays.asList("c", "false")),
                new ArrayList<String>(Arrays.asList("f", "true", "true", "false", "false")))));
    
    this.huffman2.initCode();
    this.huffman2.createLeaves();
    t.checkExpect(this.huffman2.trees, new ArrayList<Tree>(Arrays.asList(new Leaf("r", 2),
        new Leaf("g", 5), new Leaf("f", 6))));
    t.checkExpect(this.huffman2.code,
        new ArrayList<ArrayList<String>>(Arrays.asList(new ArrayList<String>(Arrays.asList("g")),
        new ArrayList<String>(Arrays.asList("f")), new ArrayList<String>(Arrays.asList("r")))));

    this.huffman2.mergeAll();
  }

  // to test charInList in Huffman
  boolean testcharInList(Tester t) {
    this.init();
    this.initHuffman();
    return t.checkExpect(huffman1.charInList("d"), true)
        && t.checkExpect(huffman2.charInList("g"), true)
        && t.checkException(
            new IllegalArgumentException("Tried to encode s but that is not part of the language."),
            huffman1, "charInList", "s");

  }

  // to test encode in Huffman
  boolean testEncode(Tester t) {
    this.init();
    return t.checkExpect(this.huffman1.encode("cabda"), new ArrayList<Boolean>(
        Arrays.asList(false, true, false, true, true, false, true, true, true, true, true, false)))
        && t.checkException(new IllegalArgumentException(
            "Tried to encode s but that is not part of the language."),
            huffman1, "encode", "s")
        && t.checkExpect(this.huffman2.encode("ffgr"), new ArrayList<Boolean>(Arrays.asList(false,
            false, true, true, true, false)));
  }

  // to test decode in Huffman
  boolean testDecode(Tester t) {
    this.init();
    boolean test1 = t.checkExpect(this.huffman1.decode(new ArrayList<Boolean>(
        Arrays.asList(false, true, false, true, true, false, true, true, true, true, true, false))),
        "cabda");
    this.init();
    boolean test2 = t.checkExpect(this.huffman1.decode(new ArrayList<Boolean>(Arrays.asList(false,
        true, false, true, true, false, true, true, true, true, true, false, true))), "cabda?");
    return test1 && test2;
  }

  // to test Contains in Tree
  boolean testContains(Tester t) {
    this.init();
    return t.checkExpect(this.node2.contains("d"), true)
        && t.checkExpect(this.node2.contains("f"), false)
        && t.checkExpect(this.leaf1.contains("c"), true)
        && t.checkExpect(this.leaf1.contains("r"), false);
  }

  // to test ContainsLeft in Tree
  boolean testContainsLeft(Tester t) {
    this.init();
    return t.checkExpect(this.node2.containsLeft("c"), false)
        && t.checkExpect(this.node2.containsLeft("g"), true)
        && t.checkExpect(this.leaf1.containsLeft("c"), true)
        && t.checkExpect(this.leaf1.containsLeft("r"), false);
  }

  // to test containsRight in Tree
  boolean testContainsRight(Tester t) {
    this.init();
    return t.checkExpect(this.node2.containsRight("c"), true)
        && t.checkExpect(this.node2.containsRight("g"), false)
        && t.checkExpect(this.leaf1.containsRight("c"), true)
        && t.checkExpect(this.leaf1.containsRight("r"), false);
  }

  // to test traverse in Tree
  boolean testTraverse(Tester t) {
    this.init();
    this.initHuffman();
    return t.checkExpect(this.huffman1.trees.get(0).traverse(this.code1, 0), "b4")
        && t.checkExpect(this.huffman1.trees.get(0).traverse(this.code2, 0), "c1")
        && t.checkExpect(this.huffman1.trees.get(0).traverse(this.code1, 4), "c5");
  }

  // to test compare in TreeCompare
  boolean testTreeCompare(Tester t) {
    this.init();
    return t.checkExpect(this.treeCompare.compare(this.leaf1, this.leaf2), -2)
        && t.checkExpect(this.treeCompare.compare(this.leaf2, this.leaf1), 2)
        && t.checkExpect(this.treeCompare.compare(this.leaf3, this.leaf2), 0)
        && t.checkExpect(this.treeCompare.compare(this.node2, this.node1), 18)
        && t.checkExpect(this.treeCompare.compare(this.leaf1, this.node2), -28);
  }

  // to test compare in IntCompare
  boolean testIntCompare(Tester t) {
    this.init();
    return t.checkExpect(this.intCompare.compare(4, 8), -4)
        && t.checkExpect(this.intCompare.compare(7, 3), 4)
        && t.checkExpect(this.intCompare.compare(5, 5), 0);
  }

  // to test compare in StringCompare
  boolean testStringCompare(Tester t) {
    this.init();
    return t.checkExpect(this.stringCompare.compare("a", "b"), -1)
        && t.checkExpect(this.stringCompare.compare("c", "a"), 2)
        && t.checkExpect(this.stringCompare.compare("c", "c"), 0);
  }
}