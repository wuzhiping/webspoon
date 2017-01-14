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

public class WebSpoonTest {
  private WebDriver driver;
  private Actions action;
  private String baseUrl;
  private WebElement element;

  @Before
  public void setUp() throws Exception {
    driver = new ChromeDriver();
    action = new Actions( driver );
    baseUrl = System.getProperty( "test.baseurl", "http://localhost:8080/spoon/" );
    driver.get( baseUrl );
    driver.manage().timeouts().implicitlyWait( 10, TimeUnit.SECONDS );
    driver.manage().window().setSize( new Dimension( 1280, 800 ) );
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
    action.clickAndHold( element ).moveByOffset( 300, 0 ).release().build().perform();

    // Open a step dialog
    driver.findElement( By.xpath( "//div[@test-id = 'tree_exploreSolution']" ) ).click();
    driver.findElement( By.xpath( "//div[@test-id = 'tree_expandAll']" ) ).click();
    element = driver.findElement( By.xpath( "//div[@test-id = 'tree_Steps']/../..//div[text() = 'Generate Rows']" ) );
    action.click( element ).click( element ).build().perform();

    Assert.assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Never stop generating rows']" ) ).size() );
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
  }
}
