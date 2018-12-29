import java.io.*;
import java.util.*;
import java.math.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// This class represents an object that can read and process the mining.txt file which contains information on individual cells of various plates.
// It creates the "cells.txt" and "target_cells.txt" files.
class PlateDataMiner
	implements Runnable
{
	Vector<Cell> cells;

	Vector<Cell> targetCells;

	BufferedReader reader;

	PrintWriter writer;

	String inFileName;

	PlateDataFormatter host;

	ProgressDialog progressDialog;

	ElementCombinationDialog elementCombinationDialog;

	Vector<String> targetElements;

	boolean targetsSelected;

	boolean canceledTargetSelection;

	Vector<String> ids;
	Vector<String> plateInfo;

	int targetDataSetsCount;

	Vector<ElementRatioCount> targetRatioCounts;

	Vector<ElementRatioCount> ratioCounts;

	// Default constructor - constructs a few Vectors and initializes a couple pointers.
	PlateDataMiner(PlateDataFormatter host, String inFileName)
	{
		this.host = host;
		this.inFileName = inFileName;

		targetElements = new Vector();
		targetCells = new Vector();
		cells = new Vector();
	}

	// This method is part of the Runnable interface. It calls various methods to read the mining text file,
	// create Cell objects, sort the Cell objects by their ratings, write the Cell objects to a file,
	// and - potentially - allow the user to select target elements to create and additional file (target_cells.txt).
	// Code that is not in the SwingUtilities.invokeLater() method is executed by a new Thread.
	// The program was designed this way to allow the user to kill the program mid operation.
	// If a different Thread was not used, this code would be executed on the event dispatcher Thread.
	// This is the same Thread that responds to the user interaction with GUI components.
	// If this Thread is busy, then the GUI would become unresponsive until the Thread is no longer busy.
	public void run()
	{
		// The following two booleans are used to determine whether the user has selected target elements or has canceled.
		targetsSelected = false;
		canceledTargetSelection = false;

		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					progressDialog = new ProgressDialog("Mining...");
				}
			});
		}

		catch (Exception e)
		{
			System.out.println("Exception while creating progress bar!");
		}

		processFile();

		sortCellsByRating(cells);

		writeCellsToFile("cells.txt");

		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					progressDialog.dispose();

					// If the user has selected the find target element combination button, create the appropriate dialog.
					if (host.findElementCombinationButton.isSelected())
						elementCombinationDialog = new ElementCombinationDialog(host, host.elements);


				}
			});
		}

		catch (Exception e)
		{
			System.out.println("Exception while creating progress bar!");
		}

		//System.out.println(targetsSelected);

		if (host.findElementCombinationButton.isSelected())
		{
			// Wait until the user selects or cancels.
			while (targetsSelected == false && canceledTargetSelection == false)
			{
				try
				{
					Thread.sleep(500);
				}

				catch (Exception e)
				{
					System.out.println("EXCEPTION IN TARGETS SELECTED WHILE LOOP");

					break;
				}

				//System.out.println(targetsSelected);
				//System.out.println(canceledTargetSelection);
			}

			//System.out.println(targetsSelected);
			//System.out.println(canceledTargetSelection);

			if (targetsSelected)
			{
				try
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							progressDialog = new ProgressDialog("Searching...");
						}
					});
				}

				catch (Exception e)
				{
					System.out.println("Exception while creating progress bar!");
				}

				getCellsWithTargetElementCombination();

				getPlatesWithTargetElementCombination();

				writeTargetCellsToFile("target_cells.txt", targetDataSetsCount, plateInfo);

				getTargetCellRatioCounts();
				sortTargetCellRatioCounts();
				printTargetCellRatioCounts();

				getCellRatioCounts();
				sortCellRatioCounts();
				printCellRatioCounts();

				try
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							progressDialog.dispose();
						}
					});
				}

				catch (Exception e)
				{
					System.out.println("Exception while disposing progress bar!");
				}
			}
		}

		// Enable all the fields and buttons on the GUI since the formatting/mining/searching process is complete.
		host.input.setEnabled(true);
		host.field_ratingThreshold.setEnabled(true);
		host.field_sampleCoefficientOfVariationThreshold.setEnabled(true);
		host.format.setEnabled(true);
		host.clear.setEnabled(true);
		host.findElementCombinationButton.setEnabled(true);
	}

	// This method is used execute the PlateDataMiner. Assuming the reader is successfully constructed from the inFileName,
	// the run() method is called.
	public void initialize()
	{
		boolean error = false;

		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName)));
			//writer = new PrintWriter(new FileOutputStream("miner_test.txt"));
		}

		catch (Exception e)
		{
			error = true;

			JOptionPane.showMessageDialog(host, "Something went wrong in PlateDataMiner -> setupMiner().", "Error", JOptionPane.INFORMATION_MESSAGE);
		}

		if (!error)
		{
			new Thread(this).start(); // Effectively executes the public void run() method.
		}
	}

	// This method is responsible for reading the mining.txt file, creating the Cell objects, and storing those Cells in the "cells" vector.
	public void processFile()
	{
		String line = "";
		String parts[];
		String headers[];

		try
		{
			line = reader.readLine();

			Cell cell = new Cell();

			boolean done = false;

			headers = line.split(" ");

			line = reader.readLine();

			while (line != "" && !done)
			{
				try
				{
					try
					{
						parts = line.split(" ");
					}

					catch (Exception f)
					{
						done = true;

						break;
					}

					cell = new Cell();

					cell.plateNumber = Integer.parseInt(parts[0]);
					cell.plateType = parts[1];
					cell.row = Integer.parseInt(parts[2]);
					cell.col = Integer.parseInt(parts[3]);
					cell.value = Double.parseDouble(parts[4]);
					cell.valueDividedByLeftAverage = Double.parseDouble(parts[5]);
					cell.valueDividedByRightAverage = Double.parseDouble(parts[6]);
					cell.iron_nonStandard = Integer.parseInt(parts[7]);
					cell.copper_nonStandard = Integer.parseInt(parts[8]);
					cell.id = new BigInteger(parts[9]);

					if (cell.plateType.equals("red") && cell.col != 0 && cell.col != 5)
					{
						cell.rating = cell.valueDividedByRightAverage;
					}

					if (cell.plateType.equals("black") && cell.col != 0 && cell.col != 5)
					{
						cell.rating = cell.valueDividedByLeftAverage;
					}

					for (int x = 10; x < parts.length; x++)
					{
						String atomicSymbol = headers[x];

						int ratio = Integer.parseInt(parts[x]);

						cell.elements.add(new Element(atomicSymbol, ratio));
					}

					if (cell.col != 0 && cell.col != 5)
						cells.add(cell);

					line = reader.readLine();
				}

				catch (Exception x)
				{
					x.printStackTrace();

					System.out.println("Exception while processing row for: " + cell.plateNumber + " " + cell.plateType + " " + cell.id);
				}

				/*

				System.out.println(cell.plateNumber + " " + cell.plateType + " " + cell.row	+ " " + cell.col + " " + cell.value
				+ " " + cell.valueDividedByLeftAverage + " " + cell.valueDividedByRightAverage + " " + cell.iron_nonStandard
				+ " " + cell.copper_nonStandard + " " + cell.id);

				for (Element y : cell.elements)
				{
					System.out.println(y.atomicSymbol + " " + y.ratio);
				}

				*/
			}
		}

		catch (IOException e)
		{
			//e.printStackTrace();

			//System.out.println("IOEXCEPTION WHILE PROCESSING FILE");
		}
	}

	// This method will remove all factor of 100 Plates that have another Plate of the same number to compare to.
	// This method is NOT CALLED ANYWHERE. The factors of 100 are eliminated in the PlateDataFormatter by recalculating the ratios.
	public void eliminateFactorsOf100(Vector vectorToCleanse)
	{
		Vector<Cell> vector = vectorToCleanse;

		Cell iCell;
		Cell jCell;

		for (int i = 0; i < vector.size(); i++)
		{
			for (int j = 0; j < vector.size(); j++)
			{
				iCell = vector.get(i);

				jCell = vector.get(j);

				if (iCell.plateType.equals(jCell.plateType) && (iCell.plateNumber == jCell.plateNumber) && (iCell.row == jCell.row) && (iCell.col == jCell.col) && (iCell.id != jCell.id))
				{
					double jCellFactorValueLeft = (jCell.valueDividedByLeftAverage * 100);
					double jCellFactorValueRight = (jCell.valueDividedByRightAverage * 100);
					double iCellFactorValueLeft = (iCell.valueDividedByLeftAverage * 100);
					double iCellFactorValueRight = (iCell.valueDividedByRightAverage * 100);

					//System.out.println(jCellFactorValueLeft);
					//System.out.println(jCellFactorValueRight);

					if (iCell.valueDividedByLeftAverage <= (jCellFactorValueLeft + 5) &&
						iCell.valueDividedByLeftAverage >= (jCellFactorValueLeft - 5) &&
						iCell.valueDividedByRightAverage <= (jCellFactorValueRight + 5) &&
						iCell.valueDividedByRightAverage >= (jCellFactorValueRight - 5))
					{
						vector.remove(i);

						//System.out.println("REMOVED: " + iCell.plateNumber + " " + iCell.plateType + " " + iCell.row + " " + iCell.col + " " + iCell.rating);
					}

					else if (jCell.valueDividedByLeftAverage <= (iCellFactorValueLeft + 5) &&
						jCell.valueDividedByLeftAverage >= (iCellFactorValueLeft - 5) &&
						jCell.valueDividedByRightAverage <= (iCellFactorValueRight + 5) &&
						jCell.valueDividedByRightAverage >= (iCellFactorValueRight - 5))
					{
						vector.remove(j);

						//System.out.println("REMOVED: " + jCell.plateNumber + " " + jCell.plateType + " " + jCell.row + " " + jCell.col + " " + jCell.rating);
					}
				}
			}
		}
	}

	// This method uses selection sort to sort the contents of the cells Vector by each of the Cell's rating.
	public void sortCellsByRating(Vector vectorToSort)
	{
		Vector<Cell> vector = vectorToSort;

		for (int i = 0; i < vector.size(); i++)
		{
			int swapIndex = i;

			for (int j = i + 1; j < vector.size(); j++)
			{
				if (vector.get(j).rating > vector.get(swapIndex).rating)
				{
					swapIndex = j;
				}
			}

			if (swapIndex != i)
			{
				Cell tempCell = vector.get(i);

				Cell cellToSwap = vector.get(swapIndex);

				vector.remove(i);

				vector.add(i, cellToSwap);

				vector.remove(swapIndex);

				vector.add(swapIndex, tempCell);
			}
		}
	}

	// This method writes the contents of the "cells" vector to the argument fileName.
	public void writeCellsToFile(String fileName)
	{
		try
		{
			writer = new PrintWriter(new FileOutputStream(fileName));
		}

		catch (Exception e)
		{
			System.out.println("Exception while opening file to write cells to");
		}

		writer.println("This file contains information on all nonstandard cells from valid plates.");
		writer.println("Cells are sorted by their ratings into descending order.");
		writer.println("");
		writer.println("FORMAT:");
		writer.print("*plate_number*    ");
		writer.print("*plate_type*    ");
		writer.print("*row*    ");
		writer.print("*col*    ");
		writer.print("*plate_rating*    ");
		writer.print("*elements*    ");
		writer.print("*data_id*");
		writer.println("");
		writer.println("");

		for (Cell x : cells)
		{
			try
			{
				writer.print(x.plateNumber + "     ");
				writer.print(x.plateType + "     ");

				if (x.plateType.equals("red"))
					writer.print("  ");

				writer.print(x.row + "     " + x.col + "     ");
				//writer.print(x.value + "     ");
				writer.print(x.rating + "     ");
				//writer.print(x.valueDividedByLeftAverage + " ");
				//writer.print(x.valueDividedByRightAverage + " ");
				//writer.print(x.iron_nonStandard + " ");
				//writer.print(x.copper_nonStandard + " ");
				writer.print("[ ");

				for (Element element : x.elements)
				{
					if (element.ratio != 0)
					{
						writer.print(element.atomicSymbol + " " + element.ratio + " ");
					}
				}

				writer.print("]");

				writer.print("     " + x.id);
			}

			catch (Exception e)
			{
				e.printStackTrace();

				System.out.println("EXCEPTION WHILE WRITING CELL VECTOR TO FILE");
			}

			writer.println("");
		}

		writer.close();
	}

	// This method writes the target Plates and target Cells to the target_cells.txt file.
	public void writeTargetCellsToFile(String fileName, int targetDataSetsCount, Vector<String> plateInfo)
	{
		try
		{
			writer = new PrintWriter(new FileOutputStream(fileName, false));
		}

		catch (Exception e)
		{
			System.out.println("Exception while opening file to write cells to");
		}

		writer.println("This file contains information on all target cells.");
		writer.println("Target cells contain one or more target elements and are obtained from valid plates.");
		writer.println("");

		writer.print("Target elements: [ ");

		for (String element : targetElements)
			writer.print(element + " ");

		writer.println("]");

		writer.println("");

		writer.println("Number of valid data sets with target elements: " + targetDataSetsCount);
		writer.println("");
		writer.println("Number of cells with target elements: " + targetCells.size());
		writer.println("");
		writer.println("================================================================================================================");
		writer.println("");
		writer.println("Data set information:");
		writer.println("");

		writer.println("FORMAT:");
		writer.print("*plate_number*    ");
		writer.print("*plate_type*    ");
		writer.print("*plate_rating*    ");
		writer.print("*data_id*");
		writer.println("");
		writer.println("");

		for (String info : plateInfo)
		{
			writer.println(info);
			writer.println("");
		}

		writer.println("================================================================================================================");
		writer.println("");
		writer.println("Cell information:");
		writer.println("");

		writer.println("FORMAT:");
		writer.print("*plate_number*    ");
		writer.print("*plate_type*    ");
		writer.print("*row*    ");
		writer.print("*col*    ");
		writer.print("*plate_rating*    ");
		writer.print("*elements*    ");
		writer.print("*data_id*");
		writer.println("");
		writer.println("");

		for (Cell x : targetCells)
		{
			try
			{
				writer.print(x.plateNumber + "     ");
				writer.print(x.plateType + "     ");

				if (x.plateType.equals("red"))
					writer.print("  ");

				writer.print(x.row + "     " + x.col + "     ");
				//writer.print(x.value + "     ");
				writer.print(x.rating + "     ");
				//writer.print(x.valueDividedByLeftAverage + " ");
				//writer.print(x.valueDividedByRightAverage + " ");
				//writer.print(x.iron_nonStandard + " ");
				//writer.print(x.copper_nonStandard + " ");
				writer.print("[ ");

				for (Element element : x.elements)
				{
					if (element.ratio != 0)
					{
						writer.print(element.atomicSymbol + " " + element.ratio + " ");
					}
				}

				writer.print("]");

				writer.print("     " + x.id);
			}

			catch (Exception e)
			{
				e.printStackTrace();

				System.out.println("EXCEPTION WHILE WRITING CELL VECTOR TO FILE");
			}

			writer.println("");
		}

		writer.close();
	}

	// This method finds all Cells that contain the target elements. These Cells are stored in the targetCells Vector.
	public void getCellsWithTargetElementCombination()
	{
		Vector<String> cellElements;

		for (Cell c : cells)
		{
			boolean contains = true;

			cellElements = new Vector();

			for (Element e : c.elements)
				if (e.ratio != 0)
				{
					cellElements.add(e.atomicSymbol);
				}

			for (String targetElement : targetElements)
			{
				boolean has = false;

				for (String cellElement : cellElements)
				{
					if (targetElement.equals(cellElement))
					{
						has = true;
					}
				}

				if (has == false)
					contains = false;
			}

			if (contains)
				targetCells.add(c);
		}
	}

	// This method finds all Plate objects that contain a target Cell object.
	public void getPlatesWithTargetElementCombination()
	{
		ids = new Vector(); // Contains the IDs of the Plates with the target elements.
		plateInfo = new Vector(); // Contains information on each of the Plates with the target elements.

		for (Cell x : targetCells)
		{
			String id = x.id.toString();

			if (!ids.contains(id))
			{
				ids.add(id);
				plateInfo.add(x.plateNumber + "    " + x.plateType + "    " + x.rating + "    " + id);
			}
		}

		targetDataSetsCount = ids.size();
	}

	// This method counts how many times a specific element/ratio combination occurs for each Cell in the targetCells Vector.
	// The countedCells Vector is used to determine when a Cell has already been counted.
	// The targetRatioCounts Vector is used to contain the resulting ElementRatioCount objects.
	public void getTargetCellRatioCounts()
	{
		Vector<Cell> countedCells = new Vector();

		targetRatioCounts = new Vector();

		for (Cell x : targetCells)
		{
			ElementRatioCount ratioCount = new ElementRatioCount();

			ratioCount.elements = x.elements;

			if (!countedCells.contains(x))
			{
				for (Cell y : targetCells)
				{
					if (!countedCells.contains(y))
					{
						boolean differentRatios = false;

						for (Element a : x.elements)
						{
							for (Element b : y.elements)
							{
								if ((a.atomicSymbol.equals(b.atomicSymbol)) && (a.ratio != b.ratio))
									differentRatios = true;
							}
						}

						if (!differentRatios)
						{
							if (y.rating >= host.ratingThreshold)
							{
								ratioCount.goodCount++;

								ratioCount.totalCount++;
							}

							else
								ratioCount.totalCount++;

							if (!ratioCount.data_ids.contains(y.id))
								ratioCount.data_ids.addElement(y.id);

							countedCells.add(y);
						}
					}
				}

				double tempGood = ratioCount.goodCount;

				double tempTotal = ratioCount.totalCount;

				double tempPercentage = ((tempGood / tempTotal) * 100);

				ratioCount.percentage = host.round(tempPercentage, 3);

				targetRatioCounts.add(ratioCount);
			}
		}
	}

	// This method sorts the contents of the targetRatioCounts Vector into descending order based on the "percentage" data member of
	// each ElementRatioCount object in that Vector.
	public void sortTargetCellRatioCounts()
	{
		Vector<ElementRatioCount> vector = targetRatioCounts;

		for (int i = 0; i < vector.size(); i++)
		{
			int swapIndex = i;

			for (int j = i + 1; j < vector.size(); j++)
			{
				if (vector.get(j).percentage > vector.get(swapIndex).percentage)
				{
					swapIndex = j;
				}
			}

			if (swapIndex != i)
			{
				ElementRatioCount temp = vector.get(i);

				ElementRatioCount thingToSwap = vector.get(swapIndex);

				vector.remove(i);

				vector.add(i, thingToSwap);

				vector.remove(swapIndex);

				vector.add(swapIndex, temp);
			}
		}
	}

	// This method simply prints the contents of the targetRatioCounts Vector the appropriate output file.
	public void printTargetCellRatioCounts()
	{
		try
		{
			writer = new PrintWriter(new FileOutputStream("target_cell_ratio_counts.txt", false));
		}

		catch (Exception e)
		{
			System.out.println("Exception while opening file to write cells to");
		}

		writer.println("This file contains information on element and ratio combinations for all valid nonstandard cells that contain one or more target elements.");
		writer.println("The purpose of this file is to show which of these combinations is most likely to produce useful cell readings.");
		writer.println("The percentage shows how many cell readings were greater or equal to the rating threshold (which is determined by the user).");
		writer.println("The ratio gives you the exact number of cells that were good as well as the total number of cells.");
		writer.println("The left number in the ratio is the number of good cells while the right is the total number of cells.");
		writer.println("The element and ratio combination for the current line is printed last.");
		writer.println("This file should be read line by line: each line contains information unique to the element and ratio combination on that line.");

		writer.println("");

		writer.print("Target elements: [ ");

		for (String element : targetElements)
			writer.print(element + " ");

		writer.println("]");

		writer.println("");

		writer.println("Rating threshold: " + host.ratingThreshold);

		writer.println("");

		writer.println("FORMAT:");
		writer.println("*percentage of cells that had ratings >= rating threshold* : *good_cell_count/total_cell_count* : *element and ratio combination* : *data_ids*");

		writer.println("");

		for (ElementRatioCount x : targetRatioCounts)
		{
			writer.print(x.percentage + "%" + " : ");

			writer.print(x.goodCount + "/" + x.totalCount + " : ");

			writer.print("[ ");

			for (Element a : x.elements)
			{
				if (a.ratio != 0)
					writer.print(a.atomicSymbol + " " + a.ratio + " ");
			}

			writer.print("] : ");

			for (BigInteger y : x.data_ids)
			{
				writer.print(y + " ");
			}

			writer.println("");
			writer.println("");
		}

		writer.close();
	}

	// This method counts how many times a specific element/ratio combination occurs for each Cell in the cells Vector.
	// The countedCells Vector is used to determine when a Cell has already been counted.
	// The targetRatioCounts Vector is used to contain the resulting ElementRatioCount objects.
	public void getCellRatioCounts()
	{
		Vector<Cell> countedCells = new Vector();

		ratioCounts = new Vector();

		for (Cell x : cells)
		{
			ElementRatioCount ratioCount = new ElementRatioCount();

			ratioCount.elements = x.elements;

			if (!countedCells.contains(x))
			{
				for (Cell y : cells)
				{
					if (!countedCells.contains(y))
					{
						boolean differentRatios = false;

						for (Element a : x.elements)
						{
							for (Element b : y.elements)
							{
								if ((a.atomicSymbol.equals(b.atomicSymbol)) && (a.ratio != b.ratio))
									differentRatios = true;
							}
						}

						if (!differentRatios)
						{
							if (y.rating >= host.ratingThreshold)
							{
								ratioCount.goodCount++;

								ratioCount.totalCount++;
							}

							else
								ratioCount.totalCount++;

							if (!ratioCount.data_ids.contains(y.id))
								ratioCount.data_ids.addElement(y.id);

							countedCells.add(y);
						}
					}
				}

				double tempGood = ratioCount.goodCount;

				double tempTotal = ratioCount.totalCount;

				double tempPercentage = ((tempGood / tempTotal) * 100);

				ratioCount.percentage = host.round(tempPercentage, 3);

				ratioCounts.add(ratioCount);
			}
		}
	}

	// This method sorts the contents of the ratioCounts Vector into descending order based on the "percentage" data member of
	// each ElementRatioCount object in that Vector.
	public void sortCellRatioCounts()
	{
		Vector<ElementRatioCount> vector = ratioCounts;

		for (int i = 0; i < vector.size(); i++)
		{
			int swapIndex = i;

			for (int j = i + 1; j < vector.size(); j++)
			{
				if (vector.get(j).percentage > vector.get(swapIndex).percentage)
				{
					swapIndex = j;
				}
			}

			if (swapIndex != i)
			{
				ElementRatioCount temp = vector.get(i);

				ElementRatioCount thingToSwap = vector.get(swapIndex);

				vector.remove(i);

				vector.add(i, thingToSwap);

				vector.remove(swapIndex);

				vector.add(swapIndex, temp);
			}
		}
	}

	// This method simply prints the contents of the ratioCounts Vector the appropriate output file.
	public void printCellRatioCounts()
	{
		try
		{
			writer = new PrintWriter(new FileOutputStream("cell_ratio_counts.txt", false));
		}

		catch (Exception e)
		{
			System.out.println("Exception while opening file to write cells to");
		}

		writer.println("This file contains information on element and ratio combinations for all valid nonstandard cells.");
		writer.println("The purpose of this file is to show which of these combinations is most likely to produce useful cell readings.");
		writer.println("The percentage shows how many cell readings were greater or equal to the rating threshold (which is determined by the user).");
		writer.println("The ratio gives you the exact number of cells that were good as well as the total number of cells.");
		writer.println("The left number in the ratio is the number of good cells while the right is the total number of cells.");
		writer.println("The element and ratio combination for the current line is printed last.");
		writer.println("This file should be read line by line: each line contains information unique to the element and ratio combination on that line.");
		writer.println("");

		writer.println("Rating threshold: " + host.ratingThreshold);

		writer.println("");

		writer.println("FORMAT:");
		writer.println("*percentage of cells that had ratings >= rating threshold* : *good_cell_count/total_cell_count* : *element and ratio combination* : *data_ids*");

		writer.println("");

		for (ElementRatioCount x : ratioCounts)
		{
			writer.print(x.percentage + "%" + " : ");

			writer.print(x.goodCount + "/" + x.totalCount + " : ");

			writer.print("[ ");

			for (Element a : x.elements)
			{
				if (a.ratio != 0)
					writer.print(a.atomicSymbol + " " + a.ratio + " ");
			}

			writer.print("] : ");

			for (BigInteger y : x.data_ids)
			{
				writer.print(y + " ");
			}

			writer.println("");
			writer.println("");
		}

		writer.close();
	}

	// Main method used for testing purposes.
	public static void main (String x[])
	{
		PlateDataMiner miner = new PlateDataMiner(new PlateDataFormatter(), "mining.csv");

		miner.initialize();
	}
}