package com.sylinx.service;

import com.alibaba.fastjson.JSON;
import com.sylinx.pojo.Content;
import com.sylinx.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public static void main(String []args) throws IOException {
        new ContentService().parseContent("java");
    }
    public Boolean parseContent(String keywords) throws IOException {
        ArrayList<Content> contents = new HtmlParseUtil().parseJD(keywords);
        BulkRequest bulkRequest = new BulkRequest() ;
        bulkRequest.timeout("3m");

        for(int i=0;i<contents.size();i++){
            bulkRequest.add(new IndexRequest("jd_goods").source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }

    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo <=1) {
            pageNo =1;
        }
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
        for (SearchHit documentFields :response.getHits().getHits()){
            arrayList.add(documentFields.getSourceAsMap());
        }
        return arrayList;
    }


    public List<Map<String, Object>> searchHighlightPage(String keyword, int pageNo, int pageSize) throws IOException {
        if(pageNo <=1) {
            pageNo =1;
        }
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red;'>");
        highlightBuilder.postTags("</span>");
        highlightBuilder.requireFieldMatch(false);
        sourceBuilder.highlighter(highlightBuilder);

        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
        for (SearchHit documentFields :response.getHits().getHits()){
            Map<String, HighlightField> highlightFieldsMap = documentFields.getHighlightFields();
            HighlightField title = highlightFieldsMap.get("title");
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();

            if (title != null){
               Text[] fragments =  title.fragments();
               String n_title="";
               for (Text text : fragments){
                   n_title += text;
               }
               sourceAsMap.put("title", n_title);
            }
            arrayList.add(sourceAsMap);
        }
        return arrayList;
    }
}
