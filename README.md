# Liferay Samples
These are modules and scripts that I use to explore Liferay's APIs and frameworks. Some contain pedagogical code for use in documentation, some are used to programmatically create content so I can quickly get up and running, populating a new Liferay bundle with demo content.

## Liferay Content Adder

> Add `JournalArticles` via OSGi configuration admin

With the Liferay Content Adder, you can generate basic Web Content articles in one of two ways;

1.  Visit the Configuration UI at Control Panel &rarr; Configuration &rarr; System Settings &rarr; 
Web Content &rarr; Journal Article Adder Configuration and enter _title_, _description_, and _content_ for each article you'd like to add.

2.  Provide a `.config` file properly formatted, also including the three fields mentioned above, for each Web Content article you'd like to add.

A sample configuration article is available in the module's `liferay-content-adder/configs` folder.

### Usage

First, copy the `.config` file into `Liferay_Home/osgi/configs`. A configuration listener will simply pick up the config file modification and run the service calls to add Web Content articles.

Use gradle (I run gradle through Liferay's Blade CLI tool) to build the JAR. Copy it to `Liferay_Home/deploy`.

To add additional articles, you have to at least change the titles in the configuration, then save (either ave the `.config` file or click the _Update_ if you're using the System Settings UI).

### The Liferay Content Adder is Strong-Willed

To make everything simple, these decisions are hard-coded into the Liferay Content Adder:

1.  There must be a 1:1:1 relationship between titles, contents, and descriptions.
2.  If any of the titles in the array passed from the configuration matches an existing article's title in a site, the article with that title won't be added. Any additional articles with unique titles will be added as expected. Importantly, **this pertains to the articles in the Recycle Bin as well (to be fixed later)**
3.  The first administrator retrieved from the system is always the author of the content.
4.  The articles are added to ALL sites in the system, so that if you've defined a Lunar Resort site or some other site for demonstration, it will be populated with the articles as well. 
5.  The default site locale is always used for localization.
