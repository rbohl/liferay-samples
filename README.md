# Liferay Samples
These are modules and scripts that I use to explore Liferay's APIs and frameworks. Some contain pedagogical code for use in documentation, some are used to programmatically create content so I can quickly get up and running, populating a new Liferay bundle with demo content.

## Liferay Content Adder

> Add `JournalArticles` via OSGi configuration admin

With the Liferay Content Adder, you can generate basic Web Content articles in one of two ways;

1.  Visit the Configuration UI at Control Panel &rarr; Configuration &rarr; System Settings &rarr; 
Web Content &rarr; Journal Article Adder Configuration and enter _title_, _description_, and _content_ for each article you'd like to add.

2.  Provide a `.config` file properly formatted, also including the three fields mentioned above, for each Web Content article you'd like to add.

A sample configuration article is available in the module's `liferay-content-adder/configs` folder.

### The Liferay Content Adder is Strong-Willed

To make everything simple, these decisions are hard-coded into the Liferay Content Adder:

1.  There must be a 1:1:1 relationship between titles, contents, and descriptions.
2.  If any of the titles in the array passed from the configuration matches an existing article's title, no articles will be added. All the titles must be unique in the site.
3.  The first administrator retrieved from the system is always the author of the content.
4.  The default site (GUEST) `groupId` is always used. 
5.  The default site locale is always used for localization.
