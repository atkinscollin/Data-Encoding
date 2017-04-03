// DE5D.java CS6025 Cheng 2016
// Decoding Golomb code LG(2,32)
// Usage: java DE5D < encoded > decoded

import java.io.*;
import java.util.*;

public class DE5D{
   static final int GolombK = 2;
   static final int GolombLimit = 23;
   static final int[] powersOf2 = new int[]{
    1, 2, 4, 8, 16, 32, 64, 128 }; // used by deGolomb
   int buf = 0; int position = 0;


 int inputBit(){  // 0, 1, or -1 for end of file
   if (position == 0)
     try{
       buf = System.in.read();
       if (buf < 0){ return -1; }
            
       position = 0x80;
     }catch(IOException e){
        System.err.println(e);
        return -1;
     }
   int t = ((buf & position) == 0) ? 0 : 1;
   position >>= 1;  
   return t;
 }

 int deGolomb(){ // get the next codeword, return the symbol it encodes
   int value = 0;
   int q = 0;  
   int bit = -1;
   while ((bit = inputBit()) == 0) q++;
   if (bit < 0) return -1;
   if (q < GolombLimit){
     value = q << GolombK;
     for (int k = GolombK - 1; k >= 0; k--)
       if (inputBit() == 1) value += powersOf2[k];
   }else for (int k = 7; k >= 0; k--)  
       if (inputBit() == 1) value += powersOf2[k];
   return value;
 }

 void decode(){
  int symbol = -1; 
  while ((symbol = deGolomb()) >= 0)
    System.out.write(symbol);
  System.out.flush();
 }
   
 public static void main(String[] args){
  DE5D de5 = new DE5D();
  de5.decode();
 }
}
  