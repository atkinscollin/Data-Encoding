// DE1D.java CS6025 Cheng 2016
// Huffman encoder with context
// Usage: java DE1D original > encoded

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

public class DE1D{

  static final int numberOfSymbols = 256;
  static final int blockSize = 1024;
  int[] freq = new int[numberOfSymbols];
  int[][] freq2 = null;
  int actualNumberOfSymbols = 0;  // number of symbols with freq > 0
  int filesize = 0;
  int[] actualSymbolIndex = new int[numberOfSymbols];
  int[] index2Symbol = null;
  Node[] trees = null;
  String[][] codewords = null;
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
   codewords = new String[actualNumberOfSymbols][actualNumberOfSymbols];   
  }

  void count2(String filename){ // count symbol frequencies
    freq2 = new int[actualNumberOfSymbols][actualNumberOfSymbols];
    byte[] buffer = new byte[blockSize];
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    int len = 0;
    for (int i = 0; i < actualNumberOfSymbols; i++)
      for (int j = 0; j < actualNumberOfSymbols; j++) freq2[i][j] = 0;
    int context = 0;
   try {
    while ((len = fis.read(buffer)) >= 0){
      for (int i = 0; i < len; i++){
       int symbol = buffer[i];
       if (symbol < 0) symbol += 256;
       int a = actualSymbolIndex[symbol];
       freq2[context][a]++;
       context = a;
      }
    }
    fis.close();
   } catch (IOException e){
      System.err.println("IOException");
      System.exit(1);
   }
  }


  void makeTrees(){  // make Huffman prefix codeword tree
   trees = new Node[actualNumberOfSymbols];
   PriorityQueue<Node> pq = new PriorityQueue<Node>();
   for (int i = 0; i < actualNumberOfSymbols; i++){
     pq.clear();
     for (int j = 0; j < actualNumberOfSymbols; j++) if (freq2[i][j] > 0) 
       pq.add(new Node(null, null, j, freq2[i][j]));
     while (pq.size() > 1){
       Node a = pq.poll(); Node b = pq.poll();  // remove two subtress
       pq.add(new Node(a, b, -1, a.frequency + b.frequency));  // add the merged subtree
     }
     trees[i] = pq.poll();  // root of tree as the last single subtree
    }
  }

  void dfs(int context, Node n, String code){  // generate all codewords
    if (n.symbol < 0){
      dfs(context, n.left, code + "0"); dfs(context, n.right, code + "1");
    }else codewords[context][n.symbol] = code;
  }

  void makeCodewords(){
    for (int context = 0; context < actualNumberOfSymbols; context++) 
       dfs(context, trees[context], "");
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
    int len = 0;  int context = 0;
   try {
    while ((len = fis.read(buffer)) >= 0){
      for (int i = 0; i < len; i++){
       int symbol = buffer[i];
       if (symbol < 0) symbol += 256;
       int a = actualSymbolIndex[symbol];
       outputbits(codewords[context][a]);
       context = a;
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
     System.err.println("Usage: java DE1D original > encoded");
     return;
    }
    DE1D de1 = new DE1D();
    de1.count(args[0]);
    de1.shrinkSymbols();
    de1.count2(args[0]);
    de1.makeTrees();
    de1.makeCodewords();
    de1.encode(args[0]); 
  }
}
