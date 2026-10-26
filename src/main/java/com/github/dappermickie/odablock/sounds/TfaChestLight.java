package com.github.dappermickie.odablock.sounds;

import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.NpcID;

@Singleton
public class TfaChestLight
{
	// Loot room template region, also used by the TFA Plugin Hub plugin's RaidRoom.
	private static final int LOOT_REGION = 16474;

	private final Client client;
	private final OdablockConfig config;
	private final SoundEngine soundEngine;
	private final ScheduledExecutorService executor;

	private boolean chestsHandled;
	private boolean rewardsSeen;

	@Inject
	public TfaChestLight(Client client, OdablockConfig config, SoundEngine soundEngine,
		ScheduledExecutorService executor)
	{
		this.client = client;
		this.config = config;
		this.soundEngine = soundEngine;
		this.executor = executor;
	}

	public void onGameTick(GameTick event)
	{
		Player player = client.getLocalPlayer();
		if (client.getGameState() != GameState.LOGGED_IN || player == null || player.getLocalLocation() == null)
		{
			return;
		}

		if (WorldPoint.fromLocalInstance(client, player.getLocalLocation()).getRegionID() != LOOT_REGION)
		{
			reset();
			return;
		}

		if (chestsHandled)
		{
			return;
		}

		boolean ownChest = false;
		boolean uniqueChest = false;
		for (NPC npc : client.getNpcs())
		{
			// The eight standard chests transform according to ownership and opened state.
			NPCComposition composition = npc.getTransformedComposition();
			if (composition == null)
			{
				continue;
			}
			switch (composition.getId())
			{
				case NpcID.TFA_UNIQUE_CHEST_VAULT_CLOSED:
				case NpcID.TFA_UNIQUE_CHEST_VAULT_OPEN:
					uniqueChest = true;
					break;
				case NpcID.TFA_REWARDS_CHEST_VAULT_CLOSED_MINE:
				case NpcID.TFA_REWARDS_CHEST_VAULT_OPEN_MINE:
					ownChest = true;
					break;
			}
		}

		if (!ownChest && !uniqueChest)
		{
			return;
		}

		// Give the reward NPCs another tick to load before treating an absent unique as a white.
		if (!rewardsSeen)
		{
			rewardsSeen = true;
			return;
		}

		chestsHandled = true;
		if (uniqueChest)
		{
			if (config.tfaPurpleChest())
			{
				soundEngine.playClip(Sound.GETTING_PURPLE_1, executor);
			}
		}
		else if (config.tfaWhiteChest())
		{
			soundEngine.playClip(Sound.WHITE_LIGHT_AFTER_RAID, executor);
		}
	}

	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGIN_SCREEN:
			case LOGGING_IN:
			case HOPPING:
				reset();
				break;
		}
	}

	public void reset()
	{
		chestsHandled = false;
		rewardsSeen = false;
	}
}
