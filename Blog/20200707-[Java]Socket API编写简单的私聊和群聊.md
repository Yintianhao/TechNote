<!--
 * @Author: Yintianhao
 * @Date: 2020-07-07 19:19:56
 * @LastEditTime: 2020-07-07 19:19:57
 * @LastEditors: Yintianhao
 * @Description: 
 * @FilePath: \TechNote\src\Blog\20200707-[Java]Socket API编写简单的私聊和群聊.md
 * @Copyright@Yintianhao
--> 
# 介绍
## Socket，ServerSocket
Socket就是我们所说的套接字，主要由IP地址和端口来表示，IP即目标服务器的IP地址。ServerSocket主要用在服务端，作用是监听服务器的某一个端口。通过ServerSocket的accept()可以得到一个客户端Socket，再通过输入输出流可以对其进行读写，从而实现服务器和客户端之间的交互。
## 消息格式
既然这里是需要实现私聊和群聊，自然需要规定数据格式，这里我采用JSON的格式来进行数据传输，这样的目的是可以用JSON解析工具(如FastJSON)方便地对消息进行解析，降低代码编写难度提高效率。
## 其他的问题
1，因为需要实现收发信息，而且收和发两个动作是无序的，所以需要收和发两个动作需要单独的线程来进行单独处理。       
2，实现私聊，服务器需要根据用户的唯一标识ID来转发信息，所以需要对Socket再次封装。
# 编写
## 用户对象
主要用来表示用户的基本信息。
```
/**
 * @author yintianhao
 * @createTime 2020/6/30 0:16
 * @description 用户类
 */
public class User {

    private String nickname;
    private Integer id;

    public User(String nickname, Integer id) {
        this.nickname = nickname;
        this.id = id;
    }

    public User(Integer id){
        this.id = id;
    }

    //getter setter 略
}
```
## 消息对象
消息对象包括用户发出的正文内容，消息种类(群聊，私聊，初始化消息)，消息来源(User对象)，消息目的地(User对象)。通过对消息对象和JSON字符串相互转化来实现消息的解析和生成。
```
/**
 * @author yintianhao
 * @createTime 2020/6/30 0:15
 * @description 消息对象
 */
public class Msg {

    //信息内容
    private String content;

    //信息种类，1群聊，2私聊,3初始化消息
    private Integer type;

    private User from;

    private User to;

    public Msg(String content, Integer type, User from, User to){
        this.content = content;
        this.type = type;
        this.from = from;
        this.to = to;
    }
    //getter setter 略
}
```
## 服务端编写
### 管道类
之前说过需要在Socket的基础上进行再次封装，封装成管道类，管道用一个userId来唯一标识，也就是说一个管道可以看成一个用户，在服务器启动后，客户端连接到服务器之后会发送一条初始化信息到服务端，从而在管道内的构造函数以内对初始化信息进行解析，整个管道的初始化到此完成。管道类另外一个作用就是实现读写分离。
```
/**
 * @author yintianhao
 * @createTime 2020/6/28 23:42
 * @description 管道类，实现读写分离。
 */
public class Channel implements Runnable{

    //log
    private static Logger logger = Logger.getLogger(Channel.class);

    //输入输出流
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    //客户端套接字
    private Socket client;

    //运行标志
    private boolean isRunning;

    //用户列表
    private CopyOnWriteArrayList<Channel> all;

    private Integer userId;

    public Channel(Socket client){
        //初始化
        logger.info("A client has connected");
        this.client = client;
        this.isRunning = true;
        try {
            this.dataInputStream = new DataInputStream(client.getInputStream());
            this.dataOutputStream = new DataOutputStream(client.getOutputStream());
        }catch (IOException e){
            logger.info(e.getMessage());
            //异常释放资源
            release();
        }

        //初始化管道id
        String initMsg = receive();
        logger.info("init message -- "+initMsg);
        Msg msg = JSONUtil.getJsonObject(initMsg);
        if (msg.getType() == 3) {
            this.userId = Integer.parseInt(msg.getContent());
        }
    }

    public void setChannelList(CopyOnWriteArrayList<Channel> all){
        this.all = all;
    }

    //接收消息
    private String receive(){
        String msg = "";
        try {
            msg = dataInputStream.readUTF();
            logger.info("received msg -- "+msg);
        }catch (IOException e){
           logger.info(e.getMessage());
            //异常释放资源
            release();
        }
        return msg;
    }

    //发送单条信息
    private void send(String content){
        try {
            logger.info("server transfer -- "+content);
            dataOutputStream.writeUTF(content);
            dataOutputStream.flush();
        }catch (IOException e){
            logger.info(e.getMessage());
            //异常释放资源
            release();
        }
    }

    //群聊
    private void sendOthers(String msg){
        logger.info("Send msg to others");
        logger.info("Channel list size -- "+all.size());
        for (Channel c:all){
            if(c!=this){
                c.send(msg);
            }
        }
    }

    private void sendOne(String content){
        logger.info("Send to someone");
        //转成json对象
        Msg msg = JSON.parseObject(content,Msg.class);

        User from = msg.getFrom();
        User to = msg.getTo();
        logger.info("Message from "+from.getId());
        logger.info("Message to "+to.getId());

        for (Channel c:all){
            try {
                if (c.userId.intValue()==to.getId().intValue()){
                    c.send(content);
                    break;
                }
            }catch (NullPointerException e){
                logger.error(e.getMessage());
            }
        }
    }


    //释放资源
    public void release(){
        //标志改变
        isRunning = false;
        //关闭socket 输入 输出流
        StreamUtil.close(dataInputStream,dataOutputStream,client);
        logger.info("A client has released");
    }

    @Override
    public void run() {
        while(isRunning){
            String content = receive();
            Msg msg = JSON.parseObject(content,Msg.class);
            //通过Msg的type来判断是私聊还是群聊
            if (msg.getType()==1){
                //群聊
                msg.setTo(null);
                String publicMsg = JSONUtil.getJsonString(msg);
                sendOthers(publicMsg);
                logger.info("It is a public message");
            }else if (msg.getType()==2){
                //私聊
                sendOne(content);
                logger.info("It is a private message");
            }else{
                logger.info("It is an initial message");
            }

        }
    }
}
```
### 启动服务器
```
/**
 * @author yintianhao
 * @createTime 2020/6/29 1:16
 * @description
 */
public class Server {
    //logger
    private static Logger logger = Logger.getLogger(Server.class);

    public static void main(String[] args){
        logger.info("Server has started");
        try {
            ServerSocket server = new ServerSocket(8888);
            //创建容器
            CopyOnWriteArrayList<Channel> channelList = new CopyOnWriteArrayList<>();

            while(true){
                //客户端套接字
                Socket client = server.accept();
                //新建一个管道
                Channel channel = new Channel(client);
                //加入管道列表
                channelList.add(channel);
                //管道设置自己的管道列表
                channel.setChannelList(channelList);
                //开启线程服务一个管道
                Thread thread = new Thread(channel);
                thread.start();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}

```
## 客户端编写
客户端的管道有两种，一种是发送管道，一种是接收管道。作用跟服务器的管道类似。由于这个demo是基于控制台的，这里用户的ID和需要发送的消息类型都是根据控制台输入的。          
### 发送管道
```
/**
 * @author yintianhao
 * @createTime 2020/6/29 1:31
 * @description 发送
 */
public class SendChannel implements Runnable{

    //控制台输入
    private BufferedReader console;

    //数据输出流
    private DataOutputStream dos;

    //客户端套接字
    private Socket client;

    //自身身份信息
    private User user;

    private Integer to;

    private boolean isRunning;

    private static Logger logger = Logger.getLogger(SendChannel.class);

    public SendChannel(Socket client,User user,Integer to){
        //初始化
        console = new BufferedReader(new InputStreamReader(System.in));
        this.client = client;
        this.isRunning = true;
        this.user = user;
        this.to = to;
        try {
            dos = new DataOutputStream(client.getOutputStream());
        } catch (IOException e) {
            release();
            logger.error(e.getMessage());
        }
        //发送id初始化管道
        Msg initMsg = new Msg(String.valueOf(user.getId()),3,user,null);
        send(JSONUtil.getJsonString(initMsg));
        logger.info("SendChannel has inited");
    }

    public void release(){
        isRunning = false;
        StreamUtil.close(console,dos,client);
    }

    private String getMsgFromConsole(){
        try {
            String content = console.readLine();
            //通过消息内容分割，strs[0]是消息类型
            String[] strs = content.split("-");
            Msg msg = new Msg(content,Integer.parseInt(strs[0]),user,new User(to));
            return JSON.toJSONString(msg);
        }catch (IOException e){
            logger.error(e.getMessage());
        }
        return "";
    }

    //发送消息
    private void send(String content){
        try {
            dos.writeUTF(content);
            dos.flush();
        }catch (IOException e){
            logger.error(e.getMessage());
            //异常释放资源
            release();
        }
        logger.info("Send -- "+content);
    }

    public User getUser(){
        return user;
    }

    @Override
    public void run() {
        while(isRunning){
            String msg = getMsgFromConsole();
            if (!msg.equals("")){
                send(msg);
            }
        }
    }
}
```
### 接收管道
接收管道比较简单，就是接收然后解析。
```
/**
 * @author yintianhao
 * @createTime 2020/6/29 1:31
 * @description 接收
 */
public class ReceiveChannel implements Runnable {

    private DataInputStream dos;

    private Socket client;

    private boolean isRunning;

    private static Logger logger = Logger.getLogger(ReceiveChannel.class);

    public ReceiveChannel(Socket client){


        this.client = client;
        isRunning = true;
        try {
            dos = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            logger.error(e.getMessage());
            release();
        }
        logger.info("ReceiveChannel has inited");
    }

    private void release(){
        isRunning = false;
        StreamUtil.close(dos,client);
    }

    private String getMsgFromChannel(){
        try {
            String msg = dos.readUTF();
            return msg;
        } catch (IOException e) {
            logger.error(e.getMessage());
            release();
            //e.printStackTrace();
        }
        return "";
    }
    @Override
    public void run() {
        while (isRunning){
            String content = getMsgFromChannel();
            Msg msg = JSON.parseObject(content,Msg.class);
            if (msg!=null){
                logger.info("Message from:"+msg.getFrom().getNickname()+"--"+msg.getContent());
            }
        }
    }
}
```
### 启动客户端
由于这里我没有选择从CMD输入用户昵称，所以我用了三个Client类来模拟客户端，另外两个交Client1,Client2，三个类的区别只是User对象的昵称不同。
```
/**
 * @author yintianhao
 * @createTime 2020/6/28 23:38
 * @description 读写分离，封装
 */
public class Client0 {

    private static Logger logger = Logger.getLogger(Client0.class);
    public static void main(String[] args){
        logger.info("Client0 start,Input your id and other id");
        try {
            Scanner scanner = new Scanner(System.in);
            String str = scanner.next();
            int from = Integer.parseInt(str.split("-")[0]);
            int to = Integer.parseInt(str.split("-")[1]);

            //连接
            Socket client = new Socket("127.0.0.1",8888);
            //发送信息的线程

            User user = new User("Yintianhao",from);

            SendChannel sendChannel = new SendChannel(client,user,to);

            Thread sendThread = new Thread(sendChannel);
            sendThread.start();

            ReceiveChannel receiveChannel = new ReceiveChannel(client);
            Thread receiveThread = new Thread(receiveChannel);
            receiveThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
## 演示
[录了一个小视频](http://media.izzer.cn/im_demo.mp4)

## 总结
其实这个demo虽然私聊群聊是实现了，但是其实是存在许多不足的，比如消息的确认到达机制，消息丢失怎么办，消息的持久化，这些我都没有考虑进去，这阵子我也还在学习这方面的内容，希望以这个demo为开始继续完善吧。