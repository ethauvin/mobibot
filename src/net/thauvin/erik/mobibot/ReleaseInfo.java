/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Thu Oct 11 18:06:05 PDT 2007 */
package net.thauvin.erik.mobibot;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 * 
 * @author JReleaseInfo AntTask
 */
public class ReleaseInfo {

   /**
    * Disables the default constructor.
    * @throws UnsupportedOperationException if the constructor is called.
   */
   private ReleaseInfo() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("Illegal constructor call.");
   }


   /** buildDate (set during build process to 1192151165055L). */
   private static final Date buildDate = new Date(1192151165055L);

   /**
    * Get buildDate (set during build process to Thu Oct 11 18:06:05 PDT 2007).
    * @return Date buildDate
    */
   public static Date getBuildDate() { return buildDate; }


   /**
    * Get buildNumber (set during build process to 73).
    * @return int buildNumber
    */
   public static int getBuildNumber() { return 73; }


   /** project (set during build process to "mobibot"). */
   private static final String project = "mobibot";

   /**
    * Get project (set during build process to "mobibot").
    * @return String project
    */
   public static String getProject() { return project; }


   /** version (set during build process to "0.3"). */
   private static final String version = "0.3";

   /**
    * Get version (set during build process to "0.3").
    * @return String version
    */
   public static String getVersion() { return version; }

}
