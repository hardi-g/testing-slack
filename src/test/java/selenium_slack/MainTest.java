package selenium_slack;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Test;
import org.testng.Assert;
import java.time.Duration;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
public class MainTest {
	private Properties properties;
	
	public MainTest() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("/home/hardik/eclipse-workspace/selenium_slack/src/test/java/input.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Failed to load properties file.");
        }
    }
	
    @Test
    public void signInTest() {
        WebDriverManager.firefoxdriver().setup();
        WebDriver driver = new FirefoxDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
        	String home = properties.getProperty("home");
            driver.get(home);
            String homePageTitle = driver.getTitle();
            Assert.assertEquals(homePageTitle, "Slack is your productivity platform | Slack");
            System.out.println("Title of the homepage is ==> " + homePageTitle);

            WebElement signInLink = driver.findElement(By.linkText("Sign in"));
            String signInPageUrl = signInLink.getAttribute("href");
            js.executeScript("window.open(arguments[0], '_blank');", signInPageUrl);
            ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(tabs.get(1));
            System.out.println("Sign-in page opened in a new tab successfully!");
            
            String signin = properties.getProperty("signin");
            driver.get(signin);
            WebElement loginEmailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("signup_email")));

            Thread.sleep(3000);
            
            String invalidEmail = properties.getProperty("invalidEmail");
            loginEmailInput.sendKeys(invalidEmail);
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit_btn")));
            loginButton.click();

            Thread.sleep(5000);


            verifyEmailFormatUsingWebsite(driver, wait);
            System.out.println("Please enter the verification code manually on the website and complete the sign-in process.");

            boolean loggedIn = false;
            for (int i = 0; i < 60; i++) { 
                try {
                    WebElement userButton = driver.findElement(By.xpath("//button[@aria-label='User: dummydir123']"));
                    if (userButton.isDisplayed()) {
                        loggedIn = true;
                        System.out.println("Login successful.");
                        break;
                    }
                } catch (Exception e) {
                    
                }
                Thread.sleep(1000); 
            }
            if (!loggedIn) {
                System.out.println("Login failed.");
                Assert.fail("Login failed.");
            }
            navigateToChannelAndSendMessage(driver, wait);
            Thread.sleep(5000);

            addAndCreateChannel(driver, wait);

            navigateAndAddColleagues(driver, wait);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("An error occurred: " + e.getMessage());
        } finally {
            System.out.println("log out sucessfull");
            driver.quit();
        }
    }

    private void verifyEmailFormatUsingWebsite(WebDriver driver, WebDriverWait wait) {
        try {
            System.out.println("Verifying email format ...");
            boolean isEmailErrorPresent = driver.findElements(By.xpath("//*[@id='signup_email_error']")).size() > 0;
            if (isEmailErrorPresent) {
                WebElement emailError = driver.findElement(By.xpath("//*[@id='signup_email_error']"));
                if (emailError.isDisplayed()) {
                    System.out.println("Invalid email format: " + emailError.getText());
                    WebElement emailField = driver.findElement(By.id("signup_email"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].focus(); arguments[0].value = '';", emailField);
                    Thread.sleep(500);
                    String currentValue = emailField.getAttribute("value");
                    if (currentValue.isEmpty()) {
                        String validEmail = properties.getProperty("validEmail");
                        emailField.sendKeys(validEmail);
                        System.out.println("Entered valid email: " + validEmail);
                        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit_btn")));
                        loginButton.click();
                    } else {
                        System.out.println("Failed to clear the email field.");
                    }
                } else {
                    System.out.println("Email error element is not displayed.");
                }
            } else {
                System.out.println("Email error element is not present.");
            }
        } catch (Exception e) {
            System.out.println("Failed to verify the email format.");
        }
    }

    private void navigateToChannelAndSendMessage(WebDriver driver, WebDriverWait wait) {
        try {
        	String channelXPath = properties.getProperty("channelXPath");
        	String messageInputXPath = properties.getProperty("messageInputXPath");
        	String sendButtonXPath = properties.getProperty("sendButtonXPath");
        	String message = properties.getProperty("message");
            Thread.sleep(2000);
            WebElement channel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(channelXPath)));
            channel.click();
            System.out.println("Navigated to the testing channel.");
            Thread.sleep(2000);
            WebElement messageInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(messageInputXPath)));
            messageInput.click();
            ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", messageInput);
            System.out.println("Message input focused.");
            messageInput.sendKeys(message);
            System.out.println("Message typed: " + message);
            Thread.sleep(2000);
            WebElement sendButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(sendButtonXPath)));
            sendButton.click();
            System.out.println("Message sent.");
        } catch (Exception e) {
            System.out.println("Failed to navigate to the channel, type the message, or click the send button.");
            driver.quit();
            System.exit(1); 
        }
    }
    private void addAndCreateChannel(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement addChannelsButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[data-qa='link_label']")));
            addChannelsButton.click();
            System.out.println("Clicked to add more channels.");
            WebElement createChannelMenuItem = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-qa='add_more_items_link_channel_menu_item']")));
            createChannelMenuItem.click();
            System.out.println("In the Process of creating a new channel.");
            WebElement channelNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[data-qa='channel-name-input']")));
            channelNameInput.clear();
            String invalidChannel = properties.getProperty("invalidChannel");
            channelNameInput.sendKeys(invalidChannel);
            System.out.println("Typed the channel name: " + invalidChannel);

            Thread.sleep(5000);
    
            boolean isWarningDisplayed = driver.findElements(By.id("hasNameTakenWarning")).size() > 0;
            if (isWarningDisplayed) {
                WebElement nameTakenWarning = driver.findElement(By.id("hasNameTakenWarning"));
                if (nameTakenWarning.isDisplayed()) {
                    System.out.println("Channel already present.");
                    channelNameInput.clear();
                    String validChannel = properties.getProperty("validChannel");
                    channelNameInput.sendKeys(validChannel);
                    Thread.sleep(2000);
                    System.out.println("Typed the new channel name: " + validChannel);
                }
            }
            WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-qa='create-channel-next-button']")));
            nextButton.click();
            System.out.println("Next button Clicked.");
    

            Thread.sleep(3000);

            WebElement createButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-qa='create-channel-next-button']")));
            createButton.click();
            System.out.println("created button clicked");

            Thread.sleep(2000);

            WebElement doneButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-qa='invite_to_workspace_submit_button']")));
            doneButton.click();
            System.out.println("New channel created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to add or create the channel.");
        }
    }  

    private void navigateAndAddColleagues(WebDriver driver, WebDriverWait wait) {
        try {
            int retryCount = 0;
            int maxRetries = 5;
            boolean clicked = false;
            By overlayLocator = By.className("ReactModal__Overlay--after-open");
    
            if (driver.findElements(overlayLocator).size() > 0) {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(overlayLocator));
                System.out.println("Overlay detected and waiting for it to disappear...");
            }
    
            // Click 'Add colleagues' button with retries
            while (!clicked && retryCount < maxRetries) {
                try {
                    WebElement addColleaguesElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='addMoreDM']/div/div")));
                    addColleaguesElement.click();
                    System.out.println("Clicked on 'Add colleagues'.");
                    clicked = true;
                } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                    retryCount++;
                    System.out.println("Retrying... (" + retryCount + ")");
                    Thread.sleep(1000);
                }
            }
    
            if (!clicked) {
                throw new RuntimeException("Failed to click on 'Add colleagues' after " + maxRetries + " retries.");
            }
    
            WebElement comboboxInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div#invite_modal_select.c-multi_select_input__input")));
            comboboxInput.click();
            System.out.println("In the process of adding colleagues.");
            
            String invalidName = properties.getProperty("invalidName");
            comboboxInput.sendKeys(invalidName);
            comboboxInput.sendKeys(Keys.ENTER);
            System.out.println("Entered false email id: 'name123'.");
    
            comboboxInput.click();
            Thread.sleep(2000);
    
            By removeErrorsButtonLocator = By.xpath("//button[contains(text(),'Remove all items with errors')]");
            if (driver.findElements(removeErrorsButtonLocator).size() > 0) {
                WebElement removeErrorsButton = wait.until(ExpectedConditions.elementToBeClickable(removeErrorsButtonLocator));
                removeErrorsButton.click();
                System.out.println("'Remove all items with errors' button clicked.");
            }
    
            // Wait for UI to be ready after removing errors
            Thread.sleep(2000);
    
            retryCount = 0;
            clicked = false;
    
            // Enter proper email id with retries
            while (!clicked && retryCount < maxRetries) {
                try {
                    comboboxInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div#invite_modal_select.c-multi_select_input__input")));
                    String validName = properties.getProperty("validName");
                    comboboxInput.sendKeys(validName);
                    comboboxInput.sendKeys(Keys.ENTER);
                    System.out.println("Entered proper email id: 'name1@gmail.com'.");
                    comboboxInput.click();
                    Thread.sleep(2000);
                    clicked = true;
                } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                    retryCount++;
                    System.out.println("Retrying... (" + retryCount + ")");
                    Thread.sleep(1000);
                }
            }
    
            if (!clicked) {
                throw new RuntimeException("Failed to enter proper email id after " + maxRetries + " retries.");
            }
    
            retryCount = 0;
            clicked = false;
    
            // Click on the combobox input after entering email
            while (!clicked && retryCount < maxRetries) {
                try {
                    comboboxInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div#invite_modal_select.c-multi_select_input__input")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", comboboxInput);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", comboboxInput);
                    System.out.println("Clicked on the combobox input after entering email.");
                    clicked = true;
                } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                    retryCount++;
                    System.out.println("Retrying... (" + retryCount + ")");
                    Thread.sleep(1000);
                }
            }
    
            if (!clicked) {
                throw new RuntimeException("Failed to click on the combobox input after entering email after " + maxRetries + " retries.");
            }
    
            // Wait until the 'Send' button is enabled
            WebElement sendButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button.c-button.c-button--primary.c-button--medium[type='button']")));
    
            // Wait until 'Send' button does not have 'c-button--disabled' class
            wait.until(d -> !sendButton.getAttribute("class").contains("c-button--disabled"));
    
            retryCount = 0;
            clicked = false;
    
            // Click 'Send' button with retries
            while (!clicked && retryCount < maxRetries) {
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sendButton);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sendButton);
                    System.out.println("Clicked on 'Send' button");
                    clicked = true;
                } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                    retryCount++;
                    System.out.println("Retrying... (" + retryCount + ")");
                    Thread.sleep(1000);
                }
            }
    
            if (!clicked) {
                throw new RuntimeException("Failed to click on 'Send' button after " + maxRetries + " retries.");
            }
    
            Thread.sleep(3000);
    
            WebElement thirdElement = driver.findElement(By.xpath("/html/body/div[11]/div/div/div[3]/div[2]/button[2]"));
            thirdElement.click();
            System.out.println("Invitation sent");
    
            // Logout logic
            Thread.sleep(6000);
            WebElement userButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-qa='user-button']")));
            userButton.click();
            System.out.println("Clicked on user button");
            Thread.sleep(3000);
            WebElement signOutButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@data-qa='menu_item_button' and contains(div, 'Sign out of softwaretesting')]")));
            signOutButton.click();
            System.out.println("Clicked on 'Sign out of softwaretesting' button");
            
        } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
            System.out.println("Element click intercepted during the 'Add colleagues' process.");
        } catch (Exception e) {
            System.out.println("Failed to complete the 'Add colleagues' process. Error: ");
        }
    }      
}
