import java.util.*;
import java.math.*;

class ElementRatioCount
{
	int goodCount;

	int totalCount;

	double percentage;

	Vector<Element> elements;

	Vector<BigInteger> data_ids;

	ElementRatioCount()
	{
		goodCount = 0;

		totalCount = 0;

		percentage = -999;

		elements = new Vector();

		data_ids = new Vector();
	}
}