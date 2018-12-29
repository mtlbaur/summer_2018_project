import javax.swing.*;
import java.awt.*;
import java.io.*;

// This class creates a JFrame with the text contents of an input text file (helpContentSource) with the specified fontSize.
class HelpDialog extends JFrame
{
	String title;
	String helpContentSource;

	JTextArea textArea;
	JScrollPane scroller;

	BufferedReader reader;

	HelpDialog(String title, String helpContentSource, Float fontSize)
	{
		try
		{
			Container cp = getContentPane();
			String content = "";

			this.title = title;
			this.helpContentSource = helpContentSource;

			//reader = new BufferedReader(new InputStreamReader(new FileInputStream(helpContentSource)));
			reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(helpContentSource)));

			try
			{
				while (reader.ready())
				{
					content += reader.readLine();
					content += "\n";
				}
			}

			catch (Exception x)
			{
				System.out.println("DONE READING");
			}

			textArea = new JTextArea();
			textArea.setEditable(false);
			//textArea.setLineWrap(true);
			textArea.setText(content);
			textArea.setFont(textArea.getFont().deriveFont(fontSize));

			scroller = new JScrollPane(textArea);

			cp.add(scroller);
		}

		catch (Exception e)
		{
			e.printStackTrace();

			System.out.println("ERROR WHILE CREATING HELP DIALOG");
		}

		setupHelpDialog();
	}

	public void setupHelpDialog()
	{
		Toolkit tk;

		Dimension d;

		tk = Toolkit.getDefaultToolkit();

		d = tk.getScreenSize();

		setSize(d.width/2, d.height/2);

		setLocation(d.width/4, d.height/4);

		setTitle(title);

		setDefaultCloseOperation(HIDE_ON_CLOSE);

		setVisible(true);
	}

	public static void main(String[] x)
	{
		new HelpDialog("Test Title", "READ_ME.txt", 20f);
	}
}