package com.liferay.blogs.adder;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.settings.definition.ConfigurationBeanDeclaration;

@Component
public class BlogsEntryAdderConfigurationBeanDeclaration implements ConfigurationBeanDeclaration {

@Override
public Class<?> getConfigurationBeanClass() {
    return BlogsEntryAdderConfiguration.class;
}
}
