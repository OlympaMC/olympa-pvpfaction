package fr.olympa.pvpfac.player;

import fr.olympa.api.spigot.clans.ClanPlayerData;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.observable.ObservableValue;
import fr.olympa.pvpfac.PvPFaction;
import fr.olympa.pvpfac.faction.Faction;
import fr.olympa.pvpfac.faction.claim.FactionClaimPermLevel;
import org.jetbrains.annotations.Nullable;

public class FactionPlayerData extends ClanPlayerData<Faction, FactionPlayerData> {

	private final ObservableValue<FactionRole> role;

	public FactionPlayerData(final OlympaPlayerInformations informations) {
		this(informations, FactionRole.RECRUT);
	}

	public FactionPlayerData(final OlympaPlayerInformations informations, final FactionRole role) {
		super(informations);
		this.role = new ObservableValue<>(role);
		this.role.observe("updateSQL", () -> PvPFaction.getInstance().factionManager.roleColumn.updateAsync(this, this.role.get().ordinal(), null, null));
	}

	public enum FactionRole {

		//NE PAS RAJOUTER DE VALEUR ICI SI LE SERVEUR EST EN PRODUCTION !!
		//ou retravailler la classe FactionClaim pour prendre en compte les nouveaux rôles dans l'array de ClaimPermLevel concernant les factions membres du claim
		LEADER(3, 10, "Leader", "**", FactionClaimPermLevel.LVL_4),
		OFFICER(2, 5, "Officier", "*", FactionClaimPermLevel.LVL_4),
		MEMBER(1, 2, "Membre", "+", FactionClaimPermLevel.LVL_3),
		RECRUT(0, 0, "Recrue", "-", FactionClaimPermLevel.LVL_1);

		public final int weight;
		public final int power;
		public final String name;
		public final String prefix;
		public final FactionClaimPermLevel claimLevel;

		FactionRole(final int weight, final int power, final String name, final String prefix, final FactionClaimPermLevel level) {
			this.weight = weight;
			this.power = power;
			this.name = name;
			this.prefix = prefix;
			this.claimLevel = level;
		}

		public @Nullable FactionRole getAbove() {
			try {
				return values()[ordinal() - 1];
			} catch (final ArrayIndexOutOfBoundsException ex) {
				return null;
			}
		}

		public @Nullable FactionRole getBelow() {
			try {
				return values()[ordinal() + 1];
			} catch (final ArrayIndexOutOfBoundsException ex) {
				return null;
			}
		}

		public FactionClaimPermLevel getDefaultClaimLevel() {
			return claimLevel;
		}

	}

	public FactionRole getRole() {
		return role.get();
	}

	public void setRole(final FactionRole role) {
		this.role.set(role);
	}

}
