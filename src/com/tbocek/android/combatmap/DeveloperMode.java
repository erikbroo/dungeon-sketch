package com.tbocek.android.combatmap;

import android.os.StrictMode;

/**
 * Central location to enable debug options.
 * @author Tim
 *
 */
public final class DeveloperMode {

	/**
	 * Whether developer/debug mode is enabled.
	 */
	public static final boolean DEVELOPER_MODE = true;

	/**
	 * If in developer mode and the SDK supports it, run in strict mode.
	 */
	public static void strictMode() {
    	if (android.os.Build.VERSION.SDK_INT
    			>= android.os.Build.VERSION_CODES.GINGERBREAD
    			&& DEVELOPER_MODE) {
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
	
	/**
	 * Private constructor because this is a utility class.
	 */
	private DeveloperMode() { }
}
