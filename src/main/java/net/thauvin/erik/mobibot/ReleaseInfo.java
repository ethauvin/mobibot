/*
 * @(#)ReleaseInfo.java
 *
 * Copyright (c) 2004-2014, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/* Created by JReleaseInfo AntTask from Open Source Competence Group */
/* Creation date Fri Apr 25 18:08:16 PDT 2014 */
package net.thauvin.erik.mobibot;

import java.util.Date;

/**
 * This class provides information gathered from the build environment.
 *
 * @author JReleaseInfo AntTask
 */
public class ReleaseInfo
{

	/**
	 * buildDate (set during build process to 1398474496363L).
	 */
	private static final Date buildDate = new Date(1398474496363L);

	/**
	 * project (set during build process to "mobibot").
	 */
	private static final String project = "mobibot";

	/**
	 * version (set during build process to "0.6").
	 */
	private static final String version = "0.6";

	/**
	 * Disables the default constructor.
	 *
	 * @throws UnsupportedOperationException if the constructor is called.
	 */
	private ReleaseInfo()
			throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("Illegal constructor call.");
	}

	/**
	 * Get buildDate (set during build process to Fri Apr 25 18:08:16 PDT 2014).
	 *
	 * @return Date buildDate
	 */
	public static Date getBuildDate()
	{
		return buildDate;
	}

	/**
	 * Get project (set during build process to "mobibot").
	 *
	 * @return String project
	 */
	public static String getProject()
	{
		return project;
	}

	/**
	 * Get version (set during build process to "0.6").
	 *
	 * @return String version
	 */
	public static String getVersion()
	{
		return version;
	}

	/**
	 * Get buildNumber (set during build process to 0).
	 *
	 * @return int buildNumber
	 */
	public static int getBuildNumber()
	{
		return 0;
	}

}
