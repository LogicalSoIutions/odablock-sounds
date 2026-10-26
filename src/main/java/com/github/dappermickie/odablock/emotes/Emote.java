package com.github.dappermickie.odablock.emotes;

import com.google.common.collect.ImmutableMap;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.util.ImageUtil;

@AllArgsConstructor
public enum Emote
{
	AIAIAI("aiaiai", EmoteType.GIF),
	AWKWARD("awkward", EmoteType.GIF),
	FUFU("fufu", EmoteType.GIF),
	GNEAH("gneah", EmoteType.PNG),
	HUH("huh", EmoteType.PNG),
	HYPERWANKGE("hyperwankge", EmoteType.GIF),
	JANITOR("janitor", EmoteType.PNG),
	KEK("kek", EmoteType.GIF),
	KISS("kiss", EmoteType.PNG),
	NOWAY("noway", EmoteType.PNG),
	PFACE(":p", EmoteType.GIF, new String[]{"pface"}),
	RLY("rly", EmoteType.GIF),
	SMILE(":)", EmoteType.GIF, new String[]{"smile"}),
	TUNE("tune", EmoteType.GIF),
	ODAWHAT("odawhat", EmoteType.PNG),
	DAP("dap", EmoteType.PNG),
	CAP("cap", EmoteType.PNG),
	CAPPP("cappp", EmoteType.PNG),
	WHENITREGISTERS("whenitregisters", EmoteType.GIF),
	WINK("wink", EmoteType.PNG);

	private enum EmoteType
	{
		PNG,
		GIF
	}

	private static final Map<String, Emote> emojiMap;

	@Getter
	private final String trigger;
	private final EmoteType emoteType;
	@Getter
	private final String[] altTriggers;

	Emote(final String trigger, EmoteType emoteType)
	{
		this(trigger, emoteType, new String[]{});
	}

	static
	{
		ImmutableMap.Builder<String, Emote> builder = new ImmutableMap.Builder<>();

		for (final Emote emoji : values())
		{
			builder.put(emoji.trigger, emoji);
			if (!emoji.trigger.startsWith("oda") && !emoji.trigger.startsWith(":"))
			{
				builder.put("oda" + emoji.trigger, emoji);
			}
			for (final String altTrigger : emoji.altTriggers)
			{
				builder.put(altTrigger, emoji);
				if (!altTrigger.startsWith("oda") && !altTrigger.startsWith(":"))
				{
					builder.put("oda" + altTrigger, emoji);
				}
			}
		}

		emojiMap = builder.build();
	}

	BufferedImage loadImage()
	{
		return toChatIconImage(ImageUtil.loadImageResource(getClass(), this.name().toLowerCase() + ".png"));
	}

	BufferedImage loadOverheadImage()
	{
		String path = this.name().toLowerCase() + "_overhead.png";
		try
		{
			if (getClass().getResource(path) != null)
			{
				return toChatIconImage(ImageUtil.loadImageResource(getClass(), path));
			}
		}
		catch (Exception e)
		{
			// Ignore and fallback to standard image
		}
		return loadImage();
	}

	static BufferedImage toChatIconImage(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		Set<Integer> colors = new HashSet<>();
		for (int pixel : pixels)
		{
			// RuneLite only draws fully opaque pixels in indexed chat sprites.
			if ((pixel >>> 24) == 255)
			{
				colors.add(pixel & 0xFFFFFF);
			}
		}
		if (colors.size() <= 255)
		{
			return image;
		}

		// Six levels per channel give at most 216 colors, leaving room for transparency.
		for (int i = 0; i < pixels.length; i++)
		{
			int pixel = pixels[i];
			if ((pixel >>> 24) != 255)
			{
				continue;
			}
			int red = (((pixel >>> 16) & 255) + 25) / 51 * 51;
			int green = (((pixel >>> 8) & 255) + 25) / 51 * 51;
			int blue = ((pixel & 255) + 25) / 51 * 51;
			int rgb = (red << 16) | (green << 8) | blue;
			// RuneLite reserves RGB zero for transparency; keep dark pixels visible.
			pixels[i] = 0xFF000000 | (rgb == 0 ? 1 : rgb);
		}
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		result.setRGB(0, 0, width, height, pixels, 0, width);
		return result;
	}

	static Emote getEmoji(String trigger)
	{
		return emojiMap.get(trigger);
	}
}
