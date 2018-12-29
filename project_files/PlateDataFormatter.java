import java.io.*;
import java.util.*;
import java.math.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;

// This class is the basis of the program. Here is where all other components of the program are called/constructed.
// This is where the primary main method is - this is where the program should be executed.
class PlateDataFormatter extends JFrame
	implements ActionListener, Runnable
{
	Vector<Plate> allPlatesVector;
	Vector<Plate> goodPlatesVector;
	Vector<Plate> problemPlatesVector;
	Vector<String> elements;

	BufferedReader reader;

	PrintWriter writer_allPlates;
	PrintWriter writer_goodPlates;
	PrintWriter writer_problemPlates;
	PrintWriter writer_mining;

	String inFileName;

	String outFileName_allPlates;
	String outFileName_goodPlates;
	String outFileName_problemPlates;
	String outFileName_mining;

	Double ratingThreshold;
	Double sampleCoefficientOfVariationThreshold;

	JButton format;
	JButton clear;
	JButton help;

	JTextField input;
	JTextField field_ratingThreshold;
	JTextField field_sampleCoefficientOfVariationThreshold;
	JRadioButton findElementCombinationButton;

	JLabel label_input;
	JLabel label_ratingThreshold;
	JLabel label_sampleCoefficientOfVariationThreshold;
	JLabel label_findElementCombinationButton;

	GroupLayout layout;

	JPanel fieldPanel;
	JPanel buttonPanel;

	boolean problemPlate;

	ProgressDialog progressDialog;

	PlateDataMiner miner;

	HelpDialog helpDialog;

	// Default constructor. Creates most GUI components and calls setupPlateDataFormatter() to finish the task.
	PlateDataFormatter()
	{
		Container cp = getContentPane();

		format = new JButton("Format");
		format.addActionListener(this);
		format.setActionCommand("FORMAT");

		clear = new JButton("Clear Fields");
		clear.addActionListener(this);
		clear.setActionCommand("CLEAR");

		help = new JButton("Instructions");
		help.addActionListener(this);
		help.setActionCommand("HELP");

		input = new JTextField("input_data.csv");
		field_ratingThreshold = new JTextField("1");
		field_sampleCoefficientOfVariationThreshold = new JTextField("50");
		findElementCombinationButton = new JRadioButton();
		findElementCombinationButton.setSelected(true);

		label_input = new JLabel("Input file name:");
		label_ratingThreshold = new JLabel("Lowest acceptable rating:");
		label_sampleCoefficientOfVariationThreshold = new JLabel("Highest acceptable coefficient of variation in %:");
		label_findElementCombinationButton = new JLabel("Show target elements dialog:");

		fieldPanel = new JPanel();

		layout = new GroupLayout(fieldPanel);
		fieldPanel.setLayout(layout);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

		hGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addComponent(label_input)
			.addComponent(label_ratingThreshold)
			.addComponent(label_sampleCoefficientOfVariationThreshold)
			.addComponent(label_findElementCombinationButton));

		hGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addComponent(input)
			.addComponent(field_ratingThreshold)
			.addComponent(field_sampleCoefficientOfVariationThreshold)
			.addComponent(findElementCombinationButton));

		layout.setHorizontalGroup(hGroup);

		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

		vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label_input).addComponent(input));
		vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label_ratingThreshold).addComponent(field_ratingThreshold));
		vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label_sampleCoefficientOfVariationThreshold).addComponent(field_sampleCoefficientOfVariationThreshold));
		vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(label_findElementCombinationButton).addComponent(findElementCombinationButton));

        layout.setVerticalGroup(vGroup);

        buttonPanel = new JPanel();
        buttonPanel.add(format);
        buttonPanel.add(clear);
        buttonPanel.add(help);

        cp.add(fieldPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);

        input.requestFocus();

		setupPlateDataFormatter();
	}

	// Simple GUI adjustments.
	public void setupPlateDataFormatter()
	{
		Toolkit tk;

		Dimension d;

		tk = Toolkit.getDefaultToolkit();

		d = tk.getScreenSize();

		setSize(d.width/3, d.height/3);

		setLocation(d.width/3, d.height/3);

		setTitle("Plate Data Formatter");

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setVisible(true);
    }

	// This method is reponsible for correctly reading the CSV input file which is obtained from the database.
	// This method reads one line at a time and calls the appropriate methods to create Plate objects.
	// These Plate objects are stored in two of three vectors depending on which conditional checks they pass.
	public void processFile()
	{
		String line = "";
		String prevLine = "";
		String parts[];
		boolean done = false;

		int plateNum = 1;

		double id;
		double prev_id;

		try
		{
			// Start prime read to setup the prevLine variable.

			line = reader.readLine();

			prevLine = line;

			parts = line.split(" ");

			id = Double.parseDouble(parts[12]);

			prev_id = id;

			// End prime read.

			while (true)
			{
				problemPlate = false; // Assume the next Plate object is valid.

				Plate plate = new Plate();

				// This loop reads one line at a time until it encounters the data_id of a new Plate. Each Plate has a unique data_id.
				// It calls parsePlateData() which parses the current line read from the CSV input file. It takes the parsed data and fills
				// in the current Plate object.
				while (true)
				{
					try
					{
						if (prevLine != "")
						{
							parts = prevLine.split(" ");

							parsePlateData(parts, plate);
						}

						line = reader.readLine();

						prevLine = line;

						parts = line.split(" ");

						id = Double.parseDouble(parts[12]);
					}

					catch (Exception e)
					{
						done = true; // This boolean determines when ALL lines have been read from the file.
					}

					if (id != prev_id || done)
					{
						prev_id = id;

						break;
					}

					else
					{
						parsePlateData(parts, plate);
					}
				}

				// This method is called when all data has been read for the current Plate object. This is determined via the Plate's unique data_id.
				handlePlate(plate);

				if (done)
				{
					throw new IOException("DONE READING FILE");
				}
			}
		}

		catch (IOException e)
		{
			//e.printStackTrace();

			//System.out.println("IOEXCEPTION WHILE PROCESSING FILE");
		}
	}

	// This method is responsible for correctly calculating the sample standard deviation of the left and right standards.
	// There are three cases for this method:
	// 1: If the left standard is copper-based, ignore the bottom most cell in the left column.
	// 2: If the right standard is copper-based, ignore the bottom most cell in the right column.
	// 3: If neither standard is copper-based, do not ignore any cells.
	public void calculateSampleStandardDeviationOfStandards(Plate plate)
	{
		if (plate.leftStandardElement.equals("cu"))
		{
			double leftSum = 0;
			double rightSum = 0;

			for (int x = 0; x < 5; x++)
			{
				leftSum += plate.cellTable[x][0].value;
			}

			for (int x = 0; x < 6; x++)
			{
				rightSum += plate.cellTable[x][5].value;
			}

			plate.averageOfLeftStandards = (leftSum / 5);
			plate.averageOfRightStandards = (rightSum / 6);

			double x[][] = new double[2][6];

			for (int a = 0; a < 2; a++)
			{
				for (int b = 0; b < 6; b++)
				{
					int y;
					double avg;

					if (a == 1)
					{
						avg = plate.averageOfRightStandards;

						y = 5;
					}

					else
					{
						avg = plate.averageOfLeftStandards;

						y = 0;
					}

					x[a][b] = Math.pow((plate.cellTable[b][y].value - avg), 2);
				}
			}

			leftSum = 0;
			rightSum = 0;

			for (int v = 0; v < 5; v++)
			{
				leftSum += x[0][v];
			}

			for (int v = 0; v < 6; v++)
			{
				rightSum += x[1][v];
			}

			double jLeft = (leftSum / 4);
			double kRight = (rightSum / 5);

			plate.sampleStandardDeviationOfLeftStandards = Math.sqrt(jLeft);
			plate.sampleStandardDeviationOfRightStandards = Math.sqrt(kRight);
		}

		else if (plate.rightStandardElement.equals("cu"))
		{
			double leftSum = 0;
			double rightSum = 0;

			for (int x = 0; x < 6; x++)
			{
				leftSum += plate.cellTable[x][0].value;
			}

			for (int x = 0; x < 5; x++)
			{
				rightSum += plate.cellTable[x][5].value;
			}

			plate.averageOfLeftStandards = (leftSum / 6);
			plate.averageOfRightStandards = (rightSum / 5);

			double x[][] = new double[2][6];

			for (int a = 0; a < 2; a++)
			{
				for (int b = 0; b < 6; b++)
				{
					int y;
					double avg;

					if (a == 1)
					{
						avg = plate.averageOfRightStandards;

						y = 5;
					}

					else
					{
						avg = plate.averageOfLeftStandards;

						y = 0;
					}

					x[a][b] = Math.pow((plate.cellTable[b][y].value - avg), 2);
				}
			}

			leftSum = 0;
			rightSum = 0;

			for (int v = 0; v < 6; v++)
			{
				leftSum += x[0][v];
			}

			for (int v = 0; v < 5; v++)
			{
				rightSum += x[1][v];
			}

			double jLeft = (leftSum / 5);
			double kRight = (rightSum / 4);

			plate.sampleStandardDeviationOfLeftStandards = Math.sqrt(jLeft);
			plate.sampleStandardDeviationOfRightStandards = Math.sqrt(kRight);
		}

		else
		{
			double leftSum = 0;
			double rightSum = 0;

			for (int x = 0; x < 6; x++)
			{
				leftSum += plate.cellTable[x][0].value;

				rightSum += plate.cellTable[x][5].value;
			}

			plate.averageOfLeftStandards = (leftSum / 6);
			plate.averageOfRightStandards = (rightSum / 6);

			double x[][] = new double[2][6];

			for (int a = 0; a < 2; a++)
			{
				for (int b = 0; b < 6; b++)
				{
					int y;
					double avg;

					if (a == 1)
					{
						avg = plate.averageOfRightStandards;

						y = 5;
					}

					else
					{
						avg = plate.averageOfLeftStandards;

						y = 0;
					}

					x[a][b] = Math.pow((plate.cellTable[b][y].value - avg), 2);
				}
			}

			leftSum = 0;
			rightSum = 0;

			for (int v = 0; v < 6; v++)
			{
				leftSum += x[0][v];

				rightSum += x[1][v];
			}

			double jLeft = (leftSum / 5);
			double kRight = (rightSum / 5);

			plate.sampleStandardDeviationOfLeftStandards = Math.sqrt(jLeft);
			plate.sampleStandardDeviationOfRightStandards = Math.sqrt(kRight);
		}
	}

	// This method simply calculates the coefficients of variation for the standards and
	// stores these values as data members for the current Plate object.
	public void calculateCoefficientsOfVariationOfStandards(Plate plate)
	{
		plate.sampleCoefficientOfVariationOfLeftStandards = plate.sampleStandardDeviationOfLeftStandards / plate.averageOfLeftStandards;
		plate.sampleCoefficientOfVariationOfRightStandards = plate.sampleStandardDeviationOfRightStandards / plate.averageOfRightStandards;
	}

	// This method is responsible for calculating the coefficients of variation for all nonstandards on a Plate.
	// It is similar to the two previous methods but it applies to the inner four columns of the Plate.
	public void calculateCoefficientsOfVariationOfNonstandards(Plate plate)
	{
		double sums[] = new double[4];

		double averages[] = new double[4];

		for (int x = 0; x < 6; x++)
		{
			sums[0] += plate.cellTable[x][1].value;
			sums[1] += plate.cellTable[x][2].value;
			sums[2] += plate.cellTable[x][3].value;
			sums[3] += plate.cellTable[x][4].value;
		}

		for (int x = 0; x < 4; x++)
		{
			averages[x] = (sums[x] / 6);
		}

		double x[][] = new double[4][6];

		for (int a = 0; a < 4; a++)
		{
			for (int b = 0; b < 6; b++)
			{
				x[a][b] = Math.pow((plate.cellTable[b][a + 1].value - averages[a]), 2);
			}
		}

		for (int v = 0; v < 4; v++)
		{
			sums[v] = 0;
		}

		for (int v = 0; v < 6; v++)
		{
			sums[0] += x[0][v];
			sums[1] += x[1][v];
			sums[2] += x[2][v];
			sums[3] += x[3][v];
		}

		for (int v = 0; v < 4; v++)
		{
			sums[v] = (sums[v] / 5);
		}

		for (int v = 0; v < 4; v++)
		{
			sums[v] = Math.sqrt(sums[v]);
		}

		for (int v = 0; v < 4; v++)
		{
			sums[v] = (sums[v] / averages[v]);
		}

		plate.sampleCoefficientOfVariationOfCol_1 = sums[0];
		plate.sampleCoefficientOfVariationOfCol_2 = sums[1];
		plate.sampleCoefficientOfVariationOfCol_3 = sums[2];
		plate.sampleCoefficientOfVariationOfCol_4 = sums[3];
	}

	// This method parses the current line of data for the current Plate object that is being filled in.
	// parts[] contains the information of the current line split around spaces.
	// Since each line is consistent in terms of the amount of data and the placement of the data, it is possible to
	// fill in the Plate by knowing which index of parts[] contains which component.
	public void parsePlateData(String parts[], Plate plate)
	{
		boolean elementError = false; // This boolean is set to true when the program tries to parse an empty String as a atomic symbol.

		int row = 0;
		int col = 0;
		int pos = 0;

		try
		{
			plate.plateNumber = Integer.parseInt(parts[0]);
			plate.plateType = parts[1];
			plate.iron_nonStandard = Integer.parseInt(parts[10]);
			plate.copper_nonStandard = Integer.parseInt(parts[11]);
			plate.id = new BigInteger(parts[12]);

			// If, somehow, a Plate has a number of 0, output the information for that Plate to the console.
			if (plate.plateNumber == 0)
			{
				System.out.println("*************************************************************************");
				System.out.println(plate.plateNumber);
				Integer.parseInt(parts[0]);
				System.out.println(plate.plateType);
				System.out.println(parts[1]);
				System.out.println("*************************************************************************");
			}

			// PARTS 0, 1, 10, 11, 12 DONE

			row = Integer.parseInt(parts[2]);
			col = Integer.parseInt(parts[3]);

			// If a Cell has not been placed yet, create a new Cell and store it in the correct location in the
			// two-dimensional array of Cells.
			if (plate.cellTable[row][col] == null)
			{
				Cell tempCell = new Cell();

				tempCell.value = Double.parseDouble(parts[4]);
				tempCell.valueDividedByLeftAverage = Double.parseDouble(parts[5]);
				tempCell.valueDividedByRightAverage = Double.parseDouble(parts[6]);
				tempCell.row = row;
				tempCell.col = col;

				plate.cellTable[row][col] = tempCell;
			}

			// START: SETTING RATING DATA MEMBER OF PLATE

			// The rating of a Plate is used to determine how "good" it is.
			// This rating is used by the selection sort method to sort the plates in decending order.
			// This code gets OVERRIDDEN by the "calculateRating()" which is called later.
			// Therefore, this code could be removed.
			Double tempRating;

			if (plate.plateType.equals("red") && col != 0 && col != 5)
			{
				tempRating = plate.cellTable[row][col].valueDividedByRightAverage;

				if (tempRating > plate.rating)
					plate.rating = tempRating;
			}

			else if (plate.plateType.equals("black") && col != 0 && col != 5)
			{
				tempRating = plate.cellTable[row][col].valueDividedByLeftAverage;

				if (tempRating > plate.rating)
					plate.rating = tempRating;
			}

			// END: SETTING RATING DATA MEMBER OF PLATE

			// PARTS 0, 1, 2, 3, 4, 5, 6, 10, 11 DONE

			Element newElement = new Element();

			newElement.atomicSymbol = parts[7];

			//************************************************-CORRECTION-************************************************//

			// Due to software issues, there are "atomic symbols" in the database that are incorrect - they are placeholders.
			// Currently, "potas", "ammon", and "pt" are the only placeholders.
			// The code below replaces these placeholders with the correct atomic symbols.
			if (newElement.atomicSymbol.equals("potas"))
				newElement.atomicSymbol = "k";

			if (newElement.atomicSymbol.equals("ammon"))
				newElement.atomicSymbol = "v";

			if (newElement.atomicSymbol.equals("pt"))
				newElement.atomicSymbol = "v";

			//************************************************-CORRECTION-************************************************//

			// The "ratio" variable contains the "ratio" of the current element to the other elements used in that Cell.
			String ratio = parts[9];
			ratio = ratio.substring(1, (ratio.length() - 1));
			String ratioParts[] = ratio.split(":");

			// The "pos" variable contains the position of that atomic symbol compared to the other atomic symbols.
			pos = Integer.parseInt(parts[8]);

			// Since there are plates where cells were left off in the bottom row, try to parse the ratio as an Integer.
			// If this fails, set elementError to true.
			try
			{
				newElement.ratio = Integer.parseInt(ratioParts[pos]);
			}

			catch (Exception e)
			{
				newElement.ratio = 0;

				elementError = true;
			}

			newElement.pos = pos;
			newElement.row = row;
			newElement.col = col;

			if (!elementError)
			{
				// This conditional sets the atomic symbol for the left standard column.
				if (row == 0 && col == 0 && newElement.ratio == 1)
					plate.leftStandardElement = newElement.atomicSymbol;

				// This conditional sets the atomic symbol for the right standard column.
				if (row == 0 && col == 5 && newElement.ratio == 1)
					plate.rightStandardElement = newElement.atomicSymbol;

				boolean alreadyContainsElement = false;

				for (Element e : plate.cellTable[row][col].elements)
					if (e.atomicSymbol.equals(newElement.atomicSymbol))
						alreadyContainsElement = true;

				// If the current Cell does not already contain the current element, add it.
				if (!alreadyContainsElement)
					plate.cellTable[row][col].elements.add(newElement);

				// "elements" is a Vector of Strings used to contain all atomic symbols used throughout all Plates.
				if (!elements.contains(newElement.atomicSymbol))
					elements.add(newElement.atomicSymbol);
			}

			// PARTS 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 DONE
		}

		catch (Exception e)
		{
			e.printStackTrace();

			System.out.println("plate number: " + plate.plateNumber);
			System.out.println("plate type: " + plate.plateType);
			System.out.println("row: " + row);
			System.out.println("col: " + col);

			problemPlate = true;
		}
	}

	// This method does two primary things:
	// 1: It calls a series of methods to calculate values for various data members of the Plate.
	// 2: It applies a series of conditional checks on the Plate to figure out whether it is valid or not.
	public void handlePlate(Plate plate)
	{
		if (plate.plateNumber != 0)
		{
			// Perform calculations

			calculateSampleStandardDeviationOfStandards(plate);
			calculateCoefficientsOfVariationOfStandards(plate);
			calculateCoefficientsOfVariationOfNonstandards(plate);
			calculateRatios(plate);
			calculateRating(plate);

			// Perform checks on calculated values

			// These nested for loops check whether the element combination and ratios are consistent down the first FIVE cells
			// of any of the columns.
			for (int x = 0; x < 6; x++)
			{
				String elementCombination = "";
				String ratioCombination = "";

				String prevElementCombination = "";
				String prevRatioCombination = "";

				//if (x == 1)
				//	x = 5;

				for (Element e : plate.cellTable[0][x].elements)
				{
					prevElementCombination += e.atomicSymbol;
					prevRatioCombination += e.ratio;
				}

				// NOTE: This loop only checks the first 5 rows of a plate - this is because some plates are missing
				// either the copper standard, the iron standard, or the ENTIRE bottom row.
				for (int y = 1; y < 5; y++)
				{
					elementCombination = "";
					ratioCombination = "";

					for (Element e : plate.cellTable[y][x].elements)
					{
						elementCombination += e.atomicSymbol;
						ratioCombination += e.ratio;
					}

					if (!elementCombination.equals(prevElementCombination))
						problemPlate = true;

					if (!ratioCombination.equals(prevRatioCombination))
						plate.inconsistentRatios = true;

					prevElementCombination = elementCombination;
					prevRatioCombination = ratioCombination;
				}
			}

			// If the Plate isn't good enough, list it as a problem.
			if (plate.rating < ratingThreshold)
			{
				problemPlate = true;
			}

			// These nested if statements determine two things:
			// 1: Whether the correct standard is the "high" standard.
			//	  This depends on the Plate type and which standard has which element - copper or iron.
			// 2: Whether the HIGH sample coefficient of variation of one of the standards is higher than the
			//	  user-defined threshold.
			if (plate.plateType.equals("red"))
			{
				if (plate.leftStandardElement.equals("fe"))
				{
					if (plate.averageOfLeftStandards < plate.averageOfRightStandards)
						problemPlate = true;

					if (plate.sampleCoefficientOfVariationOfLeftStandards > sampleCoefficientOfVariationThreshold)
						problemPlate = true;
				}

				else if (plate.rightStandardElement.equals("fe"))
				{
					if (plate.averageOfRightStandards < plate.averageOfLeftStandards)
						problemPlate = true;

					if (plate.sampleCoefficientOfVariationOfRightStandards > sampleCoefficientOfVariationThreshold)
						problemPlate = true;
				}
			}

			else if (plate.plateType.equals("black"))
			{
				if (plate.leftStandardElement.equals("cu"))
				{
					if (plate.averageOfLeftStandards < plate.averageOfRightStandards)
						problemPlate = true;

					if (plate.sampleCoefficientOfVariationOfLeftStandards > sampleCoefficientOfVariationThreshold)
						problemPlate = true;
				}

				else if (plate.rightStandardElement.equals("cu"))
				{
					if (plate.averageOfRightStandards < plate.averageOfLeftStandards)
						problemPlate = true;

					if (plate.sampleCoefficientOfVariationOfRightStandards > sampleCoefficientOfVariationThreshold)
						problemPlate = true;
				}
			}

			// Add to correct Vectors

			allPlatesVector.add(plate);

			if (!problemPlate)
				goodPlatesVector.add(plate);

			else
				problemPlatesVector.add(plate);
		}
	}

	// This method sorts the Vector of plates passed to it in order of the Plates rating.
	// This is done via the selection sort algorithm.
	public void sortPlateVector(Vector plateVectorToSort)
	{
		Vector<Plate> plateVector = plateVectorToSort;

		for (int i = 0; i < plateVector.size(); i++)
		{
			int swapIndex = i;

			for (int j = i + 1; j < plateVector.size(); j++)
			{
				if (plateVector.get(j).rating > plateVector.get(swapIndex).rating)
				{
					swapIndex = j;
				}
			}

			if (swapIndex != i)
			{
				Plate tempPlate = plateVector.get(i);

				Plate plateToSwap = plateVector.get(swapIndex);

				plateVector.remove(i);

				plateVector.add(i, plateToSwap);

				plateVector.remove(swapIndex);

				plateVector.add(swapIndex, tempPlate);
			}
		}
	}

	// This method writes one of three Plate Vectors to one of three output text files.
	// Which Vector and which output file is determined by the String "plateVectorType" argument passed to this method.
	public void writePlateVectorToFile(String plateVectorType)
	{
		PrintWriter writer;
		Vector<Plate> plateVector;

		if (plateVectorType.equals("ALL_PLATES"))
		{
			writer = writer_allPlates;
			plateVector = allPlatesVector;

			writer.println("This file contains information on all data sets.");
			writer.println("");
			writer.println("Input file name: " + inFileName);
			writer.println("");
			writer.println("Lowest acceptable rating: " + ratingThreshold);
			writer.println("Highest acceptable sample coefficient of variation: " + (sampleCoefficientOfVariationThreshold * 100) + "%");
			writer.println("");
			writer.println("Number of data sets: " + plateVector.size());
			writer.println("Number of data sets that are likely to have errors: " + problemPlatesVector.size());
			writer.println("");
		}

		else if (plateVectorType.equals("GOOD_PLATES"))
		{
			writer = writer_goodPlates;
			plateVector = goodPlatesVector;

			writer.println("This file contains information on all data sets that had the following:");
			writer.println("	Ratings >= " + ratingThreshold);
			writer.println("	Sample coefficient of variation of the high standard was <= " + (sampleCoefficientOfVariationThreshold * 100) + "%");
			writer.println("	Correct high/low standards depending on plate type");
			writer.println("");
			writer.println("Input file name: " + inFileName);
			writer.println("");
			writer.println("Lowest acceptable rating: " + ratingThreshold);
			writer.println("Highest acceptable sample coefficient of variation: " + (sampleCoefficientOfVariationThreshold * 100) + "%");
			writer.println("");
			writer.println("Number of valid data sets: " + plateVector.size());
			writer.println("");
		}

		else if (plateVectorType.equals("PROBLEM_PLATES"))
		{
			writer = writer_problemPlates;
			plateVector = problemPlatesVector;

			writer.println("This file contains information on all data sets that had one or more of the following:");
			writer.println("	Ratings < " + ratingThreshold);
			writer.println("	Sample coefficient of variation of the high standard was > " + (sampleCoefficientOfVariationThreshold * 100) + "%");
			writer.println("	Incorrect high/low standards depending on plate type");
			writer.println("");
			writer.println("Input file name: " + inFileName);
			writer.println("");
			writer.println("Lowest acceptable rating: " + ratingThreshold);
			writer.println("Highest acceptable sample coefficient of variation: " + (sampleCoefficientOfVariationThreshold * 100) + "%");
			writer.println("");
			writer.println("Number of potentially corrupt data sets: " + problemPlatesVector.size());
			writer.println("");
		}

		else
		{
			JOptionPane.showMessageDialog(this, "Something went wrong while writing to files!", "Error", JOptionPane.INFORMATION_MESSAGE);

			System.exit(1);

			writer = writer_allPlates;

			plateVector = allPlatesVector;

			writer.println("SOMETHING WENT WRONG WHILE WRITING TO FILES.");
		}

		writer.println("The higher the rating of a data set the better. A value of -999 indicates the rating was not calculated for that data set.");

		writer.println("");
		writer.println("============================================================================================================================");
		writer.println("");

		for (Plate x : plateVector)
		{
			writer.println("Plate Number: " + x.plateNumber + " | " + "Plate Type: " + x.plateType + " | " + "Plate Rating: " + x.rating + " | " + "data_id: " + x.id);

			writer.println("");

			if (x.inconsistentRatios == true)
			{
				writer.println("WARNING: This plate has inconsistent element ratios down a column!");
				writer.println("");
			}

			writer.println("Average of Left Standards: " + round(x.averageOfLeftStandards, 3));

			writer.println("Average of Right Standards: " + round(x.averageOfRightStandards, 3));

			writer.println("");

			writer.println("Sample Standard Deviation of Left Standards: " + round(x.sampleStandardDeviationOfLeftStandards, 2));
			writer.println("Sample Standard Deviation of Right Standards: " + round(x.sampleStandardDeviationOfRightStandards, 2));

			writer.println("");

			writer.println("Sample Coefficient of Variation of Left Standards: " + round((x.sampleCoefficientOfVariationOfLeftStandards * 100), 2) + "%");
			writer.println("Sample Coefficient of Variation of Right Standards: " + round((x.sampleCoefficientOfVariationOfRightStandards * 100), 2) + "%");

			writer.println("");
			writer.println("------------------------------------------------------------------------------");
			writer.println("");

			writer.println("Element combinations for each cell:");

			writer.println("");

			for (int row = 0; row < 6; row++)
			{
				writer.print("| ");

				for (int col = 0; col < 6; col++)
				{
					try
					{
						for (Element e : x.cellTable[row][col].elements)
						{
							String capitalizedElement = e.atomicSymbol.substring(0,1).toUpperCase() + e.atomicSymbol.substring(1);

							writer.print(capitalizedElement + " ");

							//writer.print(e.atomicSymbol + " ");
						}

						writer.print("| ");
					}

					catch (Exception e)
					{
						e.printStackTrace();

						System.out.println("EXCEPTION WHILE WRITING PLATE VECTOR TO FILE");
					}
				}

				writer.println("");
			}

			writer.println("");
			writer.println("------------------------------------------------------------------------------");
			writer.println("");

			writer.println("Element ratios for each cell:");

			writer.println("");

			for (int row = 0; row < 6; row++)
			{
				writer.print("| ");

				for (int col = 0; col < 6; col++)
				{
					try
					{
						for (Element e : x.cellTable[row][col].elements)
						{
							writer.print(e.ratio + " ");
						}

						writer.print("| ");
					}

					catch (Exception e)
					{
						e.printStackTrace();

						System.out.println("EXCEPTION WHILE WRITING PLATE VECTOR TO FILE");
					}
				}

				writer.println("");
			}

			writer.println("");
			writer.println("------------------------------------------------------------------------------");
			writer.println("");

			writer.println("Sample coefficients of variation for each column:");

			writer.println("");

			writer.print("| " + round((x.sampleCoefficientOfVariationOfLeftStandards * 100), 2) + "% ");
			writer.print("| " + round((x.sampleCoefficientOfVariationOfCol_1 * 100), 2) + "% ");
			writer.print("| " + round((x.sampleCoefficientOfVariationOfCol_2 * 100), 2) + "% ");
			writer.print("| " + round((x.sampleCoefficientOfVariationOfCol_3 * 100), 2) + "% ");
			writer.print("| " + round((x.sampleCoefficientOfVariationOfCol_4 * 100), 2) + "% ");
			writer.print("| " + round((x.sampleCoefficientOfVariationOfRightStandards * 100), 2) + "% |");

			writer.println("");

			writer.println("");
			writer.println("------------------------------------------------------------------------------");
			writer.println("");

			writer.println("Readings for each cell:");

			writer.println("");

			BigDecimal bd;
			MathContext mc = new MathContext(3);

			for (int row = 0; row < 6; row++)
			{
				writer.print("| ");

				for (int col = 0; col < 6; col++)
				{
					try
					{
						Cell c = x.cellTable[row][col];

						bd = new BigDecimal(c.value, mc);

						String value = bd.toString();

						int y = 7;

						y = y - value.length();

						for (int i = 0; i < y; i++)
							value += " ";

						writer.print(value + " | ");
					}

					catch (Exception e)
					{
						e.printStackTrace();

						System.out.println("EXCEPTION WHILE WRITING PLATE VECTOR TO FILE");
					}
				}

				writer.println("");
			}

			writer.println("");
			writer.println("------------------------------------------------------------------------------");
			writer.println("");

			writer.println("Readings divided by the average of the left standard column:");

			writer.println("");

			for (int row = 0; row < 6; row++)
			{
				writer.print("| ");

				for (int col = 0; col < 6; col++)
				{
					try
					{
						if (col == 0 || col == 5)
							writer.print("        | ");

						else
						{
							Cell c = x.cellTable[row][col];

							bd = new BigDecimal(c.valueDividedByLeftAverage, mc);

							String value = bd.toString();

							int y = 7;

							y = y - value.length();

							for (int i = 0; i < y; i++)
								value += " ";

							writer.print(value + " | ");
						}
					}

					catch (Exception e)
					{
						e.printStackTrace();

						System.out.println("EXCEPTION WHILE WRITING PLATE VECTOR TO FILE");
					}
				}

				writer.println("");
			}

			writer.println("");
			writer.println("------------------------------------------------------------------------------");
			writer.println("");

			writer.println("Readings divided by the average of the right standard column:");

			writer.println("");

			for (int row = 0; row < 6; row++)
			{
				writer.print("| ");

				for (int col = 0; col < 6; col++)
				{
					try
					{
						if (col == 0 || col == 5)
							writer.print("        | ");

						else
						{
							Cell c = x.cellTable[row][col];

							bd = new BigDecimal(c.valueDividedByRightAverage, mc);

							String value = bd.toString();

							int y = 7;

							y = y - value.length();

							for (int i = 0; i < y; i++)
								value += " ";

							writer.print(value + " | ");
						}
					}

					catch (Exception e)
					{
						e.printStackTrace();

						System.out.println("EXCEPTION WHILE WRITING PLATE VECTOR TO FILE");
					}
				}

				writer.println("");
			}

			writer.println("");
			writer.println("============================================================================================================================");
			writer.println("");
		}

		writer.close();
	}

	// This method creates an OUTDATED version of the mining.txt file. This version did not have headers.
	// The createDataMinindFileWithHeaders() method is what is used instead of this method.
	// This method is NOT CALLED ANYWHERE.
	public void createDataMiningFile()
	{
		PrintWriter writer = writer_mining;
		Vector<Plate> plateVector = allPlatesVector;

		for (Plate x : plateVector)
		{
			for (int row = 0; row < 6; row++)
			{
				for (int col = 0; col < 6; col++)
				{
					try
					{
						writer.print(x.plateNumber + " ");
						writer.print(x.plateType + " ");
						writer.print(row + " " + col + " ");
						writer.print(x.cellTable[row][col].value + " ");
						writer.print(x.cellTable[row][col].valueDividedByLeftAverage + " ");
						writer.print(x.cellTable[row][col].valueDividedByRightAverage + " ");
						writer.print(x.iron_nonStandard + " ");
						writer.print(x.copper_nonStandard + " ");
						writer.print(x.id + " ");

						for (String element : elements)
						{
							writer.print(element + " ");

							boolean containedElement = false;

							for (Element e : x.cellTable[row][col].elements)
							{
								if (e.atomicSymbol.equals(element))
								{
									containedElement = true;

									writer.print(e.ratio + " ");
								}
							}

							if (!containedElement)
								writer.print("0" + " ");
						}
					}

					catch (Exception e)
					{
						e.printStackTrace();

						System.out.println("EXCEPTION WHILE WRITING PLATE VECTOR TO FILE");
					}

					writer.println("");
				}
			}
		}

		writer.close();
	}

	// This method creates the data mining file "mining.txt" used by the PlateDataMiner class.
	// This version has headers.
	public void createDataMiningFileWithHeaders()
	{
		try
		{
			PrintWriter writer = writer_mining;

			//Vector<Plate> plateVector = allPlatesVector;

			Vector<Plate> plateVector = goodPlatesVector;

			writer.print("*number* ");
			writer.print("*type* ");
			writer.print("*row* ");
			writer.print("*col* ");
			writer.print("*value* ");
			writer.print("*leftRatio* ");
			writer.print("*rightRatio* ");
			writer.print("*ironNonstandard* ");
			writer.print("*copperNonstandard* ");
			writer.print("*dataId* ");

			for (String element : elements)
			{
				writer.print(element + " ");
			}

			writer.println("");

			for (Plate x : plateVector)
			{
				for (int row = 0; row < 6; row++)
				{
					for (int col = 0; col < 6; col++)
					{
						try
						{
							writer.print(x.plateNumber + " ");
							writer.print(x.plateType + " ");
							writer.print(row + " " + col + " ");
							writer.print(x.cellTable[row][col].value + " ");
							writer.print(x.cellTable[row][col].valueDividedByLeftAverage + " ");
							writer.print(x.cellTable[row][col].valueDividedByRightAverage + " ");
							writer.print(x.iron_nonStandard + " ");
							writer.print(x.copper_nonStandard + " ");
							writer.print(x.id + " ");

							for (String element : elements)
							{
								boolean containedElement = false;

								for (Element e : x.cellTable[row][col].elements)
								{
									if (e.atomicSymbol.equals(element))
									{
										containedElement = true;

										writer.print(e.ratio + " ");
									}
								}

								if (!containedElement)
									writer.print("0" + " ");
							}
						}

						catch (Exception e)
						{
							e.printStackTrace();

							System.out.println("EXCEPTION WHILE WRITING PLATE VECTOR TO FILE");
						}

						writer.println("");
					}
				}
			}

			writer.close();
		}

		catch (Exception e)
		{
			System.out.println("Exception in createDataMiningFileWithHeaders()");
		}
	}

	// This method will create "simple" versions of the output text files to allow a user
	// to more quickly read which plates are good.
	// It reqiures the Plate Vector to write to the file as well as the file name that the file should have.
	public void createSimplePlatesFile(Vector platesVector, String fileName)
	{
		try
		{
			PrintWriter writer = new PrintWriter(new FileOutputStream(fileName));
			Vector<Plate> plateVector = platesVector;

			if (fileName.equals("all_data_sets_simple.txt"))
			{
				writer.println("This file contains information on all data sets.");
				writer.println("");
				writer.println("Input file name: " + inFileName);
				writer.println("");
				writer.println("Lowest acceptable rating: " + ratingThreshold);
				writer.println("Highest acceptable sample coefficient of variation: " + (sampleCoefficientOfVariationThreshold * 100) + "%");
				writer.println("");
				writer.println("Number of data sets: " + plateVector.size());
				writer.println("Number of data sets that are likely to have errors: " + problemPlatesVector.size());
				writer.println("");
			}

			else if (fileName.equals("valid_data_sets_simple.txt"))
			{
				writer.println("This file contains information on all data sets that had the following:");
				writer.println("	Ratings >= " + ratingThreshold);
				writer.println("	Sample coefficient of variation of the high standard was <= " + (sampleCoefficientOfVariationThreshold * 100) + "%");
				writer.println("	Correct high/low standards depending on plate type");
				writer.println("");
				writer.println("Input file name: " + inFileName);
				writer.println("");
				writer.println("Lowest acceptable rating: " + ratingThreshold);
				writer.println("Highest acceptable sample coefficient of variation: " + (sampleCoefficientOfVariationThreshold * 100) + "%");
				writer.println("");
				writer.println("Number of valid data sets: " + plateVector.size());
				writer.println("");
			}

			else if (fileName.equals("invalid_data_sets_simple.txt"))
			{
				writer.println("This file contains information on all data sets that had one or more of the following:");
				writer.println("	Ratings < " + ratingThreshold);
				writer.println("	Sample coefficient of variation of the high standard was > " + (sampleCoefficientOfVariationThreshold * 100) + "%");
				writer.println("	Incorrect high/low standards depending on plate type");
				writer.println("");
				writer.println("Input file name: " + inFileName);
				writer.println("");
				writer.println("Lowest acceptable rating: " + ratingThreshold);
				writer.println("Highest acceptable sample coefficient of variation: " + (sampleCoefficientOfVariationThreshold * 100) + "%");
				writer.println("");
				writer.println("Number of potentially corrupt data sets: " + problemPlatesVector.size());
				writer.println("");
			}

			writer.println("FORMAT:");
			writer.print("*plate_number*    ");
			writer.print("*plate_type*    ");
			writer.print("*plate_rating*    ");
			writer.print("*data_id*");
			writer.println("");
			writer.println("");

			for (Plate x : plateVector)
			{
				writer.print(x.plateNumber + "    ");
				writer.print(x.plateType + "    ");

				if (x.plateType.equals("red"))
					writer.print("  ");

				writer.print(x.rating + "    ");
				writer.print(x.id + "    ");

				if (x.inconsistentRatios == true)
					writer.print("WARNING: This plate has inconsistent element ratios down a column!");

				writer.println("");
				writer.println("");
			}

			writer.close();
		}

		catch (Exception e)
		{
			System.out.println("Exception in createSimpleGoodPlatesFile()");
		}
	}

	// This method can be used to determine wether a plate has ratio to standard variables that are a factor
	// of 100 greater than they should be.
	// It will return true if the Plate has factor of 100 ratios, and false otherwise.
	// NOTE: There is a margin for error with this method, it is theoretically possible for it to return true incorrectly.
	// The chance of this occuring is very slim.
	// This method is NOT CALLED ANYWHERE.
	public boolean factorOf100Plate(Plate plateToVerify)
	{
		Plate tempPlate = plateToVerify;

		Cell tempCell;

		for (int x = 0; x < 6; x++)
		{
			for (int y = 1; y < 5; y++)
			{
				tempCell = tempPlate.cellTable[x][y];

				if (tempPlate.plateType.equals("red"))
				{
					double factorValueRight = ((tempCell.value / tempPlate.averageOfRightStandards) * 100);

					if (tempCell.valueDividedByRightAverage <= (factorValueRight + 5) &&
						tempCell.valueDividedByRightAverage >= (factorValueRight - 5))
					{
						return true;
					}
				}

				else if (tempPlate.plateType.equals("black"))
				{
					double factorValueLeft = ((tempCell.value / tempPlate.averageOfLeftStandards) * 100);

					if (tempCell.valueDividedByLeftAverage <= (factorValueLeft + 5) &&
						tempCell.valueDividedByLeftAverage >= (factorValueLeft - 5))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	// This method recalculates the ratio to standard values for a the argument Plate.
	// This is used to correct the factor of 100 plates.
	public void calculateRatios(Plate plateToCorrect)
	{
		Plate tempPlate = plateToCorrect;

		Cell tempCell;

		for (int x = 0; x < 6; x++)
		{
			for (int y = 1; y < 5; y++)
			{
				tempCell = tempPlate.cellTable[x][y];

				tempCell.valueDividedByLeftAverage = (tempCell.value / tempPlate.averageOfLeftStandards);
				tempCell.valueDividedByRightAverage = (tempCell.value / tempPlate.averageOfRightStandards);
			}
		}
	}

	// This method recalculates the rating of the argument Plate. This is necessary because plates
	// that suffer from factor of 100 ratios will have inordinately high ratings.
	public void calculateRating(Plate plateToCalculate)
	{
		Plate plate = plateToCalculate;

		plate.rating = -999;

		Cell tempCell;

		Double tempRating = -999.0;

		for (int x = 0; x < 6; x++)
		{
			for (int y = 1; y < 5; y++)
			{
				tempCell = plate.cellTable[x][y];

				if (plate.plateType.equals("red")) // Red means you should divide by the iron standard
				{
					if (plate.leftStandardElement.equals("fe"))
						tempRating = plate.cellTable[x][y].valueDividedByLeftAverage;

					else if (plate.rightStandardElement.equals("fe"))
						tempRating = plate.cellTable[x][y].valueDividedByRightAverage;

					if (tempRating > plate.rating)
						plate.rating = tempRating;
				}

				else if (plate.plateType.equals("black")) // Black means you should divide by the copper standard
				{
					if (plate.leftStandardElement.equals("cu"))
						tempRating = plate.cellTable[x][y].valueDividedByLeftAverage;

					else if (plate.rightStandardElement.equals("cu"))
						tempRating = plate.cellTable[x][y].valueDividedByRightAverage;

					if (tempRating > plate.rating)
						plate.rating = tempRating;
				}
			}
		}
	}

	// This method simply clears the user input fields.
	public void clear()
	{
		input.setText("");
		field_ratingThreshold.setText("");
		field_sampleCoefficientOfVariationThreshold.setText("");
		findElementCombinationButton.setSelected(false);

		input.requestFocus();
	}

	// This method performs initial operations required before the parsing of the input CSV file can begin.
	// It checks whether the user has entered valid information.
	// If no error, it disables the GUI buttons while the program is running.
	public void setupFormat()
	{
		boolean error = false;

		try
		{
			inFileName = input.getText();

			ratingThreshold = Double.parseDouble(field_ratingThreshold.getText());
			sampleCoefficientOfVariationThreshold = Double.parseDouble(field_sampleCoefficientOfVariationThreshold.getText());
			sampleCoefficientOfVariationThreshold = (sampleCoefficientOfVariationThreshold / 100);

			reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFileName)));
			writer_allPlates = new PrintWriter(new FileOutputStream("all_data_sets.txt"));
			writer_goodPlates = new PrintWriter(new FileOutputStream("valid_data_sets.txt"));
			writer_problemPlates = new PrintWriter(new FileOutputStream("invalid_data_sets.txt"));
			writer_mining = new PrintWriter(new FileOutputStream("mining.txt"));
		}

		catch (Exception e)
		{
			System.out.println("INVALID USER INPUT");

			error = true;

			JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.INFORMATION_MESSAGE);
		}

		if (!error)
		{
			input.setEnabled(false);
			field_ratingThreshold.setEnabled(false);
			field_sampleCoefficientOfVariationThreshold.setEnabled(false);
			format.setEnabled(false);
			clear.setEnabled(false);
			findElementCombinationButton.setEnabled(false);

			allPlatesVector = new Vector();
			goodPlatesVector = new Vector();
			problemPlatesVector = new Vector();
			elements = new Vector();

			new Thread(this).start(); // Effectively executes the public void run() method.
		}
	}

	// This method is part of the Runnable interface. It calls the format() method which calls
	// all other functions required to parse the input CSV file and create the Plate objects.
	// Code that is not in the SwingUtilities.invokeLater() method is executed by a new Thread.
	// The program was designed this way to allow the user to kill the program mid operation.
	// If a different Thread was not used, this code would be executed on the event dispatcher Thread.
	// This is the same Thread that responds to the user interaction with GUI components.
	// If this Thread is busy, then the GUI would become unresponsive until the Thread is no longer busy.
	public void run()
	{
		try
		{
			// This method executes its contents through via the event dispatcher.
			// This must be done for GUI updates.
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					progressDialog = new ProgressDialog("Formatting...");
				}
			});
		}

		catch (Exception e)
		{
			System.out.println("Exception while creating progress bar!");
		}

		format(); // This is the primary method responsible for calling the other parsing methods.

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
			System.out.println("Exception while disposing the progress bar!");
		}
	}

	// This method calls various other methods in the correct order to:
	// 1: Read the input CSV file and store the resulting Plate objects into various Vectors.
	// 2: Sort the contents of those Vectors by the Plate's rating.
	// 3: Write the contents of those three Vectors to their respective files.
	// 4: Create simple versions of those files.
	// 5: Create the data mining file used by the PlateDataMiner.
	// 6: Construct and initialize the PlateDataMiner.
	public void format()
	{
		processFile();

		sortPlateVector(allPlatesVector);
		sortPlateVector(goodPlatesVector);
		sortPlateVector(problemPlatesVector);

		writePlateVectorToFile("ALL_PLATES");
		writePlateVectorToFile("GOOD_PLATES");
		writePlateVectorToFile("PROBLEM_PLATES");

		createSimplePlatesFile(allPlatesVector, "all_data_sets_simple.txt");
		createSimplePlatesFile(goodPlatesVector, "valid_data_sets_simple.txt");
		createSimplePlatesFile(problemPlatesVector, "invalid_data_sets_simple.txt");

		createDataMiningFileWithHeaders();

		miner = new PlateDataMiner(this, "mining.txt");

		miner.initialize();
	}

	// Simple utility method. It returns whatever double is passed to it with the desired number of significant figures.
	public double round(Double numToRound, int sigFigs)
	{
		BigDecimal tempBigDecimal;
		double tempDouble;

		tempBigDecimal = new BigDecimal(numToRound);
		tempBigDecimal = tempBigDecimal.round(new MathContext(sigFigs));
		tempDouble = tempBigDecimal.doubleValue();

		return tempDouble;
	}

	// Part of the ActionListener interface. Crucial to the operation of the GUI.
	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		if (cmd.equals("FORMAT"))
			setupFormat();

		else if (cmd.equals("CLEAR"))
			clear();

		else if (cmd.equals("HELP"))
		{
			if (helpDialog == null)
			{
				helpDialog = new HelpDialog("Program Information", "READ_ME.txt", 20f);
			}

			else
			{
				helpDialog.setVisible(true);

				helpDialog.toFront();
			}
		}
	}

	// The main method. Used to run this program.
	public static void main (String[] x)
	{
		PlateDataFormatter plateDataFormatter = new PlateDataFormatter();
	}
}