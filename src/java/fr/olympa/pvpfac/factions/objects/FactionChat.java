package fr.olympa.pvpfac.factions.objects;

import java.util.Arrays;
import java.util.List;

import fr.olympa.api.utils.Utils;

public enum FactionChat {

	GENERAL("Géneral", "normal", "reset"),
	FACTION("Faction", "fac", "f"),
	ALLY("Allié", "ally", "a", "allier", "alliée", "alliées");

	public static FactionChat get(String name) {
		return Arrays.stream(FactionChat.values()).filter(chat -> Utils.equalsIgnoreAccents(chat.getName(), name) || chat.hasSurname(name)).findFirst().orElse(null);
	}

	String name;
	List<String> surname;

	private FactionChat(String name, String... surname) {
		this.name = name;
		this.surname = Arrays.asList(surname);
	}

	public String getName() {
		return name;
	}

	public FactionChat getOther() {
		if (this == FactionChat.GENERAL) {
			return FactionChat.FACTION;
		} else if (this == FactionChat.FACTION) {
			return FactionChat.ALLY;
		} else if (this == ALLY) {
			return FactionChat.GENERAL;
		}
		return null;
	}

	public boolean hasSurname(String surname) {
		return this.surname.stream().filter(sur -> Utils.equalsIgnoreAccents(surname, sur)).findFirst().isPresent();
	}
}
