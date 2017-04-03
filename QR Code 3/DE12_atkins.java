// DE12.java CS6025 Cheng 2016
// Decoding Reed-Solomon
// computing error magnitudes from error positions and syndromes
// QR version 1 error correction level H mask 001 only
// Usage: java DE12 DE12test1.txt

import java.io.*;
import java.util.*;

public class DE12{

  static final int maxErrors = 8;  // max number of errors to correct
  static final int irreducible = 0x11d; // for GF(2^8)
  static final int fieldSize = 256; // GF(2^8) size
  static final int oneLessFieldSize = fieldSize - 1;  // modulus for addition of log2
  static final int[] generator = new int[]{  // log2 of coeffs of generator polynomial
    43, 139, 206, 78, 43, 239, 123, 206, 214, 147, 24, 99, 150, 
    39, 243, 163, 136 };
  static final int capacity = 26; // total number of codewords/bytes in data region
  static final int correctionCapacity = 17;  // number of codewords or error correction
  int[] codewords = new int[capacity]; 
  int[] alog = new int[fieldSize];  // powers of 2 in GF(2^8)
  int[] log2 = new int[fieldSize];  // log2 of non-zero elements in GF(2^8)
  int[] errorPositions = new int[maxErrors];  // randomly generated error positions p(j)
  int[] errorMagnitudes = new int[maxErrors];  // randomly generated non-zero errors e(j)
  int[] syndromes = new int[correctionCapacity];  // evaluation of codwords on 2^i
  int[] locators = null; // error-location polynomial
  int[] Z = null;
  Random random = new Random();
	
  void makeLog2(){ // alog is powers of 2 in GF(2^8) and log2 is discret log
    alog[0] = 1;
    for (int i = 1; i < fieldSize; i++){
	alog[i] = (alog[i - 1] << 1);
	if ((alog[i] & 0x100) != 0) alog[i] ^= irreducible;
    }
    for (int i = 1; i < fieldSize; i++) log2[alog[i]] = i;
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
      codewords[capacity - 1 - errorPositions[i]] ^= errorMagnitudes[i];  // adding errors
    }
		
    displayPolynomial("Random Error Positions", errorPositions);
    displayPolynomial("Random Error Magnitudes", errorMagnitudes);
    displayPolynomial("Message with Errors", codewords);
  } 

  int inverse(int a){  // multiplicative inverse of (non-zero) a in GF(2^8)
    return alog[oneLessFieldSize - log2[a]];
  }

  int mul(int a, int b){ // multiplication in GF(2^8)
    if (a == 0 || b == 0) return 0;
    return alog[(log2[a] + log2[b]) % oneLessFieldSize];
  }

  int evaluatePolynomial(int[] p, int x){ // what is p(x)
    int len = p.length;
    int sum = p[0];
    for (int i = 1; i < len; i++)
	sum = mul(sum, x) ^ p[i];
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


  void computeSyndromes(){ // syndromes are codewords(2^i)
    for (int i = 0; i < correctionCapacity; i++)
	syndromes[i] = evaluatePolynomial(codewords, alog[i]);
    displayPolynomial("Syndromes", syndromes);
  }

  void displayPolynomial(String title, int[] p) { // display array with title
    System.out.print(title + " [ ");
    for (int i = 0; i < p.length - 1; i++)
	System.out.print(p[i] + " ");
    System.out.println(p[p.length - 1] + " ]");
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
        displayPolynomial("Iteration " + i, ep); // display Berlekamp-Massey steps
      }
    }
    locators = ep;
    displayPolynomial("Error-Location Polynomial", locators);
  }

  void solveErrorPositions() { // find roots of error locators polynomial
    errorPositions = new int[maxErrors]; 
    for (int i = 0, j = 0; i < capacity; i++)
	if (evaluatePolynomial(locators, inverse(alog[i])) == 0)  //alog[oneLessFieldSize - i]) == 0)
	  errorPositions[j++] = i;
    displayPolynomial("Decoded Error Positions", errorPositions);
  }

  void computeZ(){  
    int locatorsLen = locators.length;
    Z = new int[locatorsLen];
    for (int i = 0; i < locatorsLen; i++){
        Z[i] = locators[i];  System.out.print(Z[i]);
        for (int j = i + 1; j < locatorsLen; j++){
           Z[i] ^= mul(locators[j] , syndromes[j - i]);
        }
    }
    displayPolynomial("Z", Z); 
  }

  void computeErrorMagnitudes(){
    int[] beta = new int[maxErrors];
    int[] betaInverse = new int[maxErrors];
    for (int i = 0; i < maxErrors; i++){
      beta[i] = alog[errorPositions[i]];
      betaInverse[i] = inverse(beta[i]);
    }
    displayPolynomial("beta", beta);
    displayPolynomial("betaInverse", betaInverse);

    int[] e = new int[maxErrors];
    for (int i = 0; i < maxErrors; i++){
	int denominator = 1;
	for (int j = 0; j < maxErrors; j++) {
	    if (j != i){
		denominator = mul(denominator, (1 + mul(beta[j], betaInverse[i])) );
		e[i] = mul(mul(Z[i], betaInverse[i]), inverse(denominator));
	    }
	}
     }
    displayPolynomial("e", e); 

  }

  public static void main(String[] args){
    DE12 de12 = new DE12();
    de12.makeLog2();
    de12.readCodewords(args[0]);
    de12.addErrors();
    de12.computeSyndromes();
    de12.solveLocatorsBerlekampMassey();
    de12.solveErrorPositions();
    de12.computeZ();
    de12.computeErrorMagnitudes();
  }
}
