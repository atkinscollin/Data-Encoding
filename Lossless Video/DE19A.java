// DE19A.java CS6025 Cheng 2016
// lossless YUV video compression for 200 frames of CIF 352x288 
// Usage: java DE19A < yuvFile > compressed

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class DE19A{

 static final int width = 352;
 static final int height = 288;
 static final int numberOfFrames = 200;
 static final int frameSize = width * height * 3 / 2;
 byte[] buffer = new byte[frameSize * numberOfFrames];
 byte[] result = new byte[numberOfFrames * frameSize];
 int compressedDataLength = 0;

 void readFrames(){
    try{
      System.in.read(buffer);
    }catch(IOException e){ 
       System.err.println("IOException");
       System.exit(1); 
    }
 }

 void differential(){
   for (int frame = numberOfFrames - 1; frame > 0; frame--){
    int frameBase = frame * frameSize;
    for (int j = 0; j < frameSize; j++){
      int v = buffer[frameBase + j];
      int u = buffer[frameBase - frameSize + j];
      int diff = v - u;
      buffer[frameBase + j] = (byte)diff;
    }
   }
 }

 void compress(){
     Deflater compresser = new Deflater();
     compresser.setInput(buffer);
     compresser.finish();
     compressedDataLength = compresser.deflate(result);
     System.out.write(result, 0, compressedDataLength);
     System.out.flush();
 }

 public static void main(String[] args){
   DE19A de19 = new DE19A();
   de19.readFrames();
   de19.differential();
   de19.compress();
 }
}

