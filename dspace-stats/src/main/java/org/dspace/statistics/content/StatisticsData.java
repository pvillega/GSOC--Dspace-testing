/**
 * $Id: StatisticsData.java 5244 2010-08-08 07:35:37Z stuartlewis $
 * $URL: http://scm.dspace.org/svn/repo/sandbox/gsoc/2010/testing/dspace-stats/src/main/java/org/dspace/statistics/content/StatisticsData.java $
 * *************************************************************************
 * Copyright (c) 2002-2009, DuraSpace.  All rights reserved
 * Licensed under the DuraSpace Foundation License.
 *
 * A copy of the DuraSpace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 */
package org.dspace.statistics.content;

import org.dspace.statistics.Dataset;
import org.dspace.statistics.content.filter.StatisticsFilter;
import org.dspace.core.Context;
import org.apache.solr.client.solrj.SolrServerException;

import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.io.IOException;
import java.text.ParseException;

/**
 * Abstract "factory" for statistical queries.
 * @author kevinvandevelde at atmire.com
 * Date: 23-feb-2009
 * Time: 12:37:04
 */
public abstract class StatisticsData {

    private Dataset dataset;
    private List<DatasetGenerator> datasetgenerators;

    private List<StatisticsFilter> filters;

    /** Construct a blank query factory. */
    protected StatisticsData() {
        datasetgenerators = new ArrayList<DatasetGenerator>(2);
        filters = new ArrayList<StatisticsFilter>();
    }

    /** Wrap an existing Dataset in an unconfigured query factory. */
    protected StatisticsData(Dataset dataset) {
        this.dataset = dataset;
        datasetgenerators = new ArrayList<DatasetGenerator>(2);
        filters = new ArrayList<StatisticsFilter>();
    }

    /** Augment the list of facets (generators). */
    public void addDatasetGenerator(DatasetGenerator set){
        datasetgenerators.add(set);
    }

    /** Augment the list of filters. */
    public void addFilters(StatisticsFilter filter){
        filters.add(filter);
    }

    /** Return the current list of generators. */
    public List<DatasetGenerator> getDatasetGenerators() {
        return datasetgenerators;
    }

    /** Return the current list of filters. */
    public List<StatisticsFilter> getFilters() {
        return filters;
    }

    /** Return the existing query result if there is one. */
    public Dataset getDataset() {
        return dataset;
    }

    /** Jam an existing query result in. */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /** Run the accumulated query and return its results. */
    public abstract Dataset createDataset(Context context) throws SQLException,
            SolrServerException, IOException, ParseException;

}
