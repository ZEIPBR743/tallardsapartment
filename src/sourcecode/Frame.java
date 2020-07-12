import javax.swing.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


class MyFrame extends JFrame {

	private JButton btnTutup  = new JButton("EXIT");
	private JButton btnTambah = new JButton("Run Detailed Single Tenent");
	private JButton btnRunall = new JButton("Run All");

	private JTextField txtA = new JTextField();

	private JLabel lblA = new JLabel("Please enter a single tenent's name below (part or full) to run detailed by single tenent.");
	Date date = new Date();
	long time = date.getTime();
	private JLabel lblB = new JLabel("here");



	public MyFrame(){
		setTitle("Account Ledger Scrapper for Tallards");
		setSize(800,500);
		setLocation(new Point(300,200));
		setLayout(null);    
		setResizable(false);

		initComponent();    
		initEvent();    
	}

	private void initComponent(){
		btnTutup.setBounds(300,130, 130,25);
		btnTambah.setBounds(300,100, 230,25);
		btnRunall.setBounds(300,70, 130, 25);

		txtA.setBounds(100,33,100,20);

		lblA.setBounds(20,10,500,20);

		add(btnRunall);
		add(btnTutup);
		add(btnTambah);

		add(lblA);

		add(txtA);
	}

	private void initDoneTxt() {
		Date date = new Date();
		long time = date.getTime();
		Timestamp ts = new Timestamp(time);
		lblB.setBounds(50,50,500,20);
		add(lblB);
	}

	private void initEvent(){

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				System.exit(1);
			}
		});

		btnTutup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnTutupClick(e);
			}
		});

		btnTambah.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnTambahClick(e);
			}
		});

		btnRunall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnRunallClick(e);
			}
		});
	}

	private void btnRunallClick(ActionEvent evt) {
		calculator(0, "John");
	}

	private void btnTutupClick(ActionEvent evt){
		System.exit(0);
	}

	private void btnTambahClick(ActionEvent evt){
		String x;
		try{
			x = txtA.getText();
			initDoneTxt();
			calculator(1, x);

		}catch(Exception e){
			System.out.println(e);
			JOptionPane.showMessageDialog(null, 
					e.toString(),
					"Error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void calculator(int p, String arg) {
		//args for testing
		String argsTest[] = new String[] { 
				"listall", "NameHere"	
		};
		if (p == 1) {
			argsTest[0] = "listname";
			argsTest[1] = arg;
		}
		else if (p == 0) {
			argsTest[0] = "listall";
		}
		else {
			System.out.println("Error, wrong p vlue");
			//	throw Exception e;
		}


		//vars used for verification
		int date_cnt = 0;
		int description_cnt = 0;
		int paid_cnt = 0;
		int charge_cnt = 0;
		int payment_cnt = 0;
		int balance_cnt = 0;
		Double charged_total = 0.0;
		Double refunded = 0.0;

		List<String> dateList = new ArrayList<String>();
		List<String> descriptionList = new ArrayList<String>();
		List<String> paidByList = new ArrayList<String>();
		List<String> chargeList = new ArrayList<String>();
		List<String> paymentList = new ArrayList<String>();
		List<String> balanceList = new ArrayList<String>();

		try {

			String fileName = "./src/ledger.html";
			File input = new File(fileName);
			Document doc = Jsoup.parse(input, "UTF-8");
			Elements dates = doc.getElementsByClass("js-date");

			for (Element date : dates) {
				dateList.add(date.text());
				date_cnt ++;
			}
			Elements descriptions = doc.getElementsByClass("js-description");
			for (Element description : descriptions) {
				descriptionList.add(description.text());
				description_cnt ++;
			}
			Elements paidBys = doc.getElementsByClass("js-paid-by");
			for (Element paidBy : paidBys) {
				paidByList.add(paidBy.text());
				paid_cnt++;
			}
			Elements charges = doc.getElementsByClass("js-charge");
			for (Element charge : charges) {
				chargeList.add(charge.text());
				charge_cnt++;
			}
			Elements payments = doc.getElementsByClass("js-payment");
			for (Element payment : payments) {
				paymentList.add(payment.text());
				payment_cnt++;
			}
			Elements balances = doc.getElementsByClass("js-balance");
			for (Element balance : balances) {
				balanceList.add(balance.text());
				balance_cnt++;
			}
			if ((date_cnt != description_cnt) || 
					(date_cnt != paid_cnt )||
					(date_cnt!= charge_cnt)||
					(date_cnt!= payment_cnt)||
					(date_cnt!= balance_cnt)) {
				throw new fileIncompatableException("columnrowsmismatch");

			}
			//data cleansing, make all blanks to zero, eliminate all commas
			//eliminate stars
			for (int i = 0; i< payment_cnt; i++) {
				chargeList.set(i, chargeList.get(i).replace(",", ""));
				paymentList.set(i,paymentList.get(i).replace(",", ""));
				paidByList.set(i,paidByList.get(i)).replace("*","");
				if (paymentList.get(i).isBlank()) {
					paymentList.set(i, "0");
				}
				if (chargeList.get(i).isBlank()) {
					chargeList.set(i, "0");
				}
				Double tempvarcharge = Double.parseDouble(chargeList.get(i));
				charged_total = charged_total + tempvarcharge;
				if (tempvarcharge < 0) {
					refunded = refunded + tempvarcharge;
				}
			}
		}

		catch (FileNotFoundException e0){
			System.out.println("the file is not found, please create the file \n"
					+ "ledger.html at the same folder level (not a nested folder)");
		}
		catch (fileIncompatableException e1) {
			System.out.println("the html file has corrupted format: \n"
					+ "column and rows mismatch");			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			System.out.println("FileIOException");
			e2.printStackTrace();
		}
		if (argsTest[0] == "listall") {
			PrintWriter writer;
			try {
				Date date = new Date();
				long time = date.getTime();
				Timestamp ts = new Timestamp(time);
				writer = new PrintWriter("All_at_" + ts +".txt", "UTF-8");
				writer.println("---------------------------------BEGIN REPORT-----------------------------------------");
				List<String> names = new ArrayList<String>();
				List<Double> total = new ArrayList<Double>();
				for (int i = 0; i< paid_cnt; i++) {
					if (names.contains(paidByList.get(i))){
						int position = names.indexOf(paidByList.get(i));
						Double newAmount = Double.parseDouble(paymentList.get(i)) +
								total.get(position);
						total.set(position, newAmount);
					}
					else{
						String newname = paidByList.get(i);
						names.add(newname);
						total.add(names.indexOf(newname), Double.parseDouble(paymentList.get(i)));
					}
				}
				if (names.size() != total.size()) {
					System.out.println("error_1");
				}
				Double sum = 0.0;
				for (int j = 0; j < names.size(); j++) {
					if (!names.get(j).isBlank()){
						sum = sum + total.get(j);
						writer.println(names.get(j) + "  has_paid_total_amount_of:  $" + total.get(j));
					}

				}
				writer.println("");
				writer.println("Total paid by all tenents:  $" + sum);
				writer.println("Total charged by tallards, including refund (deducted):  $" + charged_total);
				writer.println("Total refunds from tallards:  $" + refunded);
				writer.println("Total unpaid amount:  $" + (charged_total - sum));
				writer.println("");
				writer.println("Time Stamp : " + ts);
				writer.println("---------------------------------END REPORT-------------------------------------------");

				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (argsTest[0] == "listname") {
			String namept = arg.toLowerCase().trim();
			int frequency = 0;
			Double counting = 0.0;
			Date date = new Date();
			long time = date.getTime();
			Timestamp ts = new Timestamp(time);
			PrintWriter writer;
			try {
				writer = new PrintWriter(arg + ts +".csv", "UTF-8");
				writer.println("index,name,paid date,paid amount,description,total paid amount");
				for (int i = 0; i< paidByList.size(); i++) {
					if (paidByList.get(i).toLowerCase().contains(namept)) {
						frequency++;
						counting = counting + Double.parseDouble(paymentList.get(i));
						writer.println(frequency + "," +paidByList.get(i) + "," + dateList.get(i) + "," +
								paymentList.get(i) + "," + descriptionList.get(i)+ "," + counting);
					}
				}
				writer.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};

		}
	}
}

