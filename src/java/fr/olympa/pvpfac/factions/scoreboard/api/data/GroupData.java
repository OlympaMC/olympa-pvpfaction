package fr.olympa.pvpfac.factions.scoreboard.api.data;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * This class represents a group nametag. There are several properties
 * available.
 */
public class GroupData implements INametag {

	private String groupName;
	private String prefix;
	private String suffix;
	private String permission;
	private Permission bukkitPermission;
	private int sortPriority;

	public GroupData(String groupName, String prefix, String suffix, String permission, Permission bukkitPermission, int sortPriority) {
		super();
		this.groupName = groupName;
		this.prefix = prefix;
		this.suffix = suffix;
		this.permission = permission;
		this.bukkitPermission = bukkitPermission;
		this.sortPriority = sortPriority;
	}

	public Permission getBukkitPermission() {
		return bukkitPermission;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getPermission() {
		return permission;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public int getSortPriority() {
		return sortPriority;
	}

	@Override
	public String getSuffix() {
		return suffix;
	}

	@Override
	public boolean isPlayerTag() {
		return false;
	}

	public void setBukkitPermission(Permission bukkitPermission) {
		this.bukkitPermission = bukkitPermission;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setPermission(String permission) {
		this.permission = permission;
		bukkitPermission = new Permission(permission, PermissionDefault.FALSE);
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSortPriority(int sortPriority) {
		this.sortPriority = sortPriority;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

}