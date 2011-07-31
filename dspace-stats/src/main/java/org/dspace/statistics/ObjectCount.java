/**
 * $Id: ObjectCount.java 5244 2010-08-08 07:35:37Z stuartlewis $
 * $URL: http://scm.dspace.org/svn/repo/sandbox/gsoc/2010/testing/dspace-stats/src/main/java/org/dspace/statistics/ObjectCount.java $
 * *************************************************************************
 * Copyright (c) 2002-2009, DuraSpace.  All rights reserved
 * Licensed under the DuraSpace Foundation License.
 *
 * A copy of the DuraSpace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 */
package org.dspace.statistics;

/**
 * Data structure for returning results from statistics searches.
 * 
 * @author mdiggory at atmire.com
 * @author ben at atmire.com
 * @author kevinvandevelde at atmire.com
 */
public class ObjectCount {
    private long count;
    private String value;

    public ObjectCount(){
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
