# Liferay Samples

These are modules and scripts that I use to explore Liferay's APIs and frameworks. Some contain pedagogical code for use in documentation, some are used to programmatically create content so I can quickly get up and running, populating a new Liferay bundle with demo content.

- [Liferay Content Adder](#liferay-content-adder)
- [Liferay Blogs Adder](#liferay-blogs-adder)

## Branches

At time of writing, branch management works like this:

`master`: The master branch is intended to run on master. No promises, obviously.

`7.2`: The 7.2 branch contains code that runs on Liferay CE/DXP 7.2. No promises, but if it doesn't work as advertised, I'd be interested in hearing from you.

`7.3`: The 7.3 branch contains code that runs on Liferay CE/DXP 7.2. No promises, but if it doesn't work as advertised, I'd be interested in hearing from you.

To build for running on master, this module relies on `mavenLocal()` as a repository for satisfying build dependencies, pointed at a local maven repository where your Liferay artifacts from master can be installed (using `gw install` in the module root (e.g., `liferay-portal/modules/apps/journal`) works for me). 

### On the master branch, I use local dependencies from liferay-portal master

To use the `mavenLocal` repository (the one at `USER_HOME/.m2` in a standard setup) for local dependency installation of portal artifacts, make sure you have this in your build.USERNAME.properties:

    build.repository.local.dir=

At first, I followed these steps to make Gradle find the portal root's `.m2` folder for dependencies (I don't know if Gradle would have found the .m2 in liferay-portal without this, but maybe it would):

1.  Open `USER_HOME/.m2`
2.  Add a `settings.xml` file. I copied the contents of `MAVEN_HOME/conf/settings.xml` to get started.
3.  Add this to `USER_HOME/.m2/settings.xml`: 

    ```xml
    <localRepository>/path/to/liferay-portal/.m2</localRepository>
    ```

4.  Use `mavenLocal()` in the definition of `repositories` in `build.gradle`, 

This `liferay-portal/.m2` cache is wiped out on every portal build, so this is probably a bad idea.

Master relies on the local maven repository, with the proper dependencies installed. 7.2 and other published versions will use published artifacts on Liferay's CDN site to satisfy dependencies.

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
4.  The articles are added to all sites in the system except the global site, so that if you've defined a Lunar Resort site or some other site for demonstration, it will be populated with the articles as well. 
5.  The default site locale is always used for localization.

### Troubleshooting the Liferay Content Adder

**Problem:** The bundle is active but updating the configuration (via .config file or System Settings) does not trigger the Journal Article creation.

_Cause:_ If the module JAR is deployed without a configuration file present, the Configuration class is not activated.

You'll likely see exception in the Liferay log to this effect:

```bash
2019-11-05 15:50:00.873 ERROR [fileinstall-/home/russell/liferay-bundles/master/osgi/modules][JournalArticleAdder:93] bundle com.liferay.docs.content.adder:1.0.0 (1013)[com.liferay.docs.content.adder.JournalArticleAdder(6785)] : The activate method has thrown an exception 
java.lang.RuntimeException: Unable to create snapshot class for interface com.liferay.docs.content.adder.JournalArticleAdderConfiguration
```

_Solution_: Deploy the .config file and Refresh the bundle from Gogo shell.

## Liferay Blogs Adder

> Add `BlogsEntry`s via OSGi configuration admin

With the Liferay Blogs Adder, you can generate Blogs Entries in one of two ways;

1.  Visit the Configuration UI at Control Panel &rarr; Configuration &rarr; System Settings &rarr; 
Blogs &rarr; Blogs Adder Configuration and enter _title_, _description_, and _content_ for each blog you'd like to add.

2.  Provide a `.config` file properly formatted, also including the three fields mentioned above, for each blog you'd like to add.

A sample configuration file is available in the module's `liferay-blogs-adder/configs` folder.

### Usage

First, copy the `.config` file into `Liferay_Home/osgi/configs`. A configuration listener will simply pick up the config file modification and run the service calls to add Blogs Entries.

Use gradle (I run gradle through Liferay's Blade CLI tool) to build the JAR. Copy it to `Liferay_Home/deploy`.

To add additional blog entries, you have to at least change the titles in the configuration, then save (either ave the `.config` file or click the _Update_ if you're using the System Settings UI).

### The Liferay Blogs Adder is Strong-Willed

To make everything simple, these decisions are hard-coded into the Liferay Content Adder:

1.  There must be a 1:1:1 relationship between titles, contents, and descriptions.
2.  If any of the titles in the array passed from the configuration matches an existing blog's title in a site, the blog with that title won't be added. Any additional blogs with unique titles will be added as expected. Importantly, **this pertains to the blogs in the Recycle Bin as well (to be fixed later)**
3.  The first administrator retrieved from the system is always the author.
4.  The blogs are added to all sites in the system except the global site, so that if you've defined a Lunar Resort site or some other site for demonstration, it will be populated with the blog entries as well. 
5.  The default site locale is always used for localization.

