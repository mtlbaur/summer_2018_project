import java.math.*;

// This class is used to represent a Plate object.
// Aside from the simple data members, it has a two dimensional array of Cell objects which is used to
// represent the 36 cells of the physical plate used in the research process.
class Plate
{
	int plateNumber;
	String plateType;
	Cell cellTable[][];
	int iron_nonStandard;
	int copper_nonStandard;
	double rating;
	BigInteger id;
	boolean inconsistentRatios;

	String leftStandardElement;
	String rightStandardElement;

	double averageOfLeftStandards;
	double averageOfRightStandards;

	double sampleStandardDeviationOfLeftStandards;
	double sampleStandardDeviationOfRightStandards;

	double sampleCoefficientOfVariationOfLeftStandards;
	double sampleCoefficientOfVariationOfRightStandards;

	double sampleCoefficientOfVariationOfCol_1;
	double sampleCoefficientOfVariationOfCol_2;
	double sampleCoefficientOfVariationOfCol_3;
	double sampleCoefficientOfVariationOfCol_4;

	Plate()
	{
		rating = -999;
		inconsistentRatios = false;

		cellTable = new Cell[6][6];

		leftStandardElement = "*";
		rightStandardElement = "*";
	}
}