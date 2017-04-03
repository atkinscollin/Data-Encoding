// DE1C.java CS6025 Cheng 2016
// Huffman encoder
// Usage: java DE1C original > encoded

import java.io.*;
import java.util.*;

class Node implements Comparable{
  Node left, right;
  int symbol;
  int frequency;
  public Node(Node l, Node r, int s, int f){
    left = l; right = r; symbol = s; frequency = f;
  }
  public int compareTo(Object obj){
   Node n = (Node)obj;
   return frequency - n.frequency;
  }
}

public class DE1C{

  static final int numberOfSymbols = 256;
  static final int blockSize = 1024;
  int[] freq = new int[numberOfSymbols];
  int actualNumberOfSymbols = 0;  // number of symbols with freq > 0
  int filesize = 0;
  int[] actualSymbolIndex = new int[numberOfSymbols];
  int[] index2Symbol = null;
  Node tree = null;
  String[] codewords = null;
  int buf = 0; int position = 0;  // used by outputbits()

  void count(String filename){ // count symbol frequencies
    byte[] buffer = new byte[blockSize];
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    int len = 0;
    for (int i = 0; i < numberOfSymbols; i++) freq[i] = 0;
   try {
    while ((len = fis.read(buffer)) >= 0){
      for (int i = 0; i < len; i++){
       int symbol = buffer[i];
       if (symbol < 0) symbol += 256;
       freq[symbol]++;
      }
      filesize += len;
    }
    fis.close();
   } catch (IOException e){
      System.err.println("IOException");
      System.exit(1);
   }
  }

 void shrinkSymbols(){
   for (int i = 0; i < numberOfSymbols; i++)
       actualSymbolIndex[i] = freq[i] > 0 ? actualNumberOfSymbols++ : -1;
   index2Symbol = new int[actualNumberOfSymbols];
   for (int i = 0; i < numberOfSymbols; i++) if (freq[i] > 0)
       index2Symbol[actualSymbolIndex[i]] = i;
   codewords = new String[actualNumberOfSymbols];   
  }

  void makeTree(){  // make Huffman prefix codeword tree
   PriorityQueue<Node> pq = new PriorityQueue<Node>();
   for (int i = 0; i < numberOfSymbols; i++) if (freq[i] > 0)
       pq.add(new Node(null, null, actualSymbolIndex[i], freq[i]));
   while (pq.size() > 1){
     Node a = pq.poll(); Node b = pq.poll();  // remove two subtress
     pq.add(new Node(a, b, -1, a.frequency + b.frequency));  // add the merged subtree
   }
   tree = pq.poll();  // root of tree as the last single subtree
  }

  void dfs(Node n, String code){  // generate all codewords
    if (n.symbol < 0){
      dfs(n.left, code + "0"); dfs(n.right, code + "1");
    }else codewords[n.symbol] = code;
  }

  void makeCodewords(){
    dfs(tree, "");
  }
    
  void encode(String filename){ // compress filename to System.out
    byte[] buffer = new byte[blockSize];
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    int len = 0;
   try {
    while ((len = fis.read(buffer)) >= 0){
      for (int i = 0; i < len; i++){
       int symbol = buffer[i];
       if (symbol < 0) symbol += 256;
       outputbits(codewords[actualSymbolIndex[symbol]]);
      }
    }
    fis.close();
   } catch (IOException e){
      System.err.println("IOException");
      System.exit(1);
   }
    if (position > 0){ System.out.write(buf << (8 - position)); }
    System.out.flush();
  }

  void outputbits(String bitstring){ // output codeword
     for (int i = 0; i < bitstring.length(); i++){
      buf <<= 1;
      if (bitstring.charAt(i) == '1') buf |= 1;
      position++;
      if (position == 8){
         position = 0;
         System.out.write(buf);
         buf = 0;
      }
     }
  }
    

  public static void main(String[] args){
    if (args.length < 1){
     System.err.println("Usage: java DE1A original > encoded");
     return;
    }
    DE1C de1 = new DE1C();
    de1.count(args[0]);
    de1.shrinkSymbols();
    de1.makeTree();
    de1.makeCodewords();
    de1.encode(args[0]); 
  }
}
