package com.liferay.docs.content.adder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolderConstants;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.sites.kernel.util.Sites;

@Component(configurationPid = "com.liferay.docs.content.adder.JournalArticleAdderConfiguration",
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JournalArticleAdder {

	@Modified
	protected void activate(Map<String, Object> properties) throws PortalException {
		_journalArticleConfiguration = ConfigurableUtil.createConfigurable(JournalArticleAdderConfiguration.class,
				properties);

		addArticles();
	}

	public void addArticles() throws PortalException {

		String[] titles = _journalArticleConfiguration.titles();
		String[] contents = _journalArticleConfiguration.contents();
		String[] descriptions = _journalArticleConfiguration.descriptions();

		if (titles.length == 0 | titles.length != contents.length | titles.length != descriptions.length) {
			_log.error(
					"Make sure there are titles in the confiugration, and that the number of titles, contents, and descriptions are equal.");
			return;
		}

		long companyId = PortalUtil.getDefaultCompanyId();

		long folderId = JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID;

		Role adminRole = RoleLocalServiceUtil.getRole(companyId, "Administrator");
		List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole.getRoleId());
		long userId = adminUsers.get(0).getUserId();

		ServiceContext serviceContext = new ServiceContext();

		List<Long> siteGroupIds = _getSiteGroupIds(companyId);

		for (long siteGroupId : siteGroupIds) {

			serviceContext.setScopeGroupId(siteGroupId);
			List<JournalArticle> existingArticles = _jals.getArticles(siteGroupId);
			List<String> existingTitles = new ArrayList<String>();

			existingArticles.forEach(article -> existingTitles.add(article.getTitleCurrentValue()));

			for (int i = 0; i < titles.length; i++) {

				if (existingTitles.contains(titles[i])) {

					_log.error(
							"Make sure all JournalArticle titles in the JournalArticleAdder configuration are unique in the site");
				} else {

					Map<Locale, String> titleMap = new HashMap<>();
					titleMap.put(LocaleUtil.getSiteDefault(), titles[i]);

					Map<Locale, String> descriptionMap = new HashMap<>();
					descriptionMap.put(LocaleUtil.getSiteDefault(), descriptions[i]);

					/*
					 * The private utility methods used to populate the content were stolen directly
					 * from JournalDemoDataCreatorImpl
					 * 
					 */ String content = _getStructuredContent(contents[i]);

					_jals.addArticle(userId, siteGroupId, folderId, titleMap, descriptionMap, content,
							"BASIC-WEB-CONTENT", "BASIC-WEB-CONTENT", serviceContext);
				}
			}
		}
	}

	private List<Long> _getSiteGroupIds(long companyId) {

		List<Group> sites = _groupLocalService.getGroups(companyId, GroupConstants.ANY_PARENT_GROUP_ID, true);
		List<Long> siteGroupIds = new ArrayList<Long>();

		for (Group group : sites) {
			long groupId = group.getGroupId();
			siteGroupIds.add(groupId);
		}

		return siteGroupIds;
	}

	private String _getStructuredContent(String content) {
		Locale locale = LocaleUtil.getSiteDefault();

		Document document = _createDocumentContent(locale.toString());

		Element rootElement = document.getRootElement();

		Element dynamicElementElement = rootElement.addElement("dynamic-element");

		dynamicElementElement.addAttribute("index-type", "text");
		dynamicElementElement.addAttribute("name", "content");
		dynamicElementElement.addAttribute("type", "text_area");

		Element element = dynamicElementElement.addElement("dynamic-content");

		element.addAttribute("language-id", LocaleUtil.toLanguageId(locale));
		element.addCDATA(content);

		return document.asXML();
	}

	private Document _createDocumentContent(String locale) {
		Document document = SAXReaderUtil.createDocument();

		Element rootElement = document.addElement("root");

		rootElement.addAttribute("available-locales", locale);
		rootElement.addAttribute("default-locale", locale);

		return document;
	}

	private static final Log _log = LogFactoryUtil.getLog(JournalArticleAdder.class);

	@Reference
	private JournalArticleLocalService _jals;

	@Reference
	private Sites _sites;

	@Reference
	private GroupLocalService _groupLocalService;

	private volatile JournalArticleAdderConfiguration _journalArticleConfiguration;
}
