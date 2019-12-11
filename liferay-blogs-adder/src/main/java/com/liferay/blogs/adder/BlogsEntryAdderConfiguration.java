package com.liferay.blogs.adder;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category = "blogs")
@Meta.OCD(
	id = "com.liferay.blogs.adder.BlogsEntryAdderConfiguration",
	localization = "content/Language",
	name = "Blogs Adder Configuration"
)
public interface BlogsEntryAdderConfiguration {
	
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
