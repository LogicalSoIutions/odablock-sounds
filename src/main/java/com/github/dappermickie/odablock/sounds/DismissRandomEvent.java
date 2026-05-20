package com.github.dappermickie.odablock.sounds;

import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.NpcID;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;
import net.runelite.api.widgets.Widget;

@Singleton
@Slf4j
public class DismissRandomEvent
{

	@Inject
	private Client client;

	@Inject
	private OdablockConfig config;

	@Inject
	private SoundEngine soundEngine;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private RandomEventSpawned randomEventSpawned;

	private static final String optionText = "Dismiss";
	private static final int runePouchWidgetId = 983062;
	private static final int lootingBagWidgetId = 983048;
	private static final int chuggingBarrelWidgetId = 983103;

	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		Widget widget = menuOptionClicked.getWidget();
		int widgetId = widget == null ? -1 : widget.getId();
		String option = menuOptionClicked.getMenuOption();
		// Dismiss random event
		if (config.dismissRandomEvent()
			&& option.equals(optionText)
			&& widgetId != runePouchWidgetId
			&& widgetId != lootingBagWidgetId
			&& widgetId != chuggingBarrelWidgetId
			&& menuOptionClicked.getId() != NpcID.MACRO_SANDWICH_LADY_NPC
			&& randomEventSpawned.isRandomEventNpcId(menuOptionClicked.getId()))
		{
			soundEngine.playClip(Sound.DISMISSING_RANDOM_EVENT, executor);
		}
	}
}
