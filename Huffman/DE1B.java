// DE1B.java CS6025 Cheng 2016
// context entropy of a file
// Usage: java DE1B filename

import java.io.*;
import java.util.*;

public class DE1B{

  static final int numberOfSymbols = 256;
  static final int blockSize = 1024;
  int[] freq = new int[numberOfSymbols];
  int[][] freq2 = null;
  int actualNumberOfSymbols = 0;  // number of symbols with freq > 0
  int filesize = 0;
  int[] actualSymbolIndex = new int[numberOfSymbols];

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

 void shrinkSymbols(){
   for (int i = 0; i < numberOfSymbols; i++)
       actualSymbolIndex[i] = freq[i] > 0 ? actualNumberOfSymbols++ : -1;
  }

  void count2(String filename){ // count symbol frequencies
    freq2 = new int[actualNumberOfSymbols][actualNumberOfSymbols];
    byte[] buffer = new byte[blockSize];
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    int len = 0;
    for (int i = 0; i < actualNumberOfSymbols; i++)
      for (int j = 0; j < actualNumberOfSymbols; j++) freq2[i][j] = 0;
    int context = 0;
   try {
    while ((len = fis.read(buffer)) >= 0){
      for (int i = 0; i < len; i++){
       int symbol = buffer[i];
       if (symbol < 0) symbol += 256;
       int a = actualSymbolIndex[symbol];
       freq2[context][a]++;
       context = a;
      }
    }
    fis.close();
   } catch (IOException e){
      System.err.println("IOException");
      System.exit(1);
   }
  }

 void entropy2(){
   double sum = 0;
   for (int i = 0; i < actualNumberOfSymbols; i++){
    int freq = 0;
    for (int j = 0; j < actualNumberOfSymbols; j++) if (freq2[i][j] > 0) freq += freq2[i][j];
    double sum2 = 0;
    for (int j = 0; j < actualNumberOfSymbols; j++) if (freq2[i][j] > 0)
     sum2 += freq2[i][j] * Math.log(((double)freq2[i][j]) / freq);
    sum2 /= freq * Math.log(2.0);
    sum += sum2 * freq / filesize;
   }
   System.out.println(-sum);
 }

 public static void main(String[] args){
  DE1B de1 = new DE1B();
  de1.count(args[0]);
  de1.shrinkSymbols();
  de1.count2(args[0]);
  de1.entropy2();
 }
}
