/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Sun Mar 06 08:26:51 PST 2005 */
package net.thauvin.erik.mobibot;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class ReleaseInfo {


   /** buildDate (set during build process to 1110126411140L). */
   private static Date buildDate = new Date(1110126411140L);

   /**
    * Get buildDate (set during build process to Sun Mar 06 08:26:51 PST 2005).
    * @return Date buildDate
    */
   public static final Date getBuildDate() { return buildDate; }


   /**
    * Get buildNumber (set during build process to 2).
    * @return int buildNumber
    */
   public static final int getBuildNumber() { return 2; }


   /** version (set during build process to "0.3"). */
   private static String version = new String("0.3");

   /**
    * Get version (set during build process to "0.3").
    * @return String version
    */
   public static final String getVersion() { return version; }


   /** project (set during build process to "mobibot"). */
   private static String project = new String("mobibot");

   /**
    * Get project (set during build process to "mobibot").
    * @return String project
    */
   public static final String getProject() { return project; }

}
