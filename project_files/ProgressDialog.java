import javax.swing.*;
import java.awt.*;

// This simple class creates a JProgressBar in a JFrame where the title is specified by the constructor argument.
class ProgressDialog extends JFrame
{
	JProgressBar progressBar;

	String title;

	ProgressDialog(String title)
	{
		this.title = title;

		try
		{
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
			progressBar.setVisible(true);

			add(progressBar);

			repaint();
		}

		catch(Exception e)
		{
			System.out.println("Exception in ProgressDialog's constructor.");
		}

		setupProgressDialog();
	}

	public void setupProgressDialog()
	{
		Toolkit tk;

		Dimension d;

		tk = Toolkit.getDefaultToolkit();

		d = tk.getScreenSize();

		setSize(d.width/4, d.height/13);

		setLocation(d.width/3, d.height/2);

		setTitle(title);

		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setVisible(true);
	}

	public static void main(String[] x)
	{
		new ProgressDialog("Test Title");
	}
}