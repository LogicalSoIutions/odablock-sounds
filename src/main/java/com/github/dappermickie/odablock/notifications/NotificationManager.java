package com.github.dappermickie.odablock.notifications;

import com.github.dappermickie.odablock.OdablockConfig;
import com.github.dappermickie.odablock.Sound;
import com.github.dappermickie.odablock.SoundEngine;
import com.github.dappermickie.odablock.overrides.SoundOverrideAction;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@Singleton
public class NotificationManager
{

	private Set<String> sentMessages = new HashSet<>();
	private int lastChecked = -1;

	@Inject
	private Client client;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private OdablockConfig config;

	@Inject
	private SoundEngine soundEngine;

	@Inject
	private ScheduledExecutorService executor;

	public void onGameTick(GameTick gameTick)
	{
		if (!config.notifications())
		{
			return;
		}
		int currentTick = client.getTickCount();
		if (lastChecked == -1 || currentTick - lastChecked > 100)
		{
			executor.submit(this::sendRequest);
			lastChecked = currentTick;
		}
	}

	private void sendRequest()
	{
		Request request = new Request.Builder()
			.url("https://raw.githubusercontent.com/LogicalSoIutions/odablock-sounds-live/refs/heads/main/custom_notifications.json")
			.build();
		try (Response response = okHttpClient.newCall(request).execute())
		{
			if (!response.isSuccessful() || response.body() == null)
			{
				return;
			}

			String jsonResponse = response.body().string();
			Notification[] notifications = gson.fromJson(jsonResponse, Notification[].class);
			for (Notification notification : notifications)
			{
				if (notification.getMessage() == null || notification.getMessage().isEmpty())
				{
					continue;
				}

				if (sentMessages.contains(notification.getMessage()))
				{
					continue;
				}

				sendMessage(notification);
				sentMessages.add(notification.getMessage());
				soundEngine.playClip(Sound.ODAS_ALERT, SoundOverrideAction.NOTIFICATION_ALERT, executor);
			}
		}
		catch (IOException ignored)
		{
		}
	}

	private void sendMessage(Notification notification)
	{
		ChatMessageBuilder chatMessage = new ChatMessageBuilder();
		chatMessage
			.append(ChatColorType.HIGHLIGHT)
			.append("Odablock just sent an announcement ~ ")
			.append(notification.getMessage());
		String hex = Integer.toHexString(config.notificationColor().getRGB()).substring(2);
		String message = chatMessage.build().replaceAll("colHIGHLIGHT", "col=" + hex);
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(message)
			.build());
	}
}
