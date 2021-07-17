## Bean作用域

- singleton

在spring IoC容器中仅存在一个Bean实例，Bean以单例形式存在，bean的作用域范围的默认值。

- prototype

每次从容器调用bean的时候，都返回一个新的实例。

- request

每次http请求都会创建一个新的bean，该作用域仅适用于web的spring WebApplicationContext的环境。

- session

同一个http session共享一个bean，不同session使用不同的bean，该作用域仅仅适用于web的Spring WebApplicationContext的环境。

- application

限定一个bean的作用域为ServletContext的生命周期，该作用域也仅仅适用于Spring WebApplicationContext环境。

