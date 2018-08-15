package pacman.utility;

/* This class can be used as an URL that doesn't throw all the damn exceptions.
   Use .getURL() to extract the actual URL object.
   (I would have extended URL, but it's a final class.)

   Author: Martin Stepp (stepp@cs.arizona.edu)
 */

import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class EasyURLHolder implements Serializable {
    private static final long serialVersionUID = 0;
    
	private URL myURL = null;

	public URL getURL() {
		return myURL;
	}

	public EasyURLHolder(URL url) {
		if (url == null)
			throw new IllegalArgumentException("null URL argument passed");

		myURL = url;
	}

	public EasyURLHolder(EasyURLHolder holder) {
		if (holder == null)
			throw new IllegalArgumentException("null EasyURLHolder argument passed");

		myURL = holder.getURL();
	}

	public EasyURLHolder(String spec) {
		try {
			myURL = new URL(spec);
		}
		catch (MalformedURLException mfurle) {
			throw new RuntimeException(mfurle.getMessage());
		}
	}

	public EasyURLHolder(String protocol, String host, int port, String file) {
		try {
			myURL = new URL(protocol, host, port, file);
		}
		catch (MalformedURLException mfurle) {
			throw new RuntimeException(mfurle.getMessage());
		}
	}

	public EasyURLHolder(String protocol, String host, String file) {
		try {
			myURL = new URL(protocol, host, file);
		}
		catch (MalformedURLException mfurle) {
			throw new RuntimeException(mfurle.getMessage());
		}
	}

	public EasyURLHolder(URL context, String spec) {
		try {
			myURL = new URL(context, spec);
		}
		catch (MalformedURLException mfurle) {
			throw new RuntimeException(mfurle.getMessage());
		}
	}

	public EasyURLHolder(EasyURLHolder holder, String spec) {
		try {
			myURL = new URL(holder.getURL(), spec);
		}
		catch (MalformedURLException mfurle) {
			throw new RuntimeException(mfurle.getMessage());
		}
	}

	public int getPort()
	{
		return myURL.getPort();
	}

	public String getProtocol()
	{
		return myURL.getProtocol();
	}

	public String getHost()
	{
		return myURL.getHost();
	}

	public String getFile()
	{
		return myURL.getFile();
	}

	public String getRef()
	{
		return myURL.getRef();
	}

	public boolean equals(Object obj)
	{
		return myURL.equals(obj);
	}

	public int hashCode()
	{
		return myURL.hashCode();
	}

	public boolean sameFile(URL other)
	{
		return myURL.sameFile(other);
	}

	public String toString()
	{
		return myURL.toString();
	}

	public String toExternalForm()
	{
		return myURL.toExternalForm();
	}

	public URLConnection openConnection()
	{
		try {
			return myURL.openConnection();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.getMessage());
		}
	}

	public final InputStream openStream() {
		try {
			return myURL.openStream();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.getMessage());
		}
	}

	public final Object getContent() {
		try {
			return myURL.getContent();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe.getMessage());
		}
	}
}