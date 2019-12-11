package com.liferay.blogs.adder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalService;
import com.liferay.blogs.service.BlogsEntryService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.dao.orm.QueryDefinition;
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
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.sites.kernel.util.Sites;

@Component(
		configurationPid = "com.liferay.blogs.adder.BlogsEntryAdderConfiguration",
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class BlogsEntryAdder {

	@Modified
	protected void activate(Map<String, Object> properties) throws PortalException {
		_blogsConfiguration = ConfigurableUtil.createConfigurable(BlogsEntryAdderConfiguration.class,
				properties);

		adds();
	}

	public void adds() throws PortalException {

		String[] titles = _blogsConfiguration.titles();
		String[] contents = _blogsConfiguration.contents();
		String[] descriptions = _blogsConfiguration.descriptions();

		if (titles.length == 0 | titles.length != contents.length | titles.length != descriptions.length) {
			_log.error(
					"Make sure there are titles in the configuration, and that the number of titles, contents, and descriptions are equal.");
			return;
		}

		long companyId = PortalUtil.getDefaultCompanyId();

		Role adminRole = RoleLocalServiceUtil.getRole(companyId, "Administrator");
		List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole.getRoleId());
		long userId = adminUsers.get(0).getUserId();

		ServiceContext serviceContext = new ServiceContext();
		List<Long> siteGroupIds = _getSiteGroupIds(companyId);

		for (long siteGroupId : siteGroupIds) {

			serviceContext.setAddGuestPermissions(true);
			serviceContext.setAddGroupPermissions(true);

			serviceContext.setScopeGroupId(siteGroupId);
			QueryDefinition<BlogsEntry> queryDefinition = new QueryDefinition<>(WorkflowConstants.STATUS_ANY);
			List<BlogsEntry> existingBlogsEntries = _bels.getGroupEntries(siteGroupId, queryDefinition);
			List<String> existingTitles = new ArrayList<String>();

			existingBlogsEntries.forEach(blogsEntry -> existingTitles.add(blogsEntry.getTitle()));

			for (int i = 0; i < titles.length; i++) {

				if (existingTitles.contains(titles[i])) {

					_log.error(
							"Make sure all Blogs titles in the BlogsEntryAdder configuration are unique in the site");
				} else {

					Map<Locale, String> titleMap = new HashMap<>();
					titleMap.put(LocaleUtil.getSiteDefault(), titles[i]);

					Map<Locale, String> descriptionMap = new HashMap<>();
					descriptionMap.put(LocaleUtil.getSiteDefault(), descriptions[i]);

					 _bels.addEntry(userId, titles[i], contents[i], serviceContext);
				}
			}
		}
	}

	// get all the sites, but exclude the global site
	private List<Long> _getSiteGroupIds(long companyId) {

		List<Group> sites = _groupLocalService.getGroups(companyId, GroupConstants.ANY_PARENT_GROUP_ID, true);
		List<Long> siteGroupIds = new ArrayList<Long>();

		for (Group group : sites) {
			if (!group.getFriendlyURL().equals(GroupConstants.GLOBAL_FRIENDLY_URL)) {
				long groupId = group.getGroupId();
				siteGroupIds.add(groupId);
			}
		}

		return siteGroupIds;
	}

	private static final Log _log = LogFactoryUtil.getLog(BlogsEntryAdder.class);

	@Reference
	private BlogsEntryLocalService _bels;

	@Reference
	private BlogsEntryService _blogsEntryService;

	@Reference
	private Sites _sites;

	@Reference
	private GroupLocalService _groupLocalService;

	private volatile BlogsEntryAdderConfiguration _blogsConfiguration;
}
