// DE10.java CS6025 Cheng 2016
// compute and compare the error-correcting codewords in QR version 1 symbols
// Usage: java DE10

import java.io.*;
import java.util.*;

public class DE10{

  static final int maxSize = 200;
  static final int irreducible = 0x11d;
  static final int fieldSize = 256;
  static final int oneLessFieldSize = fieldSize - 1;
  static final int[] generator = new int[]{
    43, 139, 206, 78, 43, 239, 123, 206, 214, 147, 24, 99, 150, 
    39, 243, 163, 136 };
  static final int capacity = 26;
  static final int dataCapacity = 9;
  static final int correctionCapacity = 17;
  int[] G = new int[correctionCapacity + 1];
  int[] codewords = new int[capacity];
  int[] alog = new int[fieldSize];
  int[] log2 = new int[fieldSize];
  Random random = new Random();

 void makeLog2(){
   alog[0] = 1;
   for (int i = 1; i < fieldSize; i++){
     alog[i] = (alog[i - 1] << 1);
     if ((alog[i] & 0x100) != 0) alog[i] ^= irreducible;
   }
   for (int i = 1; i < fieldSize; i++) log2[alog[i]] = i;
 }

 int inverse(int a){
   return alog[oneLessFieldSize - log2[a]];
 }

 int mul(int a, int b){
   if (a == 0 || b == 0) return 0;
   return alog[(log2[a] + log2[b]) % oneLessFieldSize];
 }

 void makeG(){
    G[0] = 1;
    for (int i = 0; i < correctionCapacity; i++)
      G[i + 1] = alog[generator[i]];
 }


 void getRemainder(){
     int check;
     for (int i = dataCapacity; i < capacity; i++) codewords[i] = 0;
     for (int i = 0; i < dataCapacity; i++) if (codewords[i] != 0){
	     for (int j = 0; j < correctionCapacity; j++){
		 // your code for multiplying G(j + 1) with codewords[i] 
		 // then subtracting it at position i + j + 1
		 codewords[i] = codewords[i] / G[j+1];
	     }
	 }
     for (int i = 0; i < correctionCapacity; i++){
	 System.out.println(codewords[dataCapacity + i]);
     }
 }


 int evaluatePolynomial(int[] coefficients, int x){
   // coefficients are those for a polynomial starting with the one
   // for the term x to the highest power which is coefficients.length - 1
   int len = coefficients.length;
   int sum = coefficients[0];
   for (int i = 1; i < len; i++)
     sum = mul(sum, x) ^ coefficients[i];
   return sum;
 }  

 void checkG(){
   for (int i = 0; i < correctionCapacity; i++)
     System.out.println(evaluatePolynomial(G, alog[i]));
 }

 void initialCodewords(){
   for (int i = 0; i < capacity; i++) 
    codewords[i] = random.nextInt(fieldSize);
 }
   


 void computeSyndromes(){
   for (int i = 0; i < correctionCapacity; i++)
     System.out.println(evaluatePolynomial(codewords, alog[i]));
 }


 public static void main(String[] args){
   DE10 de10 = new DE10();
   de10.makeLog2();
   de10.makeG();
   de10.checkG();
   de10.initialCodewords();
   de10.getRemainder();
   de10.computeSyndromes();
 }
}

  
