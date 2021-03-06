/*
 * Copyright 2012 Rui Araújo, Luís Fonseca
 *
 * This file is part of Router Keygen.
 *
 * Router Keygen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Router Keygen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Router Keygen.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.exobel.routerkeygen.algorithms;

import android.os.Parcel;
import android.os.Parcelable;

import org.exobel.routerkeygen.R;

import java.util.List;
import java.util.Locale;

/*
 * This is not actual an algorithm as
 * it is just a default WEP password
 * There is a second case where the password is just the MAC address
 */
public class ConnKeygen extends Keygen {

    public static final Parcelable.Creator<ConnKeygen> CREATOR = new Parcelable.Creator<ConnKeygen>() {
        public ConnKeygen createFromParcel(Parcel in) {
            return new ConnKeygen(in);
        }

        public ConnKeygen[] newArray(int size) {
            return new ConnKeygen[size];
        }
    };

    public ConnKeygen(String ssid, String mac) {
        super(ssid, mac);
    }

    private ConnKeygen(Parcel in) {
        super(in);
    }

    @Override
    public int getSupportState() {
        final String ssid = getSsidName();
        if (ssid.matches("OTE[0-9a-fA-F]{6}")) {
            return SUPPORTED;
        }
        if (ssid.matches("conn-x[0-9a-fA-F]{6}")) {
            final String mac = getMacAddress();
            if (mac.length() == 12) {
                final String macShort = mac.replace(":", "");
                final String ssidSubpart = ssid.substring(ssid.length() - 6);
                if (macShort.equalsIgnoreCase(ssidSubpart))
                    return SUPPORTED;
                else
                    return UNLIKELY_SUPPORTED;
            } else
                return UNSUPPORTED; // Should not happen because WireMatcher
            // filter thiss
        }
        return SUPPORTED;
    }

    @Override
    public List<String> getKeys() {
        final String mac = getMacAddress();
        if (mac.length() == 12) {
            addPassword(getMacAddress().toLowerCase(Locale.getDefault()));
        }
        addPassword("1234567890123");
        return getResults();
    }
}
