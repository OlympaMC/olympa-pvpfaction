package fr.olympa.pvpfac.faction.chat;

import fr.olympa.api.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public enum FactionChat {

	GENERAL("Général", "normal", "reset"),
	FACTION("Faction", "fac", "f"),
	ALLY("Allié", "ally", "a", "alliée", "alliés", "alliées");

	String name;
	List<String> surname;

	FactionChat(final String name, final String... surname) {
		this.name = name;
		this.surname = Arrays.asList(surname);
	}

	public static FactionChat get(final String name) {
		return Arrays.stream(FactionChat.values()).filter(chat -> Utils.equalsIgnoreAccents(chat.getName(), name) || chat.hasSurname(name)).findFirst().orElse(null);
	}

	public String getName() {
		return name;
	}

	public boolean hasSurname(final String surname) {
		return this.surname.stream().anyMatch(sur -> Utils.equalsIgnoreAccents(surname, sur));
	}

	public @Nullable FactionChat getOther() {
		switch (this) {
			case GENERAL:
				return FactionChat.FACTION;
			case FACTION:
				return FactionChat.ALLY;
			case ALLY:
				return FactionChat.GENERAL;
		}
		return null;
	}
}
