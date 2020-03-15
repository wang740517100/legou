# legou 这是一个用来串联各种技术方案和架构的 demo 项目

- 后台管理：

  - 后台系统核心架构：spring cloud 
	- 配置中心: apollo 统一配置中心
	- 注册中心：采用 eureka 注册中，可搭配 swagger 使用
        - 网关服务：nginx 做网关，zuul做路由
        - 信息服务：发送短信和邮件
	- 文件服务：fastdfs，分布式中小文件服务
	- 权限管理：单点登录方案，采用JWT + RSA 分布式鉴权，对用户及API进行权限控制
	- 商品查询：基于 elasticsearch 高并发大数据的查询解决方案
	- 商品详情页加载：OpenResty （nginx + lua） 静态化 + 缓存架构
	- 缓存服务：nginx （分发层 + 应用层）缓存 + redis cluster 架构 + JVM 堆缓存架构
	- 下单服务：秒杀系统架构
	- 业务信息：商品管理、用户管理、销售管理
	
	


- 前台门户
  - 功能详细：vue + element ui + Vuetify
	- 搜索商品、商品详情
    - 加入购物车
    - 下单和秒杀
  
 
