// DE7A.java CS6025 Cheng 2016
// outputs irreducible polynomials over Z2
// Usage: java DE7A

import java.io.*;
import java.util.*;

public class DE7A{

  int add (int a, int b){ // polynomial addition over Z2
    return a ^ b;
  }

  int multiply (int a, int b){ // polynomial multiplication over Z2
    int product = 0;
    for (; b > 0; b >>= 1){
      if ((b & 1) > 0) product ^= a;
      a <<= 1;
    }
    return product;
  }

  void irreducibles(){
    HashSet<Integer> hset = new HashSet<Integer>(100); 
      // all products of polynomials of degree < 8
    for (int i = 2; i < 256; i++)
      for (int j = i; j < 256; j++) hset.add(multiply(i, j));
    for (int i = 0; i < 512; i++) // all irreducibles of degree < 9
      if (!hset.contains(i)) System.out.print(i + " ");
    System.out.println();
    for (int i = 0; i < 512; i++) // in hex
      if (!hset.contains(i)) System.out.print("0x" + Integer.toString(i, 16) + ", ");
    System.out.println();
  }

public static void main(String[] args){
   DE7A de7 = new DE7A();
   de7.irreducibles();
}
}
