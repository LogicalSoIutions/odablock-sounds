package com.github.dappermickie.odablock.sounds;

import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.NpcID;

@Singleton
@Slf4j
public class SerynaSound
{
	@Inject
	private SoundEngine soundEngine;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private OdablockConfig config;

	private static final String DISMISS_OPTION = "Dismiss";

	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (!config.sandwichLadySound())
		{
			return;
		}

		if (!DISMISS_OPTION.equals(menuOptionClicked.getMenuOption()))
		{
			return;
		}

		if (menuOptionClicked.getId() != NpcID.MACRO_SANDWICH_LADY_NPC)
		{
			return;
		}

		soundEngine.playClip(Sound.SERYNA_DISMISS, executor);
	}
}
