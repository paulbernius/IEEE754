import java.util.Arrays;
import java.util.Scanner;

public class IEEE754 {

	/**
	 *
	 * Paul Bernius
	 * A20296830
	 *
	 * CS 3443
	 * PGM-IEEE754
	 * April 22, 2022
	 *
	 */

	public static void main(String[] args) {

		while (true) {

			String operand1, operand2, operator; // Initialize variables
			Scanner scnr = new Scanner(System.in); // Initialize scanner

			System.out.print("Enter operand1 (decimal) ('q' to quit): ");
			operand1 = scnr.nextLine(); // Get operand1  from user

			if (operand1.equalsIgnoreCase("Q") || operand1.equalsIgnoreCase("quit")) { // if operand1 == 'q' quit the program
				break;
			}

			System.out.print("Enter operand2 (decimal): ");
			operand2 = scnr.nextLine(); // Get operand2 from user

			System.out.print("Enter operator (add or mult): ");
			operator = scnr.nextLine(); // Get operator from user

			IEEE754Representation operand1IEEE = IEEE754Representation.getIEEERepresentation(Double.parseDouble(operand1)); // Create operand1 object
			IEEE754Representation operand2IEEE = IEEE754Representation.getIEEERepresentation(Double.parseDouble(operand2)); // Create operand2 object

			System.out.println("Operand 1: " + operand1IEEE.getIEEEFormat() + "\n" + "Operand 2: " + operand2IEEE.getIEEEFormat()); // Display operands in IEEE754 representation

			if (operator.equalsIgnoreCase("add")) { // If operator == 'add', add the two numbers
				IEEE754Representation res = IEEE754Representation.add(operand1IEEE, operand2IEEE); // Create result object contains the result of the addition
				System.out.println("Sum:       " + res.getIEEEFormat() + "\n"); // Print Sum
			} else if (operator.equalsIgnoreCase("mult")) { // If operator == 'add', add the two numbers
				IEEE754Representation res = IEEE754Representation.mult(operand1IEEE, operand2IEEE); // Create result object contains the result of the multiplication
				System.out.println("Product:   " + res.getIEEEFormat() + "\n"); // Print Product
			} else { // Error, enter inputs again
				System.out.println("Error in operator, input values again.");
			}

		}
	}

}

class IEEE754Representation {

	int signBit; // Signed bit variable
	int[] biasedExponent, mantissa; // Exponent and mantissa variables

	// Multiple overloaded constructors to handle all variable types

	IEEE754Representation() { // Default constructor

		// Initialize variables

		this.signBit = 0;
		this.biasedExponent = new int[1];
		this.mantissa = new int[1];
	}

	IEEE754Representation(int signBit, int[] biasedExponent, int[] mantissa) { // Overloaded constructor
		// Initialize variables

		this.signBit=signBit;

		this.biasedExponent=new int[8];
		System.arraycopy(biasedExponent, 0, this.biasedExponent, 0, this.biasedExponent.length);

		this.mantissa=new int[23];
		System.arraycopy(mantissa, 0, this.mantissa, 0, this.mantissa.length);
	}

	IEEE754Representation(int signBit, int biasedExponent, int[] mantissa) { // Overloaded constructor
		// Initialize variables

		this.signBit = signBit;

		this.biasedExponent = new int[8];
		String x = Integer.toBinaryString(biasedExponent);
		while (x.length() < 8){
			x = "0" + x;
		}
		for (int i = 0; i < 8; i++) {
			this.biasedExponent[i] = x.charAt(i) - 48;
		}

		this.mantissa = new int[23];
		System.arraycopy(mantissa, 0, this.mantissa, 0, this.mantissa.length);

	}

	IEEE754Representation(int signBit, int biasedExponent, String mantissa) { // Overloaded constructor
		// Initialize variables

		this.signBit=signBit;

		this.biasedExponent=new int[8];
		String x=Integer.toBinaryString(biasedExponent);
		while (x.length()<8) {
			x = "0" + x;
		}
		for (int i = 0; i < 8; i++) {
			this.biasedExponent[i] = x.charAt(i) - 48;
		}

		this.mantissa=new int[23];
		for (int i=0;i<23;i++) {
			this.mantissa[i] = mantissa.charAt(i) - 48;
		}

	}

	static IEEE754Representation getIEEERepresentation(double x) {
		// Returns decimal value 'num' in IEEE754 representation

		if(x == 0.0) { // If num is 0, all fields in the representation are also 0.
			return new IEEE754Representation(0, new int[8], new int[23]); // Return IEEE754Representation object with 0's
		}

		int signBit = 0; // Initialize signed bit variable

		if (x < 0) { // If num is negative, signed bit = 1
			signBit = 1;
			x = Math.abs(x); // Convert num to positive value
		}

		// Initialize variables for conversion
		String binary;
		int resultWholeNumber = (int)x;
		double decimal = x - resultWholeNumber;
		binary = Integer.toBinaryString(resultWholeNumber);
		int precision = 23, flag = 0, indexOfFirstOne = binary.indexOf('1');


		// Convert decimal to binary
		if (indexOfFirstOne >= 0) {
			precision -= binary.substring(indexOfFirstOne + 1).length();
			flag = 1;
		}
		binary += ".";
		while (precision > 0) {
			decimal *= 2;
			int fractionBit = (int)decimal;
			binary += fractionBit;
			if (flag == 1) {
				if(fractionBit == 1) {
					decimal -= fractionBit;
				}
				precision--;
			} else {
				if(fractionBit == 1) {
					decimal -= fractionBit;
					flag = 1;
				}
			}
		}

		// Get mantissa and normalize
		indexOfFirstOne = binary.indexOf('1');
		int indexOfDecimal = binary.indexOf('.');
		String mantissa = binary.substring(indexOfFirstOne + 1).replace(".", ""); // Get mantissa
		int trueExponent = indexOfDecimal - indexOfFirstOne; // // Get exponent
		if (indexOfDecimal > indexOfFirstOne) { // Normalize mantissa and exponent
			trueExponent -= 1;
		}
		int biasedExponent = trueExponent + 127; // Convert to biased exponent

		// Return IEEE754Representation object with computed properties
		return new IEEE754Representation(signBit, biasedExponent, mantissa);
	}

	static int compareExponents(IEEE754Representation operand1, IEEE754Representation operand2) {
		// Compare exponents (Bitwise)

		for (int i = 0; i < 8; i++) {
			if (operand1.biasedExponent[i] > operand2.biasedExponent[i]) {
				return 1;
			}
			if (operand1.biasedExponent[i] < operand2.biasedExponent[i]) {
				return -1;
			}
		}

		return 0; // Exponents are equal

	}

	static int compareMantissa(IEEE754Representation operand1, IEEE754Representation operand2) {
		// Compare mantissas (Bitwise)

		for (int i = 0; i < 8; i++) {
			if (operand1.mantissa[i]>operand2.mantissa[i]) {
				return 1;
			}
			if (operand1.mantissa[i]<operand2.mantissa[i]) {
				return -1;
			}
		}

		return 0;

	}

	static int compare(IEEE754Representation operand1, IEEE754Representation operand2) {
		// Compare operands

		if (compareExponents(operand1,operand2) == 0) { // If exponents are the same, compare mantissa
			return compareMantissa(operand1, operand2);
		} else {                                        // Otherwise, return the returned value from 'compareExponents()'
			return compareExponents(operand1, operand2);
		}

	}

	static IEEE754Representation mult(IEEE754Representation operand1, IEEE754Representation operand2) {
		// Multiplies two IEEE represented numbers

		// Initialize significand arrays
		int[] significandOperand1 = new int[24];
		int[] significandOperand2 = new int[24];

		// Initialize new exponent array
		int[] exp = new int[8];

		// If either operands are 0, return the zero operand
		if(isZero(operand1.biasedExponent) && isZero(operand1.mantissa)) {return operand1;}
		if(isZero(operand2.biasedExponent) && isZero(operand2.mantissa)) {return operand2;}

		// Shift significand to make room for hidden bit
		for (int i = 0; i < 23; i++) {
			significandOperand1[i+1] = operand1.mantissa[i];
			significandOperand2[i+1] = operand2.mantissa[i];
		}

		// Add hidden bit to array
		significandOperand1[0] = 1; // Hidden bits
		significandOperand2[0] = 1;

		int[] result = binaryMult(significandOperand1,  significandOperand2); // Multiply the two significands
		int resSign = XOR(operand1.signBit, operand2.signBit); // XOR to find signed bit

		int[] temp = addBinary(operand1.biasedExponent, operand2.biasedExponent); // Add exponents
		temp = addBinary(temp, twosComplement(toBinaryArray("01111111"))); // Subtract 127 from biased exponent
		//temp = addBinary(temp, toBinaryArray(twosComplement("01111111"))); // Subtract 127 from biased exponent

		// If addition of exponent overflowed, normalize it
		if (temp.length > 8) {
			System.arraycopy(temp, temp.length - 8, exp,0, exp.length);
		}

//        if (result[0] == 1) { // THIS NORMALIZES THE NUMBER BUT IN THE EXAMPLE OF OUTPUTS IT IS NOT NORMALIZED.
//            exp = toBinaryArray(addBinary(toBinaryString(exp), "00000001"));
//            shiftLeft(result);
//        } else {
//            while (result[0] == 0) {
//                exp = toBinaryArray(addBinary(toBinaryString(exp), "00000001"));
//                shiftLeft(result);
//            }
//        }

		// Return new IEEE754Representation object with computed properties
		return new IEEE754Representation(resSign,exp, Arrays.copyOf(result, result.length));
	}

	static IEEE754Representation add(IEEE754Representation operand1, IEEE754Representation operand2) {
		// Adds two IEEE represented numbers

		// Initialize significand arrays
		int[] significandOperand1 = new int[24];
		int[] significandOperand2 = new int[24];

		// If either operands are 0, return the non-zero operand
		if (isZero(operand1.biasedExponent) && isZero(operand1.mantissa)) {
			return operand2;
		}
		if (isZero(operand2.biasedExponent) && isZero(operand2.mantissa)) {
			return operand1;
		}

		// Shift significand to make room for hidden bit
		for (int i = 0; i < 23; i++) {
			significandOperand1[i + 1] = operand1.mantissa[i];
			significandOperand2[i + 1] = operand2.mantissa[i];
		}

		// Add hidden bit to array
		significandOperand1[0] = 1;
		significandOperand2[0] = 1;

		// Find larger exponent
		int expOperand1=toInteger(operand1.biasedExponent);
		int expOperand2=toInteger(operand2.biasedExponent);
		int maxExp = Math.max(expOperand1,  expOperand2);

		// Make exponents match
		while (expOperand1 < expOperand2) {
			shiftRight(significandOperand1);
			expOperand1++;
			if (isZero(significandOperand1)) {
				return operand2;
			}
		}

		while (expOperand2 < expOperand1) {
			shiftRight(significandOperand2);
			expOperand2++;
			if (isZero(significandOperand2)) {
				return operand1;
			}
		}

		if(operand1.signBit == operand2.signBit) { // If signed bits are equal
			int[] resAddition = addBinary(significandOperand1,  significandOperand2); // Add significands (Includes hidden bit)
			if (resAddition[0] == 1) { // Significand Overflow Case
				shiftRight(resAddition); // Normalizing Result
				maxExp++;
				if (maxExp > 254) { // Exponent overflow case
					System.out.println("Exponents overflowed");
				}
			}
			// Return new IEEE754Representation object with computed properties
			return new IEEE754Representation(operand1.signBit, maxExp, Arrays.copyOfRange(resAddition,2,25)); // Return
		} else { // If signed bits are not equal
			int rSignBit; // Result signed bit
			if (compare(operand1, operand2) == 1) {
				rSignBit = operand1.signBit;
			} else if (compare(operand1, operand2) == -1) {
				rSignBit = operand2.signBit;
			} else { // Error case
				// Return new IEEE754Representation object with computed properties
				return new IEEE754Representation(0, new int[8], new int[23]);
			}
			int[] resDifference = difference(significandOperand1,  significandOperand2); // Store the difference of significands in 'resDifference'
			while (resDifference[0] == 0) { // Shift left until normalized
				shiftLeft(resDifference);
				maxExp--;
				if (maxExp < 1) { // Exponent underflow case
					System.out.println("Exponents underflowed");
				}
			}
			// Return new IEEE754Representation object with computed properties
			return new IEEE754Representation(rSignBit,maxExp,Arrays.copyOfRange(resDifference,1,24));
		}
	}

	String getIEEEFormat() {
		// Print IEEE754 represented number in correct representation

		return signBit+"\t"+toBinaryString(biasedExponent)+"\t"+toBinaryString(mantissa);
	}

	static int[] toBinaryArray(String x) {
		// Converts a string to an array

		int[] binary = new int[x.length()];
		for(int i = 0; i < binary.length; i++) {
			binary[i] = x.charAt(i) - 48;
		}
		return binary;
	}

	static String toBinaryString(int[] x) {
		// Converts an array to String

		String binary = "";
		for (int j : x) binary += j;
		return binary;
	}

	static int toInteger(int[] x) {
		// Converts a binary array to an Integer value

		int dec = 0, n = x.length;
		for (int i = 0; i < n; i++) {
			if (x[i] == 1) {
				dec += x[i] * Math.pow(2 , n - i - 1);
			}
		}
		return dec;
	}

	static void shiftRight(int[] x) {
		// Shifts array one element to the right

		if (x.length - 1 >= 0) System.arraycopy(x, 0, x, 1, x.length - 1);
		x[0] = 0;
	}

	static void shiftLeft(int[] x) {
		// Shifts array one element to the left

		if (x.length - 1 >= 0) System.arraycopy(x, 1, x, 0, x.length - 1);
		x[x.length - 1] = 0;
	}

	static int[] twosComplement(int[] binary) {
		// Performs Two's Complement on a binary array

		// Create new array for result
		int[] comp = new int[binary.length];

		// Flip signs
		for (int i = 0; i < binary.length; i++) {
			if (binary[i] == 0) {
				comp[i] = 1;
			} else {
				comp[i] = 0;
			}
		}

		// Add one
		for (int i = binary.length - 1; i >= 0; i--) {
			if (comp[i] == 1) {
				comp[i] = 0;
			} else {
				comp[i] = 1;
				break;
			}
		}

		return comp;
	}

	static String addBinary(String a,String b) {
		// Adds two binary Strings together
		// This function performs similarly to the addBinary array function but with a String instead

		String result = "";

		if (a.indexOf('.') == -1 && b.indexOf('.') == -1) { // No decimal
			while (a.length() > b.length()) {
				b = "0" + b;
			}
			while (a.length() < b.length()) {
				a = "0" + a;
			}
			int n = a.length(), sum, carry = 0;
			for (int i = n - 1; i >= 0; i--) {
				sum = (a.charAt(i) - 48) + (b.charAt(i) - 48) + carry;
				if (sum == 3) {
					result = "1" + result;
					carry = 1;
				} else if (sum == 2) {
					result = "0" + result;
					carry = 1;
				} else {
					result = sum + result;
					carry = 0;
				}
			}
			if (carry == 1) {
				result = carry + result;
			}
		} else if (a.indexOf('.') != -1 && b.indexOf('.') != -1) { // Has decimal
			while (a.indexOf('.') > b.indexOf('.')) {
				b = "0" + b;
			}
			while (a.indexOf('.')<b.indexOf('.')) {
				a = "0" + a;
			}
			while (a.substring(a.indexOf('.') + 1).length() > b.substring(b.indexOf('.') + 1).length()){
				b += "0";
			}
			while (a.substring(a.indexOf('.')+1).length()<b.substring(b.indexOf('.')+1).length()) {
				a += "0";
			}
			int n = a.length(), sum, carry = 0;
			for (int i = n - 1; i >= 0; i--) {
				if (a.charAt(i) =='.') {
					result = "." + result;
					continue;
				}
				sum=(a.charAt(i)-48)+(b.charAt(i)-48)+carry;
				if (sum == 3) {
					result = "1" + result;
					carry = 1;
				} else if (sum == 2) {
					result = "0" + result;
					carry = 1;
				} else {
					result = sum + result;
					carry = 0;
				}
			}
			if (carry == 1) {
				result = carry + result;
			}
		} else {
			if (a.indexOf('.') != -1) { // 'a' has decimal
				result = addBinary(a.substring(0, a.indexOf('.')), b);
				result += a.substring(a.indexOf('.'));
			} else { // 'b' has decimal
				result = addBinary(a, b.substring(0, b.indexOf('.')));
				result += b.substring(b.indexOf('.'));
			}
		}
		return result;
	}

	static int[] addBinary(int[] tempA, int[] tempB) {
		// Adds two binary arrays together

		// Initialize variables
		int carry = 0, sum;
		int n;
		int[] a, b;

		// Make the two arrays the same size, padding the small array with 0's
		if (tempA.length > tempB.length) {
			n = tempA.length;
			b = new int[n];
			for (int i = 0; i < (n - tempB.length); i++) {
				b[i] = 0;
			}
			System.arraycopy(tempB, 0, b, n - tempB.length, tempB.length);
			a = tempA;
		} else if (tempA.length < tempB.length) {
			n = tempB.length;
			a = new int[n];
			for (int i = 0; i < (n - tempA.length); i++) {
				a[i] = 0;
			}
			System.arraycopy(tempA, 0, a, n - tempA.length, tempA.length);
			b = tempB;
		} else {
			a = tempA;
			b = tempB;
			n = a.length;
		}

		// Create result array with n + 1 size in case of carry
		int[] result = new int[n+1];

		// For loop to add arrays
		for (int i = n - 1; i >= 0; i--) {
			sum = a[i] + b[i] + carry;
			if (sum == 3) {
				result[i + 1] = 1;
				carry = 1;
			} else if (sum == 2) {
				result[i + 1] = 0;
				carry = 1;
			} else {
				result[i + 1] = sum;
				carry = 0;
			}
		}

		result[0] = carry;

		return result;
	}

	static int[] difference(int[] a, int[] b) {
		// Computes difference of two binary arrays

		int[] bComp = twosComplement(b); // Get Two's Complement of 'b'
		int[] tmpResult = addBinary(a, bComp); // Add the two binary numbers
		int[] result = new int[a.length];
		System.arraycopy(tmpResult, 1, result, 0, result.length);
		if(tmpResult[0] == 0) { // Answer is in Two's Complement
			return twosComplement(result);
		} else { // Answer is not in Two's Complement
			return result;
		}
	}

	static int[] binaryMult(int[] a, int[] b) { // 'binaryMult' is never called without passing arrays, therefore doesn't need a String equivalent.
		// Multiplies two binary arrays

		int placeHolderCount = 0; // Variable to keep track of product shift
		String[] results = new String[b.length]; // Results String array
		int[] result = new int[a.length + b.length]; // Results Int array

		for (int i = b.length - 1; i >= 0; i--) {
			for (int j = 0; j < placeHolderCount; j++) { // For loop to decide how many placeholders to place before each product
				results[placeHolderCount] += "0";
			}

			if (b[i] == 0) {
				for (int j = 0; j < a.length; j++) { // If zero, add zeros to result array
					results[placeHolderCount] = "0" + results[placeHolderCount];
				}
			} else {
				for (int j = a.length - 1; j >= 0; j--) { // If one, add all of a to result array
					results[placeHolderCount] = a[j] + results[placeHolderCount];
				}
			}
			// For some reason 'null' was showing up in the array, so I removed it with this line
			results[placeHolderCount] = results[placeHolderCount].replaceAll("null", "");

			// Increment placeHolderCounter
			placeHolderCount++;
		}

		// Add all the products together
		String tempResult = results[0];
		for (int i = 1; i < b.length; i++) {
			tempResult = addBinary(tempResult, results[i]);
		}

		while (tempResult.length() < 48) {
			tempResult = "0" + tempResult;
		}

		result = toBinaryArray(tempResult);

		return result;
	}

	static boolean isZero(int[] x) {
		// Checks for zeros in an array

		for (int j : x) {
			if (j == 1) {
				return false;
			}
		}
		return true;
	}

	static int XOR(int a, int b) {
		// XOR function for multiplication signed bit

		if (a == 0 && b == 0) {
			return 0;
		} else if (a == 0 && b == 1) {
			return 1;
		} else if (a == 1 && b == 0) {
			return 1;
		} else {
			return 0;
		}
	}
}
