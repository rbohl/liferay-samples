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
		required = false)
	public String [] titles();

	@Meta.AD
		(name = "content",
		required = false)
	public String [] contents();

	@Meta.AD
		(name = "description",
		required = false)
	public String [] descriptions();

}
