// DE11.java CS6025 Cheng 2016
// DE11test1.txt is a QRv1 high code (26,9,8)
// DE11 randomly chooses 8 bytes (errorPositions) and add random errors (errorMagnitudes) to them
// It then uses the 17 syndromes to solve for errorPositions
// Usage: java DE11 DE11test1.txt

import java.io.*;
import java.util.*;

public class DE11{

  static final int irreducible = 0x11d;
  static final int fieldSize = 256;
  static final int oneLessFieldSize = fieldSize - 1;
  static final int[] generator = new int[]{
    43, 139, 206, 78, 43, 239, 123, 206, 214, 147, 24, 99, 150, 
    39, 243, 163, 136 };
  static final int capacity = 26;
  static final int dataCapacity = 9;
  static final int correctionCapacity = 17;
  static final int maxErrors = 8;
  int[] G = new int[correctionCapacity + 1];
  int[] codewords = new int[capacity];
  int[] syndromes = new int[correctionCapacity];
  int[] errorPositions = new int[maxErrors];  // randomly generated error positions p(j)
  int[] errorMagnitudes = new int[maxErrors];  // randomly generated non-zero errors e(j)
  int[] locators = null; // error locators polynomial
  int[] alog = new int[fieldSize];
  int[] log2 = new int[fieldSize];
  Random random = new Random();

 void makeLog2(){
   alog[0] = 1;
   for (int i = 1; i < fieldSize; i++){
     alog[i] = (alog[i - 1] << 1);
     if ((alog[i] & 0x100) != 0) alog[i] ^= irreducible;
   }
   for (int i = 1; i < fieldSize; i++) log2[alog[i]] = i;
 }

 int inverse(int a){
   return alog[oneLessFieldSize - log2[a]];
 }

 int mul(int a, int b){
   if (a == 0 || b == 0) return 0;
   return alog[(log2[a] + log2[b]) % oneLessFieldSize];
 }

 void makeG(){
    G[0] = 1;
    for (int i = 0; i < correctionCapacity; i++)
      G[i + 1] = alog[generator[i]];
 }

 int evaluatePolynomial(int[] coefficients, int x){
    int len = coefficients.length;
   int sum = coefficients[0];
   for (int i = 1; i < len; i++)
     sum = mul(sum, x) ^ coefficients[i];
   return sum;
 }  

 void readCodewords(String filename){
   Scanner in = null;
   try {
     in = new Scanner(new File(filename));
   } catch (FileNotFoundException e){
     System.err.println(filename + " not found");
     System.exit(1);
   }
   String[] terms = in.nextLine().split(" ");
   for (int i = 0; i < capacity; i++) 
    codewords[i] = Integer.parseInt(terms[i]);
 }
   
 void computeSyndromes(){  // should all be zeros in this program
   for (int i = 0; i < correctionCapacity; i++)
     syndromes[i] = evaluatePolynomial(codewords, alog[i]);
   displayPolynomial("syndromes", syndromes);
 }

  void displayPolynomial(String title, int[] p) { // display array with title
    System.out.print(title + " [ ");
    for (int i = 0; i < p.length - 1; i++)
	System.out.print(p[i] + " ");
    System.out.println(p[p.length - 1] + " ]");
  }

  void addErrors(){ // randomly generate 8 error locations and error values
    for (int i = 0; i < maxErrors; i++){
      errorPositions[i] = random.nextInt(capacity);
      boolean unique = false;
      while (!unique){
	int j = 0; for (; j < i; j++) 
	  if (errorPositions[i] == errorPositions[j]) break;
	if (j == i) unique = true;
	else errorPositions[i] = random.nextInt(capacity);
      }
      errorMagnitudes[i] = random.nextInt(oneLessFieldSize) + 1;
      codewords[errorPositions[i]] ^= errorMagnitudes[i];  // adding errors
    }
		
    displayPolynomial("Random Error Positions", errorPositions);
    displayPolynomial("Random Error Magnitudes", errorMagnitudes);
    displayPolynomial("Message with Errors", codewords);
  } 

  int[] shiftPolynomial(int[] p){ // xp(x)
    int[] shifted = new int[p.length + 1];
    for (int i = 0; i < p.length; i++) shifted[i] = p[i];
    shifted[p.length] = 0;
    return shifted;
  }

  int[] scalePolynomial(int[] p, int a){ // ap(x)
     int[] scaled = new int[p.length];
     for (int i = 0; i < p.length; i++) scaled[i] = mul(p[i], a);
     return scaled;
  }

  int[] addPolynomials(int[] p, int[] q) { // p + q
    int[] tmp = new int[Math.max(p.length, q.length)];
    for (int i = 0; i < p.length; i++)
      tmp[i + tmp.length - p.length] = p[i];
    for (int i = 0; i < q.length; i++)
      tmp[i + tmp.length - q.length] ^= q[i];
    return tmp;
  }

  void solveLocatorsBerlekampMassey() {
    System.out.println("\nBerlekamp-Massey Decoder");
    int[] ep = new int[1]; ep[0] = 1;  // approximation for error locators poly
    int[] op = new int[1]; op[0] = 1;  
    int[] np = null;
    for (int i = 0; i < syndromes.length; i++) { // Iterate over syndromes
      op = shiftPolynomial(op);
      int delta = syndromes[i]; // discrepancy from
      for (int j = 1; j < ep.length; j++) // recurrence for syndromes
         delta ^= mul(ep[ep.length - 1 - j], syndromes[i - j]);
      if (delta != 0) { // has discrepancy
        if (op.length > ep.length){
      	  np = scalePolynomial(op, delta);
          op = scalePolynomial(ep, inverse(delta));
	  ep = np;
	}
        ep = addPolynomials(ep, scalePolynomial(op, delta));
        displayPolynomial(Integer.toString(i), ep); // display Berlekamp-Massey steps
      }
    }
    locators = ep;
  }	

    /*
      i to capacity
      take inverse then log of i
      
      next tues: review, next thurs: midterm
     */
    
 void solveErrorPositions2() { // find roots of error locators polynomial
    errorPositions = new int[maxErrors]; 
    for (int i = 0, j = 0; i < capacity; i++)
	if (evaluatePolynomial(locators, inverse(alog[i])) == 0) {
	    errorPositions[j++] = i;
	}
    displayPolynomial("Decoded Error Positions", errorPositions);
  }

 public static void main(String[] args){
   if (args.length < 1){
     System.err.println("Usage: java DE11 RS(26,9,8)code");
     System.exit(1);
   }
   DE11 de11 = new DE11();
   de11.makeLog2();
   de11.makeG();
   de11.readCodewords(args[0]);
   de11.computeSyndromes();
   de11.addErrors();
   de11.computeSyndromes();
   de11.solveLocatorsBerlekampMassey();
   de11.solveErrorPositions2();

 }
}

  
