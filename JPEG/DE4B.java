// DE4B.java CS6025 Cheng 2016
// Inverse of DE4A.java, decoding JPEGLS
// Usage: java DE4B < encoded.de4 > restored.bmp

import java.io.*;
import java.util.*;

public class DE4B{
   static int borderValue = 128; // a,b,c for x on first row and column
   int width, height;  // image dimensions
   int[][][] raw;      // the raw image stored here
   

 void readHeader(){
   byte[] inHeader = new byte[4];
   byte[] outHeader = new byte[54];
   for (int i = 0; i < 54; i++) outHeader[i] = 0;
   outHeader[0] = 'B'; outHeader[1] = 'M';
   outHeader[14] = 40; outHeader[28] = 24;
   try {
      System.in.read(inHeader);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
   int w1 = outHeader[18] = inHeader[0]; 
   int w2 = outHeader[19] = inHeader[1];
   if (w1 < 0) w1 += 256; if (w2 < 0) w2 += 256;
   width = w2 * 256 + w1;
   int h1 = outHeader[22] = inHeader[2]; 
   int h2 = outHeader[23] = inHeader[3];
   if (h1 < 0) h1 += 256; if (h2 < 0) h2 += 256;
   height = h2 * 256 + h1;
   try {
     System.out.write(outHeader);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
 }

  int predict(int a, int b, int c){
     int x;
     if ((c >= a) && (c >= b)) x = (a >= b) ? b : a;
     else if ((c <= a) && (c <= b)) x = (a >= b) ? a : b;
     else x = a + b - c;        
     return x;
  }


 public void deJpegls(){
   int a, b, c;
   for (int i = 0; i < height; i++)   //  find the neighboring pixels
     for (int j = 0; j < width; j++)
       for (int k = 0; k < 3; k++){
        if (j == 0) a = borderValue; 
        else a = raw[i][j - 1][k];
        if (i == 0){ 
           b = c = borderValue;
        }else{
           if (j == 0) c = borderValue;
           else c = raw[i - 1][j - 1][k];
           b = raw[i - 1][j][k];
        }
        int prediction = predict(a, b, c);  
        raw[i][j][k] = unmapError(raw[i][j][k], prediction);
        System.out.write(raw[i][j][k]);
     }
   System.out.flush();
  }

  int unmapError(int error, int predicted){
      int e = 0;
      if((error % 2) == 0) {
	  error = error/2;
	  e = error;}
      else{
	  error = ((error+1) * -1)/2;
	  e = error;}
	  // Your code for computing e from error
          // need inverse of error = f(e) as the following line in DE4A.mapError():
          //    int error = (e >= 0) ? e * 2 : -e * 2 - 1;
          //  e = f^-1(error);  f^-1 is the inverse of f above.
     int value = predicted + e;
     if (value > 255) value -= 256;
     else if (value < 0) value += 256;
     return value;
  }

 void readJpegls(){
   byte[] jpegls = new byte[height * width * 3];
   raw = new int[height][width][3];
   try {
      System.in.read(jpegls);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
   int index = 0;
   for (int i = 0; i < height; i++)
    for (int j = 0; j < width; j++)
      for (int k = 0; k < 3; k++){
        raw[i][j][k] = jpegls[index++];
        if (raw[i][j][k] < 0) raw[i][j][k] += 256;
   }
 }


 public static void main(String[] args){
  DE4B de4 = new DE4B();
  de4.readHeader();
  de4.readJpegls();
  de4.deJpegls();
 }
}


