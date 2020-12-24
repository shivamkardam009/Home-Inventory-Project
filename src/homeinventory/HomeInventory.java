package homeinventory;

import homeinventory.InventoryItem;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;
import java.text.*; 
import com.toedter.calendar.*;
import java.awt.print.*;

public class HomeInventory extends JFrame 
{
	GridBagConstraints gridConstraints;
	Container c;
	JToolBar inventorytoolbar;
	JButton newbutton,deletebutton,savebutton,previousbutton,nextbutton,printbutton,exitbutton,photobutton;
	JLabel itemlabel,locationlabel,seriallabel,pricelabel,datelabel,storelabel,notelabel,photolabel;
	JTextField itemtextfield,serialtextfield,pricetextfield,storetextfield,notetextfield;
	JComboBox locationcombobox;
	JCheckBox markedcheckbox;
	JDateChooser datedatechooser;
	static JTextArea phototextarea;
	JPanel searchpanel;
	JButton[] searchbutton;
	PhotoPanel photoPanel;
	
	InventoryItem myItem=new InventoryItem(); 
	static final int maximumentries=300;
	static int numberentries;
	static InventoryItem[] myInventory=new InventoryItem[maximumentries];
	int currententry;
	static final int entriesperpage=2;
	static int lastpage;
	
	public static void main(String[] args) 
	{
		new HomeInventory();
	}
	
	public HomeInventory()
	{
	super("Home Inventory");
	setSize(Toolkit.getDefaultToolkit().getScreenSize());
	setResizable(false);
	c=getContentPane();
	setVisible(true);
	c.setLayout(new GridBagLayout());
	gridConstraints=new GridBagConstraints();
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	
	addWindowListener(new WindowAdapter()
	{
		public void windowClosing(WindowEvent evt)
		{
			dispose();
		}
	});
	
	inventorytoolbar=new JToolBar();
	inventorytoolbar.setFloatable(false);
	inventorytoolbar.setBackground(Color.blue);
	inventorytoolbar.setOrientation(SwingConstants.VERTICAL);
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=0;
	gridConstraints.gridy=0;
	gridConstraints.gridheight=8;
	gridConstraints.fill=GridBagConstraints.VERTICAL;
	c.add(inventorytoolbar,gridConstraints);
	inventorytoolbar.addSeparator();
	
	Dimension bSize=new Dimension(70,50);
	
	newbutton=new JButton(new ImageIcon(this.getClass().getResource("/newfile.png")));
	newbutton.setText("New"); 
	sizeButton(newbutton,bSize);
	newbutton.setToolTipText("Add New Item");
	newbutton.setHorizontalTextPosition(SwingConstants.CENTER);
	newbutton.setVerticalTextPosition(SwingConstants.BOTTOM);
	newbutton.setFocusable(false);
	inventorytoolbar.add(newbutton);
	
	newbutton.addActionListener(new ActionListener() 
	{
		public void actionPerformed(ActionEvent e) 
		{
			checksave();
			blankvalues();
		}
	});
	
	deletebutton=new JButton(new ImageIcon(this.getClass().getResource("/delete.gif")));
	deletebutton.setText("Delete");
	sizeButton(deletebutton,bSize);
	deletebutton.setToolTipText("Delete Current Item");
	deletebutton.setHorizontalTextPosition(SwingConstants.CENTER);
	deletebutton.setVerticalTextPosition(SwingConstants.BOTTOM);
	deletebutton.setFocusable(false);
	inventorytoolbar.add(deletebutton);
	deletebutton.addActionListener(new ActionListener() 
	{
		public void actionPerformed(ActionEvent e) 
		{
			if(JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this item?", "Delete Inventory Item", JOptionPane.YES_NO_OPTION ,JOptionPane.QUESTION_MESSAGE)== JOptionPane.NO_OPTION)   
				return;
			deleteentry(currententry);
			if(numberentries==0)
			{
				currententry=0;
				blankvalues();
			}
			else
			{
				currententry--;
				if(currententry==0)
					currententry=1;
				showentry(currententry);
			}	
		}
	});
	
	savebutton=new JButton(new ImageIcon(this.getClass().getResource("/save.gif")));
	savebutton.setText("Save");
	sizeButton(savebutton,bSize);
	savebutton.setToolTipText("Save Current Item");
	savebutton.setHorizontalTextPosition(SwingConstants.CENTER);
	savebutton.setVerticalTextPosition(SwingConstants.BOTTOM);
	savebutton.setFocusable(false);
	inventorytoolbar.add(savebutton);
    savebutton.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			//check for description
			itemtextfield.setText(itemtextfield.getText().trim());
			if(itemtextfield.getText().equals(""))
			{
				JOptionPane.showConfirmDialog(null, "Must have item description", "Error" , JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
				itemtextfield.requestFocus();
				return;
			}
			if(newbutton.isEnabled())
			{
				//delete edit entry then resave
				deleteentry(currententry);
			}
			//Textfield First Letter Capital
			String s=itemtextfield.getText();
			itemtextfield.setText(s.substring(0,1).toUpperCase()+s.substring(1));
			numberentries++;
			
			//determine new current entry location based on description
			currententry=1;
			if(numberentries!=1)
			{
				do
				{
					if(itemtextfield.getText().compareTo(myInventory[currententry-1].description)<0)
					break;
					currententry++;
				}while(currententry<numberentries);
			}
			
			//move all entries below new value down one position unless at end
			if(currententry!=numberentries)
			{
				for(int i=numberentries;i>=currententry+1;i--)
				{
					myInventory[i-1]=myInventory[i-2];
					myInventory[i-2]=new InventoryItem();
				}
			}
			
			myInventory[currententry-1]=new InventoryItem();
			myInventory[currententry-1].description=itemtextfield.getText();
			myInventory[currententry-1].location=locationcombobox.getSelectedItem().toString();
			myInventory[currententry-1].marked=markedcheckbox.isSelected();
			myInventory[currententry-1].serialnumber=serialtextfield.getText();
			myInventory[currententry-1].purchaseprice=pricetextfield.getText();
			myInventory[currententry-1].purchasedate=dateToString(datedatechooser.getDate());
			myInventory[currententry-1].purchaselocation=storetextfield.getText();
			myInventory[currententry-1].photofile=phototextarea.getText();
			myInventory[currententry-1].note=notetextfield.getText();
			showentry(currententry);
			if(numberentries<maximumentries)
				newbutton.setEnabled(true);
			else
				newbutton.setEnabled(true);
			deletebutton.setEnabled(true);
			printbutton.setEnabled(true);
			
	    }
	});
    
    inventorytoolbar.addSeparator();
    
    previousbutton=new JButton(new ImageIcon(this.getClass().getResource("/previous.gif")));
    previousbutton.setText("Previous");
    sizeButton(previousbutton,bSize);
    previousbutton.setToolTipText("Display Previous Item");
    previousbutton.setHorizontalTextPosition(SwingConstants.CENTER);
    previousbutton.setVerticalTextPosition(SwingConstants.BOTTOM);
    previousbutton.setFocusable(false);
    inventorytoolbar.add(previousbutton);
    
    previousbutton.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			checksave();
			currententry--;
			showentry(currententry);
			
	    }
	}	);
    
    nextbutton=new JButton(new ImageIcon(this.getClass().getResource("/next.gif")));
    nextbutton.setText("Next");
    sizeButton(nextbutton,bSize);
    nextbutton.setToolTipText("Display Next Item");
    nextbutton.setHorizontalTextPosition(SwingConstants.CENTER);
    nextbutton.setVerticalTextPosition(SwingConstants.BOTTOM);
    nextbutton.setFocusable(false);
    inventorytoolbar.add(nextbutton);
    
    nextbutton.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			checksave();
			currententry++;
			showentry(currententry);
	  	}
	});
    
    inventorytoolbar.addSeparator();
    
	printbutton=new JButton(new ImageIcon(this.getClass().getResource("/print.gif")));
	printbutton.setText("Print");
	sizeButton(printbutton,bSize);
	printbutton.setToolTipText("Print Inventory List");
	printbutton.setHorizontalTextPosition(SwingConstants.CENTER);
	printbutton.setVerticalTextPosition(SwingConstants.BOTTOM);
	printbutton.setFocusable(false);
	inventorytoolbar.add(printbutton);
	
	printbutton.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			lastpage=(int)(1+(numberentries-1)/entriesperpage);
			PrinterJob inventoryPrinterJob=PrinterJob.getPrinterJob();
			inventoryPrinterJob.setPrintable(new InventoryDocument());
			if(inventoryPrinterJob.printDialog())
			{
				try {
					inventoryPrinterJob.print();
				}catch(PrinterException ex)
				{
					JOptionPane.showConfirmDialog(null, ex.getMessage() ,"Print Error", JOptionPane.DEFAULT_OPTION , JOptionPane.ERROR_MESSAGE );
				}
			}
	    }
	});

	exitbutton=new JButton();
	exitbutton.setText("Exit");
	sizeButton(exitbutton,bSize);
	exitbutton.setToolTipText("Exit Program");
	exitbutton.setFocusable(false);
	inventorytoolbar.add(exitbutton);
	exitbutton.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e)
		{
			if(JOptionPane.showConfirmDialog(null, "Any unsaved changes will be lost.\n Are you sure you want to exit?" , "Exit Program" , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)==JOptionPane.NO_OPTION)
				return;
				
			//Entry write back to file
			try
			{
				PrintWriter outputFile=new PrintWriter(new BufferedWriter(new FileWriter("inventory.txt")));
				outputFile.println(numberentries);
				if(numberentries!=0)
				{
					for(int i=0;i<numberentries;i++)
					{
						outputFile.println(myInventory[i].description);
						outputFile.println(myInventory[i].location);
						outputFile.println(myInventory[i].serialnumber);
						outputFile.println(myInventory[i].marked);
						outputFile.println(myInventory[i].purchaseprice);
						outputFile.println(myInventory[i].purchasedate);
						outputFile.println(myInventory[i].purchaselocation);
						outputFile.println(myInventory[i].note);
						outputFile.println(myInventory[i].photofile);
					}
				}
				
				//combobox entries
				outputFile.println(locationcombobox.getItemCount());
				if(locationcombobox.getItemCount()!=0)
				{
					for(int i=0;i<locationcombobox.getItemCount();i++)
						outputFile.println(locationcombobox.getItemAt(i));
				}
				outputFile.close();
			}catch(Exception ex) {System.out.println(e);}
			System.exit(0);
	    }
	});
	
	itemlabel=new JLabel("Inventory Item");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=1;
	gridConstraints.gridy=0;
	gridConstraints.insets=new Insets(10,10,0,10);
	gridConstraints.anchor=GridBagConstraints.EAST;
	c.add(itemlabel,gridConstraints);
	
	itemtextfield=new JTextField();
	itemtextfield.setPreferredSize(new Dimension(400,25));
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=2;
	gridConstraints.gridy=0;
	gridConstraints.gridwidth=5;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(itemtextfield,gridConstraints);
	itemtextfield.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			locationcombobox.requestFocus();
	    }
	}	);
	
	locationlabel=new JLabel("Location");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=1;
	gridConstraints.gridy=1;
	gridConstraints.insets=new Insets(10,10,0,10);
	gridConstraints.anchor=GridBagConstraints.EAST;
	c.add(locationlabel,gridConstraints);
	
	locationcombobox=new JComboBox();
	locationcombobox.setPreferredSize(new Dimension(270,25));
	locationcombobox.setFont(new Font("Aral",Font.PLAIN,12));
	locationcombobox.setEditable(true);
	locationcombobox.setBackground(Color.white);
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=2;
	gridConstraints.gridy=1;
	gridConstraints.gridwidth=3;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(locationcombobox,gridConstraints);
	locationcombobox.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			//found in combobox
			if(locationcombobox.getItemCount()!=0)
			{
				//if in list exit method
				for(int i=0;i<locationcombobox.getItemCount();i++)
				{
					if(locationcombobox.getSelectedItem().toString().equals(locationcombobox.getItemAt(i).toString()))
					{
						serialtextfield.requestFocus();
						return;
					}
				}
			}
			//If not found,Add to list box
			locationcombobox.addItem(locationcombobox.getSelectedItem());
			serialtextfield.requestFocus();
	    }
	}	);
	
	
	markedcheckbox=new JCheckBox();
	markedcheckbox.setText("Marked?");
	markedcheckbox.setFocusable(false);
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=5;
	gridConstraints.gridy=1;
	gridConstraints.insets=new Insets(10,10,0,0);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(markedcheckbox,gridConstraints);
	
	seriallabel=new JLabel("Serial Number");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=1;
	gridConstraints.gridy=2;
	gridConstraints.insets=new Insets(10,10,0,10);
	gridConstraints.anchor=GridBagConstraints.EAST;
	c.add(seriallabel,gridConstraints);

	serialtextfield=new JTextField();
	serialtextfield.setPreferredSize(new Dimension(270,25));
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=2;
	gridConstraints.gridy=2;
	gridConstraints.gridwidth=3;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(serialtextfield,gridConstraints);
	serialtextfield.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			pricetextfield.requestFocus();
	    }
	}	);
	
	pricelabel=new JLabel("Purchase Price");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=1;
	gridConstraints.gridy=3;
	gridConstraints.insets=new Insets(10,10,0,10);
	gridConstraints.anchor=GridBagConstraints.EAST;
	c.add(pricelabel,gridConstraints);
	
	pricetextfield=new JTextField();
	pricetextfield.setPreferredSize(new Dimension(160,25));
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=2;
	gridConstraints.gridy=3;
	gridConstraints.gridwidth=2;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(pricetextfield,gridConstraints);
	pricetextfield.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			datedatechooser.requestFocus();
		}	
    });
	
	datelabel=new JLabel("Date Purchased");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=4;
	gridConstraints.gridy=3;
	gridConstraints.insets=new Insets(10,10,0,0);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(datelabel,gridConstraints);
	
	datedatechooser=new JDateChooser();
	datedatechooser.setPreferredSize(new Dimension(120,25));
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=5;
	gridConstraints.gridy=3;
	gridConstraints.gridwidth=2;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(datedatechooser,gridConstraints);
	datedatechooser.addPropertyChangeListener(new PropertyChangeListener() 
	{
		public void propertyChange(PropertyChangeEvent evt) 
		{
			storetextfield.requestFocus();
		}
	});	

	storelabel=new JLabel("Store/Website");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=1;
	gridConstraints.gridy=4;
	gridConstraints.insets=new Insets(10,10,0,10);
	gridConstraints.anchor=GridBagConstraints.EAST;
	c.add(storelabel,gridConstraints);
	
	storetextfield=new JTextField();
	storetextfield.setPreferredSize(new Dimension(400,25));
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=2;
	gridConstraints.gridy=4;
	gridConstraints.gridwidth=5;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(storetextfield,gridConstraints);
	storetextfield.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			notetextfield.requestFocus();
		}	
    });
	
	notelabel=new JLabel("Note");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=1;
	gridConstraints.gridy=5;
	gridConstraints.insets=new Insets(10,10,0,10);
	gridConstraints.anchor=GridBagConstraints.EAST;
	c.add(notelabel,gridConstraints);
	
	notetextfield=new JTextField();
	notetextfield.setPreferredSize(new Dimension(400,25));
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=2;
	gridConstraints.gridy=5;
	gridConstraints.gridwidth=5;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(notetextfield,gridConstraints);
	notetextfield.addActionListener(new ActionListener() 
    {	
		public void actionPerformed(ActionEvent e) 
		{
			photobutton.requestFocus();
		}	
    });
	
	photolabel=new JLabel("Photo");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=1;
	gridConstraints.gridy=6;
	gridConstraints.insets=new Insets(10,10,0,10);
	gridConstraints.anchor=GridBagConstraints.EAST;
	c.add(photolabel,gridConstraints);
	
	phototextarea=new JTextArea();
	phototextarea.setPreferredSize(new Dimension(350,35));
	phototextarea.setFont(new Font("Arial",Font.PLAIN,12));
	phototextarea.setEditable(false);
	phototextarea.setLineWrap(true);
	phototextarea.setWrapStyleWord(true);
	phototextarea.setBackground(new Color(255,255,192));
	phototextarea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=2;
	gridConstraints.gridy=6;
	gridConstraints.gridwidth=4;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(phototextarea,gridConstraints);
	
	photobutton=new JButton("...");
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=6;
	gridConstraints.gridy=6;
	gridConstraints.insets=new Insets(10,0,0,10);
	gridConstraints.anchor=GridBagConstraints.WEST;
	c.add(photobutton,gridConstraints);
	photobutton.addActionListener(new ActionListener() 
	{
		public void actionPerformed(ActionEvent e) 
		{
			photobuttonactionperformed(e);
		}
	});
	
	searchpanel=new JPanel();
	searchpanel.setPreferredSize(new Dimension(240,160));
	searchpanel.setBorder(BorderFactory.createTitledBorder("Item Search"));
	searchpanel.setLayout(new GridBagLayout());
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=1;
	gridConstraints.gridy=7;
	gridConstraints.gridwidth=3;
	gridConstraints.insets=new Insets(10,0,10,0);
	gridConstraints.anchor=GridBagConstraints.CENTER;
	c.add(searchpanel,gridConstraints);
	
	int x=0,y=0;
	//Alphabet 26 Buttons
	searchbutton=new JButton[26];
	for(int i=0;i<26;i++)
	{
		//create new buttons
		searchbutton[i]=new JButton();
		//set text 
		searchbutton[i].setText(""+(char)(65+i));
		searchbutton[i].setFont(new Font("Arial",Font.BOLD,12));
		searchbutton[i].setMargin(new Insets(-10,-10,-10,-10));
		sizeButton(searchbutton[i],new Dimension(37,27));
		searchbutton[i].setBackground(Color.YELLOW);
		searchbutton[i].setFocusable(false);
		gridConstraints=new GridBagConstraints();
		gridConstraints.gridx=x;
		gridConstraints.gridy=y;
		searchpanel.add(searchbutton[i],gridConstraints);
		
		//add method
		searchbutton[i].addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				int i;
				if(numberentries==0)
					return;
				//search for item letter
				String letterclicked=e.getActionCommand();
				i=0;
				do
				{
					if(myInventory[i].description.substring(0,1).contentEquals(letterclicked))
					{
						currententry=i+1;
						showentry(currententry);
						return;
					}
					i++;
				}while(i<numberentries);
				JOptionPane.showConfirmDialog(null, "No "+letterclicked+" inventory items.", "None Found", JOptionPane.DEFAULT_OPTION  , JOptionPane.INFORMATION_MESSAGE);
				
			}
		 });
		
		x++;
		if(x%6==0) 
		{
			x=0;
			y++;
		}
	  }
	
	photoPanel=new PhotoPanel();
	photoPanel.setPreferredSize(new Dimension(240,160));
	gridConstraints=new GridBagConstraints();
	gridConstraints.gridx=4;
	gridConstraints.gridy=7;
	gridConstraints.gridwidth=3;
	gridConstraints.insets=new Insets(10,0,10,10);
	gridConstraints.anchor=GridBagConstraints.CENTER;
	c.add(photoPanel,gridConstraints);
	
	int n;
	//Data Entry in file
	try
	{	
		BufferedReader inputFile=new BufferedReader(new FileReader("inventory.txt"));
		numberentries=Integer.parseInt(inputFile.readLine());
		if(numberentries!=0)
		{
			for(int i=0;i<numberentries;i++)
			{
				myInventory[i]=new InventoryItem();
				myInventory[i].description=inputFile.readLine();
				myInventory[i].location=inputFile.readLine();
				myInventory[i].serialnumber=inputFile.readLine();
				myInventory[i].marked=Boolean.parseBoolean(inputFile.readLine());
				myInventory[i].purchaseprice=inputFile.readLine();
				myInventory[i].purchasedate=inputFile.readLine();
				myInventory[i].purchaselocation=inputFile.readLine();
				myInventory[i].note=inputFile.readLine();
				myInventory[i].photofile=inputFile.readLine();
			}
	   	 }
		
		//read in combo box elements
		n=Integer.parseInt(inputFile.readLine());
		if(n!=0)
		{
			for(int i=0;i<n;i++)
			{
				locationcombobox.addItem(inputFile.readLine());
			}
		}
		inputFile.close();
		currententry=1;
		showentry(currententry);
		
	}catch(Exception ex) 
	  {
		numberentries=0;
		currententry=0;
	  }
	if(numberentries==0)
	{
		newbutton.setEnabled(false);
		deletebutton.setEnabled(false);
		nextbutton.setEnabled(false);
		previousbutton.setEnabled(false);
		printbutton.setEnabled(false);
	}
}

	public void sizeButton(JButton b,Dimension d)
	{
		b.setPreferredSize(d);
		b.setMinimumSize(d);
		b.setMaximumSize(d);
	}
		
	private Date stringToDate(String s)
	{
		int m=Integer.valueOf(s.substring(0,2)).intValue()-1;
		int d=Integer.valueOf(s.substring(3,5)).intValue();
		int y=Integer.valueOf(s.substring(6)).intValue()-1900;
		return(new Date(y,m,d));	
	}
	
	private String dateToString(Date dd)
	{
		String yString =""+(dd.getYear()+1900); 
		int m = dd.getMonth() + 1;
		String mString = new DecimalFormat("00").format(m);
		int d = dd.getDate();
		String dString = new DecimalFormat("00").format(d);
		return(mString + "/" + dString + "/" + yString);
	}
	
	private void showPhoto(String photoFile)
	{
		if(!photoFile.equals(""))
		{
			try
			{
				phototextarea.setText(photoFile);
			}catch(Exception ex)
			{
				phototextarea.setText("");
			}
		}
		else
		{
			phototextarea.setText("");
		}
		photoPanel.repaint();
	}
	
	private void showentry(int j)
	{
		//display entry(1 to number entries)
		itemtextfield.setText(myInventory[j-1].description);
		locationcombobox.setSelectedItem(myInventory[j-1].location);
		markedcheckbox.setSelected(myInventory[j-1].marked);
		serialtextfield.setText(myInventory[j-1].serialnumber);
		pricetextfield.setText(myInventory[j-1].purchaseprice);
		datedatechooser.setDate(stringToDate(myInventory[j-1].purchasedate));
		storetextfield.setText(myInventory[j-1].purchaselocation);
		notetextfield.setText(myInventory[j-1].note);
		showPhoto(myInventory[j-1].photofile);
		nextbutton.setEnabled(true);
		previousbutton.setEnabled(true);
		if(j==1)
			previousbutton.setEnabled(false);
		if(j==numberentries)
			nextbutton.setEnabled(false);
		itemtextfield.requestFocus();	
	}
		
	private void photobuttonactionperformed(ActionEvent e)
	{
		JFileChooser openchooser=new JFileChooser();
		openchooser.setDialogType(JFileChooser.OPEN_DIALOG);
		openchooser.setDialogTitle("Open Photo File");
		openchooser.addChoosableFileFilter(new FileNameExtensionFilter("photo file", "jpg"));
		if(openchooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
			showPhoto(openchooser.getSelectedFile().toString());
	 }
	
	private void blankvalues()
	{
		//blank input screen
		newbutton.setEnabled(false);
		deletebutton.setEnabled(false);
		savebutton.setEnabled(true);
		previousbutton.setEnabled(false);
		nextbutton.setEnabled(false);
		printbutton.setEnabled(false);
		itemtextfield.setText("");
		locationcombobox.setSelectedItem("");
		markedcheckbox.setSelected(false);
		serialtextfield.setText("");
		pricetextfield.setText("");
		datedatechooser.setDate(new Date());
		storetextfield.setText("");
		notetextfield.setText("");
		phototextarea.setText("");
		photoPanel.repaint();
		itemtextfield.requestFocus();	
	}
	
	private void deleteentry(int j)
	{
		//delete entry 
		if(j!=numberentries)
		{
			//move all entry one level up
			for(int i=j;i<numberentries;i++)
			{
				myInventory[i-1]=new InventoryItem();
				myInventory[i-1]=myInventory[i];
			}
		}
		numberentries--;
	}
	
	public void checksave()
	{
		boolean edited=false;
		if(!myInventory[currententry-1].description.equals(itemtextfield.getText()))
			edited=true;
		else if(!myInventory[currententry-1].location.equals(locationcombobox.getSelectedItem().toString()))
			edited=true;
		else if(myInventory[currententry-1].marked!=markedcheckbox.isSelected())
			edited=true;
		else if(!myInventory[currententry-1].serialnumber.equals(serialtextfield.getText()))
			edited=true;
		else if(!myInventory[currententry-1].purchaseprice.equals(pricetextfield.getText()))
			edited=true;
		else if(!myInventory[currententry-1].purchasedate.equals(dateToString(datedatechooser.getDate())))
			edited=true;
		else if(!myInventory[currententry-1].purchaselocation.equals(storetextfield.getText()))
			edited=true;
		else if(!myInventory[currententry-1].note.equals(notetextfield.getText()))
			edited=true;
		else if(!myInventory[currententry-1].photofile.equals(phototextarea.getText()))
			edited=true;
		if(edited)
		{
			if(JOptionPane.showConfirmDialog(null, "You have edited this item. Do you want to save the changes?", "Save Item", JOptionPane.YES_NO_OPTION , JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
				savebutton.doClick();	
		}
	}	
}

class PhotoPanel extends JPanel
{
	public void paintComponent(Graphics g)
	{
		Graphics2D g2d=(Graphics2D)g;
		super.paintComponent(g2d);
		
		//draw border
		g2d.setPaint(Color.BLACK);
		g2d.draw(new Rectangle2D.Double(0,0,getWidth()-1, getHeight()-1));
		
		//show photo
		Image photoImage=new ImageIcon(HomeInventory.phototextarea.getText()).getImage();
		int w=getWidth();
		int h=getHeight();
		double rWidth=(double)getWidth()/(double)photoImage.getWidth(null);
		double rHeight=(double)getHeight()/(double)photoImage.getHeight(null);
		if(rWidth>rHeight)
		{
			//leave height at display height,change width by amount height is changed
			w=(int)(photoImage.getWidth(null)*rHeight);
		}
		else
		{
			//leave width at display width,change height by amount width is changed
			h=(int)(photoImage.getHeight(null)*rWidth);
		}
		
		//center in panel
		g2d.drawImage(photoImage,(int)(0.5*(getWidth()-w)),(int)(0.5*(getHeight()-h)),w,h,null);		
		g2d.dispose();
	}
}


class InventoryDocument implements Printable
{
	public int print(Graphics g,PageFormat pf,int pageIndex)
	{
		Graphics2D g2d=(Graphics2D)g;
		if((pageIndex+1)>HomeInventory.lastpage)
		{
			return NO_SUCH_PAGE;
		}
		int i,iEnd;
		//u decide what goes on each page and draw it.  
		//header
		g2d.setFont(new Font("Arial", Font.BOLD, 14));
		g2d.drawString("Home Inventory Items-Page"+String.valueOf(pageIndex+1), (int)pf.getImageableX(),(int)(pf.getImageableY()+25));
		//get Starting y
		int dy=(int)g2d.getFont().getStringBounds("S", g2d.getFontRenderContext()).getHeight();
		int y=(int)(pf.getImageableY()+4*dy);
		iEnd=HomeInventory.entriesperpage*(pageIndex+1);
		if(iEnd>HomeInventory.numberentries)
			iEnd=HomeInventory.numberentries;
		for(i=0+HomeInventory.entriesperpage*pageIndex;i<iEnd;i++)
		{
			//Dividing Line
			Line2D.Double dividingLine=new Line2D.Double(pf.getImageableX(),y,pf.getImageableX()+pf.getImageableWidth(),y);
			g2d.draw(dividingLine);
			y=y+dy;
			
			g2d.setFont(new Font("Arial",Font.BOLD,12));
			g2d.drawString(HomeInventory.myInventory[i].description, (int) pf.getImageableX(), y);
			y=y+dy;
			
			g2d.setFont(new Font("Arial", Font.PLAIN, 12));
			g2d.drawString("Location: " + HomeInventory.myInventory[i].location,(int)(pf.getImageableX()+25),y);
			y=y+dy;
			
			if (HomeInventory.myInventory[i].marked)
			g2d.drawString("Item is marked with identifying information.", (int)(pf.getImageableX()+25),y);
			else
			g2d.drawString("Item is Not marked with identifying information.",(int)(pf.getImageableX()+25),y);
			y=y+dy;
			g2d.drawString("Serial number: "+HomeInventory.myInventory[i].serialnumber,(int)(pf.getImageableX()+25),y);
			y=y+dy;
			g2d.drawString("Price: $" + HomeInventory.myInventory[i].purchaseprice + ",Purchased on: " + HomeInventory.myInventory[i].purchasedate,(int)(pf.getImageableX()+25),y);
			y=y+dy;
			g2d.drawString("Purchased at: " +HomeInventory.myInventory[i].purchaselocation,(int)(pf.getImageableX()+25),y);
			y=y+dy;
			g2d.drawString("Note: " + HomeInventory.myInventory[i].note,(int)(pf.getImageableX()+25),y);
			y=y+dy;
			
			try
			{
				//maintain original width/height ratio
				Image inventoryImage=new ImageIcon(HomeInventory.myInventory[i].photofile).getImage();
				double ratio=(double)(inventoryImage.getWidth(null))/(double)inventoryImage.getHeight(null);
				g2d.drawImage(inventoryImage, (int)(pf.getImageableX()+25),y,(int)(100*ratio),100,null);
			}catch(Exception ex)
			{
				//have place to go in case image file doesn't open 
				System.out.println(ex);
			}
			y=y+2*dy+100;
		  }
	return PAGE_EXISTS;		
   }
		
}
