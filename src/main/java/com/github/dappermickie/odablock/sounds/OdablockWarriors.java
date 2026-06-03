package com.github.dappermickie.odablock.sounds;


import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

public class OdablockWarriors
{
	private static final String ORIGINAL_TRACK_NAME = "7th Realm";
	private static final String REPLACEMENT_TRACK_NAME = "Odablock Warriors";

	@Inject
	private Client client;

	@Inject
	private SoundEngine soundEngine;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private OdablockConfig config;

	private int warriorWidgetId = -1;

	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.MUSIC)
		{
			return;
		}

		warriorWidgetId = -1;

		if (!config.warriors())
		{
			return;
		}

		renameTrackWidget();
	}

	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getParam1() >>> 16 != InterfaceID.MUSIC)
		{
			return;
		}

		String target = stripTags(event.getMenuTarget());
		if (!config.warriors())
		{
			soundEngine.stopManagedClip();
			return;
		}

		if (warriorWidgetId != event.getParam1()
			&& !ORIGINAL_TRACK_NAME.equals(target)
			&& !REPLACEMENT_TRACK_NAME.equals(target))
		{
			soundEngine.stopManagedClip();
			return;
		}

		event.consume();

		soundEngine.playManagedClip(Sound.WARRIOR, executor);
	}

	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!config.warriors())
		{
			return;
		}

		renameTrackWidget();
	}

	public void onConfigChanged(String key)
	{
		if (!"warriors".equals(key))
		{
			return;
		}

		if (!config.warriors())
		{
			warriorWidgetId = -1;
			soundEngine.stopManagedClip();
		}
	}

	private void renameTrackWidget()
	{
		Widget trackList = client.getWidget(InterfaceID.Music.JUKEBOX);
		if (trackList == null)
		{
			return;
		}

		for (Widget track : trackList.getDynamicChildren())
		{
			if (renameTrackWidget(track))
			{
				return;
			}
		}
	}

	private boolean renameTrackWidget(Widget widget)
	{
		if (widget == null)
		{
			return false;
		}

		String text = widget.getText();
		if (ORIGINAL_TRACK_NAME.equals(text) || REPLACEMENT_TRACK_NAME.equals(text))
		{
			widget.setText(REPLACEMENT_TRACK_NAME);
			widget.setName("<col=ff9040>" + REPLACEMENT_TRACK_NAME + "</col>");
			widget.setTextColor(901389);
			widget.revalidate();
			warriorWidgetId = widget.getId();
			return true;
		}

		return false;
	}

	private static String stripTags(String text)
	{
		if (text == null)
		{
			return "";
		}

		return text.replaceAll("<[^>]*>", "");
	}
}
