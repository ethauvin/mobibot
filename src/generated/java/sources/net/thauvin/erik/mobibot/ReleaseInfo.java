/*
 * This file is automatically generated.
 * Do not modify! -- ALL CHANGES WILL BE ERASED!
 */
package net.thauvin.erik.mobibot;

import java.time.*;

/**
 * Provides semantic version information.
 *
 * @author <a href="https://github.com/ethauvin/semver">Semantic Version
 *         Annotation Processor</a>
 */
public final class ReleaseInfo {
    public final static String PRERELEASE_PREFIX = "-";
    public final static String BUILDMETA_PREFIX = "+";

    public final static String PROJECT = "mobibot";
    public final static LocalDateTime BUILDDATE =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(1493616818868L), ZoneId.systemDefault());
    public final static int MAJOR = 0;
    public final static int MINOR = 7;
    public final static int PATCH = 1;
    public final static String PRERELEASE = "beta";
    public final static String BUILDMETA = "018";

   /**
     * The full version string.
     * <p>
     * Formatted as:
     * <blockquote>
     * <code>MAJOR.MINOR.PATCH[-PRERELEASE][+BUILDMETADATA]</code>
     * </blockquote>
     * <p>
     * For example:
     * <ul>
     * <li><code>1.0.0</code></li>
     * <li><code>1.0.0-beta</code></li>
     * <li><code>1.0.0+20160124144700</code></li>
     * <li><code>1.0.0-alpha+001</code></li>
     * </ul>
     */
    public final static String VERSION = Integer.toString(MAJOR) + '.'
            + Integer.toString(MINOR) + '.'
            + Integer.toString(PATCH)
            + preReleaseWithPrefix() + buildMetaWithPrefix();

    /**
     * Disables the default constructor.
     *
     * @throws UnsupportedOperationException If the constructor is called.
     */
    private ReleaseInfo()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Illegal constructor call.");
    }

    /**
     * Returns the build metadata with {@value #BUILDMETA_PREFIX} prefix.
     *
     * @return The build metadata, if any.
     */
    public static String buildMetaWithPrefix() {
        return buildMetaWithPrefix(BUILDMETA_PREFIX);
    }

    /**
     * Returns the build metadata.
     *
     * @param prefix Prefix to prepend.
     * @return The build metadata, if any.
     */
    public static String buildMetaWithPrefix(final String prefix) {
        if (BUILDMETA.length() > 0 && prefix.length() > 0) {
            return prefix + BUILDMETA;
        } else {
            return BUILDMETA;
        }
    }

    /**
     * Returns the pre-release version with {@value #PRERELEASE_PREFIX} prefix.
     *
     * @return The pre-release version, if any.
     */
    public static String preReleaseWithPrefix() {
        return preReleaseWithPrefix(PRERELEASE_PREFIX);
    }

    /**
     * Returns the pre-release version.
     *
     * @param prefix The prefix to prepend.
     * @return The pre-release version, if any.
     */
    public static String preReleaseWithPrefix(final String prefix) {
        if (PRERELEASE.length() > 0 && prefix.length() > 0) {
            return prefix + PRERELEASE;
        } else {
            return PRERELEASE;
        }
    }
}