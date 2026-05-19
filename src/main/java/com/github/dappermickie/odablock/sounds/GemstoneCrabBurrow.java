package com.github.dappermickie.odablock.sounds;

import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ScheduledExecutorService;

@Singleton
@Slf4j
public class GemstoneCrabBurrow {
	@Inject
	private Client client;

	@Inject
	private OdablockConfig config;

	@Inject
	private SoundEngine soundEngine;

	@Inject
	private ScheduledExecutorService executor;

	private static final String GEMSTONE_CRAB_DEATH_MESSAGE =
		"The gemstone crab burrows away, leaving a piece of its shell behind.";

	public boolean onChatMessage(ChatMessage chatMessage) {
		if (config.gemstoneCrabBurrow() &&
				chatMessage.getMessage().contains(GEMSTONE_CRAB_DEATH_MESSAGE)) {
			soundEngine.playClip(Sound.GEMSTONE_CRAB_BURROW, executor);
			return true;
		}
		return false;
	}
}
