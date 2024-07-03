String userKeywords="able";
// Searches object entries with a field named Foo Text to match the keywords
// Searches DDM structure field to match the keywords: you'll need to update the field name as it's not detemrinistic for DDM
// Searches root content and folders (folderId: 0) of web content and documents and media in the localized title field for the keywords
// Change the userKeywords above to search for another term


import com.liferay.object.model.*
import com.liferay.portal.kernel.model.*
import com.liferay.portal.kernel.module.util.SystemBundleUtil
import com.liferay.portal.kernel.service.*
import com.liferay.portal.kernel.search.SearchContext
import com.liferay.portal.kernel.util.*
import com.liferay.portal.search.hits.SearchHit
import com.liferay.portal.search.query.*
import com.liferay.portal.search.searcher.Searcher
import com.liferay.portal.search.searcher.SearchResponse
import com.liferay.portal.search.searcher.SearchRequest
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory
import com.liferay.portal.search.searcher.SearchRequestBuilder

import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker

// Get the system bundle context
BundleContext bundleContext = SystemBundleUtil.getBundleContext();

// Set up Service trackers for the Searcher, SearchRequestBuilderFactory, and
// Queries services
ServiceTracker<Searcher, Searcher> searcherST;
ServiceTracker<SearchRequestBuilderFactory, SearchRequestBuilderFactory> searchRequestBuilderFactoryST;
ServiceTracker<Queries, Queries> queriesST;

// Get a Searcher service reference and open the service tracker, then
// instantiate an Object with the service
searcherST = new ServiceTracker(bundleContext, Searcher.class, null);
searcherST.open();
Searcher searcher = searcherST.waitForService(500);

// Get a SearchRequestBuilderFactory service reference and open the service
// tracker, then instantiate an Object with the service
searchRequestBuilderFactoryST = new ServiceTracker(bundleContext, SearchRequestBuilderFactory.class, null);
searchRequestBuilderFactoryST.open();
SearchRequestBuilderFactory searchRequestBuilderFactory = searchRequestBuilderFactoryST.waitForService(500);

// Get a Queries service reference and open the service tracker, then
// instantiate an Object with the service
queriesST = new ServiceTracker(bundleContext, Queries.class, null);
queriesST.open();
Queries queries = queriesST.waitForService(500);

// get the default Company ID and the guest group ID
long companyId = PortalUtil.getDefaultCompanyId()
Group guestGroup = GroupLocalServiceUtil.getGroup(companyId,
    GroupConstants.GUEST)
guestGroupId=guestGroup.getGroupId()
long[] groupIds = [guestGroupId]

//Wrap all the queries in a parent boolean query so they can be made "should" clauses. Only one of the child queries needs to match in order to return results.
BooleanQuery parentBooleanQuery = queries.booleanQuery();

// Match a DDM Text Field with this ddmFieldArray:
// {ddmFieldName=ddm__keyword__35174__Text25689566_en_US, ddmFieldValueKeyword_en_US=Able text in the structure, ddmFieldValueKeyword_en_US_String_sortable=able text in the structure, ddmValueFieldName=ddmFieldValueKeyword_en_US}

// MatchQuery for the field name and another to match the field value to the user's keywords
MatchQuery ddmFieldNameQuery = queries.match("ddmFieldArray.ddmFieldName", "ddm__text__35174__Text25689566_en_US");

MatchQuery ddmFieldValueQuery = queries.match("ddmFieldArray.ddmFieldValueText_en_US", userKeywords);

// Add the queries as must clauses to a boolean query
BooleanQuery booleanDDMQuery = queries.booleanQuery();
booleanDDMQuery.addMustQueryClauses(ddmFieldNameQuery, ddmFieldValueQuery);

// Add the boolean query to a nested Query with the path nestedFieldArray
NestedQuery nestedDDMQuery = queries.nested("ddmFieldArray", booleanDDMQuery);

// Match an Object Text Field with this nestedFieldArray:
//[{fieldName=fooText, value_en_US=Able Text, valueFieldName=value_en_US}, {fieldName=fooText, value_keyword_lowercase=Able Text, valueFieldName=value_keyword_lowercase}]

// MatchQuery for the field name and another to match the field value to the user's keywords
MatchQuery objectFieldNameQuery = queries.match("nestedFieldArray.fieldName", "fooText");

MatchQuery objectFieldValueQuery = queries.match("nestedFieldArray.value_en_US", userKeywords);

// Add the queries as must clauses to a boolean query
BooleanQuery booleanObjectQuery = queries.booleanQuery();
booleanObjectQuery.addMustQueryClauses(objectFieldNameQuery, objectFieldValueQuery);

// Add the boolean query to a nested Query with the path nestedFieldArray
NestedQuery nestedObjectQuery = queries.nested("nestedFieldArray", booleanObjectQuery);

// Match the user's keywords to the localized title of the web content folder
MatchQuery titleQuery = queries.match("localized_title_en_US", userKeywords);

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

// Add the two boolean queries as should clauses, so that if we match either one the content is returned
parentBooleanQuery.addShouldQueryClauses(booleanQuery, nestedObjectQuery, nestedDDMQuery);

// Build and execute a Search Request
SearchRequestBuilder searchRequestBuilder = searchRequestBuilderFactory.builder();

// necessary if not passing keywords into the search context
// searchRequestBuilder.emptySearchEnabled(true);

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

SearchRequest searchRequest = searchRequestBuilder.query(parentBooleanQuery).build();

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
