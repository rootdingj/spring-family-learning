Spring 中的数据操作
====================
# 1、JDBC 必知必会

## 1.1、配置单数据源

### Spring Boot 配置数据源
- 使用 [Spring Initializr](https://start.spring.io/) 构建工程；

- 引入 H2数据库驱动，使用 H2数据库内存模式；

- 引入 JDBC 驱动，spring-boot-starter-jdbc；

- 获取 DataSource bean 信息；

-  也可通过 /acturator/beans 查看 Bean，如：http://localhost:8080/actuator/beans。

### Spring 配置数据源
- 数据源 ：DataSource,根据选择的连接池确定；

- 事物（可选）：PlatformTransactionManager（DataSourceTransactionManager） 、  TransactionTemplate；

- 操作（可选）：JdbcTemplate。

### Spring Boot 配置数据源做了哪些事情：
- DataSourceAutoConﬁguration ，配置 DataSource

- DataSourceTransactionManagerAutoConﬁguration ，配置 DataSourceTransactionManager

- JdbcTemplateAutoConﬁguration ， 配置 JdbcTemplate

## 1.2、配置多数据源

注意事项：
- 不同数据源的配置要分开
- 关注每次使用的数据源 ：
  - 有多个 DataSource 时系统如何判断
  - 对应的设施（事务、ORM 等）如何选择 DataSource

## 1.3、连接池 HikariCP
HikariCP 官网： https://github.com/brettwooldridge/HikariCP

### HikariCP 为什什么快：
- 1.字节码级别优化（很多⽅方法通过 JavaAssist ⽣生成） 
- 2.⼤量小优化 
    - ⽤用 FastStatementList 代替 ArrayList
    - 无锁集合 ConcurrentBag
    - 代理理类的优化（如，⽤用 invokestatic 代替了了 invokevirtual）

## 1.4、连接池 Druid
Alibaba Druid 官网： https://github.com/alibaba/druid/wiki/Druid%E8%BF%9E%E6%8E%A5%E6%B1%A0%E4%BB%8B%E7%BB%8D

### 实⽤用的功能：
- 详细的监控
- ExceptionSorter，针对主流数据库的返回码都有⽀支持
- SQL 防注⼊入
- 内置加密配置
- 众多扩展点，⽅便进⾏行定制

### 数据源配置
- 1.直接配置 DruidDataSource 
- 2.通过 druid-spring-boot-starter 配置 spring.datasource.druid.*
```test
spring.output.ansi.enabled=ALWAYS

spring.datasource.url=jdbc:h2:mem:foo
spring.datasource.username=sa
## 密码加密 
spring.datasource.password=n/z7PyA5cvcXvs8px8FVmBVpaRyNsvJb3X7YfS38DJrIg25EbZaZGvH4aHcnc97Om0islpCAPc3MqsGvsrxVJw==
spring.datasource.druid.connection-properties=config.decrypt=true;config.decrypt.key=${public-key}
spring.datasource.druid.filter.config.enabled=true

spring.datasource.druid.initial-size=5
spring.datasource.druid.max-active=5
spring.datasource.druid.min-idle=5
## Filter 配置
spring.datasource.druid.filters=conn,config,stat,slf4j

spring.datasource.druid.test-on-borrow=true
spring.datasource.druid.test-on-return=true
spring.datasource.druid.test-while-idle=true

## SQL 防注⼊
spring.datasource.druid.ﬁlter.wall.enabled=true
spring.datasource.druid.ﬁlter.wall.db-type=h2
spring.datasource.druid.ﬁlter.wall.conﬁg.delete-allow=false
spring.datasource.druid.ﬁlter.wall.conﬁg.drop-table-allow=false

public-key=MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALS8ng1XvgHrdOgm4pxrnUdt3sXtu/E8My9KzX8sXlz+mXRZQCop7NVQLne25pXHtZoDYuMh3bzoGj6v5HvvAQ8CAwEAAQ==
```

### Druid Filter
- ⽤用于定制连接池操作的各种环节
- 可以继承 FilterEventAdapter 以便便⽅方便便地实现 Filter
- 修改 META-INF/druid-ﬁlter.properties 增加 Filter 配置

### 连接池选择时的考量量点
- 可靠性
- 性能
- 功能
- 可运维性 
- 可扩展性
- 其他

## 1.5、通过 Spring JDBC 访问数据库

### spring-jdbc 操作类：
- core，JdbcTemplate 等相关核⼼心接⼝和类
- datasource，数据源相关的辅助类
- object，将基本的 JDBC 操作封装成对象
- support，错误码等其他辅助⼯工具

### 常⽤用的 Bean 注解：
- @Component
- @Repository
- @Service
- @Controller
    - @RestController

### 简单的 JDBC 操作
#### JdbcTemplate
- query
- queryForObject
- queryForList
- update
- execute
- batchUpdate
    - BatchPreparedStatementSetter

#### NamedParameterJdbcTemplate 
- batchUpdate
    - SqlParameterSourceUtils.createBatch

## 1.6、Spring 的事物抽象

### 什么是事物？
> 事务是逻辑上的一组操作，要么都执行，要么都不执行。

### 事物的特性（ACID）
- **原子性**： 事务是最小的执行单位，不允许分割；事务的原子性确保动作要么全部完成，要么完全不起作用。
- **一致性**： 执行事务前后，数据保持一致。
- **隔离性**： 并发访问数据库时，一个用户的事物不被其他事物所干扰，各并发事务之间数据库是独立的。
- **持久性**: 一个事务被提交之后。它对数据库中数据的改变是持久的，即使数据库发生故障也不应该对其有任何影响。

### Spring事务管理接口
事务管理，就是按照给定的事务规则来执行提交或者回滚操作。

- ``PlatformTransactionManager`` : 平台事务管理器
- ``TransactionDefinition``： 事务定义信息(事务隔离级别、传播行为、超时、只读、回滚规则)
- ``TransactionStatus``： 事务运行状态

### 相关平台的事物实现
<div align="center"> <img src="../Assets/images/02.spring-transaction.png" width="480px"> </div><br>

### 事务隔离特性
| 隔离性  | 值  | 脏读 | 不可重复读  | 幻读 |
| :--:    | :--: | :--: | :--: | :--: |
|ISOLATION_READ_UNCOMMITTED |  1  | Y | Y | Y |
|ISOLATION_READ_COMMITTED   |  2  | N | Y | Y |
|ISOLATION_REPEATABLE_READ  |  3  | N | N | Y |
|ISOLATION_SERIALIZABLE     |  4  | N | N | N |

### 事务传播特性 

| 传播性   | 值   | 描述 |
| :--:    | :--: | :--: |
|PROPAGATION_REQUIRED     |  0  | 当前有事务就用当前的，没有就用新的 |
|PROPAGATION_SUPPORTS     |  1  | 事务可有可无，不是必须的 |
|PROPAGATION_MANDATORY    |  2  | 当前一定要有事务，不然就抛异常   |
|PROPAGATION_REQUIRES_NEW |  3  | 无论是否有事务，都起个新的事务 |
|PROPAGATION_NOT_SUPPORTED| 4   | 不支持事务，按非事务方式运⾏ |
|PROPAGATION_NEVER        | 5   | 不支持事务，如果有事务则抛异常 |
|PROPAGATION_NESTED       | 6   | 当前有事务就在当前事务里再起一个事务 |

### 编程式事务
- TransactionTemplate 
    - TransactionCallback
    - TransactionCallbackWithoutResult

- PlatformTransactionManager 
    - 可以传入TransactionDeﬁnition进行定义

### 申明式事务
- 开启事务注解的方式 ：
    - @EnableTransactionManagement
    - \<tx:annotation-driven/>

- 一些配置:
    - proxyTargetClass
    - mode
    - order

- @Transactional 
    - transactionManager
    - propagation
    - isolation
    - timeout
    - readOnly
    - 怎么判断回滚

## 1.7、Spring 的 JDBC 异常抽象
Spring 会将数据操作的异常转换为 DataAccessException，⽆无论使⽤用何种数据访问⽅方式，都能使⽤用⼀一样的异常。

### JDBC 异常类型
<div align="center"> <img src="../Assets/images/02.DataAccessException.png" width="520px"> </div><br>

### Spring是怎么认识那些错误码的
- 通过 SQLErrorCodeSQLExceptionTranslator 解析错误码 
- ErrorCode 定义 
    - org/springframework/jdbc/support/sql-error-codes.xml Spring自带错误码
    - Classpath 下的 sql-error-codes.xml 用户自定义错误码

### 定制错误码解析逻辑
```Test
<bean id="H2" class="org.springframework.jdbc.support.SQLErrorCodes">
        <property name="badSqlGrammarCodes">
            <value>42000,42001,42101,42102,42111,42112,42121,42122,42132</value>
        </property>
        <property name="duplicateKeyCodes">
            <value>23001,23505</value>
        </property>
        <property name="dataIntegrityViolationCodes">
            <value>22001,22003,22012,22018,22025,23000,23002,23003,23502,23503,23506,23507,23513</value>
        </property>
        <property name="dataAccessResourceFailureCodes">
            <value>90046,90100,90117,90121,90126</value>
        </property>
        <property name="cannotAcquireLockCodes">
            <value>50200</value>
        </property>
        <property name="customTranslations">
            <bean class="org.springframework.jdbc.support.CustomSQLErrorCodesTranslation">
                <property name="errorCodes" value="23001,23505" />
                <property name="exceptionClass"
                          value="com.spring.errorcode.CustomDuplicatedKeyException" />
            </bean>
        </property>
    </bean>
```

# 2、O/R Mapping 实践

## 2.1.认识 Spring Data JPA

### 对象与关系不匹配
|     | Object   | RDBMS |
| :--:     | :--: | :--: |
| 粒度     |  类      | 表   |
| 继承     |  有      | 没有 |
| 唯一性   |  a == b  | 主键 |
| 关联     |  引用    | 外键 |
| 数据访问 |  逐级访问 | 表于表之间的连接 |

### JPA 简介

JPA (``Java Persistence API``) ，Java持久层API，是JDK 5.0注解或XML描述对象－关系表的映射关系，并将运行期的实体对象持久化到数据库中的持久化模型。
JPA 为对象关系映射提供了一种基于 POJO 的持久化模型 ：

**JPA 包括以下3方面的内容**：

- 一套 API 标准。在 javax.persistence 的包下面，用来操作实体对象，执行 CRUD 操作，框架在后台替代我们完成所有的事情，开发者从烦琐的 JDBC 和 SQL 代码中解脱出来。

- 面向对象的查询语言：Java Persistence QueryLanguage（JPQL）。通过面向对象而非面向数据库的查询语言查询数据，避免程序的SQL语句紧密耦合

- ORM（object/relational metadata）元数据的映射。JPA 支持 XML 和 JDK5.0注 解两种元数据的形式，元数据描述对象和表之间的映射关系，框架据此将实体对象持久化到数据库表中。

### Spring Data 
Spring Data 提供一个大家熟悉的、一致的、基于Spring的数据访问编程模型，同时仍然保留底层数据存储的特殊特性。它可以轻松地让开发者使用数据访问技术，包括关系数据库、非关系数据库（NoSQL）和基于云的数据服务。

Spring Data JPA 是 Spring Data 项目中的一个模块，可以理解为 JPA 规范的再次封装抽象。

## 2.2.JPA 中常用注解

### java 对象与数据库字段转化
- @Entity：标识实体类是 JPA 实体，告诉 JPA 在程序运行时生成实体类对应表

- @Table：设置实体类在数据库所对应的表名

- @Id：标识类里所在变量为主键

- @GeneratedValue：设置主键生成策略（依赖于具体的数据库）

- @Column：表示属性所对应字段名进行个性化设置

- @Transient：表示属性并非数据库表字段的映射,ORM框架将忽略该属性

- @Temporal：将日期字段转化成 java.util 包中的时间日期类型。注入数据库的类型有三种：
    - TemporalType.DATE（2008-08-08）
    - TemporalType.TIME（20:00:00）
    - TemporalType.TIMESTAMP（2008-08-08 20:00:00.000000001）

- @Enumerated：使用此注解映射枚举字段，以String类型存入数据库。注入数据库的类型有两种：EnumType.ORDINAL（Interger）、EnumType.STRING（String）

- @Embeddable、@Embedded：　当一个实体类要在多个不同的实体类中进行使用，而其不需要生成数据库表。
    - @Embeddable：注解在类上，表示此类是可以被其他类嵌套
    - @Embedded：注解在属性上，表示嵌套被@Embeddable注解的同类型类

- @ElementCollection：集合映射

- @CreatedDate、@CreatedBy、@LastModifiedDate、@LastModifiedBy：表示字段为创建时间字段（insert自动设置）、创建用户字段（insert自动设置）、最后修改时间字段（update自定设置）、最后修改用户字段（update自定设置）

- @MappedSuperclass：注解的类继承另一个实体类 或 标注@MappedSuperclass类，他可使用@AttributeOverride 或 @AttributeOverrides注解重定义其父类属性映射到数据库表中字段。

### java对象与json转化
- @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")：将Date属性转换为String类型， timezone解决（相差8小时）

- @JsonSerialize：作用在类或字段上，转化java对象到json格式（需自定义转化类继承JsonSerializer<T>）

- @JsonDeserialize：作用在类或字段上，转化json格式到java对象（需自定义转化类继承JsonDeserializer<T>）

- @JsonProperty：作用在属性上，把属性名称序列化为另一个名称（trueName属性序列化为name）

- @JsonIgnoreProperties(ignoreUnknown = true)：作用在类上，忽略掉json数据里包含了实体类没有的字段

- @JsonIgnore：在json序列化时将java bean中的一些属性忽略掉，序列化和反序列化都受影响

## 2.2.Spring Data JPA 中常用注解
通过简单注解来实现精简代码来达到消除冗长代码的目的。

### 常用注解

- ``val``：用在局部变量前面，相当于将变量声明为 final
- ``@NonNull``：给方法参数增加这个注解会自动在方法内对该参数进行是否为空的校验，如果为空，则抛出 NPE（NullPointerException）
- ``@Cleanup``：自动管理资源，用在局部变量之前，在当前变量范围内即将执行完毕退出之前会自动清理资源，自动生成 try-finally 这样的代码来关闭流
- ``@Getter/@Setter``：用在属性上，再也不用自己手写 setter 和 getter 方法了，还可以指定访问范围
- ``@ToString``：用在类上，可以自动覆写 toString 方法，当然还可以加其他参数，例如 @ToString(exclude=”id”) 排除 id 属性，或者 @ToString(callSuper=true, includeFieldNames=true) 调用父类的 toString 方法，包含所有属性
- ``@EqualsAndHashCode``：用在类上，自动生成 equals 方法和 hashCode 方法
- ``@NoArgsConstructor, @RequiredArgsConstructor and @AllArgsConstructor``：用在类上，自动生成无参构造和使用所有参数的构造函数以及把所有 @NonNull 属性作为参数的构造函数，如果指定 staticName = “of” 参数，同时还会生成一个返回类对象的静态工厂方法，比使用构造函数方便很多
- ``@Data``：注解在类上，相当于同时使用了 @ToString、@EqualsAndHashCode、@Getter、@Setter和@RequiredArgsConstrutor 这些注解，对于 POJO 类十分有用
- ``@Value``：用在类上，是 @Data 的不可变形式，相当于为属性添加 final 声明，只提供 getter 方法，而不提供 setter 方法
- ``@Builder``：用在类、构造器、方法上，为你提供复杂的 builder APIs，让你可以像如下方式一样调用 Person.builder().name("Adam Savage").city("San Francisco").job("Mythbusters").job("Unchained Reaction").build(); 更多说明参考 Builder

### Lombok 使用需注意的点

- 在类需要序列化、反序列化时详细控制字段时。
- 在使用lombok 虽然能够省去手动创建 setter 和 getter 方法繁琐，
但是却降低了源代码文件的可读性和完整性，降低了阅读源代码的舒适度。
- 使用 @Slf4 还是 @Log4J 看教程中使用的框架
- 选择合适的地方使用Lombox，例如:POJO ，因为 POJO 比较单纯

### 实现原理
<div align="center"> <img src="../Assets/images/02.lombook-process.png" width="480px"> </div><br>
Lombok 处理流程作用于 Javac 的编译期，在Javac 解析成抽象语法树之后(AST), Lombok 根据自己的注解处理器，动态的修改 AST，增加新的节点(所谓代码)，最终通过分析和生成字节码。（详见参考）

## 2.3.通过 Spring Data JPA 操作数据库
### Repository
- @EnableJpaRepositories ：开启JPA存储库扫描
- Repository<T, ID> 接⼝
    - CrudRepository<T, ID> 
    - PagingAndSortingRepository<T, ID> 
    - JpaRepository<T, ID> 

### 定义查询    
- 根据⽅方法名定义查询
    - find … By … / read … By … / query … By … / get … By … 
    - count … By … 
    - … OrderBy … [Asc / Desc] 
    - And / Or / IgnoreCase 
    - Top / First / Distinct 
- 分⻚页查询
    - PagingAndSortingRepository<T, ID> 
    - Pageable / Sort 
    - Slice<T> / Page<T> 

## 2.4.Repository 是怎么从接⼝口变成 Bean 的

### Repository Bean 是如何创建的
- ``org.springframework.data.jpa.repository.config.JpaRepositoriesRegistrar`` : 激活了 @EnableJpaRepositories ，并返回了 JpaRepositoryConfigExtension 

- ``RepositoryBeanDeﬁnitionRegistrarSupport.registerBeanDeﬁnitions`` ：为每一个 Repository 注册 Repository Bean（类型是 JpaRepositoryFactoryBean ）

- ``RepositoryConﬁgurationExtensionSupport.getRepositoryConﬁgurations`` ：取得所有Repository 配置

- ``JpaRepositoryFactory.getTargetRepository`` : 创建了 Repository 

### 接⼝中的方法是如何被解释的
- ``RepositoryFactorySupport.getRepository`` : 添加了许多 Advice，如：DefaultMethodInvokingMethodInterceptor 、 QueryExecutorMethodInterceptor 

- ``AbstractJpaQuery.execute`` ：执⾏具体的查询

- ``org.springframework.data.repository.query.parser.Part`` : 语法解析

## 2.5.通过 MyBatis 操作数据库
### 简介
[MyBatis](https://github.com/mybatis/mybatis-3) 是一款优秀的持久层框架，支持定制化 SQL、存储过程和高级映射。

在 Spring 中使用 MyBatis :
- [MyBatis Spring Adapter](https://github.com/mybatis/spring) 
- [MyBatis Spring-Boot-Starter](https://github.com/mybatis/spring-boot-starter) 

SQL 较简单用 JPA，SQL复杂用 MyBatis 等框架，大厂 DBA 对 SQL 的要求。

### 简单配置

- mybatis.mapper-locations = classpath*:mapper/**/*.xml 
- mybatis.type-aliases-package = 类型别名的包名
- mybatis.type-handlers-package = TypeHandler 扫描包名
- mybatis.configuration.map-underscore-to-camel-case = true 

### Mapper 的定义与扫描
- @MapperScan 配置扫描位置
- @Mapper 定义接⼝
- 映射的定义—— XML 与注解

## 2.6.官方插件 MyBatis Generator
[MyBatis Generator](http://www.mybatis.org/generator/) 是 MyBatis 官方的代码⽣成器，可以根据数据库表生成相关代码：
- POJO
- Mapper 接⼝
- SQL Map XML

### 运⾏ MyBatis Generator
- 命令行 ：java -jar mybatis-generator-core-x.x.x.jar -conﬁgﬁle generatorConﬁg.xml 

- Maven Plugin（mybatis-generator-maven-plugin）： 
    - mvn mybatis-generator:generate 
    - ${basedir}/src/main/resources/generatorConﬁg.xml
- Java 程序

### 配置 MyBatis Generator
- generatorConﬁguration 
- context  
    - jdbcConnection  
    - javaModelGenerator 
    - sqlMapGenerator 
    - javaClientGenerator （ANNOTATEDMAPPER / XMLMAPPER / MIXEDMAPPER）
    - table 

### 使⽤用⽣成的对象
- 简单操作，直接使⽤生成的 xxxMapper 的方法
- 复杂查询，使⽤生成的 xxxExample 对象
- 如果需要手写微调，使用两套 Mapper 比较好，重新生成不被覆盖。

## 2.7.国产插件  MyBatis PageHelper
### MyBatis PageHepler（https://pagehelper.github.io） 
- ⽀持多种数据库
- ⽀持多种分⻚方式
- SpringBoot ⽀持（https://github.com/pagehelper/pagehelper-spring-boot ）
    - pagehelper-spring-boot-starter

# 3、NoSQL 实践
## 3.1.通过 Docker 辅助开发

### Docker 简介
Linux 容器是一种虚拟化技术，与虚拟机不同的是，他不是模拟一个完整的操作系统，而是对进程进行隔离。或者说，在正常进程的外面套了一个保护层。对于容器里面的进程来说，它接触到的各种资源都是虚拟的，从而实现与底层系统的隔离。

由于容器是进程级别的，相比虚拟机有很多优势：
- 启动快
- 资源占用少
- 体积小

Docker 属于 Linux 容器的一种封装，提供简单易用的容器使用接口。Docker 将应用程序与该程序的依赖，打包在一个文件里面。运行这个文件，就会生成一个虚拟容器。程序在这个虚拟容器里运行，就好像在真实的物理机上运行一样。

### Docker 的主要用途：
- 提供一次性的环境。比如，本地测试他人的软件、持续集成的时候提供单元测试和构建的环境。

- 提供弹性的云服务。因为 Docker 容器可以随开随关，很适合动态扩容和缩容。

- 组建微服务架构。通过多个容器，一台机器可以跑多个服务，因此在本机就可以模拟出微服务架构。

### Docker 的常用命令：
#### 镜像相关 
- docker pull \<image> 
- docker search \<image> 

#### 容器器相关 
- docker run 
- docker start/stop \<容器器名> 
- docker ps \<容器器名> 
- docker logs \<容器器名> 
- docker container rm \<容器器名> 

## 3.2.在Spring 中访问 MongoDB 

### 通过 Docker 启动 MongoDB

1.获取镜像
- ``docker pull mongo`` 

2.运⾏行行 MongoDB 镜像 
 - `` docker run --name mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin -d mongo ``

  - `` docker run --name mongo -p 27017:27017 -v ~/docker-data/mongo:/data/db -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=admin -d mongo ``


3.查看 mongo 容器是否启动成功
- docker ps
- docker ps -a
- docker start mongo
- docker stop mongo

3.登录到 MongoDB 容器器中 
- ``docker exec -it mongo bash`` 

4.通过 Shell 连接 MongoDB 
- ``mongo -u admin -p admin``

### 初始化 MongoDB 的库及权限
- 创建库   
`` use springbucks ``;

- 创建⽤用户  
```Text 
db.createUser(    
        {      
            user: "springbucks",      
            pwd: "springbucks",      
            roles: [         
                {
                    role: "readWrite", 
                    db: "springbucks" 
                }      
            ]    
        }  
    )
```

- show users;
    - use springbucks;
    - show collections;
    - db.coffee.find();

### Spring Data MongoDB 的基本⽤用法

- [MongoDB](https://www.mongodb.com) 是⼀一款开源的文档型数据库。
- Spring 对 MongoDB 的支持 
    - Spring Data MongoDB 
    - MongoTemplate 
    - Repository ⽀持
- 注解 
    - @Document 
    - @Id 
- MongoTemplate 
    - save / remove 
    - Criteria / Query / Update 
-  Repository
    - @EnableMongoRepositories   
    - 对应接口
        - MongoRepository<T, ID> 
        - PagingAndSortingRepository<T, ID> 
        - CrudRepository<T, ID> 

## 3.3.在Spring 中访问 Redis

### 通过 Docker 启动 redis
- 获取镜像 ：docker pull redis 
- 启动 Redis ：docker run --name redis -d -p 6379:6379 redis  

### 
 [Redis](https://redis.io) 是一款开源的内存 K-V 存储，⽀持多种数据结构。

 Spring 对 Redis 的支持，Spring Data Redis：
 - 支持的客户端 Jedis / Lettuce
 - RedisTemplate
 - Repository 支持  

Jedis 客户端：
- Jedis 不是线程安全的
- 通过 JedisPool 获得 Jedis 实例
- 直接使用 Jedis 中的方法

## 3.4.Redis 的哨兵与集群模式
### Redis 的哨兵模式
Redis Sentinel 是 Redis 的一种⾼可用⽅案 
-  监控、通知、⾃自动故障转移、服务发现

JedisSentinelPool 

### Redis 的集群模式
Redis Cluster 
- 数据⾃动分片（分成16384个 Hash Slot ）
- 在部分节点失效时有一定可用性

JedisCluster 
- Jedis 只从 Master 读数据，如果想要⾃动读写分离，可以定制

## 3.5.Spring 的缓存抽象

### 为不同的缓存提供一层抽象 
- 为 Java ⽅法增加缓存，缓存执行结果
- 支持 ConcurrentMap、EhCache、Caﬀeine、JCache（JSR-107）
- 接口
    - org.springframework.cache.Cache 
    - org.springframework.cache.CacheManager 

### 基于注解的缓存
@EnableCaching  
- @Cacheable 
- @CacheEvict 
- @CachePut 
- @Caching 
- @CacheConfig

## 3.6.Redis 在 Spring 中的其他⽤用法

### 与 Redis 建⽴立连接
配置连接⼯厂 
- LettuceConnectionFactory 与 JedisConnectionFactory 
    - RedisStandaloneConfiguration 
    - RedisSentinelConfiguration 
    - RedisClusterConfiguration 

### 读写分离
Lettuce 内置支持读写分离 ：
- 只读主、只读从
- 优先读主、优先读从

LettuceClientConfiguration 、LettucePoolingClientConfiguration 、LettuceClientConfigurationBuilderCustomizer 

### RedisTemplate
RedisTemplate<K, V>  
- opsForXxx() 

StringRedisTemplate

### Redis Repository 的实体注解 
- @RedisHash 
- @Id 
- @Indexed


### 处理不同类型数据源的 Repository 如何区分这些 Repository 
- 根据实体的注解
- 根据继承的接口类型
- 扫描不不同的包

# 4、数据访问进阶



# 参考资料
- [spring-framework-reference](https://docs.spring.io/spring/docs/current/spring-framework-reference/)
- [spring事务管理(详解和实例)](https://www.cnblogs.com/yixianyixian/p/8372832.html)
- [十分钟搞懂 Lombok 使用与原理](https://juejin.im/post/5a6eceb8f265da3e467555fe)
- [docker hub](https://hub.docker.com/)






