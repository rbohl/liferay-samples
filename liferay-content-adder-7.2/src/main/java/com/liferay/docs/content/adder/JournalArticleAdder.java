package com.liferay.docs.content.adder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalFolderConstants;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalFolderLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

@Component(configurationPid = "com.liferay.docs.content.adder.JournalArticleAdderConfiguration")
public class JournalArticleAdder {

	@Modified
	protected void activate(Map<String, Object> properties) throws PortalException {
		_journalArticleConfiguration = ConfigurableUtil.createConfigurable(JournalArticleAdderConfiguration.class,
				properties);

		addArticles();
	}

	public void addArticles() throws PortalException {

		long companyId = PortalUtil.getDefaultCompanyId();
		long folderId = JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID;

		Group guestGroup = _groupLocalService.getGroup(companyId, GroupConstants.GUEST);
		long groupId = guestGroup.getGroupId();

		Role adminRole = RoleLocalServiceUtil.getRole(companyId, "Administrator");
		List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole.getRoleId());
		long userId = adminUsers.get(0).getUserId();

		String portletId = PortletProviderUtil.getPortletId(JournalArticle.class.getName(),
				PortletProvider.Action.EDIT);

		String articleURL = _portal.getControlPanelFullURL(groupId, portletId, null);

		String namespace = _portal.getPortletNamespace(portletId);

		String articleId = StringPool.BLANK;

		articleURL = _http.addParameter(articleURL, namespace + "groupId", groupId);
		articleURL = _http.addParameter(articleURL, namespace + "folderId", folderId);
		articleURL = _http.addParameter(articleURL, namespace + "articleId", articleId);

		ServiceContext serviceContext = new ServiceContext();

		long[] assetCategoryIds = new long[1];

		if (_assetVocabularyLocalService.getAssetVocabulariesCount() == 0) {
			AssetVocabulary av = _assetVocabularyLocalService.addVocabulary(userId, groupId, "space", serviceContext);
			AssetCategory ac = _assetCategoryLocalService.addCategory(userId, groupId, "lunar", av.getVocabularyId(),
					serviceContext);

			assetCategoryIds = new long[(int) ac.getCategoryId()];		  
		} else {
			List<AssetCategory> acs = _assetCategoryLocalService.getCategories();
			AssetCategory ac = acs.get(0);
			assetCategoryIds = new long[(int) ac.getCategoryId()];		  

		}
		 
		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setScopeGroupId(groupId);
		
		  serviceContext.setAssetCategoryIds(assetCategoryIds);
		  serviceContext.setAssetEntryVisible(true);
		  serviceContext.setAssetLinkEntryIds(null);
		  serviceContext.setAssetPriority(1);
		  serviceContext.setAssetTagNames(null);
		 
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_PUBLISH);
		serviceContext.setIndexingEnabled(true);
		Date now = new Date();
		serviceContext.setModifiedDate(now);
		serviceContext.setCreateDate(now);

		String[] titles = _journalArticleConfiguration.titles();
		String[] contents = _journalArticleConfiguration.contents();
		String[] descriptions = _journalArticleConfiguration.descriptions();

		if (titles.length == 0 | titles.length != contents.length) {

			_log.error(
					"Make sure there are titles in the confiugration, and that the number of titles and content are equal.");
			return;
		}

		for (int i = 0; i < titles.length; i++) {

			Map<Locale, String> friendlyURLMap = new HashMap<>();
			friendlyURLMap.put(LocaleUtil.US, titles[i]);

			friendlyURLMap = null;

			Map<Locale, String> titleMap = new HashMap<>();
			titleMap.put(LocaleUtil.getSiteDefault(), titles[i]);

			Map<Locale, String> descriptionMap = new HashMap<>();
			descriptionMap.put(LocaleUtil.getSiteDefault(), descriptions[i]);

			/*
			 * Stolen directly from JournalDemoDataCreatorImplThis is a stupid hack, find a way to send structured content via the api
			 * Maybe JournalConverter holds the magic String content =
			 * _journalConverter.getContent(ddmStructure, fields);
			 */
			String content = _getStructuredContent(contents[i]);

			/*
			 * _jals.addArticle(userId, groupId, folderId, classNameId, classPK, articleId,
			 * autoArticleId, version, titleMap, descriptionMap, friendlyURLMap, content,
			 * "BASIC-WEB-CONTENT", "BASIC-WEB-CONTENT", layoutUuid, displayDateMonth,
			 * displayDateDay, displayDateYear, displayDateHour, displayDateMinute, date,
			 * date, date, date, date, neverExpire, date, date, date, date, date,
			 * neverReview, indexable, smallImage, smallImageURL, smallImageFile, images,
			 * articleURL, serviceContext);
			 */

			/*
			 * List<JournalArticle> existingArticles = _jals.getArticles(groupId);
			 * 
			 * 
			 * for (JournalArticle article:existingArticles) { if (titles[i] ==
			 * article.getTitle()) {
			 * 
			 * double version = article.getVersion(); _jals.updateArticle(userId, groupId,
			 * folderId, articleId, version, titleMap, descriptionMap, content,
			 * "BASIC-WEB-CONTENT", serviceContext); } }
			 */
			_jals.addArticle(userId, groupId, folderId, titleMap, descriptionMap, content, "BASIC-WEB-CONTENT",
					"BASIC-WEB-CONTENT", serviceContext);
		}
	}

	private String _getStructuredContent(String content) {
		Locale locale = LocaleUtil.getSiteDefault();

		Document document = _createDocumentContent(locale.toString());

		Element rootElement = document.getRootElement();

		Element dynamicElementElement = rootElement.addElement(
			"dynamic-element");

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
	private JournalFolderLocalService _journalFolderLocalService;

	@Reference
	private DDMStructureLocalService _ddmStructureLocalService;

	@Reference
	private DDMTemplateLocalService _ddmTemplateLocalService;

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	Portal _portal;

	@Reference
	private Http _http;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	private AssetVocabularyLocalService _assetVocabularyLocalService;

	private volatile JournalArticleAdderConfiguration _journalArticleConfiguration;
}
