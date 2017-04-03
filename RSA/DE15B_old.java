// DE15B.java CS6025 Cheng 2016
// RSA-PSS verification 
// Usage:  java DE15B publicKey signature < message 

import java.io.*;
import java.util.*;
import java.math.*;
import java.security.*;

public class DE15B{

   static final int hLen = 20;
   static final int sLen = 20;
   static final int inBufferSize = 4096;
   Random rand = new Random();
   BigInteger n = null;
   int emBits;
   int emLen;
   BigInteger e = null;
   byte[] inBuffer = new byte[inBufferSize]; 
   int messageLen = 0;
   MessageDigest MD = null;
   byte[] mHash = null;
   byte[] padding1 = new byte[8];
   byte[] salt = null;
   byte[] H = null;
   byte[] Hprime = null;
   byte[] DB = null;
   byte[] dbMask = null;
   byte[] EM = null;
   BigInteger m = null;
   BigInteger s = null;


   void readPublicKey(String filename){
    Scanner in = null;
    try {
     in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
     e = new BigInteger(in.nextLine(), 16);
     n = new BigInteger(in.nextLine(), 16);
     in.close();
     emBits = n.bitLength() - 1;
     emLen = emBits % 8 > 0 ? emBits / 8 + 1 : emBits / 8;
   }

   void readSignature(String filename){
    Scanner in = null;
    try {
     in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
     s = new BigInteger(in.nextLine(), 16);
     in.close();
   }

   void verifyStep1(){
     byte[] inBuffer = new byte[inBufferSize]; 
     int messageLen = 0;
     try {
       MD = MessageDigest.getInstance("SHA-1");
     } catch (NoSuchAlgorithmException e){
       System.err.println(e.getMessage());
       System.exit(1);
     }
    do {
     try {
        messageLen = System.in.read(inBuffer);
     } catch (IOException e){
        System.err.println(e.getMessage());
        System.exit(1);
     }
     if (messageLen > 0) MD.update(inBuffer, 0, messageLen);
    } while (messageLen > 0);
     mHash = MD.digest();
   }

   void verifyStep2(){
     if (emLen < hLen + sLen + 2){
        System.err.println("Inconsistent: emLen too big");
        System.exit(1);
     }
   }

   void verifyStep3(){
     int lastByte = EM[emLen - 1];
     if (lastByte < 0) lastByte += 256;
     if (lastByte != 0xbc){
        System.err.println("Inconsistent: BC");
        System.exit(1);
     }
   }

   void verifyStep4(){  // opposite of encodingStep8, fill DB and H
       int border = emLen - hLen - 1;
       DB = new byte[border];
       H = new byte[hLen];
       for (int i = 0; i < border; i++) DB[i] = EM[i];
       for (int i = 0; i < hLen; i++) H[i] = EM[border + i];
   } 

   void verifyStep5(){  // checking the result of encodingStep7
       if (DB[0] == 0) { System.err.println("Consistent: DB zero"); }
       else { System.err.println("Inconsistent: DB zero"); }
   }

   void verifyStep6(){  //  identical to encodingStep5
       dbMask = MGF1(H, emLen - hLen - 1);
   }

   void verifyStep7(){  // identical to encodingStep6
       for (int i = 0; i < emLen - hLen - 1; i++)
       DB[i] ^= dbMask[i];
   }  

   void verifyStep8(){  // identical to encodingStep7
       int diff = 8 * emLen - emBits;
       int singleBit = 0x80;  int mask = 0xff;
       for (int i = 0; i < diff; i++){
	   mask ^= singleBit;
	   singleBit >>= 1;
       }
       DB[0] &= mask;
   }

   void verifyStep9(){  // checking padding2, or part of encodingStep4
       int border = emLen - hLen - sLen - 2;
       for (int i = 0; i < border; i++) {
	   if(DB[i] != 0) {
	       System.err.println("Inconsistent: DB, padding2");
	       System.exit(1);
	   }
       }
       System.err.println("Consistent: DB, padding2");
   }

   void verifyStep10(){  // opposite of part of encodingStep4, fill salt
       int border = emLen - hLen - sLen - 1;
       salt = new byte[sLen];
       for (int i = 0; i < sLen; i++) { salt[i] = DB[border + i]; }
   }

   void verifyStep11(){  // part of encodingStep2, fill padding1
       // salt is already there, do not randomly generate it again
       for (int i = 0; i < 8; i++) padding1[i] = 0;
   }
  
   void verifyStep12(){  // identical to encodingStep3, except naming the digest H'
       Hprime = new byte[hLen];
       MD.reset();
       MD.update(padding1);
       MD.update(mHash);
       Hprime = MD.digest(salt);
   }

   void verifyStep13(){ // compare H' with H or simply print them out
       System.out.println("H: " + H + "   H': " + Hprime);
       if(H == Hprime) {System.out.println("Consistent: H");}
       else {System.out.println("Inconsistent: H");}
   }

   byte[] MGF1(byte[] X, int maskLen){
     byte[] mask = new byte[maskLen];
     byte[] counter = new byte[4];
     for (int i = 0; i < 4; i++) counter[i] = 0;
     int k = maskLen % hLen > 0 ? maskLen / hLen : maskLen / hLen - 1;
     int offset = 0;
     for (byte count = 0; count <= k; count++){
       MD.reset(); MD.update(X); counter[3] = count; byte[] h = MD.digest(counter);
       for (int i = 0; i < hLen; i++) if (offset + i < maskLen) mask[offset + i] = h[i];
       offset += hLen;
     }
     return mask;
   }

  void decrypt(){
     m = s.modPow(e, n);// compute m from s
     EM = m.toByteArray();
     emLen = EM.length;
  }

   void verify(){
      decrypt();
      verifyStep1();
      verifyStep2();
      verifyStep3();  
      // step 4 to step 13 on page 411
      verifyStep4(); 
      verifyStep5();
      verifyStep6();
      verifyStep7();  
      verifyStep8();
      verifyStep9();
      verifyStep10();  
      verifyStep11();
      verifyStep12();
      verifyStep13();
   }
 

 public static void main(String[] args){
   if (args.length < 2){
     System.out.println("Usage: java H17B publicKey signature < message");
     return;
   }
   DE15B de15 = new DE15B();
   de15.readPublicKey(args[0]);
   de15.readSignature(args[1]);
   de15.verify();
 }
}
