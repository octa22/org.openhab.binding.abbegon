/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.abbegon.internal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.openhab.binding.abbegon.AbbEgonBindingProvider;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;


/**
 * Implement this class if you are going create an actively polling service
 * like querying a Website/Device.
 * 
 * @author Ondrej Pecta
 * @since 1.9.0
 */
public class AbbEgonBinding extends AbstractActiveBinding<AbbEgonBindingProvider> {

	private static final Logger logger = 
		LoggerFactory.getLogger(AbbEgonBinding.class);

	/**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is set in the activate()
	 * method and must not be accessed anymore once the deactivate() method was called or before activate()
	 * was called.
	 */
	private BundleContext bundleContext;


	/**
	 * the refresh interval which is used to poll values from the ABB Ego-n
	 * server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;
	private String user;
	private String password;
	private String host;
	private String device = "";
	
	public AbbEgonBinding() {
	}
		
	
	/**
	 * Called by the SCR to activate the component with its configuration read from CAS
	 * 
	 * @param bundleContext BundleContext of the Bundle that defines this component
	 * @param configuration Configuration properties for this component obtained from the ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;

		// the configuration is guaranteed not to be null, because the component definition has the
		// configuration-policy set to require. If set to 'optional' then the configuration may be null


		readConfiguration(configuration);
		login();
		if(!device.equals("0") && tryParseInt(device)) {
			logger.info("Detected ABB Ego-n device: " + device);
			setProperlyConfigured(true);
		}
		else
			setProperlyConfigured(false);

	}

	private void readConfiguration(final Map<String, Object> configuration) {
		// to override the default refresh interval one has to add a
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}

		String ip = "";
		String port = "80";

		String ipString = (String) configuration.get("ip");
		if (StringUtils.isNotBlank(ipString)) {
			ip = ipString;
		}

		String portString = (String) configuration.get("port");
		if (StringUtils.isNotBlank(portString)) {
			port = portString;
		}

		String userString = (String) configuration.get("user");
		if (StringUtils.isNotBlank(userString)) {
			user = userString;
		}

		String passwordString = (String) configuration.get("password");
		if (StringUtils.isNotBlank(passwordString)) {
			password = passwordString;
		}

		host = "http://" + ip + ":" + port;
	}

	private void login() {
		String url = null;

		try {
			url = host + "/authorize.html?user=" + user + "&password=" + password;
			String line = getHttpResponse(url);

			device = line.replace("device=", "").replace("\n","");

			if (!tryParseInt(device)) {
				logger.debug("ABB Ego-n user response: " + line);
				throw new AbbEgonException(line);
			}
		} catch (MalformedURLException e) {
			logger.error("The URL '" + url + "' is malformed: " + e.toString());
		} catch (Exception e) {
			logger.error("Cannot get ABB Ego-n user device: " + e.toString());
		}
	}

	boolean tryParseInt(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private String getHttpResponse(String url) throws Exception {
		URL cookieUrl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) cookieUrl.openConnection();
		connection.setRequestMethod("GET");

		InputStream response = connection.getInputStream();
		return readResponse(response);
	}

	private String readResponse(InputStream response) throws Exception {
		String line;
		StringBuilder body = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(response));

		while ((line = reader.readLine()) != null) {
			body.append(line).append("\n");
		}
		line = body.toString();
		logger.debug(line);
		return line;
	}

	/**
	 * Called by the SCR when the configuration of a binding has been changed through the ConfigAdmin service.
	 * @param configuration Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		// update the internal configuration accordingly
	}
	
	/**
	 * Called by the SCR to deactivate the component when either the configuration is removed or
	 * mandatory references are no longer satisfied or the component has simply been stopped.
	 * @param reason Reason code for the deactivation:<br>
	 * <ul>
	 * <li> 0 – Unspecified
     * <li> 1 – The component was disabled
     * <li> 2 – A reference became unsatisfied
     * <li> 3 – A configuration was changed
     * <li> 4 – A configuration was deleted
     * <li> 5 – The component was disposed
     * <li> 6 – The bundle was stopped
     * </ul>
	 */
	public void deactivate(final int reason) {
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and 
		// should be reset when activating this binding again
	}

	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "AbbEgon Refresh Service";
	}
	
	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		logger.debug("execute() method is called!");
		login();

		String status = getEgonStatus();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(status)));

			XPath xpath = XPathFactory.newInstance().newXPath();

			for (final AbbEgonBindingProvider provider : providers) {
				for (final String itemName : provider.getItemNames()) {
					String id = provider.getItemType(itemName);

					String state = getState(id, document, xpath );

					if (!state.equals(provider.getItemState(itemName)) && !"???".equals(state)) {
						provider.setItemState(itemName, state);
						if(state.startsWith("UP") || state.startsWith("DOWN")) {
							eventPublisher.postUpdate(itemName, new PercentType(state.startsWith("DOWN") ? "100" : "0"));
						}
						else
						if (state.contains("."))
								eventPublisher.postUpdate(itemName, new DecimalType(state));
						else
							eventPublisher.postUpdate(itemName, new StringType(state));


					}
				}
			}
		}
		catch (Exception ex)
		{
			logger.error(ex.toString());
		}

	}

	private String getState(String id, Document document, XPath xpath) throws Exception {

		String value = (String) xpath.evaluate("/egon_data/element_states/element_state[attribute::id='" + id + "']/@value", document, XPathConstants.STRING);
		logger.debug("ABB Ego-n id: " + id + " value: " + value);

		return value.toUpperCase();
	}

	private String getEgonStatus() {
		String url = null;

		try {
			url = host + "/state.html?device=" + device;

			String line = getHttpResponse(url);

			logger.debug("ABB Ego-n States response: " + line);

			if (line.startsWith("<?xml ")) {
				return line;
			} else {
				logger.debug("ABB Ego-n response: " + line);
				throw new AbbEgonException(line);
			}

		} catch (MalformedURLException e) {
			logger.error("The URL '" + url + "' is malformed: " + e.toString());
		} catch (Exception e) {
			logger.error("Cannot get ABB Ego-n status: " + e.toString());
		}
		return "";
	}

	private void refreshEgon() {
		String url = null;

		try {
			url = host + "/refresh.html?device=" + device;

			String line = getHttpResponse(url);

			logger.debug("ABB Ego-n Refresh response: " + line);
		} catch (MalformedURLException e) {
			logger.error("The URL '" + url + "' is malformed: " + e.toString());
		} catch (Exception e) {
			logger.error("Cannot send ABB Ego-n refresh: " + e.toString());
		}
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the 
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveCommand({},{}) is called!", itemName, command);
		String type = getEgonDevice(itemName);
		doAction(type, command);
	}

	private String getEgonDevice(String itemName) {

		for (final AbbEgonBindingProvider provider : providers) {
			return provider.getItemType(itemName);
		}
		return "";
	}

	private void doAction(String id, Command command) {
		String url = null;

		try {
			url = host + "/action.html?action=" + command + "&device=" + device + "&id=" + id;
			String line = getHttpResponse(url);

			if (!"OK\n".equals(line)) {
				logger.debug("ABB Ego-n response: " + line);
				throw new AbbEgonException(line);
			}
		} catch (MalformedURLException e) {
			logger.error("The URL '" + url + "' is malformed: " + e.toString());
		} catch (Exception e) {
			logger.error("Cannot send ABB Ego-n action: " + e.toString());
		}
	}



	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// the code being executed when a state was sent on the openHAB
		// event bus goes here. This method is only called if one of the 
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveUpdate({},{}) is called!", itemName, newState);
	}

}
