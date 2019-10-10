package com.liferay.docs.content.adder;

import org.osgi.service.component.annotations.Component;

import com.liferay.portal.kernel.settings.definition.ConfigurationBeanDeclaration;

@Component
public class JournalArticleAdderConfigurationBeanDeclaration implements ConfigurationBeanDeclaration {

@Override
public Class<?> getConfigurationBeanClass() {
    return JournalArticleAdderConfiguration.class;
}
}
