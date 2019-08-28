import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class HtmlUtilTest {
	@Test
	public void documentWrite() throws Exception {
/*	    final WebClient webClient = new WebClient();

	    final HtmlPage page = webClient.getPage("http://wenshu.court.gov.cn/list/list/?sorttype=1&number=Z54GSZ74&guid=7117cba2-7b67-f703b39a-a2c3017ff877&conditions=searchWord+1+AJLX++%E6%A1%88%E4%BB%B6%E7%B1%BB%E5%9E%8B:%E5%88%91%E4%BA%8B%E6%A1%88%E4%BB%B6&conditions=searchWord+%E9%9D%9E%E6%B3%95%E5%8D%A0%E6%9C%89+++%E5%85%B3%E9%94%AE%E8%AF%8D:%E9%9D%9E%E6%B3%95%E5%8D%A0%E6%9C%89&conditions=searchWord+%E8%87%AA%E9%A6%96+++%E5%85%B3%E9%94%AE%E8%AF%8D:%E8%87%AA%E9%A6%96&conditions=searchWord+%E5%87%8F%E8%BD%BB%E5%A4%84%E7%BD%9A+++%E5%85%B3%E9%94%AE%E8%AF%8D:%E5%87%8F%E8%BD%BB%E5%A4%84%E7%BD%9A&conditions=searchWord+%E6%8B%98%E5%BD%B9+++%E5%85%B3%E9%94%AE%E8%AF%8D:%E6%8B%98%E5%BD%B9");
	    final DomElement el = page.getElementById("tree");
	    System.out.println(el.asXml());
*/
	    
	    /*final HtmlPage page = webClient.getPage("http://localhost:8380");
	    final HtmlForm form = page.getFormByName("form1");
	    
	    for (int i = 1; i <= 5; i++) {
	        final String expectedName = "textfield" + i;
	        System.out.println(form.<HtmlInput>getInputByName(expectedName).getAttribute("value"));
	        Assert.assertEquals(
	            "text", 
	            form.<HtmlInput>getInputByName(expectedName).getTypeAttribute());
	    }*/
		
		System.setProperty("webdriver.gecko.driver","E:/opensource/selenium/geckodriver-v0.20.1-win64/geckodriver.exe");
		RemoteWebDriver driver = new FirefoxDriver();
		WebDriver.Navigation navigate = driver.navigate();
        navigate.to("http://wenshu.court.gov.cn/List/List?sorttype=1&conditions=searchWord+1+AJLX++%E6%A1%88%E4%BB%B6%E7%B1%BB%E5%9E%8B:%E5%88%91%E4%BA%8B%E6%A1%88%E4%BB%B6");
        driver.manage().window().maximize();
        Thread.sleep(5000);
        System.out.println(driver.getPageSource());
        Document doc = Jsoup.parse(driver.getPageSource());
        System.out.println("--"+doc.getElementById("tree").html());
        
//        WebElement keywordInput = driver.findElement(By.id("tree"));
       
//        System.out.println(keywordInput.getText());
        //System.out.println(driver.getPageSource());
	}
}
