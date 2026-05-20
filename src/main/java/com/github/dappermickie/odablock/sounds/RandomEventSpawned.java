package com.github.dappermickie.odablock.sounds;

import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.Client;
import net.runelite.api.gameval.NpcID;

@Singleton
@Slf4j
public class RandomEventSpawned
{
	@Inject
	private Client client;

	@Inject
	private OdablockConfig config;

	@Inject
	private SoundEngine soundEngine;

	@Inject
	private ScheduledExecutorService executor;

	private static final Set<Integer> RANDOM_EVENT_NPC_IDS = Set.of(
		NpcID.MACRO_BEEKEEPER_INVITATION,
		NpcID.MACRO_COMBILOCK_PIRATE,
		NpcID.MACRO_JEKYLL,
		NpcID.MACRO_JEKYLL_UNDERWATER,
		NpcID.MACRO_DWARF,
		NpcID.PATTERN_INVITATION,
		NpcID.MACRO_EVIL_BOB_OUTSIDE,
		NpcID.MACRO_EVIL_BOB_PRISON,
		NpcID.PINBALL_INVITATION,
		NpcID.MACRO_FORESTER_INVITATION,
		NpcID.MACRO_FROG_CRIER,
		NpcID.MACRO_GENI,
		NpcID.MACRO_GENI_UNDERWATER,
		NpcID.MACRO_GILES,
		NpcID.MACRO_GILES_UNDERWATER,
		NpcID.MACRO_GRAVEDIGGER_INVITATION,
		NpcID.MACRO_MILES,
		NpcID.MACRO_MILES_UNDERWATER,
		NpcID.MACRO_MYSTERIOUS_OLD_MAN,
		NpcID.MACRO_MYSTERIOUS_OLD_MAN_UNDERWATER,
		NpcID.MACRO_MAZE_INVITATION,
		NpcID.MACRO_MIME_INVITATION,
		NpcID.MACRO_NILES,
		NpcID.MACRO_NILES_UNDERWATER,
		NpcID.MACRO_PILLORY_GUARD,
		NpcID.GRAB_POSTMAN,
		NpcID.MACRO_MAGNESON_INVITATION,
		NpcID.MACRO_HIGHWAYMAN,
		NpcID.MACRO_HIGHWAYMAN_UNDERWATER,
		NpcID.MACRO_SANDWICH_LADY_NPC,
		NpcID.MACRO_DRILLDEMON_INVITATION,
		NpcID.MACRO_COUNTCHECK_SURFACE,
		NpcID.MACRO_COUNTCHECK_UNDERWATER
	);

	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		if (!config.randomEventSpawned() || npcSpawned.getNpc() == null)
		{
			return;
		}

		final int npcId = npcSpawned.getNpc().getId();
		if (!isRandomEventNpcId(npcId))
		{
			return;
		}

		final Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || npcSpawned.getNpc().getInteracting() != localPlayer)
		{
			return;
		}

		log.debug("Random event NPC spawned for local player: {}", npcId);
		soundEngine.playClip(Sound.GETTING_RAGGED, executor);
	}

	public boolean isRandomEventNpcId(final int npcId)
	{
		return RANDOM_EVENT_NPC_IDS.contains(npcId);
	}
}
