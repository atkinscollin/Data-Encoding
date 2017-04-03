// DE5B.java CS6025 Cheng 2016
// Decode Gamma coded file
// Usage: java DE5B < encoded > original

import java.io.*;
import java.util.*;

class DE5B{
  static final int[] powersOf2 = new int[]{
    1, 2, 4, 8, 16, 32, 64, 128 };
  int length = 0;  // length of block
  int buf = 0; int position = 0;


 int inputBit(){ 
   if (position == 0)
     try{
       buf = System.in.read();
       if (buf < 0){ return -1; }
            
       position = 0x80;
     }catch(IOException e){
        System.err.println(e);
        return -1;
     }
   int t = ((buf & position) == 0) ? 0 : 1;
   position >>= 1;
   return t;
 }

 int deGamma(){
  int bit = -1; 
  int offsetLength = 0;
  while ((bit = inputBit()) == 1) {offsetLength++;}
  if (bit < 0) {return -1;}
  int number = powersOf2[offsetLength];
  //System.out.print("off: " + offsetLength);
  //System.out.print("   num: " + number + "\n");
  // Your code to add to number
  // powersOf2[i] for those positions i with inputBit() == 1
  /*for(int i = offsetLength-1; i >= 0; i--) {
      bit = inputBit();
      //System.out.print("bit: " + bit + "   i: " + i);
      if (bit < 0) {return -1;}
      if (bit == 1) {number += powersOf2[i];}
      //System.out.print("   number: " + number + "\n");
  }*/
  for(int i = 1; i <= offsetLength; i++) {
      bit = inputBit();
      //System.out.print("bit: " + bit + "   i: " + i);
      if (bit < 0) {return -1;}
      if (bit == 1) {number += powersOf2[offsetLength - i];}
      //System.out.print("   number: " + number + "\n");
  }
  return (number-1);
 }
 
 void decode(){
  int c = 0;
  while ((c = deGamma()) >= 0) System.out.write(c);
 }

 public static void main(String[] args){
  DE5B de5 = new DE5B();
  de5.decode();
 }
}
