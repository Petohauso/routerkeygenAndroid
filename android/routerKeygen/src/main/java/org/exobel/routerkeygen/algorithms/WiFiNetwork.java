package org.exobel.routerkeygen.algorithms;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import org.exobel.routerkeygen.WirelessMatcher;

import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipInputStream;

public class WiFiNetwork implements Comparable<WiFiNetwork>, Parcelable {

	// Constants used for different security types
	private static final String PSK = "PSK";
	private static final String WEP = "WEP";
	private static final String EAP = "EAP";
	private static final String OPEN = "Open";
	public static final Parcelable.Creator<WiFiNetwork> CREATOR = new Parcelable.Creator<WiFiNetwork>() {

		public WiFiNetwork[] newArray(int size) {
			return new WiFiNetwork[size];
		}

		@Override
		public WiFiNetwork createFromParcel(Parcel source) {
			return new WiFiNetwork(source);
		}
	};
	final private String ssidName;
	final private String macAddress;
	final private int level;
	final private int frequency;
	final private String encryption;
	private ScanResult scanResult;
	private ArrayList<Keygen> keygens = new ArrayList<>();

	public WiFiNetwork(ScanResult scanResult, ZipInputStream magicInfo) {
		this(scanResult.SSID, scanResult.BSSID,
				WifiManager.calculateSignalLevel(scanResult.level, 4),
				scanResult.frequency, scanResult.capabilities, magicInfo);
        this.scanResult = scanResult;
	}

	public WiFiNetwork(final String ssid, final String mac, int level,
					   String enc, ZipInputStream magicInfo){
		this(ssid, mac, level, 0, enc, magicInfo);
	}

	public WiFiNetwork(final String ssid, final String mac, int level, int frequency,
					   String enc, ZipInputStream magicInfo) {
		this.ssidName = ssid;
		this.macAddress = mac.toUpperCase(Locale.getDefault());
		this.level = level;
		this.frequency = frequency;
		this.encryption = enc;
		this.scanResult = null;
		this.keygens = WirelessMatcher.getKeygen(ssidName, macAddress, frequency,
				magicInfo);
	}

	private WiFiNetwork(Parcel in) {
		ssidName = in.readString();
		if (in.readInt() == 1)
			macAddress = in.readString();
		else
			macAddress = "";
		if (in.readInt() == 1)
			encryption = in.readString();
		else
			encryption = OPEN;
		level = in.readInt();
		frequency = in.readInt();
		keygens = new ArrayList<>();
		in.readList(keygens, Keygen.class.getClassLoader());
		if (in.readInt() == 1)
			scanResult = in.readParcelable(ScanResult.class.getClassLoader());
		else
			scanResult = null;
	}

	/**
	 * @return The security of a given {@link ScanResult}.
	 */
	private static String getScanResultSecurity(WiFiNetwork scanResult) {
		final String cap = scanResult.encryption;
		final String[] securityModes = {WEP, PSK, EAP};
		for (int i = securityModes.length - 1; i >= 0; i--) {
			if (cap.contains(securityModes[i])) {
				return securityModes[i];
			}
		}

		return OPEN;
	}

	public String getSsidName() {
		return ssidName;
	}

	public int getFrequency() {
		return frequency;
	}

	public int getLevel() {
		return level;
	}

	public String getEncryption() {
		return encryption;
	}

	public int getSupportState() {
		if (keygens.isEmpty())
			return Keygen.UNSUPPORTED;
		for (Keygen k : keygens) {
			//Getting some crash reports with k==null
			//very odd.
			if (k != null && k.getSupportState() == Keygen.SUPPORTED)
				return Keygen.SUPPORTED;
		}
		// If there is a supported keygen then it is already supported
		return Keygen.UNLIKELY_SUPPORTED;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public int compareTo(@NonNull WiFiNetwork another) {
		if (getSupportState() == another.getSupportState()) {
			if (another.level == this.level)
				return ssidName.compareTo(another.ssidName);
			else
				return another.level - level;
		} else
			return another.getSupportState() - getSupportState();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(ssidName);
		dest.writeInt(macAddress != null ? 1 : 0);
		if (macAddress != null)
			dest.writeString(macAddress);
		dest.writeInt(encryption != null ? 1 : 0);
		if (encryption != null)
			dest.writeString(encryption);
		dest.writeInt(level);
		dest.writeInt(frequency);
		dest.writeList(keygens);
		dest.writeInt(scanResult != null ? 1 : 0);
		if (scanResult != null)
			dest.writeParcelable(scanResult, flags);
	}

	public boolean isLocked() {
		return !OPEN.equals(getScanResultSecurity(this));
	}

	public ScanResult getScanResult() {
		return scanResult;
	}

    public ArrayList<Keygen> getKeygens() {
        return keygens;
    }

	public void setKeygens(ZipInputStream magicInfo) {
		this.keygens = WirelessMatcher.getKeygen(ssidName, macAddress, frequency,
				magicInfo);
	}
}
