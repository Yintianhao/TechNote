

### Cookie，Session，Token

#### Cookie

Cookie指的是浏览器里面能够永久存储的一种数据，它和服务器是没有关系的，只是浏览器的一种数据存储功能。Cookie由服务器生成，发送给浏览器，浏览器把它存下来，格式是KV格式，下一次请求网站的时候，就会带上cookie给服务器。由于存在浏览器，所以浏览器可以加入一些限制让cookie不会被恶意使用，cookie在磁盘空间上也不会占据太多。

- 客户端设置cookie

```
document.cookie = "name = thyin; age = 12"
```

客户端可以设置cookie的expires，domain，path，secure（只有在https协议的网页中才会生效），但不能设置httpOnly。设置之后，cookie会被添加到request header中。

- 服务端设置cookie

在每一次http请求的回复报文中，报文头中都有一项set-cookie，这个就是服务端用来设置cookie的。一个set-cookie只能设置一个cookie，设置多个需要添加同样的set-cookie，和客户端不同的地方在于，服务端可以设置cookie的所有属性：expires，domain，path，secure，httponly

#### Session

Session，顾名思义会话，比如从小听到大的英语听力中的SessionA，那么Session有啥作用呢，那自然是为了让服务器知道当前的这个请求是来自于谁了，所以服务器需要给每个客户端安排一个身份的标识，即session，然后客户端来访问服务器的时候会带上这个session，这样服务端就能知道当前请求来自于谁了。

##### Session存在的问题和解决办法

单实例的情况下，Session没什么问题，但是在如今都是多实例的情况下，Session就会有一定的问题了，由于负载均衡的原因，每次请求都可能会到达不同的机器上，而不同机器上的session可能不一样，就会有问题，解决的办法一般有：

- Session复制

既然存在一台机器上可能读取不到session的问题，那么在用户登录A服务的时候，把Session复制到B那边不就行了，这就是Session的复制，一个服务器的Session发生改变，那么这个节点就把Session广播到其他节点。这种做法的好处是每个服务器上都能读到Session，但是存在网络延迟的问题，同时如果实例太多的情况下，这并不是一个好的

- Session共享

这种实现的方式就是把Session和服务分开，设置专门的Session存储的地方，可能是一个Redis集群也可能是一个专门的服务，这种做法好处是架构很清晰，但是缺点也存在，那就是存储Session可能存在可用性的问题。

- ip hash

这个办法就是在Nginx层配置ip的hash，也就是说，来自于同一个ip 的请求会一直打到一台机器上，这样的话，就不会存在session的问题，但是缺点也很明显，这样的做法负载均衡就没有多大意义，对于大型系统不适用。

#### token

token就是在用户第一次登陆的时候，服务端给客户端的一个凭证，在这之后请求的时候，都带上这个token，做解密和签名认证，判断请求的有效性。

#### JWT 

JWT由三部分组成，Header，Payload，Signature。所以JWT的形式一般是XXX.YYY.ZZZ。		

- header

是一个json对象，里面字段为algo，表示签名的算法，type表示token的种类

```
{
  "alg": "HS256", // 表示签名的算法，默认是 HMAC SHA256（写成 HS256）
  "typ": "JWT"  // 表示Token的类型，JWT 令牌统一写为JWT
}
```

- payload

是一个json对象，用来存放实际传输的数据，jwt默认不加密，所以重要的隐私信息不能放在payload当中

```
{
  // 7个官方字段
  "iss": "a.com", // issuer：签发人
  "exp": "1d", // expiration time： 过期时间
  "sub": "test", // subject: 主题
  "aud": "xxx", // audience： 受众
  "nbf": "xxx", // Not Before：生效时间
  "iat": "xxx", // Issued At： 签发时间
  "jti": "1111", // JWT ID：编号
  // 可以定义私有字段
  "name": "John Doe",
  "admin": true
}
```

- signature

  对header和payload进行签名，防止数据被篡改。		

  首先需要指定一个密钥，这个密钥只有服务端知道，不能泄露给用户，然后，使用header中的algo所代表的算法，按照公式来进行加密。

  ```
  //algo为算法，默认是HMAC SHA156，secret就是上面的密钥
  algo(base644UrlEncode(header)+"."+base64UrlEncode(payload),secret);
  ```

  这一步会算出签名，将Header，Payload，Signature三个部分拼成一个字符串，返回给用户。

  ```
  JWT = base64(header)+"."+base64(payload)+"."+signature
  ```

  JWT 发送要使用HTTPS，使用HTTP的话，就不要写入隐私数据。同时JWT的payload要设置过期时间，这样才能保证安全。