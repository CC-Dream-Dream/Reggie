# 概述
## 功能架构图
![在这里插入图片描述](https://img-blog.csdnimg.cn/00f39dbdd7d24d88b56c10eabeecdd7d.png)

# 数据库建库建表
## 表说明
![在这里插入图片描述](https://img-blog.csdnimg.cn/ee424986569f45b6a071f1556bfe10b8.png)
# 开发环境
## Maven搭建
直接创建新工程
继承父工程的形式来做这个，这里新建父工程
![在这里插入图片描述](https://img-blog.csdnimg.cn/6e78719f1b0e465e816e50584a93d39c.png)
pom文件

```yml
server:
  port: 9001
spring:
  application:
    name: ccTakeOut
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ruiji?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
      username: root
      password: 333

  redis:
    host: localhost # 本地IP 或是 虚拟机IP
    port: 6379
    #    password: root
    database: 0  # 默认使用 0号db
  cache:
    redis:
      time-to-live: 1800000  # 设置缓存数据的过期时间，30分钟

mybatis-plus:
  configuration:
    #在映射实体或者属性时，将数据库中表名和字段名中的下划线去掉，开启按照驼峰命名法映射
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID

```
## 启动测试
创建测试类并启动
![在这里插入图片描述](https://img-blog.csdnimg.cn/8f3df341f8ed469a96de5831708a5fc5.png)
## 导入前端页面
![在这里插入图片描述](https://img-blog.csdnimg.cn/d8af2e9f8b1f4c52b8b881801c5d8c3b.png)

### 导入
在默认页面和前台页面的情况下，直接把这俩拖到resource目录下直接访问是访问不到的，因为被mvc框架拦截了
所以我们要编写一个映射类放行这些资源
#### 创建配置映射类
![在这里插入图片描述](https://img-blog.csdnimg.cn/434a4b66a3534624ae8576c71d5e43ac.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/ddb9c62315e74bfba369d3812b957453.png)

访问成功
![在这里插入图片描述](https://img-blog.csdnimg.cn/1a44e566a18b4da1bdbcb2e20abcd048.png)
# 后台开发
## 数据库实体类映射
用mybatis plus来实现逆向工程
这里是老版本的逆向工程
```java
    <dependency>
      <groupId>org.freemarker</groupId>
      <artifactId>freemarker</artifactId>
      <version>2.3.30</version>
    </dependency>

    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-boot-starter</artifactId>
      <version>3.3.1</version>
    </dependency>

    <!--mybatis-plus 代码生成器依赖-->
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-generator</artifactId>
      <version>3.3.2</version>
    </dependency>
```
具体怎么玩看这里
[MP逆向工程教程](https://blog.csdn.net/weixin_48678547/article/details/123379415)
![在这里插入图片描述](https://img-blog.csdnimg.cn/9063d65d4f074b488c44675c078965c2.png)

# 账户操作
## 登陆功能
前端页面
![在这里插入图片描述](https://img-blog.csdnimg.cn/018840778788414d802ec5309034a30b.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/7981e9e300884b3285073d4072fdd775.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/aa04438900b64014a89cbcb2115d9463.png)

数据库
![在这里插入图片描述](https://img-blog.csdnimg.cn/fbf6a0035c4c4e929e6736bb592abc1f.png)
业务逻辑
![在这里插入图片描述](https://img-blog.csdnimg.cn/257ae0d260b2430b8890d054fc72b65a.png)
**这里两个字符串的比较没法用!=来实现**，只能equals再取反来判断
直接上代码，这里没有涉及service层的操作

```java
    /**
     * @param request 如果登陆成功把对象放入Session中，方便后续拿取
     * @param employee 利用@RequestBody注解来解析前端传来的Json，同时用对象来封装
     * @return
     */
    @PostMapping("/login")
    public Result login(HttpServletRequest request, @RequestBody Employee employee) {
        String password=employee.getPassword();
        String username = employee.getUsername();
        log.info("登陆");
        //MD5加密
        MD5Util md5Util = new MD5Util();
        password=MD5Util.getMD5(password);
        //通过账户查这个员工对象，这里就不走Service层了
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(Employee::getUsername, username);
        Employee empResult=employeeService.getOne(lambdaQueryWrapper);
            //判断用户是否存在
        if (!empResult.getUsername().equals(username)){
            return Result.error("账户不存在");
            //密码是否正确
        }else if (!empResult.getPassword().equals(password)){
            return Result.error("账户密码错误");
            //员工账户状态是否正常，1状态正常，0封禁
        }else if (empResult.getStatus()!=1){
            return Result.error("当前账户正在封禁");
            //状态正常允许登陆
        }else {
            log.info("登陆成功，账户存入session");
            //员工id存入session，
            request.getSession().setAttribute("employ",empResult.getId());
            return Result.success("登陆成功");
        }
    }
```
具体代码可以参考如下路径
```
com.cc.controller.EmployeeController
```
[关于RequestBody何时使用](https://blog.csdn.net/weixin_44062380/article/details/116103642)
## 退出功能
点击退出
![在这里插入图片描述](https://img-blog.csdnimg.cn/09194dda97404a7db7a4438d056df2b6.png)
删除session对象
```java
    /**
     * @param request 删除request作用域中的session对象，就按登陆的request.getSession().setAttribute("employ",empResult.getId());删除employee就行
     * @return
     */
    @PostMapping("/logout")
    public Result login(HttpServletRequest request) {
        //尝试删除
        try {
            request.getSession().removeAttribute("employ");
        }catch (Exception e){
            //删除失败
            return Result.error("登出失败");
        }
        return Result.success("登出成功");
    }
```
## 完善登陆（添加过滤器）
这里的话用户直接url+资源名可以随便访问，所以要加个拦截器，没有登陆时，不给访问，自动跳转到登陆页面
![在这里插入图片描述](https://img-blog.csdnimg.cn/470f1c39f58f4cb09ee3878195622ca1.png)
过滤器配置类注解`@WebFilter(filterName="拦截器类名首字母小写"，urlPartten=“要拦截的路径，比如/*”)`
![在这里插入图片描述](https://img-blog.csdnimg.cn/4cf893fe589242eba9be0c206389bc18.png)判断用户的登陆状态这块之前因为存入session里面有一个名为employee的对象，那么只需要看看这个session还在不在就知道他是否在登陆状态
注意，想存或者想获取的话，就都得用`HttpServletRequest`的对象来进行获取，别的request对象拿不到的


这里提一嘴
调用Spring核心包的字符串匹配类的对象，对路径进行匹配，并且返回比较结果
如果相等就为true
```java
public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/a4e9e841bb7c43179bbcb2905d172a2c.png)
前端拦截器完成跳转到登陆页面，不在后端做处理
![在这里插入图片描述](https://img-blog.csdnimg.cn/f0a1667cce664c35b64925c48dfb59f0.png)
代码太多了，给个路径好啦，直接去Gitee看
request的js代码路径：`resource/backend/js/request.js`
拦截器的路径：`com.cc.filter.LoginCheckFilter`
## 新增员工
新增员工功能，（前端对手机号和身份证号长度做了一个校验）
![在这里插入图片描述](https://img-blog.csdnimg.cn/966dd380180a47018ed362e125406271.png)
请求 URL: http://localhost:9001/employee  （POST请求）
![在这里插入图片描述](https://img-blog.csdnimg.cn/8bae2348243f4d0ebe58ce97aff511d3.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/f6299bb555784e8e88b08d3cfa88be77.png)
改造一下Employee实体类，通用id雪花自增算法来新增id
![在这里插入图片描述](https://img-blog.csdnimg.cn/d38ab9e9810244bab56a09ca6d40bfa9.png)
这里用service接口继承的MybatisPlus的功能
![在这里插入图片描述](https://img-blog.csdnimg.cn/0b5eeb36e8214deb8ceea0385d4f89a0.png)
注入一下就可以使用了，插入方法
![在这里插入图片描述](https://img-blog.csdnimg.cn/e297f0e3af7649eda9ac74fb9b19b19c.png)
基本上都是自动CRUD，访问路径：`com.cc.controller.EmployeeController`

## 全局异常处理
先看看这种代码的try catch
这种try catch来捕获异常固然好，**但是，代码量一大起来，超级多的try catch就会很乱**
![在这里插入图片描述](https://img-blog.csdnimg.cn/232dfeaca28f420cb0fd5fae01f4ce15.png)
所以我们要加入全局异常处理，在Common包下，和Result同级，这里只是示例，并不完整
![在这里插入图片描述](https://img-blog.csdnimg.cn/b8da6a3931714deeb724381c0ad2da41.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/33fe7337e5464cf9b07a7f0a31f67a04.png)
当报错信息出现Duplicate entry时，就意味着新增员工异常了
所以，我们对异常类的方法进行一些小改动，让这个异常反馈变得更人性化
![在这里插入图片描述](https://img-blog.csdnimg.cn/b795bb06e106477192795c9fa56e15fa.png)
这个时候再来客户端试试，就会提供人性化的报错，非常的快乐~
![在这里插入图片描述](https://img-blog.csdnimg.cn/39454913ca2a48778c8c25281a03ff06.png)
**这回再回到Controller，这时就不需要再来try catch这种形式了，不用管他，因为一旦出现错误就会被我们的AOP捕获。所以，不需要再用try catch来抓了**
![在这里插入图片描述](https://img-blog.csdnimg.cn/0e1e9a3feb7c4b7c8bfa1dc61286437f.png)
异常类位置：`com.cc.common.GloableExceptionHandler`

## 员工信息分页查询
### 接口分析
老生常谈分页查询了
需求
![在这里插入图片描述](https://img-blog.csdnimg.cn/3899bbef468e4224b6b26fa6a13e103c.png)
分页请求接口
![在这里插入图片描述](https://img-blog.csdnimg.cn/288dd9777c1944d195cb421038fbc885.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/3b9f9ddf4a304f44923ef89fa9b22ca7.png)
查询员工及显示接口
![在这里插入图片描述](https://img-blog.csdnimg.cn/cc0e4ee9f4b14b8d9e2c12428471889d.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/e339d98a293a43c19d3c6a6252da428b.png)
逻辑流程
![在这里插入图片描述](https://img-blog.csdnimg.cn/50fa6620581044c69049028b6921f72e.png)
### 分页插件配置类
先弄个MP分页插件配置类
**原因是和3.2.3版本的代码生成器冲突**
[分页插件爆红解决方案](https://blog.csdn.net/weixin_49530535/article/details/119815650)
![在这里插入图片描述](https://img-blog.csdnimg.cn/d21457c19dd14f89ab7c314137ae7b33.png)
直接注释掉
![在这里插入图片描述](https://img-blog.csdnimg.cn/0d56b79072e548799e4bde12f7cc7bc2.png)
加入配置类
![在这里插入图片描述](https://img-blog.csdnimg.cn/928faf96126040248a6c7629433b8f4a.png)
### 接口设计
前端注意事项
![在这里插入图片描述](https://img-blog.csdnimg.cn/0e4186ba7e804ab6b1b3c6b2a3f1ce05.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/eae2123e010e4d048e3b5a1339cf41ce.png)
page对象内部
![在这里插入图片描述](https://img-blog.csdnimg.cn/87eab00f37584a51bcee03f83d665e32.png)
里面包含了查询构造器的使用
具体的细节在这个包下：com.cc.controller.EmployeeController.page
```java
    /**
     * 分页展示员工列表接口、查询某个员工
     * @param page 查询第几页
     * @param pageSize 每页一共几条数据
     * @param name 查询名字=name的数据
     * @return 返回Page页
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize,String name){
        //分页构造器,Page(第几页, 查几条)
        Page pageInfo = new Page(page, pageSize);
        //查询构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper();
        //过滤条件.like(什么条件下启用模糊查询，模糊查询字段，被模糊插叙的名称)
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name), Employee::getName, name);
        //添加排序
        lambdaQueryWrapper.orderByDesc(Employee::getCreateTime);
        //查询分页、自动更新
        employeeService.page(pageInfo, lambdaQueryWrapper);
        //返回查询结果 
        return Result.success(pageInfo);
    }

```
## 启用、禁用员工账号
无非就是修改status，0禁用，1启用
![在这里插入图片描述](https://img-blog.csdnimg.cn/cbc14ff5649f4c3ab58a61f6a3717d55.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/00fd8c22798a4be0935fbe4706aac1a0.png)
这种根据登陆人物来进行判断的玩法，是前端
这个页面的位置`resource/backend/page/member/list.html`
![在这里插入图片描述](https://img-blog.csdnimg.cn/15cb1554d5014d91a50a1d33faff5f0e.png)
看拿出来的对象是什么样子的，如果是admin，vue的v-if指令就会把编辑按钮显示出来
如果是普通用户，就会把编辑按钮隐藏
![在这里插入图片描述](https://img-blog.csdnimg.cn/1a813cac7216496c9f007b2e89e94571.png)
### 修复一个小Bug
前端一直不显示编辑按钮，在localStorage里没有发现admin对象
![在这里插入图片描述](https://img-blog.csdnimg.cn/656ddea591c644369951b448d9437162.png)
这个值不应该是登陆成功，应该是Employee的对象Json
猜测是登陆的时候往request里存对象没存好
![在这里插入图片描述](https://img-blog.csdnimg.cn/e02134f0443f4cebaf01da0bebb88be3.png)
改成对象存入就好了
![在这里插入图片描述](https://img-blog.csdnimg.cn/3695c863613643ebb7c9d2fb6f44b1f2.png)
这回都正常了
![在这里插入图片描述](https://img-blog.csdnimg.cn/f291200889854951bae0025d0c96d67f.png)
### 功能编写
复习一下
==**PutMapping是Resultful风格的请求方式**==
![在这里插入图片描述](https://img-blog.csdnimg.cn/0dcd79a42b334bae8337cb40a73fd17c.png)
当前状态是1，直接带着目标状态值（状态改禁用）进行更新
![在这里插入图片描述](https://img-blog.csdnimg.cn/bbc8b8c97744496491c18389cf13f2f1.png)
Id精度丢失，js独有的bug，直接处理Long处理不了，要Long转String再返回去
![在这里插入图片描述](https://img-blog.csdnimg.cn/bd851e41062148db9a7d4b84b687372f.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/65954dc589544fe1bc98a70d0a878936.png)
利用对象转换器JacksonObjectMapper，将对象转Json
将Long型的Id转换为String类型的数据
![在这里插入图片描述](https://img-blog.csdnimg.cn/7b18d09c0e75446db59dab3f6f4f505a.png)

在MVC配置类中扩展一个消息转换器
![在这里插入图片描述](https://img-blog.csdnimg.cn/bc895faafbb247eb9e580f553d32d4e6.png)
测试功能正常，正常更新员工状态
消息扩展器配置位置：`com.cc.common.JacksonObjectMapper`
对象映射器位置：`com.cc.config.WebMvcConfig`
员工状态更新位置：`com.cc.controller.EmployeeController`
## 编辑员工信息
![在这里插入图片描述](https://img-blog.csdnimg.cn/83b635aff2ca4445840f24ef5c60a22b.png)
请求API，这个是先发请求，查到用户，然后填充到页面上
可以看出来，这种请求方式是ResultFul风格的请求方式
在控制器中要用@PathVariable("/{参数名称}")注解来进行接收
![在这里插入图片描述](https://img-blog.csdnimg.cn/7f1a236c859d418eb4aa1177cbf82543.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/f7b34494b2eb42b8b315e9f7c2802956.png)
完美更新
更新方法位置：`com.cc.controller.EmployeeController.getEmployee`
## 公共字段自动填充
像是一部分公共字段，反复填充起来没有意义，简化填充的操作。
把这个功能拿出来，单独拎出来做自动填充处理
![在这里插入图片描述](https://img-blog.csdnimg.cn/f6b5a68d4b8646aab2b39db34fba625a.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/09eaaee0510849749512ef973529a4ed.png)
为实体类属性上面加入注解`@TableField(fill = 填充条件)`
看一下源码。fill是填充条件，用枚举来进行处理的
![在这里插入图片描述](https://img-blog.csdnimg.cn/c41f5a745d6c48d5b944fbe2b81e5726.png)
加完注解和条件不算完，还要加入配置类进行处理，对填充的数据做规定
在common包下创建一个自定义类，最关键的是要实现`MetaObjectHandler接口下的insertFill和updateFill`
确认填充时需要的字段。还有要加入@Component注解，将这个类交给框架来管理，否则的话容易找不到,setValue的值会根据注解加入的字段名称来锁定是否需要更新
位置：`com.cc.common.MyMetaObjectHandler`
![在这里插入图片描述](https://img-blog.csdnimg.cn/1810345a8b204a639754df2220f32bc2.png)
但是这里有个问题，如果我想去更新管理员字段是非常困难的，因为我这里拿不到Request的作用域对象，所以要想个办法来处理。
这个时候就需要`ThreadLocal`来进行对象的获取，这个线程是贯穿整个运行的，可以通过他来获取
### 使用时
何为ThreadLocal
==**重点来了**==
这个图
![在这里插入图片描述](https://img-blog.csdnimg.cn/a747301972964b88a7db5dc9d84995e6.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/62ec83c598a546b89722fef26f3ca4b4.png)
我的思路就是在用户登陆的时候，把这个id存进去，等到在填充字段的时候，从ThreadLocal里把这个资源再拿出来。
直接操作不太好，把他封装成一个工具类，这个工具类里方法都是静态的，可以通过类直接调用、并且都是静态方法，来操作保存和读取
我选择在Utils下创建
### 第一次的Bug
具体包在utils里，有Bug，封装的类ThreadLocal获取不到数据，不太清楚为什么，暂时就把这个写死了
```java
// 基于ThreadLocal 封装工具类，用户保存和获取当前登录的用户id
// ThreadLocal以线程为 作用域，保存每个线程中的数据副本
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //  设置当前用户id
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
```
注意，ThreadLocal不是一个线程，只有同一个线程才能拿到，不是一个线程拿不到的

### 解决方案
更改setId的位置，存储的时候放在过滤器内部，就算是一个线程了，就能拿到。不过我都试过了，确实是一个线程，但是还是拿不到。
换个思路：因为我想拿Request对象里的Id嘛，所以，只要有Request的id就行，不必过于执着一定要用ThreadLocal来存，因此，我这里选择注入一下HttpServletRequest对象来解决这个问题。
![在这里插入图片描述](https://img-blog.csdnimg.cn/58b8b8d5442a4da29ef667430a521262.png)
# 菜品页面
## 菜品分类
![在这里插入图片描述](https://img-blog.csdnimg.cn/2c38b870546f49bd9a9d00572c2f1578.png)

涉及的表有分类表category
![在这里插入图片描述](https://img-blog.csdnimg.cn/5c9af552c151402d99d419b44601110a.png)
业务流程
![在这里插入图片描述](https://img-blog.csdnimg.cn/0e55226139a848f4ba8a792c62a6feb3.png)
### 新增菜品分类
请求方式是Post请求
![在这里插入图片描述](https://img-blog.csdnimg.cn/d43ee4cb2aeb4259abe4129cb1672978.png)![在这里插入图片描述](https://img-blog.csdnimg.cn/dc4cdb56b1ff40039d61b044f0259ea4.png)
控制器位置：`com.cc.controller.CategoryController (save)`
### 菜品分类展现
![在这里插入图片描述](https://img-blog.csdnimg.cn/9dc65e03155d4e29b1dd85b73e79468c.png)
还是那几步

 1. 创建分页构造器 Page pageInfo = new Page(第几页,每页几条数据);
 2. 如果有需要条件过滤的加入条件过滤器LambaQueryWarpper
 3. 注入的service对象（已经继承MP的BaseMapper接口）去调用Page对象
	 service对象.page(分页信息,条件过滤器)
 4. 返回结果就可以了

分页查询位置：`com.cc.controller.CategoryController.page`
### 删除菜品分类
![在这里插入图片描述](https://img-blog.csdnimg.cn/d3c5ede5188a46caa8bef24e4c14b9fb.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/e35caef5f5d8421aae24cfc2832fe3d0.png)
普通版本，没有考虑分类有关联的情况
![在这里插入图片描述](https://img-blog.csdnimg.cn/62d820a820634bcc841b41bcc0bc3987.png)
完善一下，==**如果当前菜品分类下有菜品的话，就不许删除**==
所以在删除之前要先做判断才可以删除，不符合条件的，我们要抛出异常进行提示
因为没有返回异常信息的类，我们这里要做一个自定义的专门返回异常信息的类`CustomerException`
这个类的位置也在common包下
![在这里插入图片描述](https://img-blog.csdnimg.cn/40de841c594443398553b1509512fa5e.png)
因为我们之前创建了一个全局异常处理，也要用上，因为要拦截异常统一处理
还是`com.cc.common.GloableExceptionHandler`
对抛出异常进行处理，就可以对新增的异常提供目标的拦截和异常通知
![在这里插入图片描述](https://img-blog.csdnimg.cn/035d8b14a54f4cb1bffcd4c63673d0f8.png)
删除菜品分类的controller接口在：`com.cc.controller.CategoryController (delCategory)`
因为业务特殊，且比较长，就分离出来把业务放在service包下
service接口位置：`com.cc.service.impl.CategoryServiceImpl (removeCategory)`

## 修改套餐信息
![在这里插入图片描述](https://img-blog.csdnimg.cn/e6c29b9b209c4881815bdffa24f571e8.png)
非常简单的CRUD，直接调用MP更新一下就行
API位置
```java
com.cc.controller.CategoryController (updateCategory)
```
## 文件上传下载（重点）
### 上传逻辑
第一次接触上传和下载的功能
文件上传逻辑（后端）
![在这里插入图片描述](https://img-blog.csdnimg.cn/6fddfbb6992645358dd6f7fc9e7ed79d.png)
参数名有要求的
接收的文件类型一定是 方法名(MultipartFile 前端上传的文件名称)
![在这里插入图片描述](https://img-blog.csdnimg.cn/dee0540b7de5467ea6695e058a833b7f.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/b0f446d645654cd89577b9ba0a369f34.png)
所以后端的接收名字也得改为file
![在这里插入图片描述](https://img-blog.csdnimg.cn/8d04a387872f43f199fadcd1d2c79090.png)
### 上传逻辑实现
具体的存储路径写在配置文件里了
![在这里插入图片描述](https://img-blog.csdnimg.cn/57186bd868ce4e8fadf0090dd495c9ce.png)
用@Value注入到业务里就可以了
![在这里插入图片描述](https://img-blog.csdnimg.cn/8e10ef4ade02405d86215df5653076f7.png)

具体位置在`com.cc.controller.CommonController (upLoadFile)`

### 下载逻辑
![在这里插入图片描述](https://img-blog.csdnimg.cn/9748d9f84a224b329f2035afcf31091d.png)
==图片回显功能==
用到了输入输出流
位置：`com.cc.controller.CommonController (fileDownload)`
# 菜品管理页面
## 新增菜品
### 需求分析
![在这里插入图片描述](https://img-blog.csdnimg.cn/307c50613cf9427eb8af9ca1edf35f10.png)
涉及表为dish和dish_flavor
![在这里插入图片描述](https://img-blog.csdnimg.cn/2bee4656682c429093d0b487f7453028.png)
开发逻辑
![在这里插入图片描述](https://img-blog.csdnimg.cn/0c69f61ce4e84e87b2f70cef256a597c.png)
### 新增实现
由于是多表的操作，MP直接干肯定不行，所以就把service层抽离出来进行处理

还有，因为涉及两张表，这里还要加入事务进行控制，防止多表操作崩溃

```java
多表操作只能一个一个来，MP没有办法一次性操作多张表
因为涉及到多表的问题，所以还要加入注解来处理事务
@Transactional 开启事务
@EnableTransactionManagement 在启动类加入，支持事务开启
```
Controller位置：`com.cc.controller.DishController (addDish)`
Service位置：`com.cc.service.DishService `
ServiceImpl位置：`com.cc.service.impl.DishServiceImpl (addDishWithFlavor)`
### 新增菜品之获取菜品种类
![在这里插入图片描述](https://img-blog.csdnimg.cn/260620e272564731a10cf74b24d47b95.png)
从前端接收一个type=1的标注，目的是在分类表中，菜品分类是1，套餐分类是2，把二者区分开，获取所有的菜品类型
![在这里插入图片描述](https://img-blog.csdnimg.cn/c139415e958e4a4d81a9687d3f04a524.png)
位置：`com.cc.controller.CategoryController （listCategory）`

### 菜品分页
顺手把菜品分页也做了，不写太多了，位置在：`com.cc.controller.CategoryController （dishPage）`
记录一个知识点，如果说后端没有类和前端要的数据对应，那么自己就可以封装一个类来对前端特殊需要的数据进行封装

## DTO对象
这个类可以是对一些实体类进行扩展，继承于某个父类，再添加一些内容
比如Dish和DishDto
DishDto就继承于Dish类，并在此基础上进行了扩展
![在这里插入图片描述](https://img-blog.csdnimg.cn/c20e2473b88b49f684764ddab99f9cfb.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/a365d359125f498799f4b96a8e43fd0a.png)
## 更新菜品信息
就是个update
![在这里插入图片描述](https://img-blog.csdnimg.cn/e48ab4407978498d8321be25c5da40a0.png)
逻辑
![在这里插入图片描述](https://img-blog.csdnimg.cn/31c8e202fea94bcf8a775644996a6cc5.png)
注意，这里回显数据是要用DishDto，因为前端要显示口味等信息，这里如果用Dish是无法完美显示的，所以要用DishDto
### 回显填充查询
![在这里插入图片描述](https://img-blog.csdnimg.cn/7fd4eb56b06542b490e056bc8989a12c.png)

除此之外，这是个多表联查，用MP肯定不行，得自己写
Controller位置：`com.cc.controller.DishController (updateDish)`
Service位置：`com.cc.service.DishService `
ServiceImpl位置：`com.cc.service.impl.DishServiceImpl`
### 更新实现
实际上就是两个表联动更新和删除操作，所以MP直接操作是不可以的，所以要在Service层自己再封装一个删除方法，给Controller层调用删除就行
对于Dish对象可以直接进行更新，因为DishDto是Dish的子类
因此可以调用DishService的update方法传入DishDto对象，来实现Dish的更新
Controller位置：`com.cc.controller.DishController (updateDish)` 确实和上面那个一样，因为请求方式不一样
Service位置：`com.cc.service.DishService `
ServiceImpl位置：`com.cc.service.impl.DishServiceImpl (updateDishWithFlavor)`

### 其他功能
完成一些小功能的开发
![在这里插入图片描述](https://img-blog.csdnimg.cn/b4b59c021748427c87fa6edc48619920.png)
#### 停售功能
就是把数据库的status值更新一下，两个路径，一个启售，一个停售
![在这里插入图片描述](https://img-blog.csdnimg.cn/f1da740301ed403399148b6d9afb31f5.png)
停售请求路径
![在这里插入图片描述](https://img-blog.csdnimg.cn/2969987e765b4a8e8b4fd71b772bcfa0.png)
如果状态不一样了，会从停售变成启售，同时对应的请求路径也不一样
![在这里插入图片描述](https://img-blog.csdnimg.cn/335331ee558541e089128ff2b36faa6e.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/f3b5006617a144c2a50cf48e91daf7db.png)
Controller位置：`com.cc.controller.DishController (updateStatusStop)`停止
Controller位置：`com.cc.controller.DishController (updateStatusStart)`启动
#### 删除功能
![在这里插入图片描述](https://img-blog.csdnimg.cn/2e291e2e9a5941dbafb90caed4c894a2.png)
菜品删除功能
完成逻辑删除，不是真删
![在这里插入图片描述](https://img-blog.csdnimg.cn/2b84f5bc3cd54e1997a87f5768f0b48a.png)
位置：
Controller位置：`com.cc.controller.DishController (deleteDish)`停止
# 套餐页面
实际上就是一组菜品的集合
## 新增套餐概述
涉及到的数据库
![在这里插入图片描述](https://img-blog.csdnimg.cn/490ee26fc870424092c6f06b3b637de2.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/6ab0bc680c2d40d48da71becdf139330.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/458f7e16da474362ad0f5b49656bdc06.png)
导入SetmealDto
![在这里插入图片描述](https://img-blog.csdnimg.cn/9f5bd7dfcecc4c5a95d3ce1a48358980.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/0bb88c91dfba4c759e8cca3eb574682e.png)
## 新增套餐之菜品列表
![在这里插入图片描述](https://img-blog.csdnimg.cn/7b242922f194454cbce663c939c19fe8.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/fd6a01bbfbee43a1b7ff6b420815ef7f.png)
Controller位置：`com.cc.controller.DishController (listCategory)`
## 新增套餐实现
和新增菜品差不多，这里也是多表的操作
Controller位置：`com.cc.controller.SetmealController (saveSetmeal)`
Service位置：`com.cc.service.SetmealService`
ServiceImpl位置：`com.cc.service.impl.SetmealServiceImpl(saveWithDish)`
## 套餐分页
这里的套餐分页和以往不同，设计到了多表内容
![在这里插入图片描述](https://img-blog.csdnimg.cn/28b34889b2e4467c9e3e4a79aa3d2d18.png)
套餐分页Controller位置：`com.cc.controller.SetmealController.pageList`
套餐Mapper接口位置：`com.cc.mapper.SetmealMapper`
Mapper文件位置：`resource.mapper.SetmealMapper`
![在这里插入图片描述](https://img-blog.csdnimg.cn/a02a17ef6b8648a59e2b90ef9ec70a3f.png)
## 更新套餐
添加套餐和更新套餐是几乎完全一致的，字段巴拉巴拉的都一样
![在这里插入图片描述](https://img-blog.csdnimg.cn/5bf7c40f7b9147b7a093da18ae860952.png)
但是注意，修改套餐的话，需要先对菜品页面进行填充，这一页都是需要填充满要修改的菜品信息的。![在这里插入图片描述](https://img-blog.csdnimg.cn/07c01da9636d4643acea8689f2579f82.png)
先发请求，一看就是Restful风格请求
![在这里插入图片描述](https://img-blog.csdnimg.cn/cb797ab490434dd2ad5de3e282e3f4c4.png)
获取套餐Controller位置：`com.cc.controller.SetmealController.getSetmal`
## 更新销售状态
![在这里插入图片描述](https://img-blog.csdnimg.cn/53db02cdb6fd4574b2471bdaf7825d78.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/f2158ec7d2924b2eb17e2ef7b92fc6f3.png)
和之前一个业务逻辑很像，不想多赘述了，直接放接口位置
![在这里插入图片描述](https://img-blog.csdnimg.cn/bb10511b3fd14b398095865065700680.png)Controller位置：`com.cc.controller.SetmealController (startSale/stopSale)`
## 删除套餐
可以单独删，也可以批量删，接口是万金油，都能接，主要看传来的数据是几个
![在这里插入图片描述](https://img-blog.csdnimg.cn/734b880bdf304e70bb9376ecebcd3d15.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/730459957d464af3827df49084205353.png)
接口
![在这里插入图片描述](https://img-blog.csdnimg.cn/678ea2f5eb194625a05567061bba5ad0.png)
== 多表删除，在Controller直接实现不太现实，所以要在Service把业务写好==
Controller位置：`com.cc.controller.SetmealController (deleteSetmeal)`
Service位置：`com.cc.service.SetmealService`
ServiceImpl位置：`com.cc.service.impl.SetmealServiceImpl(removeWithDish)`
![在这里插入图片描述](https://img-blog.csdnimg.cn/bb4351bafe314861b6c775620f39641e.png)
# 前台开发（手机端）
# 账户登陆
## 短信发送
![在这里插入图片描述](https://img-blog.csdnimg.cn/6db71e2024264927a59a3ae59bca5890.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/56cadd94e4de4536aec20ff921842039.png)
[阿里云短信业务教程](https://blog.csdn.net/qq_55106682/article/details/121920826)
### 代码实现
[官方文档地址](https://help.aliyun.com/document_detail/112148.html)
导入Maven
```java
<dependency>
  <groupId>com.aliyun</groupId>
  <artifactId>aliyun-java-sdk-core</artifactId>
  <version>4.5.16</version>
</dependency>
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>aliyun-java-sdk-dysmsapi</artifactId>
    <version>1.1.0</version>
</dependency>
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/ca8d01eaf61a485a9e0edae0c0307c3a.png)
导入短信登陆的工具类，把ACCESSKeyID和Secret更换到位就行
![在这里插入图片描述](https://img-blog.csdnimg.cn/fab29b651545452b8aaf515717a3244d.png)

## 验证码发送
数据模型user表，手机验证码专用的表
![在这里插入图片描述](https://img-blog.csdnimg.cn/582420e32f9449699765ad9db8f665bc.png)
开发流程
![在这里插入图片描述](https://img-blog.csdnimg.cn/03e0ad86bbda4229b8985e6644e2e5f7.png)
修改拦截器，放行操作
![在这里插入图片描述](https://img-blog.csdnimg.cn/2c136ec0905248328f25be7e137b2da0.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/15b21dad09be44bfa030984f5f4c1731.png)
controller位置：`com.cc.controller.UserController （sendMsg）`
发送完还需要验证，验证就是另一个login了
## 用户登陆
![在这里插入图片描述](https://img-blog.csdnimg.cn/16f3f2b8f68948b0b4f2e01133adc6ce.png)
controller位置：`com.cc.controller.UserController （login）`
这里登陆还涉及到过滤器放行的功能，不要忘记了，把用户id存入session，过滤器会进行验证
过滤器
![在这里插入图片描述](https://img-blog.csdnimg.cn/16d1b1cf6fa54b408d9b5bcd495962f5.png)
controller
![在这里插入图片描述](https://img-blog.csdnimg.cn/7da50768c0b043f3a9a5f3dbce6915c1.png)
# 前台页面
## 导入用户地址簿
![在这里插入图片描述](https://img-blog.csdnimg.cn/732d6d4d8bba4911b903055d7a2003cf.png)
地址表
![在这里插入图片描述](https://img-blog.csdnimg.cn/e45d3cd4c91e4619a21a3342d4683508.png)
这里直接导入现成的AddressBookController，没有自己写
```java
com.cc.controller.AddressBookController
```
## 菜品展示
逻辑梳理
![在这里插入图片描述](https://img-blog.csdnimg.cn/db2983fb1a8a46bc85f4836dae41a24c.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/c7e3567ea3f94b718e33ba680e573121.png)
修改DishController的list方法，来符合前台请求的要求
controller位置：`com.cc.controller.DishController （listCategory）`
套餐内菜品Controller：`com.cc.controller.SetmealController （list）`
## 购物车
把菜品加入购物车
![在这里插入图片描述](https://img-blog.csdnimg.cn/159ef74addd64f60bfa9d9a44bf22d64.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/3b7b1d5d90d949cb94c6c4f77a2f9d96.png)
逻辑梳理
![在这里插入图片描述](https://img-blog.csdnimg.cn/48b1358d65a8418689ada067bc6fbdd2.png)
注意，这里不需要后端去管总价的计算，就是单价*数量的这个操作，不是后端的内容。前端在展示的时候自己就计算了。
位置：`com.cc.controller.ShoppingCartController （add）`

## 下单
![在这里插入图片描述](https://img-blog.csdnimg.cn/37b2f6bb4b2e4d82942e38ceea4bbc78.png)
对应的两个表，一个是orders表，另一个是orders_detail表

orders表
![在这里插入图片描述](https://img-blog.csdnimg.cn/37f1f4d9aec2474ab3578ad01062abf9.png)
orders_detail表
![在这里插入图片描述](https://img-blog.csdnimg.cn/d07d325b341841e787117d1f12d727e0.png)
交互流程
![在这里插入图片描述](https://img-blog.csdnimg.cn/04bfdd8093cb41f7956332bb1561e12f.png)
业务比较复杂，在Service里写的`com.cc.service.impl.OrdersServiceImpl`

至此基础部分完成，开始对项目性能进行优化

# 小知识点总结
### @RequestBody的使用
只有传来的参数是Json才能用RequestBody接收，如果不是Json的情况（比如那种？key=value&key=value）是不可以用的，会400错误
[关于RequestBody何时使用](https://blog.csdn.net/weixin_44062380/article/details/116103642)




#  缓存优化
基于Redis进行缓存优化
![在这里插入图片描述](https://img-blog.csdnimg.cn/017c0aab4eb345d68ba9a5a309e78c7a.png)
## 环境搭建
### Redis进行配置
加入Pom文件
```xml
        <!--导入Redis依赖-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
```

加入Redis配置类

```java
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        //默认的Key序列化器为：JdkSerializationRedisSerializer
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
```
yml中加入配置
![在这里插入图片描述](https://img-blog.csdnimg.cn/a737f91d8934469ca405f2acf279272d.png)
## 短信验证码、登陆优化
给验证码加入有效时间的验证，设置好短信验证码的有效时间
![在这里插入图片描述](https://img-blog.csdnimg.cn/dd9f7fcb4dd948b380008dd1d769e535.png)
如果登陆成功，就自动删除缓存中的验证码
优化位置：com.cc.controller.UserController sendMsg和login
注入RedisTemplete
![在这里插入图片描述](https://img-blog.csdnimg.cn/e881a9dc80a740aab43c8bc30f45a91f.png)
针对验证码进行优化
![在这里插入图片描述](https://img-blog.csdnimg.cn/4139a4aebf384f8e958f0587c7fe4acb.png)
针对登录后进行优化
login方法中
![在这里插入图片描述](https://img-blog.csdnimg.cn/ccc12a60c924407bb8c859aa66b61c38.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/24b7a6e20b7840e1a868dea4be2f0787.png)
这里过滤器也要改，因为登陆的id数据由session变成了redis存放，所以要把过滤器的相关部分进行改造
```
com.cc.filter.LoginCheckFilter
```
同样要先注入RedisTemplate
![在这里插入图片描述](https://img-blog.csdnimg.cn/bb4030b56bd64c6dbdc5633c67e4bbfb.png)
## 缓存前台菜品数据
![在这里插入图片描述](https://img-blog.csdnimg.cn/32925b5c084942ecb5f5f9aa153a6d96.png)
缓存思路，要保证缓存数据库和DBMS内的数据保持同步，避免读到脏数据（没更新的数据）
![在这里插入图片描述](https://img-blog.csdnimg.cn/ed16b6270e734adaaf78058373894604.png)
对DishController进行优化，加入了缓存
再次访问可以发现，如果已经缓存过了当前的菜品分类，就不会再查数据库了
### 更新菜品同时更新缓存
保证少出现脏数据，所以加入清理缓存，不及时清理的话，新数据保存上来，列表数据库无法同步更新。就会出现问题。
这里清理精确数据。大面积清缓存也是比较费性能的
==这种就是全清理==
![在这里插入图片描述](https://img-blog.csdnimg.cn/60b7f47bf7d24be0bec55a2309e1f227.png)
==这种是精确清理==
![在这里插入图片描述](https://img-blog.csdnimg.cn/958c3fd2cb824012af93adfd54d4ea24.png)
## SpringCache
### 简介
![在这里插入图片描述](https://img-blog.csdnimg.cn/a59b7272acce47e98a6bd7505772e071.png)
### SpringCache常用注解及功能
![在这里插入图片描述](https://img-blog.csdnimg.cn/768c769e2725422f88530feca96d70b8.png)
### 快速起步
启动类上要加入`@EnableCaching`注解，启用缓存框架
![在这里插入图片描述](https://img-blog.csdnimg.cn/d29b058bff0d4269aa35202a9e35143d.png)
#### @CachePut注解
缓存方法返回值，缓存一条或者多条数据
![在这里插入图片描述](https://img-blog.csdnimg.cn/0d18d4dcaad54cc7814e3fa1a82ed8c8.png)
#### @CacheEvict注解
删除缓存
![在这里插入图片描述](https://img-blog.csdnimg.cn/ae87712409804775b614bebb226e8896.png)

#### @Cacheable注解
先看看Spring是否已经缓存了当前数据，如果已经缓存那么直接返回。
如果没有缓存就直接缓存到内存里
![在这里插入图片描述](https://img-blog.csdnimg.cn/c2e3aa34ea3442d894543d105ebb9b41.png)
一些特殊情况，condition属性和Unless属性![在这里插入图片描述](https://img-blog.csdnimg.cn/ec27f124b8c047e08a8b95af613ed922.png)

前面都是用SpringCache自带的缓存容器，性能肯定比不了Redis
所以现在开始引入Redis作为SpringCache缓存的产品
切换为Redis作为缓存产品
#### SpringCache-Redis
![在这里插入图片描述](https://img-blog.csdnimg.cn/8dae4993b8494af598d870917e46ba56.png)
导入jar包
![在这里插入图片描述](https://img-blog.csdnimg.cn/afdfe6133e254348a03303db222f1762.png)
注入相对应的缓存产品Manager就可以了，这里以RedisManager为例
![在这里插入图片描述](https://img-blog.csdnimg.cn/69344c2b79044bb7a354d96cb148d495.png)
## 利用SpringCache-Redis来缓存套餐数据
![在这里插入图片描述](https://img-blog.csdnimg.cn/ffd8677c072d4697971562d1a96134bc.png)
启动类上要加入`@EnableCaching`注解，启用缓存框架
![在这里插入图片描述](https://img-blog.csdnimg.cn/d29b058bff0d4269aa35202a9e35143d.png)
加入注解时的坑
这里相当于是从Return中拿到Setmeal中的属性，但是Return时的数据是Result封装的Setmeal数据，显然无法完成序列化，这里也是需要对Result类进行序列化的改造
![在这里插入图片描述](https://img-blog.csdnimg.cn/f1309a8146514e37bb8078ef9b0cc7ca.png)
继承序列化类，使其可以序列化
![在这里插入图片描述](https://img-blog.csdnimg.cn/f25595d9069441d1adf22a469e842c09.png)
===此时就完成了缓存的优化，此时如果缓存中有当前value名字的缓存，就自动返回，如果没有就查询一下。当前缓存自动过期的时间在yml里面有详细配置==

保存套餐方法缓存优化
一保存套餐，对应的缓存就得删除，因为数据更新了要重新获取
还有更新套餐，理由同上
删除方法要加
![在这里插入图片描述](https://img-blog.csdnimg.cn/0896df7570f843a19afb69975c4a328f.png)
保存方法也要加
![在这里插入图片描述](https://img-blog.csdnimg.cn/3206a5c50388428ea4809425d083bc72.png)

# 数据库优化
## MySQL读写分离
将单点数据库改成分布式的数据库服务器
主写从读。
![在这里插入图片描述](https://img-blog.csdnimg.cn/95b2e61b3c594adeac1732bcc6d719d6.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/be32928754c8409785c39f857ee78381.png)

## MySQL主从复制搭建
### 主库设置
主从复制架构图
![在这里插入图片描述](https://img-blog.csdnimg.cn/c72d940c5ff143ea834e53e05fb4db83.png)
以上就可以做到主库数据和从库数据保持同步

对主库进行配置
Linux改法
![在这里插入图片描述](https://img-blog.csdnimg.cn/71faca81d5524f7f81249639ec421334.png)

```sql
log-bin=mysql-bin #启动二进制
server-id=100 #唯一id
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/07f64dd342a54a02bb9db8055535c02d.png)

windows改法

在mysql安装路径下
![在这里插入图片描述](https://img-blog.csdnimg.cn/ddcf86e4bfc344c3874a528909018c4d.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/d85b28a9fe5c4fdebb9bef5d96717860.png)


修改好了重启MySQL

![在这里插入图片描述](https://img-blog.csdnimg.cn/841a999f27fe4910baf41c7ba6728ad7.png)
windows版本的重启教程在这里
[重启mysql](https://blog.csdn.net/weixin_31444279/article/details/113223859)

=======================================================

![在这里插入图片描述](https://img-blog.csdnimg.cn/06539e930d864f08831019eed03e50c0.png)
```sql
GRANT REPLICATION SLAVE ON*.*to'xiaoming'@'%'identified by 'Root@123456';
```
这里我把本地的MySQL作为主机，把阿里云作为从机
运行一下权限SQL
![在这里插入图片描述](https://img-blog.csdnimg.cn/bbbc91e5cd674f60a0e3765c1e3a477b.png)

查看主机状态`show master status;`
![在这里插入图片描述](https://img-blog.csdnimg.cn/2c46f23083eb4d009cee681610f20467.png)
### 从库设置
从库这里选择了阿里云
还是先修改配置文件，加入端口id
![在这里插入图片描述](https://img-blog.csdnimg.cn/59c47eb35c4748948c96ac6bf1a0eb17.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/b8ac44ce50c546b5bfeb507a0ab7ef4f.png)
第二步还是从库重启（Linux中）
![在这里插入图片描述](https://img-blog.csdnimg.cn/5f1f8347417441ccb00d15c08ff5cb5a.png)
第三部，设置连接到主机
运行SQL
![在这里插入图片描述](https://img-blog.csdnimg.cn/dda57b14af0845aca9056e4788a34fea.png)

运行一下
具体的可以去从机用show master status查看
```sql
change master to master_host='ip',master_user='xiaoming',master_password='Root@123456',master_log_file='mysql-bin.eo0001',master_log_pos=主机的position
```
![在这里插入图片描述](https://img-blog.csdnimg.cn/1f88d8237d804843849777aa1c3e0f4e.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/e9080c8d03a24206a4a00b553b1c9ade.png)
==这里我是两台服务器，一台docker安装的mysql（从机）
另一台是普通安装的mysql做主机，配置过程中遇到了很多问题，参考了下面的链接==
[参考教程](https://blog.csdn.net/xizhen2791/article/details/123660049)



一定一定记着上面从机连接命令运行成功后要启动从机也就是
```sql
slave start
```


最后运行`start slave`就算是执行成功了
![在这里插入图片描述](https://img-blog.csdnimg.cn/cf6e9e14501149a197b7a6cc329cc1d0.png)
查看一下从机状态
```sql
show slave status
```
这样就算搭建好了
![在这里插入图片描述](https://img-blog.csdnimg.cn/54747e8178044d8c9f68a7ecf1862bb7.png)
### 测试
![在这里插入图片描述](https://img-blog.csdnimg.cn/e471059730274a98a3a6b2e71728724f.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/dc067197d93b4799a37e5d51f28c5736.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/5657bb24170a4fbe8a9139b954934164.png)
到这里就算搭建完成了


### 遇到的问题
这里遇到的问题，连不上
![在这里插入图片描述](https://img-blog.csdnimg.cn/cbb888adff9c45be994e4087e528d9dc.png)
想本地当主机，外网当从机好像不太行，我就又弄了台服务器做读写分离

搞到了从机之后，就开始配置，安装MySQL等等

有的时候会提示io冲突，这是因为之前的从机没有关闭，关闭一下就可以了
`stop slave` 一下 就可以运行了

一个从机启动命令忘记了，改了一晚上
如果不运行从机启动就会变成这样
![在这里插入图片描述](https://img-blog.csdnimg.cn/85ee4381b01c44ccb0777995ba042e46.png)

## 主写从读实战
### 概述
![在这里插入图片描述](https://img-blog.csdnimg.cn/f141bcd96b9840bd8a4e1295055408a1.png)
难么如何去确定来的SQL应该分配到哪个库上，这个就要靠Sharding-jdbc框架来读写分离的分流处理
![在这里插入图片描述](https://img-blog.csdnimg.cn/83f78cd07cd848de9c4b5abd422d4b91.png)
### 实战
步骤如下
![在这里插入图片描述](https://img-blog.csdnimg.cn/f72981dfa9204bab9545793d04bc2ed9.png)
导入Maven坐标
```sql
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
            <version>4.0.0-RC1</version>
        </dependency>
```
==配置yml文件==
```sql
spring:
  application:
    name: ccTakeOut
  shardingsphere:
    datasource:
      names:
        master,slave
      # 主库（增删改操作）
      master:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://121.89.200.204:3306/ruiji?characterEncoding=utf-8
        username: root
        password: 333
      # 从数据源（读操作）
      slave:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://121.36.51.170:3306/ruiji?characterEncoding=utf-8
        username: root
        password: 333
    masterslave:
      # 读写分离配置
      load-balance-algorithm-type: round_robin #轮询（如果有多个从库会轮询着读）
      # 最终的数据源名称
      name: dataSource
      # 主库数据源名称
      master-data-source-name: master
      # 从库数据源名称列表，多个逗号分隔
      slave-data-source-names: slave
    props:
      sql:
        show: true #开启SQL显示，默认false
  main:
    allow-bean-definition-overriding: true #允许bean数据源覆盖

```
解读一下yml配置
![在这里插入图片描述](https://img-blog.csdnimg.cn/7e5c203773a04fdd9b9a096659db9749.png)
**允许Bean定义覆盖很重要**

### 测试
启动项目，可以看到，读写操作分别到达了不同的主机上
读写分离测试
![在这里插入图片描述](https://img-blog.csdnimg.cn/f647f0ab726e48f7a4ae0445970980d2.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/fd937b8b481946bba8325127319d22f9.png)
# Nginx部署
[Nginx笔记](https://blog.csdn.net/weixin_46906696/article/details/125569407?csdn_share_tail=%7B%22type%22:%22blog%22,%22rType%22:%22article%22,%22rId%22:%22125569407%22,%22source%22:%22weixin_46906696%22%7D&ctrtid=O48Rx)

# 前后端分离开发
![在这里插入图片描述](https://img-blog.csdnimg.cn/5ae7a5c5f65049e698b47a060c9b9ac8.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/9b200bae5d514949b1fed2860598906a.png)
## 开发流程
![在这里插入图片描述](https://img-blog.csdnimg.cn/9d4d3ca75521432a999246d1ff68cea2.png)
## YApi
![在这里插入图片描述](https://img-blog.csdnimg.cn/b56ddabf9931461dab56dafdd5614088.png)

## Swagger（常用）
主要作用就是帮助后端人员生成后端接口文档的
![在这里插入图片描述](https://img-blog.csdnimg.cn/7352d150c2b64d3e9c84f15ce56c7c1d.png)
使用方式
![在这里插入图片描述](https://img-blog.csdnimg.cn/b22b2f0ae0c04a4db76fe8e8fa047615.png)
导入坐标

```sql
        <!--knife4j接口管理-->
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-spring-boot-starter</artifactId>
            <version>3.0.2</version>
        </dependency>
```

导入配置类
![在这里插入图片描述](https://img-blog.csdnimg.cn/8efe0ecd0f32415296336792c002cb93.png)
具体配置位置`com.cc.config.WebMvcConfig`

启动服务，访问路径+doc.html
进入之后就可以对已有的接口进行管理了
![在这里插入图片描述](https://img-blog.csdnimg.cn/28fa1199bed745909c1ab3566ef46517.png)

## Swagger常用注解
直接生成的注解内容并不是很完善
![在这里插入图片描述](https://img-blog.csdnimg.cn/7b2482af3df7429ea060c692c9778524.png)
Swagger常用注解
![在这里插入图片描述](https://img-blog.csdnimg.cn/96378f7383e34de1a6ef9f7785989c2d.png)
以实体类为例
![在这里插入图片描述](https://img-blog.csdnimg.cn/0fbe9d3e59ee47d0b5a9f8d6b21609af.png)
Controller上的注解
![在这里插入图片描述](https://img-blog.csdnimg.cn/920dcfe50e8544f0ab5a42581722b3e3.png)
以上均为示例，最终完善好注解，文档会更好用，更详细。

# 项目部署
## 前端
前端作为一个工程，同样需要打包，打包完为dist目录
![在这里插入图片描述](https://img-blog.csdnimg.cn/a2cee8e970754767bf5efab50eb3d065.png)
把这个dist目录，扔进Nginx里HTML文件夹就可以了，也就是那个静态资源
![在这里插入图片描述](https://img-blog.csdnimg.cn/e05ae7caf6ce4c82b5dd35e96ba97059.png)
传上来不算完，还要好好配置一下
一个是静态资源，另一个是反向代理
### 静态资源配置
先配置静态资源
![在这里插入图片描述](https://img-blog.csdnimg.cn/85400722cc984e53bc62af167a80ba13.png)
### 请求代理配置
重启Nginx，测试一下，访问。
随便一个请求可以看到，带了前缀
![在这里插入图片描述](https://img-blog.csdnimg.cn/77debb51210c4259a4c59d76778d4f9b.png)
后端项目给的端口是9001
请求路径为：http://www.ccsmile.fun:9001/api/employee/login
我们后端是没有这个api的前缀的
通过重写url，就可以把
`http://www.ccsmile.fun:9001/api/employee/login`
变成
`http://www.ccsmile.fun:9001/employee/login`的请求地址，这样就完成了请求代理转发操作
![在这里插入图片描述](https://img-blog.csdnimg.cn/cebfe85548264cdaafb056fb8a16c5b9.png)
配置文件如下

```sql
server{
  listen 80;
  server_name localhost;
#静态资源配置
  location /{
    root html/dist;
    index index.html;
  }
#请求转发代理，重写URL+转发
  location ^~ /api/{
          rewrite ^/api/(.*)$ /$1 break;
          proxy_pass http://后端服务ip:端口号;
  }
#其他
  error_page 500 502 503 504 /50x.html;
  location = /50x.html{
      root html;
  }
}
```
最后保存文件，重启Nginx，就配置完成了
不过还是不知道为啥不太好用，还有待解决，实在不行就在后端上加入接收请求前缀就好了
## 后端
![在这里插入图片描述](https://img-blog.csdnimg.cn/23e1f1e20f0b420ca3fa8856aa07efaa.png)
上传脚本，自动拉取最新脚本
这样在开发端和Linux端就通过Gitee间接实现同步了
![在这里插入图片描述](https://img-blog.csdnimg.cn/c2cf53f4a6044202b5e5b7136a9bc0ef.png)
脚本内容

```sql
#!/bin/sh
echo =================================
echo  自动化部署脚本启动
echo =================================

echo 停止原来运行中的工程
APP_NAME=reggie_take_out

tpid=`ps -ef|grep $APP_NAME|grep -v grep|grep -v kill|awk '{print $2}'`
if [ ${tpid} ]; then
    echo 'Stop Process...'
    kill -15 $tpid
fi
sleep 2
tpid=`ps -ef|grep $APP_NAME|grep -v grep|grep -v kill|awk '{print $2}'`
if [ ${tpid} ]; then
    echo 'Kill Process!'
    kill -9 $tpid
else
    echo 'Stop Success!'
fi

echo 准备从Git仓库拉取最新代码
cd /usr/local/javaapp/reggie_take_out

echo 开始从Git仓库拉取最新代码
git pull
echo 代码拉取完成

echo 开始打包
output=`mvn clean package -Dmaven.test.skip=true`

cd target

echo 启动项目
nohup java -jar reggie_take_out-1.0-SNAPSHOT.jar &> reggie_take_out.log &
echo 项目启动完成


```
执行脚本就OK了
![在这里插入图片描述](https://img-blog.csdnimg.cn/079d294160f94539b7b3db5d09af969e.png)

记得修改yml文件中的部分内容，比如文件路径等等信息~
完结撒花啦
