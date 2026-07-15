package com.github.dappermickie.odablock.sounds;

import java.util.regex.Pattern;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KillingPlayerTest
{
	private static final Pattern[] KILL_PATTERNS = {Pattern.compile("rip.*")};

	@Test
	public void lootBroadcastFromPlayerNamedRipIsNotAPlayerKill()
	{
		assertFalse(KillingPlayer.isPlayerKillMessage(
			"rip arthur received a drop: 2 x rune platelegs",
			KILL_PATTERNS));
	}

	@Test
	public void actualRipPlayerKillMessageStillMatches()
	{
		assertTrue(KillingPlayer.isPlayerKillMessage("rip arthur.", KILL_PATTERNS));
	}
}
