package com.tbocek.android.combatmap;

import android.os.StrictMode;

public final class DeveloperMode {
	public static final boolean DEVELOPER_MODE = true;

	public static void strictMode() {
    	if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }
	}
}
