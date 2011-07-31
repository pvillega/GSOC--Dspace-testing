/**
 * $Id: StatisticsFilter.java 5244 2010-08-08 07:35:37Z stuartlewis $
 * $URL: http://scm.dspace.org/svn/repo/sandbox/gsoc/2010/testing/dspace-stats/src/main/java/org/dspace/statistics/content/filter/StatisticsFilter.java $
 * *************************************************************************
 * Copyright (c) 2002-2009, DuraSpace.  All rights reserved
 * Licensed under the DuraSpace Foundation License.
 *
 * A copy of the DuraSpace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 */
package org.dspace.statistics.content.filter;

/**
 * A wrapper for some kind of Solr filter expression.
 * @author kevinvandevelde at atmire.com
 * Date: 12-mrt-2009
 * Time: 10:36:03
 */
public interface StatisticsFilter {

    /** Convert this filter's configuration to a query string fragment. */
    public String toQuery();
}
