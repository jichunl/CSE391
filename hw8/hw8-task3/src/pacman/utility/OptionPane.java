package pacman.utility;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;


/** <P>OptionPane makes it easy to pop up a standard dialog box that prompts users
  * for a value or informs them of something.</P>
  *
  * <P>While the OptionPane class may appear complex because of the large number of methods,
  * almost all uses of this class are one-line calls to one of the static showXxxDialog
  * methods shown below: </P>
  *
  * <P><UL><LI>showConfirmDialog asks a confirming question, like yes/no/cancel.</LI>
  * <LI>showInputDialog prompts for some input.</LI>
  * <LI>showMessageDialog tells the user about something that has happened.</LI></UL></P>
  *
  * <P>This class may be freely redistributed for non-profit purposes.</P>
  *
  * @author Martin Stepp (<A HREF="mailto:stepp@cs.arizona.edu">stepp@cs.arizona.edu</A>)
  * @version 2001/7/17 last update (to remove deprecation)
  */
public class OptionPane extends Dialog implements ActionListener {
    private static final long serialVersionUID = 0;
    
    // constants
    /** This constant is returned by OptionPane.showConfirmDialog when the user clicks the Yes button. */
    public static final int         YES_OPTION = 0;

    /** This constant is returned by OptionPane.showConfirmDialog when the user clicks the No button. */
    public static final int         NO_OPTION = 1;

    /** This constant is returned by OptionPane.showConfirmDialog and
      * OptionPane.showInputDialog when the user clicks the Cancel button. */
    public static final int         CANCEL_OPTION = 2;


	// instance vars
	private static int CONFIRM_TYPE = 0;
	private static int INPUT_TYPE = 1;
	private static int MESSAGE_TYPE = 2;

	private Button my_yesButton, my_noButton, my_okButton, my_cancelButton;
	private TextField my_field;
	private Object my_choice;


	/** Constructs a new OptionPane of the given type with the given Frame as
	  * its parent, with the given window title, displaying the given message.
	  *
	  * If parent is non-null, the OptionPane will center itself with respect
	  * to the parent.  Otherwise, the OptionPane will locate itself at (0, 0).
	  *
	  * Acceptable types are OptionPane.CONFIRM_TYPE, OptionPane.INPUT_TYPE, and OptionPane.MESSAGE_TYPE.
	  */
	private OptionPane(Frame parent, String title, String message, int type) {
		super((parent != null) ? parent : new Frame(), title, true);

		// construct components
		my_yesButton    = new Button("Yes");
		my_noButton     = new Button("No");
		my_okButton     = new Button("OK");
		my_cancelButton = new Button("Cancel");
		my_field = new TextField(10);

		// event listening
		my_yesButton.addActionListener(this);
		my_noButton.addActionListener(this);
		my_okButton.addActionListener(this);
		my_cancelButton.addActionListener(this);
		my_field.addActionListener(this);

		// layout
		Panel contentPane = new Panel(new BorderLayout());
		Panel centerPanel = new Panel();
		Panel southPanel = new Panel();

		Panel labelPanel = new Panel(new GridLayout(0, 1));
		StringTokenizer tokenizer = new StringTokenizer(message, "\n");
		while (tokenizer.hasMoreTokens())
			labelPanel.add(new Label(tokenizer.nextToken()));

		if (type == CONFIRM_TYPE) {
			centerPanel.add(labelPanel);
			southPanel.add(my_yesButton);
			southPanel.add(my_noButton);
			southPanel.add(my_cancelButton);
			my_yesButton.requestFocus();
		}
		else if (type == INPUT_TYPE) {
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(labelPanel, BorderLayout.CENTER);
			centerPanel.add(my_field, BorderLayout.SOUTH);
			southPanel.add(my_okButton);
			southPanel.add(my_cancelButton);
			my_okButton.requestFocus();
		}
		else if (type == MESSAGE_TYPE) {
			centerPanel.add(labelPanel);
			southPanel.add(my_okButton);
			my_okButton.requestFocus();
		}

		contentPane.add(centerPanel, BorderLayout.CENTER);
		contentPane.add(southPanel, BorderLayout.SOUTH);
		add(contentPane);
		pack();

		// set location to center of parent
		int x = 0;
		int y = 0;

		if (parent != null) {
			Point p = parent.getLocation();
			Dimension size = parent.getSize();
			x = p.x + (size.width  - getSize().width ) / 2;
			y = p.y + (size.height - getSize().height) / 2;
		}

		setLocation(x, y);
	}


	/** Processes ActionEvents in this OptionPane. */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		if (source == my_yesButton)
			my_choice = new Integer(YES_OPTION);
		if (source == my_noButton)
			my_choice = new Integer(NO_OPTION);
		else if (source == my_okButton  ||  source == my_field)
			my_choice = my_field.getText();
		else if (source == my_cancelButton)
			my_choice = null;

		// hide();
		dispose();
	}


	/** Shows this OptionPane and returns the resulting input, if any. */
	private Object showDialog() {
		setVisible(true);
		return my_choice;
	}


	/** Shows a confirmation dialog with the given message, using the given Frame as its parent.
	  * @return an integer corresponding to the button the user pressed.  The integer may be one of
	  * OptionPane.YES_OPTION, OptionPane.NO_OPTION, or OptionPane.CANCEL_OPTION.
	  */
	public static int showConfirmDialog(Frame parent, String message) {
		Integer i = (Integer)(new OptionPane(parent, "Confirm", message, CONFIRM_TYPE).showDialog());
		return (i != null) ? i.intValue() : CANCEL_OPTION;
	}
	public static int showConfirmDialog(Applet parent, String message) {
		return showConfirmDialog((Frame)null, message);
	}

	/** Shows a dialog asking for input, with the given message, using the given Frame as its parent.
	  * @return the input text typed by the user; null if the user presses Cancel.
	  */
	public static String showInputDialog(Frame parent, String message) {
		return (String)(new OptionPane(parent, "Enter Input", message, INPUT_TYPE).showDialog());
	}
	public static String showInputDialog(Applet parent, String message) {
		return showInputDialog((Frame)null, message);
	}

	/** Shows a dialog displaying the given message, using the given Frame as its parent. */
	public static void showMessageDialog(Frame parent, String message) {
		new OptionPane(parent, "Message", message, MESSAGE_TYPE).showDialog();
	}
	public static void showMessageDialog(Applet parent, String message) {
		showMessageDialog((Frame)null, message);
	}
}