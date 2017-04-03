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
    private final static String buildmeta = "014";
    private final static LocalDateTime date =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(1491184254508L), ZoneId.systemDefault());
    private final static int major = 0;
    private final static int minor = 7;
    private final static int patch = 0;
    private final static String prerelease = "beta";
    private final static String project = "mobibot";

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
     * Returns the build date.
     *
     * @return The build date.
     */
    public static LocalDateTime getBuildDate() {
        return date;
    }

    /**
     * Returns the project name.
     *
     * @return The project name, if any.
     */
    public static String getProject() {
        return project;
    }

    /**
     * Returns the full version string.
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
     *
     * @return The version string.
     */
    public static String getVersion() {
        return Integer.toString(getMajor()) + '.'
                + Integer.toString(getMinor()) + '.'
                + Integer.toString(getPatch())
                + getPreRelease(true) + getBuildMetadata(true);
    }

    /**
     * Returns the major version.
     *
     * @return The major version.
     */
    public static int getMajor() {
        return major;
    }

    /**
     * Returns the minor version.
     *
     * @return The minor version.
     */
    public static int getMinor() {
        return minor;
    }

    /**
     * Returns the patch version.
     *
     * @return The patch version.
     */
    public static int getPatch() {
        return patch;
    }

    /**
     * Returns the pre-release version.
     *
     * @param isHyphen Prepend a hyphen, if <code>true</code>.
     * @return The pre-release version, if any.
     */
    public static String getPreRelease(final boolean isHyphen) {
        if (prerelease.length() > 0) {
            if (isHyphen) {
                return '-' + prerelease;
            } else {
                return prerelease;
            }
        }

        return "";
    }

    /**
     * Returns the pre-release version.
     *
     * @return The pre-release version, if any.
     */
    public static String getPreRelease() {
        return getPreRelease(false);
    }

    /**
     * Returns the build metadata.
     *
     * @param isPlus Prepend a plus sign, if <code>true</code>.
     * @return The build metadata, if any.
     */
    public static String getBuildMetadata(final boolean isPlus) {
        if (buildmeta.length() > 0) {
            if (isPlus) {
                return '+' + buildmeta;
            } else {
                return buildmeta;
            }
        }

        return "";
    }

    /**
     * Returns the build metadata.
     *
     * @return The build metadata, if any.
     */
    public static String getBuildMetadata() {
        return getBuildMetadata(false);
    }
}