/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebSpoonTest {
  private WebDriver driver;
  private Actions actions;
  private String baseUrl;
  private WebElement element;
  private WebDriverWait wait;

  @Before
  public void setUp() throws Exception {
    driver = new ChromeDriver();
    actions = new Actions( driver );
    wait = new WebDriverWait( driver, 10 );
    baseUrl = System.getProperty( "test.baseurl", "http://localhost:8080/spoon" );
    driver.get( baseUrl );
    driver.manage().timeouts().implicitlyWait( 10, TimeUnit.SECONDS );
    driver.manage().window().setSize( new Dimension( 1280, 800 ) );

    // Login with username and password
    if ( driver.findElements( By.xpath( "//input[@name = 'username']" ) ).size() != 0 ) {
      driver.findElement( By.xpath( "//input[@name = 'username']" ) ).sendKeys( "user" );
      driver.findElement( By.xpath( "//input[@name = 'password']" ) ).sendKeys( "password" );
      driver.findElement( By.xpath( "//input[@name = 'submit']" ) ).click();
    }
  }

  @Test
  public void testAppLoading() throws Exception {
    Assert.assertEquals( driver.getTitle(), "Spoon" );
  }

  @Test
  public void testNewTransformation() throws Exception {
    // Create a new transformation
    driver.findElement( By.xpath( "//div[text() = 'File']" ) ).click();
    driver.findElement( By.xpath( "//div[text() = 'New']" ) ).click();
    driver.findElement( By.xpath( "//div[text() = 'Transformation']" ) ).click();

    // Drag & drop a step
    driver.findElement( By.xpath( "//div[text() = 'Input']" ) ).click();
    element = driver.findElement( By.xpath( "//div[text() = 'Generate Rows']" ) );
    actions.clickAndHold( element ).moveByOffset( 300, 0 ).release().build().perform();

    // Open a step dialog
    driver.findElement( By.xpath( "//div[@test-id = 'tree_exploreSolution']" ) ).click();
    driver.findElement( By.xpath( "//div[@test-id = 'tree_expandAll']" ) ).click();
    element = driver.findElement( By.xpath( "//div[@test-id = 'tree_Steps']/../..//div[text() = 'Generate Rows']" ) );
    actions.click( element ).click( element ).build().perform();

    Assert.assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Never stop generating rows']" ) ).size() );
  }

  @Test
  public void testDatabaseConnectionDialog() throws Exception {
    // Create a new transformation
    driver.findElement( By.xpath( "//div[text() = 'File']" ) ).click();
    driver.findElement( By.xpath( "//div[text() = 'New']" ) ).click();
    driver.findElement( By.xpath( "//div[text() = 'Transformation']" ) ).click();

    // Filter a step
    driver.findElement( By.xpath( "//input[@test-id = 'selectionFilter']" ) ).sendKeys( "table" );

    // Draw a step
    element = driver.findElement( By.xpath( "//div[text() = 'Table input']" ) );
    actions.click( element ).click( element ).build().perform();

    // Open a step dialog
    driver.findElement( By.xpath( "//div[@test-id = 'tree_exploreSolution']" ) ).click();
    driver.findElement( By.xpath( "//div[@test-id = 'tree_expandAll']" ) ).click();
    element = driver.findElement( By.xpath( "//div[@test-id = 'tree_Steps']/../..//div[text() = 'Table input']" ) );
    actions.click( element ).click( element ).build().perform();

    /* TODO
     * Cancel button does not become clickable unless thread.sleep and window.setSize.
     * The wait duration might depend on an environment.
     */
    wait.until( ExpectedConditions.elementToBeClickable( By.xpath( "//div[text() = 'New...']" ) ) ).click();
    Thread.sleep( 1000 );
    driver.manage().window().setSize( new Dimension( 1280, 799 ) );
    wait.until( ExpectedConditions.elementToBeClickable( By.xpath( "//div[text() = 'Cancel']" ) ) ).click();
    Thread.sleep( 1000 );
    driver.manage().window().setSize( new Dimension( 1280, 800 ) );
    wait.until( ExpectedConditions.elementToBeClickable( By.xpath( "//div[text() = 'Edit...']" ) ) ).click();
    Thread.sleep( 1000 );
    driver.manage().window().setSize( new Dimension( 1280, 799 ) );
    wait.until( ExpectedConditions.elementToBeClickable( By.xpath( "//div[text() = 'Cancel']" ) ) ).click();
    Thread.sleep( 1000 );
    Assert.assertEquals( "5", driver.switchTo().activeElement().getAttribute( "tabindex" ) );
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
  }
}
