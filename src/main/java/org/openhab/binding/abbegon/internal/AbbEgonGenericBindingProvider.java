/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.abbegon.internal;

import org.openhab.binding.abbegon.AbbEgonBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;


/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Ondrej Pecta
 * @since 1.9.0
 */
public class AbbEgonGenericBindingProvider extends AbstractGenericBindingProvider implements AbbEgonBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "abbegon";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		//if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
		//	throw new BindingConfigParseException("item '" + item.getName()
		//			+ "' is of type '" + item.getClass().getSimpleName()
		//			+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		//}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		AbbEgonBindingConfig config = new AbbEgonBindingConfig(bindingConfig);
		
		//parse bindingconfig here ...
		
		addBindingConfig(item, config);		
	}

	public void setItemState(String itemName, String state) {
		final AbbEgonBindingConfig config = (AbbEgonBindingConfig) this.bindingConfigs.get(itemName);
		config.setState(state);
	}

	public String getItemState(String itemName) {
		final AbbEgonBindingConfig config = (AbbEgonBindingConfig) this.bindingConfigs.get(itemName);
		return config != null ? (config.getState()) : "";
	}


	public String getItemType(String itemName) {
		final AbbEgonBindingConfig config = (AbbEgonBindingConfig) this.bindingConfigs.get(itemName);
		return config != null ? (config.getType()) : null;
	}
	
	/**
	 * This is a helper class holding binding specific configuration details
	 * 
	 * @author Ondrej Pecta
	 * @since 1.9.0
	 */
	class AbbEgonBindingConfig implements BindingConfig {
		// put member fields here which holds the parsed values
		// put member fields here which holds the parsed values
		private String type;
		private String state;

		AbbEgonBindingConfig(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

	}
	
	
}
