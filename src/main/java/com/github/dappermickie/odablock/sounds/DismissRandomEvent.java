package com.github.dappermickie.odablock.sounds;

import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.MenuOptionClicked;
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
		NPC clickedNpc = getClickedNpc(menuOptionClicked);
		int clickedNpcId = clickedNpc == null ? -1 : clickedNpc.getId();
		// Dismiss random event
		if (config.dismissRandomEvent()
			&& option.equals(optionText)
			&& widgetId != runePouchWidgetId
			&& widgetId != lootingBagWidgetId
			&& widgetId != chuggingBarrelWidgetId
			&& randomEventSpawned.isRandomEventNpcId(clickedNpcId))
		{
			soundEngine.playClip(Sound.DISMISSING_RANDOM_EVENT, executor);
		}
	}

	private NPC getClickedNpc(final MenuOptionClicked menuOptionClicked)
	{
		final int npcIndex = menuOptionClicked.getId();
		final NPC[] cachedNpcs = client.getCachedNPCs();
		if (cachedNpcs == null || npcIndex < 0 || npcIndex >= cachedNpcs.length)
		{
			return null;
		}

		return cachedNpcs[npcIndex];
	}
}
