// DE1A.java CS6025 Cheng 2016
// entropy of a file
// Usage: java DE1A filename

import java.io.*;
import java.util.*;

public class DE1A{

  static final int numberOfSymbols = 256;
  static final int blockSize = 1024;
  int[] freq = new int[numberOfSymbols];
  int actualNumberOfSymbols = 0;  // number of symbols with freq > 0
  int filesize = 0;

  void count(String filename){ // count symbol frequencies
    byte[] buffer = new byte[blockSize];
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    int len = 0;
    for (int i = 0; i < numberOfSymbols; i++) freq[i] = 0;
   try {
    while ((len = fis.read(buffer)) >= 0){
      for (int i = 0; i < len; i++){
       int symbol = buffer[i];
       if (symbol < 0) symbol += 256;
       freq[symbol]++;
      }
      filesize += len;
    }
    fis.close();
   } catch (IOException e){
      System.err.println("IOException");
      System.exit(1);
   }
  }

 void entropy(){
   double sum = 0;
   for (int i = 0; i < numberOfSymbols; i++) if (freq[i] > 0){
     actualNumberOfSymbols++;
     sum += freq[i] * Math.log(((double)freq[i]) / filesize);
   }
   sum /= filesize * Math.log(2.0);
   System.out.println(actualNumberOfSymbols + " " + -sum);
 }

 public static void main(String[] args){
  DE1A de1 = new DE1A();
  de1.count(args[0]);
  de1.entropy();
 }
}
