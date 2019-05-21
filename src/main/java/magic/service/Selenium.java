package magic.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import io.github.bonigarcia.wdm.WebDriverManager;

public abstract class Selenium implements IService {
	protected final Logger log = LoggerFactory.getLogger( getClass() );

	private static final String DATA_URI = "data:image/png;base64,%s";

	@Value( "${GOOGLE_CHROME_SHIM:}" )
	private String bin;

	protected final void exec( String... arguments ) {
		WebDriver driver = chrome( arguments );

		try {
			exec( driver );

		} finally {
			driver.quit();

		}
	}

	protected final BufferedImage screenshot( WebDriver driver, WebElement element ) {
		File screenshot = ( ( TakesScreenshot ) driver ).getScreenshotAs( OutputType.FILE );

		Point point = element.getLocation();

		Dimension size = element.getSize();

		try {
			return ImageIO.read( screenshot ).getSubimage( point.getX(), point.getY(), size.getWidth(), size.getHeight() );

		} catch ( IOException e ) {
			throw new RuntimeException( e );

		}
	}

	protected final String base64( BufferedImage image ) {
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			ImageIO.write( image, "png", stream );

			return String.format( DATA_URI, DatatypeConverter.printBase64Binary( stream.toByteArray() ) );

		} catch ( IOException e ) {
			throw new RuntimeException( e );

		}
	}

	protected final void sleep() {
		sleep( 5000 );
	}

	protected final void sleep( long millis ) {
		try {
			Thread.sleep( millis );

		} catch ( InterruptedException e ) {
			throw new RuntimeException();

		}
	}

	protected abstract void exec( WebDriver driver );

	private WebDriver chrome( String... arguments ) {
		ChromeOptions options = new ChromeOptions();

		if ( bin.isEmpty() ) {
			WebDriverManager.chromedriver().setup();

		} else {
			System.setProperty( "webdriver.chrome.driver", "/app/.chromedriver/bin/chromedriver" );

			options.setBinary( bin );

		}

		options.addArguments( ArrayUtils.addAll( arguments, "--headless", "--disable-gpu" ) );

		return new ChromeDriver( options );
	}
}