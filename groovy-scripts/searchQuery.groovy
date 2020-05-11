String userKeywords="sale";
// Create a Web Content Folder titled "Homes for Sale" before running this script
// Change the userKeywords above to search for another term


import com.liferay.portal.kernel.model.*
import com.liferay.portal.kernel.service.*
import com.liferay.portal.kernel.util.*
import com.liferay.portal.kernel.search.SearchContext
import com.liferay.portal.search.hits.SearchHit
import com.liferay.portal.search.query.TermsQuery
import com.liferay.portal.search.query.MatchQuery
import com.liferay.portal.search.query.BooleanQuery
import com.liferay.portal.search.query.Queries
import com.liferay.portal.search.searcher.Searcher
import com.liferay.portal.search.searcher.SearchResponse
import com.liferay.portal.search.searcher.SearchRequest
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory
import com.liferay.portal.search.searcher.SearchRequestBuilder

import com.liferay.portal.scripting.groovy.internal.GroovyExecutor

import org.osgi.framework.Bundle
import org.osgi.framework.FrameworkUtil
import org.osgi.util.tracker.ServiceTracker

// Since a com.liferay.portal.scripting.groovy.internal.GroovyExecutor instance
// executes the script, the instance’s bundle is used to track the service.
Bundle bundle = FrameworkUtil.getBundle(GroovyExecutor.class);

// Set up Service trackers for the Searcher, SearchRequestBuilderFactory, and
// Queries services
ServiceTracker<Searcher, Searcher> searcherST;
ServiceTracker<SearchRequestBuilderFactory, SearchRequestBuilderFactory> searchRequestBuilderFactoryST;
ServiceTracker<Queries, Queries> queriesST;

// Get a Searcher service reference and open the service tracker, then
// instantiate an Object with the service
searcherST = new ServiceTracker(bundle.getBundleContext(), Searcher.class, null);
searcherST.open();
Searcher searcher = searcherST.waitForService(500);

// Get a SearchRequestBuilderFactory service reference and open the service
// tracker, then instantiate an Object with the service
searchRequestBuilderFactoryST = new ServiceTracker(bundle.getBundleContext(), SearchRequestBuilderFactory.class, null);
searchRequestBuilderFactoryST.open();
SearchRequestBuilderFactory searchRequestBuilderFactory = searchRequestBuilderFactoryST.waitForService(500);

// Get a Queries service reference and open the service tracker, then
// instantiate an Object with the service
queriesST = new ServiceTracker(bundle.getBundleContext(), Queries.class, null);
queriesST.open();
Queries queries = queriesST.waitForService(500);

// get the default Company ID and the guest group ID
long companyId = PortalUtil.getDefaultCompanyId()
Group guestGroup = GroupLocalServiceUtil.getGroup(companyId,
    GroupConstants.GUEST)
guestGroupId=guestGroup.getGroupId()
long[] groupIds = [guestGroupId]

// We want to match our keywords to the localized title of the web content folder
MatchQuery titleQuery = queries.match("title_en_US", "Home");

// This TermsQuery acts as a filter, making sure we only return Web Content
// Folders with the ID 0, which is the DEFAULT_PARENT_FOLDER_ID in
// JournalFolderConstants
TermsQuery folderQuery = queries.terms("folderId");
folderQuery.addValues("0");

// This boolean query wraps our Match- and TermsQuery. Both queries we set up
// must match. The TermsQuery must match its search for folder Ids of 0 (root
// folders) and the MatchQuery is used to do a full text search of the
// localized Title field for our keywords.
BooleanQuery booleanQuery = queries.booleanQuery();
booleanQuery.addMustQueryClauses(folderQuery, titleQuery);

// Build and execute a Search Request
SearchRequestBuilder searchRequestBuilder = searchRequestBuilderFactory.builder();

// necessary if not passing keywords into the search context
searchRequestBuilder.emptySearchEnabled(true);

// not really necessary but i wanted to try it out
searchRequestBuilder.includeResponseString(true);

// Set the company Id into the searchContext, at a minimum. We're also setting
// the keywords into the searchContext. This is necessary now, but I think at
// least companyId is going to be added via the Request Builder in a future
// version?
// toggle the comment off to set keywords into the search context. empty search
// enabled makes this optional
searchRequestBuilder.withSearchContext(
    { searchContext -> searchContext.setCompanyId(companyId);
        searchContext.setGroupIds(groupIds);
        searchContext.setKeywords(userKeywords); 
} );

SearchRequest searchRequest = searchRequestBuilder.query(booleanQuery).build();

// The searcher.search call instantiates a SearchResponse
SearchResponse searchResponse = searcher.search(searchRequest);

// This is just some proof, so we can see what we got in our search response.
hitsCount = searchResponse.getTotalHits()
responseString = searchResponse.getResponseString()
searchHitsObject = searchResponse.getSearchHits()

// loop through the hits, get each one's document, then do something, like
// print the title and the viewCount?
searchHits = searchHitsObject.getSearchHits();

out.println("Searching for " + userKeywords + " returned " + hitsCount + " hits")

for (SearchHit hit : searchHits ) {
    hitId = hit.getId()
    hitScore = hit.getScore()
    doc = hit.getDocument()
    docViewCount = doc.getString("viewCount")
    message = "Document " + hitId + " had a score of " + hitScore + " and a view count of " + docViewCount
    out.println(
"""
                <div style="background-color:gray; text-align: left">
                    <body style="color: #37A9CC; font-size:large">
                        ${message}
                    </body>
                </div>
        """ )
}

// close the service trackers
searcherST.close();
searchRequestBuilderFactoryST.close();
queriesST.close();
