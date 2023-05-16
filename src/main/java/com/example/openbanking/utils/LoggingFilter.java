package com.example.openbanking.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        //reuqest, response 받아옴
        chain.doFilter(requestWrapper, responseWrapper);

        //로그 입력
        log.info("\n" +
                        "url : {}\n" +
//              "Headers : {}\n" +
                        "Request : {}\n" +
                        "Response : {}\n",
                ((HttpServletRequest) request).getRequestURI(),
//              getHeaders((HttpServletRequest) request),
                getRequestBody(requestWrapper),
                getResponseBody(responseWrapper));
    }

    //request return
    private String getRequestBody(ContentCachingRequestWrapper request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                try {
                    return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    return " - ";
                }
            }else{
                Map<String, String[]> map = request.getParameterMap();
                Set<String> keySet=  map.keySet();
                Iterator<String> itr = keySet.iterator();
                String param = "{ \n";
                while(itr.hasNext()) {
                    String key = itr.next();
                    String[] values = map.get(key);
                    param +=  key + " : " + Arrays.toString(values) + "\n";
                }
                param += "}";
                return param;
            }
        }
        return " - ";
    }

    //response return
    private String getResponseBody(final HttpServletResponse response) throws IOException {
        String payload = null;
        ContentCachingResponseWrapper wrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                wrapper.copyBodyToResponse();
            }
        }

        if (null == payload) {
            return "-";
        }
        else {
            if (payload.contains("swagger")) {
                return " swagger load ";
            }
            else {
                return payload;
            }
        }
    }
}
