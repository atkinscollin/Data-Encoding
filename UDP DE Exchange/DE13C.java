// DE13C.java CS6025 Cheng 2016
// checking primality of Group 5 q and (q-1)/2
// checking that 2 is a primitive element in GF(q) (a primitive root of q)
// Usage: java DE13C

import java.math.*;
import java.io.*;
import java.util.*;

public class DE13C{
  String hexQ = null;
  BigInteger q = null;
  BigInteger p = null;  // p = (q-1)/ 2
  static BigInteger two = new BigInteger("2");

  void readQ(String filename){
    Scanner in = null;
    try {
     in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    hexQ = in.nextLine();
    in.close();
    q = new BigInteger(hexQ, 16);
  }

 void testPrimality(){
   if (q.isProbablePrime(200)) 
    System.out.println("q is probably prime");
   // your code for (q-1)/2
   p = q.subtract(BigInteger.ONE).divide(two);
   if (p.isProbablePrime(200)) 
    System.out.println("p is probably prime");
 }

 void testPrimitiveness(){
   // compute pow(2, p) mod q
   BigInteger pq = two.modPow(p, q);
   System.out.println(pq.toString(16));
 }

 public static void main(String[] args){
   DE13C de13 = new DE13C();
   de13.readQ("DHgroup5.txt");
   de13.testPrimality();
   de13.testPrimitiveness();
 }
}

   
