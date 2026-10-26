package com.github.dappermickie.odablock.emotes;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import net.runelite.api.Client;
import net.runelite.api.IndexedSprite;
import net.runelite.client.util.ImageUtil;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmoteTest
{
	@Test
	public void paletteReductionPreservesTransparencyAndDarkPixels()
	{
		BufferedImage image = new BufferedImage(258, 1, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < 256; x++)
		{
			image.setRGB(x, 0, 0xFF000000 | (x << 16) | (255 - x));
		}
		image.setRGB(0, 0, 0xFF010101);
		image.setRGB(256, 0, 0x00123456);
		image.setRGB(257, 0, 0x80123456);
		BufferedImage reduced = Emote.toChatIconImage(image);
		assertEquals(image.getWidth(), reduced.getWidth());
		assertEquals(image.getHeight(), reduced.getHeight());
		assertEquals(0x00123456, reduced.getRGB(256, 0));
		assertEquals(0x80123456, reduced.getRGB(257, 0));
		Set<Integer> colors = new HashSet<>();
		for (int x = 0; x < 256; x++)
		{
			int pixel = reduced.getRGB(x, 0);
			assertEquals(255, pixel >>> 24);
			assertTrue((pixel & 0xFFFFFF) != 0);
			colors.add(pixel);
		}
		assertTrue(colors.size() <= 255);
	}

	@Test
	public void imageAtPaletteLimitIsUnchanged()
	{
		BufferedImage image = new BufferedImage(255, 1, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < 255; x++)
		{
			image.setRGB(x, 0, 0xFF000001 + x);
		}
		assertSame(image, Emote.toChatIconImage(image));
	}

	@Test
	public void everyEmoteLoadsChatAndOverheadImages()
	{
		Client client = mock(Client.class);
		when(client.createIndexedSprite()).thenAnswer(invocation -> mock(IndexedSprite.class));
		for (Emote emote : Emote.values())
		{
			assertNotNull(emote.name(), emote.loadImage());
			assertNotNull(emote.name(), emote.loadOverheadImage());
			ImageUtil.getImageIndexedSprite(emote.loadImage(), client);
			ImageUtil.getImageIndexedSprite(emote.loadOverheadImage(), client);
		}
	}
}
