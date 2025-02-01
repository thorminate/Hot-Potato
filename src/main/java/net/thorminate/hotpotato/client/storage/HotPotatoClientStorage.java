package net.thorminate.hotpotato.client.storage;

/**
 * Storage for the current hot potato on the client, uses simple static variables stored in memory.
 * The setters are just for syncing up with the server, and they shouldn't be changed by the client itself.
 */
public class HotPotatoClientStorage {
    private static int countdown;

    /**
     * Gets the current countdown seconds stored in client memory.
     * @return The countdown in seconds, negative numbers are considered null.
     */
    public static int getCountdown() {
        return countdown;
    }

    /**
     * Sets the current countdown seconds stored in client memory. This is used for syncing up with the server and should not be used otherwise.
     * @param countdown The countdown in seconds to override the current countdown, will be considered as null if negative.
     */
    public static void setCountdown(int countdown) {
        HotPotatoClientStorage.countdown = countdown;
    }
}
