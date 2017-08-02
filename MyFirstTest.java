import org.testng.annotations.Test;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarQueryParam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
//import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarQueryParam;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.proxy.CaptureType;

public class MyFirstTest {
	
	//Path to store har file
	String fileName = "/home/ram/Har/mydata.har";
	
	public WebDriver driver;
	public BrowserMobProxy proxy;
	
	
	@BeforeTest
	public void setUp() {
		
	   // start the proxy
	    proxy = new BrowserMobProxyServer();
	    proxy.start(0);

	    //get the Selenium proxy object - org.openqa.selenium.Proxy;
	    Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

	    // configure it as a desired capability
	    DesiredCapabilities capabilities = new DesiredCapabilities();
	    capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
		
	    //set chromedriver system property
		System.setProperty("webdriver.chrome.driver", "/home/ram/eclipse/chromedriver");
		driver = new ChromeDriver(capabilities);
		
	    // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
	    proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
	    
	    

	    // create a new HAR with the label
	    proxy.newHar("dataliciousTest");

	    // open datalicious.com
	    driver.manage().window().maximize();
		driver.get("http://google.com/");
		driver.findElement(By.className("gsfi")).sendKeys("datalicious");
		driver.findElement(By.xpath("//input[@value='Google Search']")).click();
		WebDriverWait wait = new WebDriverWait(driver,20);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='rso']/div/div/div[1]/div/div/h3/a")));
		WebElement element=driver.findElement(By.xpath("//*[@id='rso']/div/div/div[1]/div/div/h3/a"));

		String link=element.getAttribute("href");
		driver.navigate().to(link);
	    
		
	}
	
	@Test
	public void testCaseOne() 
	{
		System.out.println("Navigated to datalicious.com");
		
	}
	
	@AfterTest
	public void tearDown() {

		// get the HAR data
		Har har = proxy.getHar();

		//Create a new file for har file
		File harFile = new File(fileName);
		try {
			//Write HAR Data in a File
			har.writeTo(harFile);
			
		} catch (IOException ex) {
			 System.out.println (ex.toString());
		     System.out.println("Could not find file " + fileName);
		}
		
		if (driver != null) {
			proxy.stop();
			driver.quit();
		}
	}
	
	@AfterTest
	public void task2(){
		//To create instance of harReader
	HarReader harReader = new HarReader();
	
	//create a new file to log the output and give the file name with respect to local folder location
	File file=new File("/home/ram/Har/log.csv");
	
	FileWriter fw=null;
	BufferedWriter bw=null;
	//separator used to separate the lines
	String seperator=System.lineSeparator();
	try {
		//To read from the har file 
		//Give the file location of the har file from local system
		de.sstoehr.harreader.model.Har har = harReader.readFromFile(new File("/home/ram/Har/mydata.har"));
		
		
		fw=new FileWriter(file);
		bw=new BufferedWriter(fw);
		//Provide headers to the log csv file
		bw.write("name,value");
		bw.write(seperator);
		//To list all har entries
		List<HarEntry> list=new ArrayList<HarEntry>();
		list=har.getLog().getEntries();
		for (HarEntry har2 : list) {
			//To get all http requests 
			System.out.println(har2.getRequest().getUrl());
			
			
			
			//To check "dc.optimahub.com" reachability
			if(har2.getRequest().getUrl().contains("dc.optimahub.com"))
				/*Not available to get request for "dc.optimahub.com" as 
				"dc.optimahub.com" request is not populated in the captured har while its getting populated in har file populated through F12 devloper tool*/
			{
				
				System.out.println("Is dc.optimahub.com reachable :"+har2.getRequest().getUrl());
			}
			//To check google.analytics reachability
			if(har2.getRequest().getUrl().contains("https://www.google-analytics.com/r/collect?")){
			System.out.println("Is google.analytics reachable ::"+har2.getRequest().getUrl());
			Iterator<HarQueryParam> iterator=har2.getRequest().getQueryString().iterator();
			String match;
			HarQueryParam value;
			
			while(iterator.hasNext()){
				value=iterator.next();
				match=value.getName();
				if(match.equals("dt")||match.equals("dl"))
				{
				
				bw.write(value.getName()+","+value.getValue()+"\n");
				
				//System.out.println("------------------");
				//System.out.println(value.getName());
			//	System.out.println(value.getValue());
			}
				
		}
		}
		}
		
		
		
	} catch (HarReaderException e) {
		
		e.printStackTrace();
	} catch (IOException e) {
	
		e.printStackTrace();
	}
	finally {

		try {

			if (bw != null)
				bw.close();

			if (fw != null)
				fw.close();

		} catch (IOException ex) {

			ex.printStackTrace();

		}

	}
	
	
	
}
}

