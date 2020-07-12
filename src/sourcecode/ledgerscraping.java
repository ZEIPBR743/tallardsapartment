import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Date;
import java.sql.Timestamp;



class fileIncompatableException extends Exception
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public fileIncompatableException(String message)
	{
		super(message);
	}
}


public class ledgerscraping{

	
	public static void main(String[] args) throws IOException, fileIncompatableException{
	    MyFrame f = new MyFrame();
	    f.setVisible(true);
	    
		//args for testing
		long startTime = System.nanoTime();
		String argsTest[] = new String[] { 
			"listall", "NameHere"	
		};
		

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
		}
		if (argsTest[0] == "listall") {
			
			System.out.println("---------------------------------BEGIN REPORT-----------------------------------------");
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
					System.out.println(names.get(j) + "  has_paid_total_amount_of:  $" + total.get(j));
				}

			}
			System.out.println("");
			System.out.println("Total paid by all tenents:  $" + sum);
			System.out.println("Total charged by tallards, including refund (deducted):  $" + charged_total);
			System.out.println("Total refunds from tallards:  $" + refunded);
			System.out.println("Total unpaid amount:  $" + (charged_total - sum));
			System.out.println("");
			Date date = new Date();
			long time = date.getTime();
			Timestamp ts = new Timestamp(time);
			System.out.println("Time Stamp : " + ts);
			System.out.println("---------------------------------END REPORT-------------------------------------------");

		}
		if (argsTest[0] == "listname") {
			String namept = argsTest[1].toLowerCase().trim();
			int frequency = 0;
			Double counting = 0.0;
			for (int i = 0; i< paidByList.size(); i++) {
				if (paidByList.get(i).toLowerCase().contains(namept)) {
					frequency++;
					counting = counting + Double.parseDouble(paymentList.get(i));
					System.out.println(frequency + "," +paidByList.get(i) + "," + dateList.get(i) + "," +
							paymentList.get(i) + "," + descriptionList.get(i)+ "," + counting);
				}
			}
		}
	}
	
	

}
