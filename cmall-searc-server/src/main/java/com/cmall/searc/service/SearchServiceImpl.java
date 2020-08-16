package com.cmall.searc.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.cmall.bean.PmsSearchParms;
import com.cmall.bean.PmsSearchSkuInfo;
import com.cmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;


    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParms pmsSearchParms) {

        String delStr=getSearchDsl(pmsSearchParms);

        System.err.println(delStr);

        //用api执行复杂查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();


        Search search = new Search.Builder(
                delStr).addIndex("cmall").addType("PmsSkuInfo").build();

        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;

           Map<String,List<String>> highlight=hit.highlight;
           if(highlight!=null) {
               String skuName = highlight.get("skuName").get(0);
               source.setSkuName(skuName);
           }
            pmsSearchSkuInfos.add(source);
        }

        System.out.println(pmsSearchSkuInfos.size());


        return pmsSearchSkuInfos;
    }

    private String getSearchDsl(PmsSearchParms pmsSearchParms) {

        String[] skuAttrValues = pmsSearchParms.getValueId();
        String keyword = pmsSearchParms.getKeyword();
        String catalog3Id = pmsSearchParms.getCatalog3Id();

        //jest的del工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //filter

        if (StringUtils.isNotBlank(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);

        }

        if (skuAttrValues != null) {
            for (String skuAttrValue : skuAttrValues) {
                //filter
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", skuAttrValue);
                boolQueryBuilder.filter(termQueryBuilder);

            }
        }


       /* TermQueryBuilder termQueryBuilder1=new TermQueryBuilder("","");
        boolQueryBuilder.filter(termQueryBuilder1);

        TermsQueryBuilder termsQueryBuilder=new TermsQueryBuilder("","","","");
        boolQueryBuilder.filter(termsQueryBuilder);*/


        if (StringUtils.isNoneBlank(keyword)) {
            //must
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);

        }

        //query
        searchSourceBuilder.query(boolQueryBuilder);
        //from
        searchSourceBuilder.from(0);
        //size
        searchSourceBuilder.size(20);
        //highlight
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;'>");
        highlightBuilder.field("skuName");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);


        //sort
        searchSourceBuilder.sort("id", SortOrder.DESC);


        //aggs

        TermsAggregationBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);


        return searchSourceBuilder.toString();
    }
}
