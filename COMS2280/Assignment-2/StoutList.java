package edu.iastate.cs228.hw2;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author Madhava Nallamilli
 */

/**
 * Implementation of the list interface based on linked nodes
 * that store multiple items per node.  Rules for adding and removing
 * elements ensure that each node (except possibly the last one)
 * is at least half full.
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
  /**
   * Default number of elements that may be stored in each node.
   */
  private static final int DEFAULT_NODESIZE = 4;
  
  /**
   * Number of elements that can be stored in each node.
   */
  private final int nodeSize;
  
  /**
   * Dummy node for head.  It should be private but set to public here only  
   * for grading purpose.  In practice, you should always make the head of a 
   * linked list a private instance variable.  
   */
  public Node head;
  
  /**
   * Dummy node for tail.
   */
  private Node tail;
  
  /**
   * Number of elements in the list.
   */
  private int size;
  
  /**
   * Constructs an empty list with the default node size.
   */
  public StoutList()
  {
    this(DEFAULT_NODESIZE);
  }

  /**
   * Constructs an empty list with the given node size.
   * @param nodeSize number of elements that may be stored in each node, must be 
   *   an even number
   */
  public StoutList(int nodeSize)
  {
    if (nodeSize <= 0 || nodeSize % 2 != 0) throw new IllegalArgumentException();
    
    // dummy nodes
    head = new Node();
    tail = new Node();
    head.next = tail;
    tail.previous = head;
    this.nodeSize = nodeSize;
  }
  
  /**
   * Constructor for grading only.  Fully implemented. 
   * @param head
   * @param tail
   * @param nodeSize
   * @param size
   */
  public StoutList(Node head, Node tail, int nodeSize, int size)
  {
	  this.head = head; 
	  this.tail = tail; 
	  this.nodeSize = nodeSize; 
	  this.size = size; 
  }

  @Override
  public int size()
  {
    return size;
  }
  
  @Override
  public boolean add(E item)
  {
	if(item == null) {
		throw new NullPointerException();
	}
	
	if(head.next == tail) {
		Node newNode = new Node();
		newNode.addItem(item);
		newNode.previous = head;
		newNode.next = tail;
		head.next = newNode;
		tail.previous = newNode;
	}
	else {
		Node last = tail.previous;
		if(last.count < nodeSize) {
			last.addItem(item);
		}
		else {
			Node newNode = new Node();
			newNode.addItem(item);
			newNode.previous = last;
			newNode.next = tail;
			last.next = newNode;
			tail.previous = newNode;
		}
	}
	
	
	size++;
	return true;
  }

  @Override
  public void add(int pos, E item) {
      if (item == null)
          throw new NullPointerException();
      if (pos < 0 || pos > size)
          throw new IndexOutOfBoundsException();

      NodeInfo info;
      if (pos == size) {
          // special case: add at the end
          info = new NodeInfo(tail, 0);
      } else {
          info = find(pos);
      }

      add(info.node, info.offset, item);
      size++;
  }


  @Override
  public E remove(int pos) {
      if (pos < 0 || pos >= size) {
          throw new IndexOutOfBoundsException();
      }

      NodeInfo info = find(pos);
      Node node = info.node;
      int offset = info.offset;
      E removed = node.data[offset];

      // Remove the item from the node
      node.removeItem(offset);
      size--;

      // If node is now underfilled (less than nodeSize / 2) and it's not the last node
      if (node != tail.previous && node.count < nodeSize / 2) {
          Node next = node.next;

          // Try to borrow from next node
          if (next != tail && next.count > nodeSize / 2) {
              // Move the first item from next node to this one
              node.addItem(next.data[0]);
              next.removeItem(0);
          } 
          // Try to merge with next node
          else if (next != tail) {
              // Move all items from next into current node
              for (int i = 0; i < next.count; i++) {
                  node.addItem(next.data[i]);
              }

              // Remove next node from list
              node.next = next.next;
              next.next.previous = node;
          }
      }

      return removed;
  }

  /**
   * Sort all elements in the stout list in the NON-DECREASING order. You may do the following. 
   * Traverse the list and copy its elements into an array, deleting every visited node along 
   * the way.  Then, sort the array by calling the insertionSort() method.  (Note that sorting 
   * efficiency is not a concern for this project.)  Finally, copy all elements from the array 
   * back to the stout list, creating new nodes for storage. After sorting, all nodes but 
   * (possibly) the last one must be full of elements.  
   *  
   * Comparator<E> must have been implemented for calling insertionSort().    
   */
  public void sort()
  {
      if (size == 0) return;

      // Step 1: Copy elements to array
      E[] arr = (E[]) new Comparable[size];
      int idx = 0;
      Node curr = head.next;
      while (curr != tail) {
          for (int i = 0; i < curr.count; i++) {
              arr[idx++] = curr.data[i];
          }
          curr = curr.next;
      }

      // Step 2: Clear the list
      head.next = tail;
      tail.previous = head;
      size = 0;

      // Step 3: Sort array
      insertionSort(arr, Comparator.naturalOrder());

      // Step 4: Rebuild the list
      for (E item : arr) {
          add(item);
      }
  }
  
  /**
   * Sort all elements in the stout list in the NON-INCREASING order. Call the bubbleSort()
   * method.  After sorting, all but (possibly) the last nodes must be filled with elements.  
   *  
   * Comparable<? super E> must be implemented for calling bubbleSort(). 
   */
  public void sortReverse()
  {
      if (size == 0) return;

      // Step 1: Copy elements to array
      E[] arr = (E[]) new Comparable[size];
      int idx = 0;
      Node curr = head.next;
      while (curr != tail) {
          for (int i = 0; i < curr.count; i++) {
              arr[idx++] = curr.data[i];
          }
          curr = curr.next;
      }

      // Step 2: Clear the list
      head.next = tail;
      tail.previous = head;
      size = 0;

      // Step 3: Sort array in reverse
      bubbleSort(arr);

      // Step 4: Rebuild the list
      for (E item : arr) {
          add(item);
      }
  }
  
  @Override
  public Iterator<E> iterator()
  {
    return new StoutListIterator();
  }

  @Override
  public ListIterator<E> listIterator()
  {
	  return new StoutListIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index)
  {
	  return new StoutListIterator(index);
  }
  
  /**
   * Returns a string representation of this list showing
   * the internal structure of the nodes.
   */
  public String toStringInternal()
  {
    return toStringInternal(null);
  }

  /**
   * Returns a string representation of this list showing the internal
   * structure of the nodes and the position of the iterator.
   *
   * @param iter
   *            an iterator for this list
   */
  public String toStringInternal(ListIterator<E> iter) 
  {
      int count = 0;
      int position = -1;
      if (iter != null) {
          position = iter.nextIndex();
      }

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      Node current = head.next;
      while (current != tail) {
          sb.append('(');
          E data = current.data[0];
          if (data == null) {
              sb.append("-");
          } else {
              if (position == count) {
                  sb.append("| ");
                  position = -1;
              }
              sb.append(data.toString());
              ++count;
          }

          for (int i = 1; i < nodeSize; ++i) {
             sb.append(", ");
              data = current.data[i];
              if (data == null) {
                  sb.append("-");
              } else {
                  if (position == count) {
                      sb.append("| ");
                      position = -1;
                  }
                  sb.append(data.toString());
                  ++count;

                  // iterator at end
                  if (position == size && count == size) {
                      sb.append(" |");
                      position = -1;
                  }
             }
          }
          sb.append(')');
          current = current.next;
          if (current != tail)
              sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
  }


  /**
   * Node type for this list.  Each node holds a maximum
   * of nodeSize elements in an array.  Empty slots
   * are null.
   */
  private class Node
  {
    /**
     * Array of actual data elements.
     */
    // Unchecked warning unavoidable.
    public E[] data = (E[]) new Comparable[nodeSize];
    
    /**
     * Link to next node.
     */
    public Node next;
    
    /**
     * Link to previous node;
     */
    public Node previous;
    
    /**
     * Index of the next available offset in this node, also 
     * equal to the number of elements in this node.
     */
    public int count;

    /**
     * Adds an item to this node at the first available offset.
     * Precondition: count < nodeSize
     * @param item element to be added
     */
    void addItem(E item)
    {
      if (count >= nodeSize)
      {
        return;
      }
      data[count++] = item;
      //useful for debugging
      //      System.out.println("Added " + item.toString() + " at index " + count + " to node "  + Arrays.toString(data));
    }
  
    /**
     * Adds an item to this node at the indicated offset, shifting
     * elements to the right as necessary.
     * 
     * Precondition: count < nodeSize
     * @param offset array index at which to put the new element
     * @param item element to be added
     */
    void addItem(int offset, E item)
    {
      if (count >= nodeSize)
      {
    	  return;
      }
      for (int i = count - 1; i >= offset; --i)
      {
        data[i + 1] = data[i];
      }
      ++count;
      data[offset] = item;
      //useful for debugging 
      //System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
    }

    /**
     * Deletes an element from this node at the indicated offset, 
     * shifting elements left as necessary.
     * Precondition: 0 <= offset < count
     * @param offset
     */
    void removeItem(int offset)
    {
      E item = data[offset];
      for (int i = offset + 1; i < nodeSize; ++i)
      {
        data[i - 1] = data[i];
      }
      data[count - 1] = null;
      --count;
    }    
  }
  
  /**
   * helper class represents specific point of list
   */
  private class NodeInfo {
      public Node node;
      public int offset;

      public NodeInfo(Node node, int offset) {
          this.node = node;
          this.offset = offset;
      }
  }
  
  /**
   * Finds node and offset within node corresponding to given
   * logical index position in list
   *
   * @param pos the logical index position (0-based) to find
   * @return a NodeInfo object containing the node and the offset within the node
   * @throws IndexOutOfBoundsException if position is out of range 
   * @throws RuntimeException if no node contains given position 
   */
  private NodeInfo find(int pos) {
      if (pos < 0 || pos >= size) {
          throw new IndexOutOfBoundsException();
      }

      Node current = head.next;
      int indexSoFar = 0;

      while (current != tail) {
          if (indexSoFar + current.count > pos) {
              // Found the node containing the index
              return new NodeInfo(current, pos - indexSoFar);
          }
          indexSoFar += current.count;
          current = current.next;
      }

      // Should not reach here if pos is valid
      throw new RuntimeException();
  }
  
  /**
   * Adds an item into a node at a given offset, handling node splitting if necessary.
   * @param node  the node to insert into
   * @param offset the offset within the node
   * @param item   the element to insert
   * @return the NodeInfo indicating the real node and offset where the item was inserted
   */
  private NodeInfo add(Node node, int offset, E item) {
      if (node == tail) {
          // append to end
          Node last = tail.previous;
          if (last != head && last.count < nodeSize) {
              last.addItem(item);
              return new NodeInfo(last, last.count - 1);
          } else {
              Node newNode = new Node();
              newNode.addItem(item);
              insertAfter(last, newNode);
              return new NodeInfo(newNode, 0);
          }
      }

      if (node.count < nodeSize) {
          node.addItem(offset, item);
          return new NodeInfo(node, offset);
      }

      // Node is full — perform a split
      Node newNode = new Node();
      int mid = nodeSize / 2;
      int moveCount = nodeSize - mid;

      // Move last half elements to new node
      for (int i = 0; i < moveCount; i++) {
          newNode.data[i] = node.data[mid + i];
          newNode.count++;
          node.data[mid + i] = null;
      }
      node.count = mid;

      insertAfter(node, newNode);

      if (offset <= mid) {
          node.addItem(offset, item);
          return new NodeInfo(node, offset);
      } else {
          int newOffset = offset - mid;
          newNode.addItem(newOffset, item);
          return new NodeInfo(newNode, newOffset);
      }
  }
  
  
  /**
   * Inserts new node immediately after the specified current node 
   *
   * @param current the node after which the new node will be inserted
   * @param newNode the new node to be inserted
   */
  private void insertAfter(Node current, Node newNode) {
	    newNode.next = current.next;
	    newNode.previous = current;
	    current.next.previous = newNode;
	    current.next = newNode;
	}

 
  private class StoutListIterator implements ListIterator<E>
  {
	  
	private Node currentNode;   // the current node the iterator is on
	private int offset;      // position within the current node's data[]
	private int indexGlobal; // logical index in list
	private boolean canSet = false;  // true if set() or remove() is allowed after next() or previous()
	private boolean lastMoveWasNext = false;  // true if the last iterator movement was next(), false if previous()
	
	
    /**
     * Default constructor 
     */
    public StoutListIterator()
    {
    	this(0);
    }

    /**
     * Constructor finds node at a given position
     * @param pos
     */
    public StoutListIterator(int pos)
    {
    	if (pos < 0 || pos > size) {
            throw new IndexOutOfBoundsException();
        }

        if (pos == size) {
            currentNode = tail;
            offset = 0;
            indexGlobal = pos;
            return;
        }

        NodeInfo info = find(pos);
        currentNode = info.node;
        offset = info.offset;
        indexGlobal = pos;
    }
    
    
    /**
     * returns true if there are more elements when traversing forward
     * @return true if next() would return an element, false otherwise
     */
    @Override
    public boolean hasNext()
    {
    	return indexGlobal < size;
    	
    }

    
    /**
     * returns next element in iteration and advances cursor forward
     * @return the next element in the list
     * @throws NoSuchElementException if there are no more elements
     */
    @Override
    public E next()
    {
    	if (!hasNext()) {
            throw new NoSuchElementException();
        }

    	E item = currentNode.data[offset];
    	offset++;
    	indexGlobal++;

    	if (offset >= currentNode.count) {
    		currentNode = currentNode.next;
    		offset = 0;
    	}
    	
    	canSet = true;
    	lastMoveWasNext = true;
    	
    	return item;
    	
    	
    }

    

    /**
     * removes last element returned by next() or previous() from the list
     * @throws IllegalStateException if remove() is called before next()/previous()
     */
    @Override
    public void remove() {
        if (!canSet) {
            throw new IllegalStateException();
        }

        // Figure out the index to remove and update indexGlobal
        int removeIndex;
        if (lastMoveWasNext) {
            removeIndex = indexGlobal - 1;
            indexGlobal--;
        } else {
            removeIndex = indexGlobal;
        }

        // Remove from list
        StoutList.this.remove(removeIndex);

        // Reposition iterator
        NodeInfo info;
        if (indexGlobal == size) {
            currentNode = tail;
            offset = 0;
        } else {
            info = find(indexGlobal);
            currentNode = info.node;
            offset = info.offset;
        }

        canSet = false;
    }
    
    
    /**
     * returns true if there are elements before current cursor position
     * @return true if previous() would return an element, false otherwise
     */
    @Override
    public boolean hasPrevious() {
        return indexGlobal > 0;
    }
    
    
    /**
     * returns previous element in iteration and moves cursor backwards
     * @return previous element in list
     * @throws NoSuchElementException if there are no previous elements
     */
    @Override
    public E previous() {
        if (!hasPrevious()) throw new NoSuchElementException();

        if (offset > 0) {
            offset--;
        } else {
            currentNode = currentNode.previous;
            offset = currentNode.count - 1;
        }

        indexGlobal--;
        
        
        canSet = true;
        lastMoveWasNext = false;
        
        
        return currentNode.data[offset];
    }

    
    /**
     * returns index of element that would be returned by a subsequent call to next()
     * @return index of next element
     */
    @Override
    public int nextIndex() {
        return indexGlobal;
    }

    
    /**
     * returns index of element that would be returned by a subsequent call to previous()
     * @return index of previous element
     */
    @Override
    public int previousIndex() {
        return indexGlobal - 1;
    }
    
    

    /**
     * replaces last element returned by next() or previous() with specified element
     * @param e element which replaces last returned element
     * @throws IllegalStateException if set() is called before next()/previous()
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public void set(E e) {
        if (!canSet) {
            throw new IllegalStateException();
        }

        if (e == null) {
            throw new NullPointerException();
        }

        if (lastMoveWasNext) {
            // next() moves forward, so offset - 1 is the index of last returned element
            int idx = offset - 1;
            if (idx < 0) {
                // We moved to next node
                currentNode = currentNode.previous;
                idx = currentNode.count - 1;
            }
            currentNode.data[idx] = e;
        } else {
            // previous() moves backward, so offset is the index of last returned element
            currentNode.data[offset] = e;
        }
    }
    
    
    /**
     * inserts specified element into list at current cursor position
     * after insertion, the cursor is positioned after inserted element
     * @param e element to insert
     * @throws NullPointerException if specified element is null
     */
    @Override
    public void add(E e) {
        if (e == null) {
            throw new NullPointerException();
        }

        // Use the helper to insert element at (currentNode, offset)
        NodeInfo info = StoutList.this.add(currentNode, offset, e);

        // Move iterator one step forward
        indexGlobal++;

        // If inserted into currentNode, and before offset, bump offset
        if (info.node == currentNode && info.offset <= offset) {
            offset++;
        }

        // Update currentNode and offset to reflect logical position after insert
        currentNode = info.node;
        offset = info.offset + 1;

        canSet = false;
    }
    
    
    
    /**
     * returns element at specified logical position in list
     * @param pos the logical index of the element to return
     * @return the element at the specified position
     */
    public E get(int pos) {
        NodeInfo info = find(pos);
        return info.node.data[info.offset];
    }
    
  }
  

  /**
   * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING order. 
   * @param arr   array storing elements from the list 
   * @param comp  comparator used in sorting 
   */
  private void insertionSort(E[] arr, Comparator<? super E> comp)
  {
      for (int i = 1; i < arr.length; i++) {
          E key = arr[i];
          int j = i - 1;

          while (j >= 0 && comp.compare(arr[j], key) > 0) {
              arr[j + 1] = arr[j];
              j--;
          }

          arr[j + 1] = key;
      }
  }
  
  /**
   * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a 
   * description of bubble sort please refer to Section 6.1 in the project description. 
   * You must use the compareTo() method from an implementation of the Comparable 
   * interface by the class E or ? super E. 
   * @param arr  array holding elements from the list
   */
  private void bubbleSort(E[] arr)
  {
      for (int i = 0; i < arr.length - 1; i++) {
          for (int j = 0; j < arr.length - i - 1; j++) {
              if (arr[j].compareTo(arr[j + 1]) < 0) {
                  E temp = arr[j];
                  arr[j] = arr[j + 1];
                  arr[j + 1] = temp;
              }
          }
      }
  }
 
  
}