/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Fri Jun 29 06:58:32 PDT 2012 */
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


   /** buildDate (set during build process to 1340978312640L). */
   private static final Date buildDate = new Date(1340978312640L);

   /**
    * Get buildDate (set during build process to Fri Jun 29 06:58:32 PDT 2012).
    * @return Date buildDate
    */
   public static Date getBuildDate() { return buildDate; }


   /**
    * Get buildNumber (set during build process to 3).
    * @return int buildNumber
    */
   public static int getBuildNumber() { return 3; }


   /** project (set during build process to "mobibot"). */
   private static final String project = "mobibot";

   /**
    * Get project (set during build process to "mobibot").
    * @return String project
    */
   public static String getProject() { return project; }


   /** version (set during build process to "0.5"). */
   private static final String version = "0.5";

   /**
    * Get version (set during build process to "0.5").
    * @return String version
    */
   public static String getVersion() { return version; }

}
