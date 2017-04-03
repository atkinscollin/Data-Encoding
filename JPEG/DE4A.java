// DE4A.java CS6025 Cheng 2016
// JPEGLS prediction error of a BMP image with depth 24
// Usage: Java DE4A < image.bmp > encoded.de4

import java.io.*;
import java.util.*;

public class DE4A{
   static int borderValue = 128; // a,b,c for x on first row and column
   int width, height;  // image dimensions
   int[][][] raw;      // the raw image stored here


 void readHeader(){
   byte[] header = new byte[54];
   try {
      System.in.read(header);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
   if (header[0] != 'B' || header[1] != 'M'
      || header[14] != 40 || header[28] != 24){
     System.err.println("wrong file format");
     System.exit(1);
   }
   int w1 = header[18]; int w2 = header[19];
   System.out.write(w1); System.out.write(w2);
   if (w1 < 0) w1 += 256; if (w2 < 0) w2 += 256;
   width = w2 * 256 + w1;
   int h1 = header[22]; int h2 = header[23];
   System.out.write(h1); System.out.write(h2);
   if (h1 < 0) h1 += 256; if (h2 < 0) h2 += 256;
   height = h2 * 256 + h1;
 }

 void readImage(){
   byte[] image = new byte[height * width * 3];
   raw = new int[height][width][3];
   try {
      System.in.read(image);
   } catch (IOException e){
     System.err.println(e.getMessage());
     System.exit(1);
   }
   int index = 0;
   for (int i = 0; i < height; i++)
    for (int j = 0; j < width; j++)
      for (int k = 0; k < 3; k++){
        raw[i][j][k] = image[index++];
        if (raw[i][j][k] < 0) raw[i][j][k] += 256;
   }
 }


 public void jpegls(){
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
        System.out.write(mapError(raw[i][j][k], prediction));
     }
   System.out.flush();

  }

  int predict(int a, int b, int c){
     int x;
     if ((c >= a) && (c >= b)) x = (a >= b) ? b : a;
     else if ((c <= a) && (c <= b)) x = (a >= b) ? a : b;
     else x = a + b - c;        
     return x;
  }

// map the prediction error to nonnegatives
  int mapError(int value, int prediction){
     int e = value - prediction;         //  prediction error
     if (e > 127) e -= 256;     //  putting error in [-128, 127]
     else if (e < -128) e += 256;
     int error = (e >= 0) ? e * 2 : -e * 2 - 1;  //  into 0 -1 1 -2 2 array
     return error;
  }


 public static void main(String[] args){
   DE4A de4 = new DE4A();
   de4.readHeader();
   de4.readImage();
   de4.jpegls();
 }
}


    
