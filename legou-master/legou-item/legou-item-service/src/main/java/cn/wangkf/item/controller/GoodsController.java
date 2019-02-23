package cn.wangkf.item.controller;

import cn.wangkf.common.vo.PageResult;
import cn.wangkf.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cn.wangkf.item.bo.*;
import cn.wangkf.common.parameter.SpuQueryByPageParameter;
import org.springframework.http.HttpStatus;
import cn.wangkf.item.pojo.SpuDetail;
import cn.wangkf.item.pojo.Sku;

import java.util.List;

@RestController
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable",defaultValue = "true") Boolean saleable){
        SpuQueryByPageParameter spuQueryByPageParameter = new SpuQueryByPageParameter(page,rows,sortBy,desc,key,saleable);
        //分页查询spu信息
        PageResult<SpuBo> result = this.goodsService.queryGoodsWithPage(spuQueryByPageParameter);
        return ResponseEntity.ok(result);
    }

    /**
     * 保存商品
     * @param spu
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spu){
        this.goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuBo> queryGoodsById(@PathVariable("id") Long id){
        SpuBo spuBo = this.goodsService.queryGoodsById(id);
        if (spuBo == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(spuBo);
    }

    /**
     * 修改商品
     * @param spuBo
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * 删除商品
     * @param ids
     * @return
     */
    @DeleteMapping("/spu/{id}")
    public ResponseEntity<Void> deleteGoods(@PathVariable("id") String ids){
        String separator="-";
        if (ids.contains(separator)){
            String[] goodsId = ids.split(separator);
            for (String id:goodsId){
                this.goodsService.deleteGoods(Long.parseLong(id));
            }
        }
        else {
            this.goodsService.deleteGoods(Long.parseLong(ids));
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 根据Spu的id查询其下所有的sku
     * @param id
     * @return
     */
    @GetMapping("/sku/list/{id}")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@PathVariable("id") Long id){
        List<Sku> list = this.goodsService.querySkuBySpuId(id);
        return ResponseEntity.ok(list);
        //没有不能报错
        /*if (list == null || list.size() < 1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else {
            return ResponseEntity.ok(list);
        }*/
    }

    /**
     * 根据spu商品id查询详情
     * @param id
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("id") Long id){
        SpuDetail spuDetail = this.goodsService.querySpuDetailBySpuId(id);
        if (spuDetail == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else {
            return ResponseEntity.ok(spuDetail);
        }
    }



}
