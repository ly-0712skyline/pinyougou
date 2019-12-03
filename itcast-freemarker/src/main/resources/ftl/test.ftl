<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>freemarker测试</title>
</head>
<body>
${name}:${message}<br>
<br>
<#-- 这是注释；在生成后的文件中是看不见的 -->
assign可以定义变量：<br>
<#assign linkman="黑马程序员"/>
${linkman}<br>
<#assign info={"mobile":"13400000000", "address":"吉山村"}>
手机号为：${info.mobile}---地址为：${info.address}
<br><hr><br>
include可以引入其它模版文件到当前模版文件中。<br>
<#include "header.ftl"/>

<br><hr><br>
if条件控制语句<br>
<#assign bool=true />
<#if bool>
    bool的值为true；
<#else>
    bool的值为false；
</#if>
<br><hr><br>
list 循环控制语句<br>
<#list goodsList as goods>
    ${goods_index} --- ${goods.name} --- ${goods.price}<br>
</#list>
集合大小为：${goodsList?size}
<br><hr><br>
内建函数测试：<br>
eval可以将字符串转换为对象:<br>
<#assign objStr='{"id":123,"name":"itcast"}'/>
<#assign obj=objStr?eval/>
${obj.id} --- ${obj.name}

<br><hr><br>
日期格式化：<br>
.now 表示当前日期时间：${.now}<br>
要获取日期：${today?date}<br>
要获取时间：${today?time}<br>
格式化显示：${today?string("yyyy年MM月dd日 HH:mm:ss ZZZZ")}
<br><hr><br>
默认情况下，如果数值超过千分位会出现, 如：${number}；数值转换为字符串${number?c}
<br><hr><br>
空值处理<br>
emp的值为空可以使用 ! 设置到变量后面，表示值为空或没有定义的话则什么都不显示；emp=${emp!}；
如果是空或者没有定义的时候需要显示默认的值，则可以在!"默认值"。 emp=${emp!"emp的值为空，这是默认值"}
<br><hr><br>
??? 前面两个??表示变量是否存在，如果存在则返回true，否则返回false，第3个?表示函数的调用<br>

<#assign bool2=false>

${bool2???string}<br>

<#if str2??>
    str2 存在
<#else >
    str2 不存在
</#if>

<br>
<br>
<br>
<br>
<br>
<br>
<br>
<br>
<br>
<br>
</body>
</html>