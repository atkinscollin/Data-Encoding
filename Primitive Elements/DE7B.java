// DE7B.java CS6025 Cheng 2016
// Outputs primitive elements of GF(2^n) with different irreducibles
// Usage: java DE7B

import java.io.*;
import java.util.*;

public class DE7B{

  static final int numberOfBits = 8;
  static final int fieldSize = 1 << numberOfBits;
  static final int[] irreducibles = new int[]{  // all degree 8 irreducible polynomials
0x11b, 0x11d, 0x12b, 0x12d, 0x139, 0x13f, 0x14d, 0x15f, 0x163, 0x165, 0x169, 0x171, 0x177, 0x17b, 0x187, 0x18b, 0x18d, 0x19f, 0x1a3, 0x1a9, 0x1b1, 0x1bd, 0x1c3, 0x1cf, 0x1d7, 0x1dd, 0x1e7, 0x1f3, 0x1f5, 0x1f9 };

  int modMultiply(int a, int b, int m){
    int product = 0;
    for (; b > 0; b >>= 1){
      if ((b & 1) > 0) product ^= a;
      a <<= 1;
      if ((a & fieldSize) > 0) a ^= m;
    }
    return product;
  }    

  void findPrimitiveElements(int irreducible){
  // Currently this function prints orders of all elements from 2 to 255
  // You need to modify it so only primitive elements smaller than 10 are printed.
    for (int i = 2; i < 10; i++){
      int power = i; 
      int j = 2; for (; j < fieldSize; j++){
          power = modMultiply(i, power, irreducible);
          if (power == 1) break;
      } 
      if (j < fieldSize) {} System.out.printf("[%s, %s] ", i, j);
    }
  }

  void smallPrimitiveElements(){  // call this function from main instead
      for (int i = 0; i < irreducibles.length; i++) {
	  findPrimitiveElements(irreducibles[i]);
	  System.out.println("");
      }
  }


public static void main(String[] args){
   DE7B de7 = new DE7B();
   //de7.findPrimitiveElements(de7.irreducibles[0]); //Replaced by smallPrimEle()
   System.out.println("[Prim Ele, Order]");
   de7.smallPrimitiveElements();
}
}
