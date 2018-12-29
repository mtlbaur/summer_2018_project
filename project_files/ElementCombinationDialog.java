import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// This class creates a JFrame with a list of JRadioButtons to represent all the elements used throughout all Plates.
// The user selects whichever elements they are interested in and either searches for those elements or cancels.
class ElementCombinationDialog extends JFrame
	implements ActionListener, WindowListener
{
	PlateDataFormatter host;

	Vector<String> elements;

	JButton search;

	JButton cancel;

	GroupLayout layout;

	JPanel buttonPanel;
	JPanel mainPanel;

	JScrollPane scroller;

	Vector<JRadioButton> buttonVector;

	ElementCombinationDialog(PlateDataFormatter host, Vector<String> elements)
	{
		Container cp = getContentPane();

		this.host = host;

		this.elements = elements;

		this.addWindowListener(this);

		search = new JButton("Search");
		search.addActionListener(this);
		search.setActionCommand("SEARCH");
		getRootPane().setDefaultButton(search);

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.setActionCommand("CANCEL");

		buttonPanel = new JPanel();
		buttonPanel.add(search);
		buttonPanel.add(cancel);

		cp.add(buttonPanel, BorderLayout.SOUTH);

		//System.out.println("elements.size(): " + elements.size());

		mainPanel = new JPanel(new GridLayout(elements.size(), 0));

		buttonVector = new Vector();

		for (String element : elements)
		{
			//System.out.println(element);

			//JLabel label = new JLabel(element);

			JRadioButton button = new JRadioButton(element, false);

			mainPanel.add(button);
			buttonVector.add(button);
		}

		scroller = new JScrollPane(mainPanel);

        cp.add(scroller, BorderLayout.CENTER);

		setupElementCombinationDialog();
	}

	public void setupElementCombinationDialog()
	{
		Toolkit tk;

		Dimension d;

		tk = Toolkit.getDefaultToolkit();

		d = tk.getScreenSize();

		setSize(d.width/4, d.height/2);

		setLocation(d.width/2, d.height/4);

		setTitle("Select Target Elements");

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		//setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setVisible(true);
	}

	// This method figures out which elements were selected as targets and
	// it tells the PlateDataMiner that the user has finished selecting.
	public void search()
	{
		//System.out.println("ElementCombinationDialog: SEARCH");

		host.miner.targetElements = new Vector();

		for (JRadioButton x : buttonVector)
		{
			if (x.isSelected())
				host.miner.targetElements.add(x.getText());
		}

		host.miner.targetsSelected = true;

		//host.miner.elementCombinationFinder = new ElementCombinationFinder(host.miner, host.miner.cells, host.miner.targetCells, host.miner.targetElements);

		//host.miner.getPlatesWithTargetElementCombination();

		this.dispose();
	}

	// This method tells the PlateDataMiner that the user has canceled the selection process.
	public void cancel()
	{
		//System.out.println("ElementCombinationDialog: CANCEL");

		//this.setVisible(false);

		host.miner.canceledTargetSelection = true;

		this.dispose();
	}

	public void actionPerformed(ActionEvent e)
	{
		String cmd = e.getActionCommand();

		if (cmd.equals("SEARCH"))
		{
			search();
		}

		else if (cmd.equals("CANCEL"))
		{
			cancel();
		}
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowClosed(WindowEvent e)
	{
	}

	public void windowClosing(WindowEvent e)
	{
		System.out.println("ElementCombinationDialog: windowClosing");
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowOpened(WindowEvent e)
	{
	}

	public static void main(String x[])
	{
		ElementCombinationDialog ecd = new ElementCombinationDialog(new PlateDataFormatter(), new Vector());
	}
}