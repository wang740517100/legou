package cn.wangkf.controller;

import cn.wangkf.common.vo.PageResult;
import cn.wangkf.service.SearchService;
import cn.wangkf.service.impl.SearchServiceImpl;
import cn.wangkf.bo.SearchRequest;
import cn.wangkf.pojo.Goods;
import cn.wangkf.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest searchRequest){
        SearchResult<Goods> result = searchService.search(searchRequest);
        if (result == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else {
            return ResponseEntity.ok(result);
        }
    }
}
