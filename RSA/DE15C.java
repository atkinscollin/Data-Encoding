// DE15C.java CS6025 Cheng 2016
// RSA Key generation 
// Usage:  java DE15C > privateKey.txt

import java.lang.*;
import java.util.*;
import java.math.*;

public class DE15C{

   Random rand = new Random();
   BigInteger p = new BigInteger(1024, 200, rand);
   BigInteger q = new BigInteger(1024, 200, rand);
   BigInteger n = p.multiply(q);
   BigInteger phi = p.subtract(BigInteger.ONE).multiply(
                  q.subtract(BigInteger.ONE));
   BigInteger e = new BigInteger("65537");
   BigInteger d = e.modInverse(phi);

   void printKey(){
     System.out.println(e.toString(16));
     System.out.println(n.toString(16));
     System.out.println(d.toString(16));
   }

 public static void main(String[] args){
   DE15C de15 = new DE15C();
   de15.printKey();
 }
}
