/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2022 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.rammelkast.anticheatreloaded.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;

public class UpdateManager {

	public static final int RESOURCE_ID = 23799;
	public static final String SPIGOT_VERSION_URL = "https://api.spigotmc.org/legacy/update.php?resource="
			+ RESOURCE_ID;

	private String latestVersion;
	private boolean isLatest;
	private boolean isAhead;

	public UpdateManager() {
		update();
	}
	
	public void update() {
		this.latestVersion = getOnlineData(SPIGOT_VERSION_URL);
		if (this.latestVersion == null) {
			this.isLatest = true;
			this.isAhead = false;
			return;
		}
		
		int splitCompare = 0;
		try {
			VersionSplit currentSplit = new VersionSplit(AntiCheatReloaded.getVersion());
			VersionSplit newSplit = new VersionSplit(this.latestVersion);
			splitCompare = currentSplit.compareTo(newSplit);
		} catch (Exception e) {}
		this.isLatest = splitCompare >= 0;
		this.isAhead = splitCompare > 0;
	}

	private String getOnlineData(final String url) {
		String data = null;
		InputStream stream = null;
		try {
			stream = new URL(url).openStream();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			final StringBuilder builder = new StringBuilder();
			int readChar;
			while ((readChar = reader.read()) != -1) {
				builder.append((char) readChar);
			}
			data = builder.toString();
			reader.close();
		} catch (final IOException exception) {}
		finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (final IOException e) {}
			}
		}
		return data;
	}

	public boolean isLatest() {
		return this.isLatest;
	}
	
	public boolean isAhead() {
		return this.isAhead;
	}
	
	public String getCurrentVersion() {
		return AntiCheatReloaded.getVersion();
	}

	public String getLatestVersion() {
		return this.latestVersion;
	}

	public class VersionSplit implements Comparable<VersionSplit> {
		private final int major, minor, build;
		private boolean prerelease = false;

		public VersionSplit(String version) throws Exception {
			if (version.endsWith("-ALPHA")) {
				version = version.substring(0, version.length() - 6);
			} else if (version.endsWith("-PRE")) {
				this.prerelease = true;
				version = version.substring(0, version.length() - 4);
			}

			final String[] versionSplit = version.split("\\.");
			if (versionSplit.length != 3) {
				// Illegal version
				throw new Exception("Version " + version + " is illegal!");
			}

			try {
				final int major = Integer.parseInt(versionSplit[0]);
				final int minor = Integer.parseInt(versionSplit[1]);
				final int build = Integer.parseInt(versionSplit[2]);
				if (major <= 0) {
					throw new Exception("Illegal version!");
				}
				
				this.major = major;
				this.minor = minor;
				this.build = build;
			} catch (Exception e) {
				throw new Exception("Illegal version!");
			}
		}

		@Override
		public int compareTo(final VersionSplit other) {
			if (other.major == this.major) {
				if (other.minor == this.minor) {
					if (other.build == this.build) {
						if (!this.prerelease) {
							return 0;
						} else {
							return -1;
						}
					}
					if (other.build > this.build) {
						return -1;
					}
					return 1;
				}
				if (other.minor > this.minor) {
					return -1;
				}
				return 1;
			}
			if (other.major > this.major) {
				return -1;
			}
			return 1;
		}

		public int getMajor() {
			return major;
		}

		public int getMinor() {
			return minor;
		}

		public int getBuild() {
			return build;
		}
	}

}
