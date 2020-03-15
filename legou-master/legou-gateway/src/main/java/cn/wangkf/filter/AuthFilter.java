package cn.wangkf.filter;

import cn.wangkf.common.utils.CookieUtils;
import cn.wangkf.auth.utils.JwtUtils;
import cn.wangkf.config.FilterProperties;
import cn.wangkf.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by wangk on 2018-12-23.
 */
@Component //注入到spring容器
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        //pre 请求在路由之前执行
        //routing 在路由请求时调用
        //post 在routing和error过滤器之后调用
        //error 处理请求时发生错误调用
        return FilterConstants.PRE_TYPE; //pre 请求在路由之前执行
    }

    @Override
    public int filterOrder() {
        //过滤器执行级别顺序 在请求头之前
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        //1.获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //2.获取request
        HttpServletRequest request = context.getRequest();
        //3.获取路径
        String requestUri = request.getRequestURI();
        //4.判断白名单
        return !isAllowPath(requestUri);
    }

    private boolean isAllowPath(String requestUri) {
        //1.定义一个标记
        boolean flag = false;

        //2.遍历允许访问的路径
        for (String path : filterProperties.getAllowPaths()){
            if (requestUri.startsWith(path)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public Object run() throws ZuulException {
        //1.获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //2.获取request
        HttpServletRequest request = context.getRequest();
        //3.获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        //4.校验
        try{
            //4.1 校验通过，放行
            JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //4.2 校验权限 TODO.......

        }catch (Exception e){
            //4.2 校验不通过，返回403
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
        }
        return null;
    }
}
