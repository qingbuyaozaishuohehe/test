package com.xu.es.demo.controller;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * es测试连接
 *
 * @author xuzhenqin
 * @date 2019/2/25
 */
@RestController
public class EsClientController {

    @Autowired
    private TransportClient client;

    /**
     * 根据id查询es信息
     * @param id
     * @return
     */
    @GetMapping("/get/book/novel")
    @ResponseBody
    public ResponseEntity get(@RequestParam(name = "id",defaultValue = "")String id){
        if (id.isEmpty()){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        GetResponse result = this.client.prepareGet("book","novel",id)
                .get();
        if (!result.isExists()){
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(result.getSource(), HttpStatus.OK);
    }

    /**
     * 插入es信息
     * @param title
     * @param author
     * @param wordCount
     * @param publicDate
     * @return
     */
    @PostMapping("add/book/novel")
    @ResponseBody
    public ResponseEntity add(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "author") String author,
            @RequestParam(name = "word_count") String wordCount,
            @RequestParam(name = "public_date")
                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date publicDate) {
        try {
            XContentBuilder content = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("title",title)
                    .field("author",author)
                    .field("word_count",wordCount)
                    .field("public_date",publicDate.getTime())
                    .endObject();

            IndexResponse result = this.client.prepareIndex("book","novel")
                    .setSource(content)
                    .get();

            return new ResponseEntity(result.getId(),HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 删除es信息
     * @param id
     * @return
     */
    @DeleteMapping("delete/book/novel")
    @ResponseBody
    public ResponseEntity delete(@RequestParam(name = "id")String id){
        DeleteResponse result = this.client.prepareDelete("book","novel",id)
                .get();
        return new ResponseEntity(result.getResult().toString(),HttpStatus.OK);
    }

    /**
     * 修改es数据
     * @param id
     * @param title
     * @param author
     * @return
     */
    @PutMapping("update/book/novel")
    @ResponseBody
    public ResponseEntity update(
            @RequestParam(name = "id")String id,
            @RequestParam(name = "title",required = false) String title,
            @RequestParam(name = "author",required = false)String author
    ){
        UpdateRequest update = new UpdateRequest("book","novel",id);
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject();
            if (title != null){
                builder.field("title",title);
            }
            if (author != null){
                builder.field("author",author);
            }
            builder.endObject();
            update.doc(builder);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            UpdateResponse result = this.client.update(update).get();
            return new ResponseEntity(result,HttpStatus.OK);
        }  catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/")
    public String index(){
        return "index";
    }
}
