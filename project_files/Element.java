// This class contains the atomic symbol and the ratio of a particular element.
// It also contains the row and column position of the Cell that contains this element.
class Element
{
	String atomicSymbol;
	int ratio;
	int pos;
	int row;
	int col;

	Element()
	{
	}

	Element(String atomicSymbol, int ratio)
	{
		this.atomicSymbol = atomicSymbol;
		this.ratio = ratio;
	}

	Element(String atomicSymbol, int ratio, int pos)
	{
		this.atomicSymbol = atomicSymbol;
		this.ratio = ratio;
		this.pos = pos;
	}
}