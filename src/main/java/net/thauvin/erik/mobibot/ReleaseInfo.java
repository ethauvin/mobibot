/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Sun Apr 20 23:26:28 PDT 2014 */
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


   /** buildDate (set during build process to 1398061588708L). */
   private static final Date buildDate = new Date(1398061588708L);

   /**
    * Get buildDate (set during build process to Sun Apr 20 23:26:28 PDT 2014).
    * @return Date buildDate
    */
   public static Date getBuildDate() { return buildDate; }


   /** project (set during build process to "mobibot"). */
   private static final String project = "mobibot";

   /**
    * Get project (set during build process to "mobibot").
    * @return String project
    */
   public static String getProject() { return project; }


   /** version (set during build process to "0.6"). */
   private static final String version = "0.6";

   /**
    * Get version (set during build process to "0.6").
    * @return String version
    */
   public static String getVersion() { return version; }


   /**
    * Get buildNumber (set during build process to 0).
    * @return int buildNumber
    */
   public static int getBuildNumber() { return 0; }

}
