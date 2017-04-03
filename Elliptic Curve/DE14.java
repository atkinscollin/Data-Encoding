// DE14.java CS6025 Cheng 016
// Elliptic curve cryptography
// Usage: java DE14 ECP256.txt

import java.math.*;
import java.io.*;
import java.util.*;

class Point{
  public BigInteger x; 
  public BigInteger y;
  static Point O = new Point(null, null);
  public Point(BigInteger xx, BigInteger yy){ x = xx; y = yy; }
  public String toString(){
    return this.equals(O) ? "O" :
    "(" + x.toString(16) + ",\n" + y.toString(16) + ")";
  }
}

public class DE14{

  static BigInteger three = new BigInteger("3");
  static BigInteger zero = new BigInteger("0");
  static final int privateKeySize = 255;
  BigInteger p; // modulus
  Point G; // base point
  BigInteger a;  // curve parameter
  BigInteger b;  // curve parameter
  BigInteger n;  // order of G
  BigInteger privateKeyA;
  Point publicKeyA;
  BigInteger privateKeyB;
  Point publicKeyB;
  Random random = new Random();

void readCurveSpecs(String filename){
    Scanner in = null;
    try {
     in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    p = new BigInteger(in.nextLine(), 16);
    n = new BigInteger(in.nextLine(), 16);
    a = new BigInteger(in.nextLine(), 16);
    b = new BigInteger(in.nextLine(), 16 );
    G = new Point(new BigInteger(in.nextLine(), 16), new BigInteger(in.nextLine(), 16));
    in.close();
}

Point add(Point P1, Point P2){
   if (P1.equals(Point.O)) return P2;
   if (P2.equals(Point.O)) return P1;
   if (P1.x.equals(P2.x)) if (P1.y.equals(P2.y)) return selfAdd(P1);
                          else return Point.O;
   BigInteger t1 = P1.x.subtract(P2.x).mod(p);
   BigInteger t2 = P1.y.subtract(P2.y).mod(p);
   BigInteger k = t2.multiply(t1.modInverse(p)).mod(p); // slope
   t1 = k.multiply(k).subtract(P1.x).subtract(P2.x).mod(p); // x3
   t2 = P1.x.subtract(t1).multiply(k).subtract(P1.y).mod(p); // y3
   return new Point(t1,t2);
}   

Point selfAdd(Point P){
     if (P.equals(Point.O)) return Point.O; // O+O=O
     BigInteger t1 = P.y.add(P.y).mod(p);  // 2y
     BigInteger t2 = P.x.multiply(P.x).mod(p).multiply(three).add(a).mod(p); // 3xx+a
     BigInteger k = t2.multiply(t1.modInverse(p)).mod(p); // slope or tangent
     t1 = k.multiply(k).subtract(P.x).subtract(P.x).mod(p); // x3 = kk-x-x
     t2 = P.x.subtract(t1).multiply(k).subtract(P.y).mod(p); // y3 = k(x-x3)-y
     return new Point(t1,t2);
}

Point multiply(Point P, BigInteger n){
     if(n.equals(BigInteger.ZERO)) return Point.O;  
     int len = n.bitLength();  // position preceding the most significant bit 1
     Point product = P;
     for(int i = len - 2; i >= 0; i--){
        product = selfAdd(product); 
        if (n.testBit(i)) product = add(product, P);
     }
     return product;
  }

 void checkParameters(){
   BigInteger lhs = G.y.multiply(G.y).mod(p);
   BigInteger rhs = G.x.modPow(three, p).add(G.x.multiply(a).mod(p)).add(b).mod(p);
   System.out.println("lhs: " + lhs.toString(16));
   System.out.println("rhs: " + rhs.toString(16)); // These two lines should be the same
   Point power = multiply(G, n);
   System.out.println("O: " + power + "\n");  // This should be O
 }

 void generateKeys(){
    privateKeyA = new BigInteger(privateKeySize, random);
    publicKeyA = multiply(G, privateKeyA);
    privateKeyB = new BigInteger(privateKeySize, random);
    publicKeyB = multiply(G, privateKeyB);
 }

 void sharedSecret(){  // Figure 10.7 of book
    Point KA = multiply(publicKeyB, privateKeyA);  // secret computed by A
    Point KB = multiply(publicKeyA, privateKeyB);  // secret computed by B
    System.out.println("KA: " + KA);
    System.out.println("KB: " + KB + "\n");
 }  

 void encryptionForB(){  // page 305 of book
   BigInteger message = new BigInteger(privateKeySize, random);
   System.out.println("message: " + message.toString(16));
   BigInteger k = new BigInteger(privateKeySize, random);

   Point kG = multiply(G, k); // k times G
   Point kY = multiply(publicKeyB, k); // k times publicKeyB
   BigInteger mu = kY.x.multiply(message).mod(p);  // message times kY.x mod p
   System.out.println("kG: " + kG);
   System.out.println("mu: " + mu.toString(16));

   // (kG, mu) is the encrypted message
   Point kY2 = multiply(kG, privateKeyB);  // B computes kY as privateKeyB times kG
   // kY2.x modinverse times mu mod p
   BigInteger decodedMessage = kY2.x.modInverse(p).multiply(mu).mod(p);
   System.out.println("decoded msg: " + decodedMessage.toString(16) + "\n");
 }

 void ECDSA(){  // 13.5 of book
   BigInteger message = new BigInteger(160, random);  // this is e = H(m) in book
   System.out.println("message: " + message.toString(16));
   BigInteger k = new BigInteger(privateKeySize, random);

   Point kG = multiply(G, k); // k times G
   BigInteger r = kG.x.mod(n); // r = kG.x mod n
   // s = k's modInverse times (message plus privateKeyB times r) mod n
   BigInteger s = k.modInverse(n).multiply(privateKeyB.multiply(r).add(message)).mod(n);
   // if r or s == 0 redo k
   // (r,s) is the signature for message (digest)
   if (r.equals(Point.O) || s.equals(Point.O)) {ECDSA();}
   System.out.println("r: " + r.toString(16));
   System.out.println("s: " + s.toString(16));

   BigInteger w = s.modInverse(n); // w is s's multiplicative inverse mod n
   BigInteger u1 = w.multiply(message); // u1 = message times w
   BigInteger u2 = w.multiply(r); // u2 = r times w

   // X is u1 times G + u2 times publicKeyB
   Point X = add(multiply(publicKeyB, u2), multiply(G, u1));
   BigInteger v = X.x.mod(n);
   System.out.print("v: " + v.toString(16) + "\n");
   if (X.equals(Point.O)) { System.out.print("\nSignature Rejected; X equal to O.\n"); }
   else if (v.equals(r)) { System.out.print("\nSignature Accepted; v equal to r.\n"); }
   else { System.out.print("\nSignature Rejected\n"); }
 }
   
   
public static void main(String[] args){
   DE14 de14 = new DE14();
   de14.readCurveSpecs(args[0]);
   de14.checkParameters();
   de14.generateKeys();
   de14.sharedSecret();
   de14.encryptionForB();
   de14.ECDSA();
}
}

  
