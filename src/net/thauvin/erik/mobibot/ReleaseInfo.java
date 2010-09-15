/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Tue Sep 14 16:51:11 PDT 2010 */
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


   /** buildDate (set during build process to 1284508271605L). */
   private static final Date buildDate = new Date(1284508271605L);

   /**
    * Get buildDate (set during build process to Tue Sep 14 16:51:11 PDT 2010).
    * @return Date buildDate
    */
   public static Date getBuildDate() { return buildDate; }


   /**
    * Get buildNumber (set during build process to 8).
    * @return int buildNumber
    */
   public static int getBuildNumber() { return 8; }


   /** project (set during build process to "mobibot"). */
   private static final String project = "mobibot";

   /**
    * Get project (set during build process to "mobibot").
    * @return String project
    */
   public static String getProject() { return project; }


   /** version (set during build process to "0.4"). */
   private static final String version = "0.4";

   /**
    * Get version (set during build process to "0.4").
    * @return String version
    */
   public static String getVersion() { return version; }

}
