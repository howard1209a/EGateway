# EGateway配置文档

## 路由配置

下面是配置一个后端服务路由的例子

```yaml
routes:
  - id: static-web # 路由id
    addresses:
      - name: address1 # 地址名称
        ip: localhost # 后端ip
        port: 12091 # 后端port
      - name: address2
        ip: localhost
        port: 12092
    predicates: # 路由断言，按顺序逐一匹配
      - /c
      - /static/**
```

## 负载均衡

实现了基本的权重轮询负载均衡器和哈希负载均衡器，自定义负载均衡器可以区实现Dispatcher接口

- 权重轮询负载均衡器：按照address的权重比例将请求转发到后端
    ```yaml
    routes:
      - id: static-web
        addresses:
          - name: address1
            ip: localhost
            port: 12091
            weight: 1000 # address1权重1000
          - name: address2
            ip: localhost
            port: 12092
            weight: 500 # address2权重500
        predicates:
          - /c
          - /static/**
        loadBalance: polling # 权重轮询负载均衡器
    ```

- 哈希负载均衡器：按照请求源ip哈希取余后转发到后端
    ```yaml
    routes:
      - id: user-service
        addresses:
          - name: address1
            ip: localhost
            port: 12091
          - name: address2
            ip: localhost
            port: 12092
        predicates:
          - /user/**
        loadBalance: hash # 哈希负载均衡器
    ```

## 持久连接

- 请求头携带了Connection字段
    - keep-alive：支持持久连接
    - close：不支持持久连接
- 请求头未携带Connection字段
    - HTTP1.1协议：支持持久连接
    - HTTP1.0协议：不支持持久连接

下面是配置持久连接的一个例子：如果某次请求建立了一个持久连接，那么服务端会每隔20s检查一次这段时间内是否产生了新的请求，如果没有产生新的请求则会主动断开连接。同时当一个持久连接累计处理3个请求后，服务端也会主动断开连接。

```yaml
protocol:
  downstream:
    timeout: 20 # 超时时间
    max: 3 # 最大请求数
```

## 请求缓存

你可以为不同的路由配置不同的缓存策略，当你不给某个路由配置缓存时，则此路由的请求不会经历缓存。

### 机制介绍

#### 请求的唯一标识key

一个HTTP请求的请求类型+资源路径+请求参数/payload拼接成一个字符串，通过md5哈希成一个32位的十六进制数字，这个数字既是唯一标识请求的key，也是请求缓存文件的文件名。

#### 什么时候会经过缓存机制

请求不会经过缓存机制：

- 客户端请求携带了forceNotCache请求头
- 客户端请求未携带forceNotCache请求头，但该请求对应路由没有配置缓存
- 客户端请求未携带forceNotCache请求头，该请求对应路由配置了缓存，但前一秒的请求频率小于等于minuse

请求不会经过缓存机制：

- 客户端请求未携带forceNotCache请求头，该请求对应路由配置了缓存，前一秒的请求频率大于minuse

如果你想让minuse不再发挥任何作用，可以把它设置成-1

缓存机制中防止了缓存击穿，保证唯一缓存重建。

#### 缓存文件目录结构

均匀地目录树可以提高存储和查找效率，参考了nginx的实现

#### 如何统计每个请求key的前一秒请求频率

每个出现过的请求key都会映射一个队列，队列中存储了前10个时间片（每个时间片长度100ms）的片内请求数目，以及当前时间片的片内请求数目。每次获取请求频率都会返回前十个时间片内的请求数总和，定时任务会每隔100ms执行一次，清除队头时间片并在队尾添加一个新的时间片。

### 配置示例

下面是一个配置请求缓存的例子，我们为static-web这个路由配置缓存。

- path：设置路由缓存的磁盘根目录，每个请求将会以一个文件的形式存储在目录内。
- level：设置缓存目录的层级结构，1/2代表以根目录内有16个文件夹，每个文件夹内有16*16个文件夹，所有的缓存文件将会均匀地分布到叶子文件夹内。
- metadata：设置是否开启元数据，如果不开启元数据，则查找缓存操作将遍历整个路由缓存根目录，如果开启了元数据，则会在内存存储一个路径映射，查找缓存操作可以直接找到对应缓存文件。
- expireTime：设置缓存失效时间，单位是秒，任何缓存都将在它创建的10s后失效，失效会清除磁盘缓存文件和内存元数据。
- statusHeader：设置是否开启响应头标识，如果开启，则响应中会携带cacheState响应头。
    - MISS：因为某些原因导致此请求不经过缓存机制
    - HIT：命中缓存，此次响应拿到的是缓存中的结果
    - EXPIRED：缓存过期，本次响应是一次缓存重建的结果
- minuse：设置请求频率下限，服务端会统计前一秒的请求频率，当请求频率大于minuse时，本次请求才会经过缓存机制

```yaml
routes:
  - id: static-web
    addresses:
      - name: address1
        ip: localhost
        port: 12091
        weight: 1000
      - name: address2
        ip: localhost
        port: 12092
        weight: 500
    predicates:
      - /c
      - /static/**
    loadBalance: polling
    cache: # 请求缓存配置
      path: F:/codes/EGateway/cache/route1 # 缓存磁盘路径
      level: 1/2 # 缓存路径结构
      metadata: false # 是否开启内存元数据
      expireTime: 10 # 缓存失效时间
      statusHeader: true # 是否开启响应头标识
      minuse: 1 # 开启缓存的请求频率下限，设置成-1可以使minuse失效
```
