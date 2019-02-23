package cn.wangkf.client;

import cn.wangkf.common.vo.PageResult;
import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.LgSearchService;
import cn.wangkf.pojo.Goods;
import cn.wangkf.repository.GoodsRepository;
import cn.wangkf.service.impl.SearchServiceImpl;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = LgSearchService.class)
public class ElasticsearchTest {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpuClient spuClient;

    @Autowired
    private SearchServiceImpl searchService;

    @Test
    public void createIndex(){
        // 创建索引
        this.elasticsearchTemplate.createIndex(Goods.class);
        // 配置映射
        this.elasticsearchTemplate.putMapping(Goods.class);
    }


    @Test
    public void loadData() throws IOException {
        int page = 1;
        int row = 200;

        //分页查询数据
        PageResult<SpuBo> result = this.goodsClient.querySpuByPage(page,row,null,true,null,true);
        List<SpuBo> spus = result.getItems();
        //创建Goods集合
        List<Goods> goodsList = new ArrayList<>();
        //遍历spu
        for (SpuBo spu : spus){
            try {
                Goods goods = this.searchService.buildGoods(spu);
                goodsList.add(goods);
            } catch (IOException e) {
                break;
            }
        }
        //把数据从数据库导入索引库
        this.goodsRepository.saveAll(goodsList);
    }


    @Test
    public void testError(){
        try {
            searchService.createIndex(48L);
        }catch (Exception e){

        }

    }



    @Test
    public void testAgg(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withQuery(QueryBuilders.termQuery("cid3",76)).withSourceFilter(new FetchSourceFilter(new String[]{""},null)).withPageable(PageRequest.of(0,1));
        Page<Goods> goodsPage = this.goodsRepository.search(queryBuilder.build());
        goodsPage.forEach(System.out::println);
    }

    @Test
    public void testList(){
        List<Integer> nums = Arrays.asList(1, 2, 3, 4);
        List<Integer> squareNums = nums.stream().
                map(n -> n * n).
                collect(Collectors.toList());
        System.out.println((nums.stream().map(n -> n*n)));
    }

    @Test
    public void testA(){
        List<Integer> list = Arrays.asList(1,2,3,4);
        List<Integer> result = new ArrayList<>();
        for (Integer i : list){
            for (Integer j : list){
                for (Integer k : list){
                    if (!i.equals(j) && !j.equals(k) && !i.equals(k)) {
                        result.add(i * 100 + j * 10 + k);
                    }
                }
            }
        }
        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        set.addAll(result);
        System.out.println("一共能组成："+set.size()+"个不重复的三位数");
        for (Integer s : set){
            System.out.println(s);
        }
    }

    public static void testC(int a,int b,int c){
        if (a>0&&b>0&&c>0&&a+b>c&&a+c>b&&b+c>a){
            System.out.println("三角形");
        }else{
            System.out.println("不能组成三角形");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("a=");
        int a =scanner.nextInt();
        System.out.print("b=");
        int b =scanner.nextInt();
        System.out.print("c=");
        int c =scanner.nextInt();
        testC(a,b,c);

    }
    static  void  SelectSort(int A[],int n){
        int min;
        for(int i=0;i<n-1;i++){
            min=i;
            for(int j=i+1;j<n;j++){
                if(A[j]<A[min]) min=j;
            }
            if(min!=i){
                int temp=A[min];
                A[min]=A[i];
                A[i]=temp;
            }
        }
    }

}
