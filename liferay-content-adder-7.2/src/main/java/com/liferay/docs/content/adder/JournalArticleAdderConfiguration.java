package com.liferay.docs.content.adder;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category = "web-content")
@Meta.OCD(
	id = "com.liferay.docs.content.adder.JournalArticleAdderConfiguration",
	localization = "content/Language",
	name = "Journal Adder Configuration"
)
public interface JournalArticleAdderConfiguration {
	
	@Meta.AD
		(name = "title",
		required = true)
	public String [] titles();

	@Meta.AD
		(name = "content",
		required = true)
	public String [] contents();

	@Meta.AD
		(name = "descripttion",
		required = true)
	public String [] descriptions();

}
