/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Sun Oct 31 00:44:26 PDT 2004 */
package net.thauvin.erik.mobibot;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class ReleaseInfo {


   /** buildDate (set during build process to 1099208666406L). */
   private static Date buildDate = new Date(1099208666406L);

   /**
    * Get buildDate (set during build process to Sun Oct 31 00:44:26 PDT 2004).
    * @return Date buildDate
    */
   public static final Date getBuildDate() { return buildDate; }


   /**
    * Get buildNumber (set during build process to 7).
    * @return int buildNumber
    */
   public static final int getBuildNumber() { return 7; }


   /** version (set during build process to "0.2"). */
   private static String version = new String("0.2");

   /**
    * Get version (set during build process to "0.2").
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
