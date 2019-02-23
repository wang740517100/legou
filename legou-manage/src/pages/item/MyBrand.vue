<template>
  <div>
    <v-layout class="px-3 pb-2">
      <v-flex xs2>
        <v-btn icon flat color="indigo" @click="addBrand">
          <v-icon drak>add</v-icon>
        </v-btn>
      </v-flex>
      <v-spacer/>
      <v-flex xs4>
        <v-text-field label="搜索" hide-details append-icon="search" v-model="key"></v-text-field>
      </v-flex>
    </v-layout>

    <v-data-table
      :headers="headers"
      :items="brands"
      :pagination.sync="pagination"
      :total-items="totalBrands"
      :loading="loading"
      class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.name }}</td>
        <td class="text-xs-center"><image :src="props.item.image"/></td>
        <td class="text-xs-center">{{ props.item.letter }}</td>
        <td class="text-xs-center">
          <v-btn flat icon color="info" @click="editBrand(props.item)">
            <v-icon>edit</v-icon>
          </v-btn>
          <v-btn flat icon color="red"  @click="deleteBrand(props.item)">
            <v-icon>delete</v-icon>
          </v-btn>
        </td>
      </template>
    </v-data-table>

    <!--v-model双向绑定-->
    <v-dialog v-model="show" scrollable persistent max-width="500px">
      <v-card  class="black--text">
        <v-card-title>
          <span class="headline">{{isEdit?'修改':'新增'}}品牌</span>
          <v-spacer></v-spacer>
          <v-btn icon @click="show = false">
            <v-icon>close</v-icon>
          </v-btn>
        </v-card-title>

        <!--表单-->
        <v-card-text class="px-5">
          <!--引入子组件-->
          <!--v-bind作用是对isEdit的vlue值的双向绑定-->
          <!--:oldBrand的作用是把data.brand赋值给oldBrand-->
          <my-brand-form  @reload="reload" v-bind:isEdit="isEdit" v-bind:oldBrand="oldBrand" ></my-brand-form>
        </v-card-text>

      </v-card>
    </v-dialog>

  </div>
</template>

<script>
  //引入弹窗组件
  import MyBrandForm from './MyBrandForm'

  export default {
    name: "MyBrand",
    data(){
      return {
        headers: [
          { text: '品牌id', align: 'center', sortable: true, value: 'id'},
          { text: '品牌名称', align: 'center', sortable: false, value: 'name'},
          { text: '品牌LOGO', align: 'center', sortable: false, value: 'image'},
          { text: '品牌首字母', align: 'center', sortable: true, value: 'letter'},
          { text: '操作', align: 'center', sortable: false, value: 'caozuo'}
        ],
        brands: [],
        pagination: {},
        totalBrands: 0,
        loading: false,
        key: '',

        show: false, //控制对话框的显示
        isEdit: false, //是否编辑
        oldBrand:{} //回显要修改的数据
      }
    },

    created(){
      //分页查询
      this.loadBrands();
    },

    watch: {
      //监控key(搜索条件)
      key(){
        this.getDataFromServer();
        //this.pagination.page = 1;
        //this.loadBrands();
      },
      //深度监控pagination
      pagination: {
        deep: true,
        handler(){
          this.getDataFromServer();
         // this.loadBrands();
        }
      }
    },

    methods: {
      //分页查询
      loadBrands(){
        // 开启进度条
        this.loading = true;
        this.$http.get('/item/brand/page', {
          params: {
            page: this.pagination.page,  //当前页
            rows: this.pagination.rowsPerPage, //每页条数
            sortBy: this.pagination.sortBy, //排序字段
            desc: this.pagination.descending, //是否降序
            key: this.key  //搜索条件
          }
        }).then(resp => {
          if (resp == null) {
            return ;
          }
          this.brands = resp.data.items;
          this.totalBrands = resp.data.total;
          //关闭进度条
          this.loading = false;
        });
      },

      //新增编辑弹窗
      addBrand(){
        this.isEdit = false;
        this.show = true;
        this.oldBrand = null;
      },

      //刷新页面
      getDataFromServer(){
        // 开启进度条
        this.loading = true;
        //发起ajax请求
        // 分页查询page,rows,key,sortBy,desc
        this.$http.get("/item/brand/page",{
          params:{
            page:this.pagination.page,
            rows:this.pagination.rowsPerPage,
            sortBy:this.pagination.sortBy,
            desc:this.pagination.descending,
            key:this.key
          }
        }).then(resp =>{
          //console.log(resp)
          this.brands=resp.data.items;
          this.totalBrands = resp.data.total;
          //关闭进度条
          this.loading = false;
        })
      },

      //新增或编辑后关闭弹窗重新加载页面
      reload(){
        //关闭弹窗
        this.show = false;
        //刷新页面
        this.getDataFromServer();
      },

      //删除品牌
      deleteBrand(item) {
        this.$message.confirm('此操作将永久删除该品牌, 是否继续?').then(() => {
          // 发起删除请求
          this.$http.delete("/item/brand/bid/" + item.id)
          .then(() => {
            // 删除成功，重新加载数据
            this.$message.success("删除成功！");
            this.getDataFromServer();
          })
        }).catch((e) => {
            this.$message.error("删除已取消！");
        });
      },

      //编辑品牌
      editBrand(oldBrand){
        //根据品牌信息查询商品分类
        this.$http.get("/item/category/bid/"+oldBrand.id).then(
          ({data}) => {
          this.isEdit=true;
          //显示弹窗
          this.show=true;
          //获取要编辑的brand
          this.oldBrand=oldBrand;
          this.oldBrand.categories = data;
        }).catch();
      },



    },

    //定义弹窗组件
    components:{
      MyBrandForm
    }

  }
</script>

<style scoped>

</style>
