package com.github.dappermickie.odablock;

import com.github.dappermickie.odablock.overrides.SoundOverrideService;
import com.github.dappermickie.odablock.overrides.SoundPools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.audio.AudioPlayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

@Singleton
@Slf4j
public class SoundEngine
{
	@Inject
	private OdablockConfig config;

	@Inject
	private AudioPlayer audioPlayer;

	@Inject
	private SoundOverrideService soundOverrideService;

	private final Object managedClipLock = new Object();
	private Clip managedClip;

	public void playClip(Sound sound, Executor executor)
	{
		executor.execute(() -> playClipInternal(sound));
	}

	public void playClip(Sound sound, ScheduledExecutorService executor, Duration initialDelay)
	{
		executor.schedule(() -> playClipInternal(sound), initialDelay.toMillis(), TimeUnit.MILLISECONDS);
	}

	public void playFile(File file, Executor executor)
	{
		if (file == null)
		{
			return;
		}
		executor.execute(() -> playFileInternal(file));
	}

	private void playFileInternal(File file)
	{
		if (SoundFileManager.getIsUpdating())
		{
			return;
		}

		float gain = 20f * (float) Math.log10(config.announcementVolume() / 100f);
		try
		{
			audioPlayer.play(file, gain);
		}
		catch (IOException | UnsupportedAudioFileException | LineUnavailableException exception)
		{
			log.warn("Failed to preview sound {}", file.getName(), exception);
		}
	}

	private void playClipInternal(Sound sound)
	{
		if (SoundFileManager.getIsUpdating())
		{
			return;
		}

		float gain = 20f * (float) Math.log10(config.announcementVolume() / 100f);
		try
		{
			File soundFile = resolveSoundFile(sound).orElse(null);
			if (soundFile == null)
			{
				log.warn("No audio file available for {}", SoundPools.getDisplayName(sound.getDirectory()));
				return;
			}

			audioPlayer.play(soundFile, gain);
		}
		catch (FileNotFoundException e)
		{
			log.warn("Sound file not found for " + sound, e);
		}
		catch (IOException e)
		{
			log.warn("Failed to play Odablock sound " + sound, e);
		}
		catch (UnsupportedAudioFileException e)
		{
			log.warn("Failed to play Odablock sound " + sound, e);
		}
		catch (LineUnavailableException e)
		{
			log.warn("Failed to play Odablock sound " + sound, e);
		}
	}

	public void playManagedClip(Sound sound, Executor executor)
	{
		executor.execute(() -> playManagedClipInternal(sound));
	}

	public void stopManagedClip()
	{
		synchronized (managedClipLock)
		{
			if (managedClip != null)
			{
				managedClip.stop();
				managedClip.close();
				managedClip = null;
			}
		}
	}

	private void playManagedClipInternal(Sound sound)
	{
		if (SoundFileManager.getIsUpdating())
		{
			return;
		}

		float gain = 20f * (float) Math.log10(config.announcementVolume() / 100f);

		try
		{
			File soundFile = resolveSoundFile(sound).orElse(null);
			if (soundFile == null)
			{
				log.warn("No audio file available for {}", SoundPools.getDisplayName(sound.getDirectory()));
				return;
			}

			try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile))
			{
				Clip clip = AudioSystem.getClip();
				clip.open(audioInputStream);

				try
				{
					var control = (javax.sound.sampled.FloatControl) clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
					control.setValue(gain);
				}
				catch (Exception e)
				{
					log.warn("Failed to set managed clip gain: {}", e.getMessage());
				}

				synchronized (managedClipLock)
				{
					if (managedClip != null)
					{
						managedClip.stop();
						managedClip.close();
					}

					managedClip = clip;
					clip.addLineListener(event ->
					{
						if (event.getType() != javax.sound.sampled.LineEvent.Type.STOP)
						{
							return;
						}

						synchronized (managedClipLock)
						{
							if (managedClip == clip)
							{
								clip.close();
								managedClip = null;
							}
						}
					});
					clip.start();
				}
			}
		}
		catch (FileNotFoundException e)
		{
			log.warn("Sound file not found for {}", sound, e);
		}
		catch (IOException | UnsupportedAudioFileException | LineUnavailableException e)
		{
			log.warn("Failed to play managed Odablock sound {}", sound, e);
		}
	}

	private Optional<File> resolveSoundFile(Sound sound) throws FileNotFoundException
	{
		String poolDirectory = sound.getDirectory();
		File soundFile = soundOverrideService.getRandomOverrideFile(poolDirectory).orElseGet(() -> {
			try
			{
				return SoundFileManager.getSoundStream(sound);
			}
			catch (FileNotFoundException fileNotFoundException)
			{
				return null;
			}
		});

		return Optional.ofNullable(soundFile);
	}

	public void close()
	{
		stopManagedClip();
	}
}
