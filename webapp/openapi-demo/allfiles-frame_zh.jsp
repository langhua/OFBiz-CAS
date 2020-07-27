<%@ page pageEncoding="utf-8" %>
<%
    String swaggerEditor = "swagger-editor-3.11.7";
    String swaggerUI = "swagger-ui-3.28.0";
%>
<html xmlns:lxslt="http://xml.apache.org/xslt">
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="/openapi-demo/<%= swaggerUI %>/dist/swagger-ui.css">
    <style>
      body
      {
        margin:0;
        background: #fafafa;
      }
      
      .swagger-ui .topbar .title {
        font-size: 1.5em;
        font-weight: 700;
        flex: 1;
        max-width: 300px;
        text-decoration: none;
        font-family: sans-serif;
        color: #fff;
        padding: 5px 0px 4px 10px;
      }
      
      a.demo-title {
        padding-left: 10px;
      }
    </style>
</head>
<body>
<div class="swagger-ui">
    <div class="topbar">
        <div class="title">开放接口列表</div>
    </div>
</div>
<br>
<p>
    <a href="index.html" class="demo-title" target="_parent">English</a>
    &nbsp;&nbsp;
    <a href="index_zh.html" class="demo-title" target="_parent">中文</a>
</p>
<br>
<p>
<table width="100%">
    <tr>
        <td nowrap><a target="fileFrame" class="demo-title"
                      href="/openapi-demo/<%= swaggerUI %>/dist/index.html?url=/openapi-demo/yaml/oauth2_zh.yaml">OAuth2开放接口</a>&nbsp;&nbsp;<a
                target="fileFrame"
                href="/openapi-demo/<%= swaggerEditor %>/index.html?url=/openapi-demo/yaml/oauth2_zh.yaml">
            <image style="" src="/openapi-demo/images/edit_20x20.png"/>
        </a></td>
    </tr>
    <tr>
        <td nowrap><a target="fileFrame" class="demo-title"
                      href="/openapi-demo/<%= swaggerUI %>/dist/index.html?url=/openapi-demo/yaml/demo_zh.yaml">开放接口演示</a>&nbsp;&nbsp;<a
                target="fileFrame"
                href="/openapi-demo/<%= swaggerEditor %>/index.html?url=/openapi-demo/yaml/demo_zh.yaml">
            <image style="" src="/openapi-demo/images/edit_20x20.png"/>
        </a></td>
    </tr>
</table>
</p>
</body>
</html>
