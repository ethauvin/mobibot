/*
 * This file is automatically generated.
 * Do not modify! -- ALL CHANGES WILL BE ERASED!
 */
package net.thauvin.erik.mobibot;

import java.time.*;

/**
 * Provides semantic version information.
 *
 * @author <a href="https://github.com/ethauvin/semver">Semantic Version Annotation Processor</a>
 */
public final class ReleaseInfo {
    public final static String PROJECT = "mobibot";
    public final static LocalDateTime BUILDDATE =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(1554878934411L), ZoneId.systemDefault());
    public final static int MAJOR = 0;
    public final static int MINOR = 7;
    public final static int PATCH = 3;
    public final static String PRERELEASE = "beta";
    public final static String BUILDMETA = "260";

    /**
     * The full semantic version string.
     */
    public final static String VERSION = Integer.toString(MAJOR) + '.'
            + Integer.toString(MINOR) + '.'
            + Integer.toString(PATCH)
            + ((!PRERELEASE.isEmpty()) ? "-" + PRERELEASE : "")
            + ((!BUILDMETA.isEmpty()) ? "+" + BUILDMETA : "");

    /**
     * Disables the default constructor.
     */
    private ReleaseInfo() {
        throw new UnsupportedOperationException("Illegal constructor call.");
    }
}
