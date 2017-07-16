title： 编译步骤
---

### 1. clone项目

> git clone git@github.com:rongwei84n/admin-manager-proj-demo.git

### 2. 开发和编译环境
* Jdk
* maven
* tomcat
* mysql
* eclipse

### 3. 编译
mvn clean package

### 4. 部署
* copy AdminEAP-web/target/AdminEAP.war到tomcat/webapps/下面，启动tomcat即可。