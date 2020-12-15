package edu.uclm.esi.videochat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uclm.esi.videochat.model.User;
import edu.uclm.esi.videochat.springdao.UserRepository;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.net.MalformedURLException;
import java.net.URL;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest
public class LoginTest {
	private WebDriver chrome, firefox;
	private Map<String, Object> vars;
	JavascriptExecutor js;

	@Autowired
	UserRepository usersRepo;
	
/*	@Before
	public void setUp() {
		System.setProperty("webdriver.chrome.driver", "/Users/macariopolousaola/Downloads/chromedriver");
		System.setProperty("webdriver.gecko.driver", "/Users/macariopolousaola/Downloads/geckodriver");
		
		chrome = new ChromeDriver();
		firefox = new FirefoxDriver();
		
		chrome.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		
		js = (JavascriptExecutor) chrome;
		vars = new HashMap<String, Object>();
	}

	@After
	public void tearDown() {
		chrome.quit();
		firefox.quit();
	}
	
	@Test
	public void conversacion() {
		hacerLogin(chrome, "pepe", "pepe");
		hacerLogin(firefox, "ana", "ana");
		
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		
		chrome.findElement(By.linkText("ana")).click();
		String mensaje = "Hola, Ana";
		chrome.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div/div[1]/div[2]/div/input")).sendKeys(mensaje);
		chrome.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div/div[1]/div[2]/div/button[1]/span")).click();
		
		try { Thread.sleep(1000); } catch (InterruptedException e) { }
		
		String body = firefox.findElement(By.tagName("body")).getText();
		assertTrue(body.contains(mensaje));
	}

	private void hacerLogin(WebDriver driver, String nombre, String pwd) {
		driver.get("https://localhost:7500/");
		
		if (driver instanceof ChromeDriver) {
			driver.findElement(By.id("details-button")).click();
			driver.findElement(By.id("proceed-link")).click();
		} 
		
		driver.manage().window().setSize(new Dimension(1161, 977));
		driver.findElement(By.cssSelector(".oj-sm-12:nth-child(1) > input")).click();
		
		WebElement cajaNombre = driver.findElement(By.cssSelector(".oj-sm-12:nth-child(1) > input"));
		cajaNombre.clear();
		cajaNombre.sendKeys(nombre);
		
		WebElement cajaPwd = driver.findElement(By.cssSelector(".oj-sm-12:nth-child(2) > input"));
		cajaPwd.clear();
		cajaPwd.sendKeys(pwd);
		
		driver.findElement(By.cssSelector("button")).click();
	}

	@Test
	public void login() {
		chrome.get("https://localhost:7500/");
		
		chrome.findElement(By.id("details-button")).click();
		chrome.findElement(By.id("proceed-link")).click();
		
		chrome.manage().window().setSize(new Dimension(1161, 977));
		chrome.findElement(By.cssSelector(".oj-sm-12:nth-child(1) > input")).click();
		
		WebElement cajaNombre = chrome.findElement(By.cssSelector(".oj-sm-12:nth-child(1) > input"));
		cajaNombre.clear();
		cajaNombre.sendKeys("pepe");
		
		WebElement cajaPwd = chrome.findElement(By.cssSelector(".oj-sm-12:nth-child(2) > input"));
		cajaPwd.clear();
		cajaPwd.sendKeys("pepe");
		
		chrome.findElement(By.cssSelector("button")).click();
		try { Thread.sleep(500); } catch (InterruptedException e) { }
		
		assertThat(chrome.findElement(By.cssSelector(".oj-hybrid-padding > h1")).getText(), is("Zona de conversación"));
	}
	
	@Test
	public void registro() {
		Optional<User> optUser = usersRepo.findByName("lucia");
		if (optUser.isPresent()) {
			User user = optUser.get();
			usersRepo.deleteById(user.getId());
		}
		
		chrome.get("https://localhost:7500/");
		
		chrome.findElement(By.id("details-button")).click();
		chrome.findElement(By.id("proceed-link")).click();
		
		WebElement link = chrome.findElement(By.linkText("Crear cuenta"));
		link.click();
		
		WebElement cajaNombre = chrome.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div/input[1]"));
		WebElement cajaEmail = chrome.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div/input[2]"));
		WebElement cajaPwd1 = chrome.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div/input[3]"));
		WebElement cajaPwd2 = chrome.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div/input[4]"));
		WebElement boton = chrome.findElement(By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div/button"));
		
		cajaNombre.sendKeys("lucía");
		cajaEmail.sendKeys("lucia@lucia.com");
		cajaPwd1.sendKeys("lucia123");
		cajaPwd2.sendKeys("lucia123");
		boton.click();
		
		try { Thread.sleep(500); } catch (Exception e) { } 
		
		assertThat(chrome.switchTo().alert().getText(), is("Registrado correctamente"));
	}*/
}
