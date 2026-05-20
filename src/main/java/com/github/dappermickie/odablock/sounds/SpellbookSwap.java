package com.github.dappermickie.odablock.sounds;

import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;

@Singleton
@Slf4j
public class SpellbookSwap
{
	private static final int SPELLBOOK_VARBIT = 4070;
	private static final int SPELLBOOK_STANDARD = 0;
	private static final int SPELLBOOK_ANCIENT = 1;
	private static final int SPELLBOOK_LUNAR = 2;
	private static final int SPELLBOOK_ARCEUUS = 3;

	@Inject
	private Client client;

	@Inject
	private OdablockConfig config;

	@Inject
	private SoundEngine soundEngine;

	@Inject
	private ScheduledExecutorService executor;

	@Setter
	private int lastLoginTick = -1;

	private int previousSpellbook = -1;

	// Number of ticks after login to suppress false triggers
	private static final int LOGIN_GRACE_TICKS = 5;

	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() != SPELLBOOK_VARBIT)
		{
			return;
		}

		final int newSpellbook = event.getValue();

		// On first load or login, just record the value without playing
		if (previousSpellbook == -1)
		{
			previousSpellbook = newSpellbook;
			return;
		}

		// If the value didn't actually change, skip
		if (newSpellbook == previousSpellbook)
		{
			return;
		}

		previousSpellbook = newSpellbook;

		// Suppress sounds during login/hop grace period
		final int currentTick = client.getTickCount();
		if (lastLoginTick != -1 && currentTick - lastLoginTick <= LOGIN_GRACE_TICKS)
		{
			return;
		}

		if (!config.spellbookSwap())
		{
			return;
		}

		final Sound sound = getSoundForSpellbook(newSpellbook);
		if (sound == null)
		{
			return;
		}

		soundEngine.playClip(sound, executor);
	}

	private Sound getSoundForSpellbook(final int spellbook)
	{
		switch (spellbook)
		{
			case SPELLBOOK_STANDARD:
				return Sound.SPELLBOOK_SWAP_4;
			case SPELLBOOK_ANCIENT:
				return Sound.SPELLBOOK_SWAP_1;
			case SPELLBOOK_LUNAR:
				return Sound.SPELLBOOK_SWAP_2;
			case SPELLBOOK_ARCEUUS:
				return Sound.SPELLBOOK_SWAP_3;
			default:
				return null;
		}
	}
}
