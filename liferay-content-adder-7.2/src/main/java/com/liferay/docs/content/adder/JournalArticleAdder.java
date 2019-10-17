package com.liferay.docs.content.adder;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalArticleConstants;
import com.liferay.journal.model.JournalFolderConstants;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.journal.service.JournalFolderLocalService;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
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

		String portletId = PortletProviderUtil.getPortletId(
			JournalArticle.class.getName(), PortletProvider.Action.EDIT);

        String articleURL = _portal.getControlPanelFullURL(
            groupId, portletId, null);

		String namespace = _portal.getPortletNamespace(portletId);

        String articleId = StringPool.BLANK;

		articleURL = _http.addParameter(
			articleURL, namespace + "groupId", groupId);
		articleURL = _http.addParameter(
			articleURL, namespace + "folderId", folderId);
		articleURL = _http.addParameter(
			articleURL, namespace + "articleId", articleId);

		ServiceContext serviceContext = new ServiceContext();
		serviceContext.setScopeGroupId(groupId);
		serviceContext.isAddGroupPermissions();
		serviceContext.isAddGuestPermissions();
		serviceContext.setAssetCategoryIds(null);
		serviceContext.setAssetEntryVisible(true);
		serviceContext.setAssetLinkEntryIds(null);
		serviceContext.setAssetPriority(0.0);
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

			// log something here
			return;
		}

		for (int i = 0; i < titles.length; i++) {
			
			Map<Locale, String> friendlyURLMap = new HashMap<>();
			friendlyURLMap.put(LocaleUtil.US, titles[i]);
			//friendlyURLMap = null;

			Map<Locale, String> titleMap = new HashMap<>();
			titleMap.put(LocaleUtil.US, titles[i]);

			Map<Locale, String> descriptionMap = new HashMap<>();
			descriptionMap.put(LocaleUtil.US, descriptions[i]);

			Map<Locale, String> contentMap = new HashMap<>();
			contentMap.put(LocaleUtil.US, contents[i]);

			/*
			 * This is a stupid hack, find a way to send structured content via the api
			 * Maybe JournalConverter holds the magic
			 * String content = _journalConverter.getContent(ddmStructure, fields);
			 */ 
			String content = "<?xml version=\"1.0\"?><root><dynamic-element name=\"content\" type=\"text_area\" index-type=\"text\" instance-id=\"qmln\"><dynamic-content language-id=\"en_US\"><![CDATA[<p>"
					+ contents[i] + "</p>]]></dynamic-content></dynamic-element></root>";

			/*
			 * _jals.addArticle(userId, groupId, folderId, classNameId, classPK, articleId,
			 * autoArticleId, version, titleMap, descriptionMap, friendlyURLMap, content,
			 * "BASIC-WEB-CONTENT", "BASIC-WEB-CONTENT", layoutUuid, displayDateMonth,
			 * displayDateDay, displayDateYear, displayDateHour, displayDateMinute, date,
			 * date, date, date, date, neverExpire, date, date, date, date, date,
			 * neverReview, indexable, smallImage, smallImageURL, smallImageFile, images,
			 * articleURL, serviceContext);
			 */

			_jals.addArticle(userId, groupId, folderId, titleMap, descriptionMap, content, "BASIC-WEB-CONTENT",
					"BASIC-WEB-CONTENT", serviceContext);
		}
	}

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

	private volatile JournalArticleAdderConfiguration _journalArticleConfiguration;
}
