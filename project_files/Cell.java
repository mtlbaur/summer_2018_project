import java.util.*;
import java.math.*;

// This class is used to represent a Cell object.
// The Vector of elements is used to hold all the elements (and their ratios to any other elements) that were used on that particular cell.
class Cell
{
	double value;
	double valueDividedByLeftAverage;
	double valueDividedByRightAverage;
	int row;
	int col;
	Vector<Element> elements;
	int plateNumber;
	String plateType;
	BigInteger id;
	int iron_nonStandard;
	int copper_nonStandard;
	double rating;

	Cell()
	{
		elements = new Vector();
	}

	Cell(double value, double valueDividedByLeftAverage, double valueDividedByRightAverage, int row, int col)
	{
		this.value = value;
		this.valueDividedByLeftAverage = valueDividedByLeftAverage;
		this.valueDividedByRightAverage = valueDividedByRightAverage;
		this.row = row;
		this.col = col;
		elements = new Vector();
	}
}