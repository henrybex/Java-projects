import tester.Tester;
import java.util.function.Predicate;

//to represent a deque data structure
class Deque<T> {
  Sentinel<T> header;

  Deque() {
    this.header = new Sentinel<T>();
  }

  Deque(Sentinel<T> header) {
    this.header = header;
  }

  // to return the amount of nodes in a deque
  int size() {
    return this.header.next.size();
  }

  // to add a node to the front of deque
  void addAtHead(T data) {
    this.header.addAtHead(data);
  }

  // to add a node to the end of deque
  void addAtTail(T data) {
    this.header.addAtTail(data);
  }

  // to remove the node at the front of deque and return it
  T removeFromHead() {
    return this.header.next.remove();
  }

  // to remove the node at the end of deque and return it
  T removeFromTail() {
    return this.header.prev.remove();
  }

  // to find node given the predicate, returns sentinel if none are found
  ANode<T> find(Predicate<T> pred) {
    return this.header.next.find(pred);
  }
}

//to represent either a sentinel or node
abstract class ANode<T> {
  ANode<T> next;
  ANode<T> prev;

  ANode() {

  }

  ANode(ANode<T> next, ANode<T> prev) {
    this.next = next;
    this.prev = prev;
  }

  // to update the this.next
  void updateNext(ANode<T> node) {
    this.next = node;
  }

  // to update this.prev
  void updatePrev(ANode<T> node) {
    this.prev = node;
  }

  // to return the amount of nodes in a deque
  abstract int size();

  // to remove this node and return it
  abstract T remove();

  // to find node given the predicate, returns sentinel if none are found
  abstract ANode<T> find(Predicate<T> pred);
}

//to represent a sentinel connected to front and tail of deque
class Sentinel<T> extends ANode<T> {

  Sentinel() {
    this.next = this;
    this.prev = this;
  }

  // to return the amount of nodes in a deque
  int size() {
    return 0;
  }

  // to add a node to the front of deque
  void addAtHead(T data) {
    this.updateNext(new Node<T>(data, this.next, this));
  }

  // to add a node to the end of deque
  void addAtTail(T data) {
    this.updatePrev(new Node<T>(data, this, this.prev));
  }

  // to remove this node and return it
  T remove() {
    throw new RuntimeException("Cannot remove empty list");
  }

  // to find node given the predicate, returns sentinel if none are found
  ANode<T> find(Predicate<T> pred) {
    return this;
  }
}

//to represent a single node with data in a deque
class Node<T> extends ANode<T> {
  T data;

  Node(T data) {
    this.data = data;
    this.prev = null;
    this.next = null;
  }

  Node(T data, ANode<T> next, ANode<T> prev) {
    if (next == null || prev == null) {
      throw new IllegalArgumentException("Given nodes cannot be null");
    }
    this.data = data;
    this.prev = prev;
    this.next = next;
    this.next.updatePrev(this);
    this.prev.updateNext(this);
  }

  // to return the amount of nodes in a deque
  int size() {
    return 1 + this.next.size();
  }

  // to remove this node and return it
  T remove() {
    this.prev.updateNext(this.next);
    this.next.updatePrev(this.prev);
    return this.data;
  }

  // to find node given the predicate, returns sentinel if none are found
  ANode<T> find(Predicate<T> pred) {
    if (pred.test(data)) {
      return this;
    } else {
      return this.next.find(pred);
    }
  }
}

//to represent a predicate for checking words that begin with a letter
class StartsWithLetter implements Predicate<String> {
  String s;

  StartsWithLetter(String s) {
    if (s.length() != 1) {
      throw new IllegalArgumentException("Given string is not length 1");
    }
    this.s = s;
  }

  // tests if given string begins with this letter
  public boolean test(String t) {
    return t.substring(0, 1).equals(s);
  }
}

//to represent a predicate for returning true if it is this length
class IsLength implements Predicate<String> {
  int length;

  IsLength(int length) {
    this.length = length;
  }

  // to return true if given string is this.length long
  public boolean test(String t) {
    return t.length() == this.length;
  }
}

//to represent examples of a deque
class ExamplesDeque {

  // initialize examples
  Deque<String> deque1;
  Deque<String> deque2;
  Deque<String> deque3;
  Sentinel<String> sentinel1;
  Sentinel<String> sentinel2;
  ANode<String> abc;
  ANode<String> bcd; 
  ANode<String> cde;
  ANode<String> def;
  ANode<String> pwd; 
  ANode<String> hello;
  ANode<String> span;
  StartsWithLetter b;
  StartsWithLetter h;
  IsLength three;
  IsLength four;
  IsLength ten;

  // assign init test conditions
  void initTestConditions() {
    deque1 = new Deque<String>();
    sentinel1 = new Sentinel<String>();
    abc = new Node<String>("abc", this.sentinel1, this.sentinel1);
    bcd = new Node<String>("bcd", this.sentinel1, this.abc);
    cde = new Node<String>("cde", this.sentinel1, this.bcd);
    def = new Node<String>("def", this.sentinel1, this.cde);
    deque2 = new Deque<String>(this.sentinel1);
    sentinel2 = new Sentinel<String>();
    pwd = new Node<String>("pwd", this.sentinel2, this.sentinel2);
    hello = new Node<String>("hello", this.sentinel2, this.pwd);
    span = new Node<String>("span", this.sentinel2, this.hello);
    deque3 = new Deque<String>(this.sentinel2);
    b = new StartsWithLetter("b");
    h = new StartsWithLetter("h");
    three = new IsLength(3);
    four = new IsLength(4);
    ten = new IsLength(10);
  }

  // to test update prev in ANode
  void testUpdatePrev(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.sentinel1.prev, this.def);
    this.sentinel1.updatePrev(this.abc);
    t.checkExpect(this.sentinel1.prev, this.abc);

    t.checkExpect(this.cde.prev, this.bcd);
    this.cde.updatePrev(this.abc);
    t.checkExpect(this.cde.prev, this.abc);
  }

  // to test update next in ANode
  void testUpdateNext(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.sentinel1.next, this.abc);
    this.sentinel1.updateNext(this.abc);
    t.checkExpect(this.sentinel1.next, this.abc);

    t.checkExpect(this.cde.next, this.def);
    this.cde.updateNext(this.abc);
    t.checkExpect(this.cde.next, this.abc);
  }

  // to test size in Deque
  boolean testSize(Tester t) {
    this.initTestConditions();
    return t.checkExpect(this.deque2.size(), 4) && t.checkExpect(this.deque1.size(), 0)
        && t.checkExpect(this.deque3.size(), 3);
  }

  // to test addAtHead in Deque
  void testAddAtHead(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.sentinel1.next, this.abc);
    this.deque2.addAtHead("ghi");
    t.checkExpect(this.sentinel1.next, new Node<String>("ghi", this.abc, this.sentinel1));
    t.checkExpect(this.abc.prev, new Node<String>("ghi", this.abc, this.sentinel1));
    t.checkExpect(this.def.next, this.sentinel1);

    t.checkExpect(this.deque1.header.next, this.deque1.header);
    this.deque1.addAtHead("lkh");
    t.checkExpect(this.deque1.header.next,
        new Node<String>("lkh", this.deque1.header, this.deque1.header));

    t.checkExpect(this.sentinel2.next, this.pwd);
    this.deque3.addAtHead("john");
    t.checkExpect(this.sentinel2.next, new Node<String>("john", this.pwd, this.sentinel2));
    t.checkExpect(this.pwd.prev, new Node<String>("john", this.pwd, this.sentinel2));
  }

  // to test addAtTail in Deque
  void testaddAtTail(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.sentinel1.prev, this.def);
    this.deque2.addAtTail("ghi");
    t.checkExpect(this.sentinel1.prev, new Node<String>("ghi", this.sentinel1, this.def));
    t.checkExpect(this.def.next, new Node<String>("ghi", this.sentinel1, this.def));
    t.checkExpect(this.cde.next, this.def);

    t.checkExpect(this.deque1.header.prev, this.deque1.header);
    this.deque1.addAtTail("lkh");
    t.checkExpect(this.deque1.header.prev,
        new Node<String>("lkh", this.deque1.header, this.deque1.header));

    t.checkExpect(this.sentinel2.prev, this.span);
    this.deque3.addAtTail("john");
    t.checkExpect(this.sentinel2.prev, new Node<String>("john", this.sentinel2, this.span));
    t.checkExpect(this.span.next, new Node<String>("john", this.sentinel2, this.span));
  }

  // to test effect of removeFromHead in Deque
  void testRemoveFromHeadEffect(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.sentinel1.next, this.abc);
    this.deque2.removeFromHead();
    t.checkExpect(this.sentinel1.next, this.bcd);

    t.checkExpect(this.sentinel2.next, this.pwd);
    this.deque3.removeFromHead();
    t.checkExpect(this.sentinel2.next, this.hello);
  }

  // to test result of removeFromHead in Deque
  boolean testRemoveFromHead(Tester t) {
    this.initTestConditions();
    return t.checkExpect(this.deque2.removeFromHead(), "abc")
        && t.checkExpect(this.deque3.removeFromHead(), "pwd") && t.checkException(
            new RuntimeException("Cannot remove empty list"), this.deque1, "removeFromHead");
  }

  // to test effect of removeFromTail in Deque
  void testRemoveFromTailEffect(Tester t) {
    this.initTestConditions();
    t.checkExpect(this.sentinel1.prev, this.def);
    this.deque2.removeFromTail();
    t.checkExpect(this.sentinel1.prev, this.cde);

    t.checkExpect(this.sentinel2.prev, this.span);
    this.deque3.removeFromTail();
    t.checkExpect(this.sentinel2.prev, this.hello);
  }

  // to test result of removeFromTail in Deque
  boolean testRemoveFromTail(Tester t) {
    this.initTestConditions();
    return t.checkExpect(this.deque2.removeFromTail(), "def")
        && t.checkExpect(this.deque3.removeFromTail(), "span") && t.checkException(
            new RuntimeException("Cannot remove empty list"), this.deque1, "removeFromTail");
  }

  // to test remove in ANode
  boolean testRemove(Tester t) {
    this.initTestConditions();
    return t.checkExpect(this.abc.remove(), "abc") && t
        .checkException(new RuntimeException("Cannot remove empty list"), this.sentinel1, "remove");
  }

  // to test test in StartsWithLetter
  boolean testStartsWithLetter(Tester t) {
    this.initTestConditions();
    return t.checkConstructorException(new IllegalArgumentException("Given string is not length 1"),
        "StartsWithLetter", "bh") && t.checkExpect(this.b.test("happy"), false)
        && t.checkExpect(this.h.test("happy"), true);
  }

  // to test test in IsLength
  boolean testIsLength(Tester t) {
    this.initTestConditions();
    return t.checkExpect(this.three.test("pwd"), true)
        && t.checkExpect(this.four.test("pwd"), false);
  }

  // to test find in Deque
  boolean testFind(Tester t) {
    this.initTestConditions();
    return t.checkExpect(this.deque1.find(b), new Sentinel<String>())
        && t.checkExpect(this.deque2.find(b), this.bcd)
        && t.checkExpect(this.deque2.find(three), this.abc)
        && t.checkExpect(this.deque2.find(ten), this.sentinel1)
        && t.checkExpect(this.deque3.find(h), this.hello)
        && t.checkExpect(this.deque3.find(four), this.span);
  }

}
