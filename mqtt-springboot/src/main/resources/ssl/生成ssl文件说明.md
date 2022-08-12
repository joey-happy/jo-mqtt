1. 参考文献
   1. EMQX MQTT 服务器启用 SSL/TLS 安全连接 
      1. https://www.emqx.com/zh/blog/emqx-server-ssl-tls-secure-connection-configuration-guide
   2. 使用openssl创建https证书 
      1. https://cloud.tencent.com/developer/article/1548350
   3. OpenSSL 1.0.0生成p12、jks、crt等格式证书的命令个过程 
      1. https://www.cnblogs.com/bluestorm/p/3155945.html
   4. Converting PEM-format keys to JKS format
      1. https://docs.oracle.com/cd/E35976_01/server.740/es_admin/src/tadm_ssl_convert_pem_to_jks.html
2. 安装openssl
3. 操作步骤
   1. 根证书
      1. 生成ca私钥 
         1. openssl genrsa -out ca.key 2048
      2. 生成ca根证书 
         1. openssl req -x509 -new -nodes -key ca.key -sha256 -days 3650 -out ca.pem
            1. common name 输入ca-mqtt.com 其他地方都输入BJ 
            2. 我们在生成ca根证书时，Common Name 最好是有效根域名(如 sztech.com ),  
               并且不能和后来服务器证书签署请求文件中填写的 Common Name 完全一样，否则会
               导致证书生成的时候出现 error 18 at 0 depth lookup:self signed certificate
      3. 导出ca.p12格式根证书
         1. openssl pkcs12 -export -clcerts -in ca.pem -inkey ca.key -password pass:mqtt -out ca.p12
            1. 密码：mqtt
         2. 生成ca.jks文件
            1. keytool -importkeystore -srckeystore ca.p12 -srcstoretype pkcs12 -destkeystore ca.jks -deststoretype jks
               1. 前两次密码：123456 最后一次密码：mqtt
         3. 查看秘钥库详细信息
            1. keytool -keystore ca.jks -list -v
      4. 生成ca.jks文件
         1. keytool -import -v -trustcacerts -keypass jo-mqtt -storepass 123456 -alias root -file ca.pem -keystore ca.jks
            1. 是否信任此证书? [否]:  是
         2. 检查别名配置
            1. keytool -keystore ca.jks -list -alias root
         3. 查看秘钥库详细信息
            1. keytool -keystore ca.jks -list -v
   2. 服务器证书
      1. 生成server私钥
         1. openssl genrsa -out server.key 2048
      2. 生成server证书申请文件
         1. openssl req -new -key server.key -out server.csr
            1. common name 输入server-mqtt.com 其他地方都输入BJ 密码输入忽略 直接回车即可
      3. 根证书来签发实体证书
         1. openssl x509 -req -in server.csr -CA ca.pem -CAkey ca.key -CAcreateserial -out server.pem -days 3650 -sha256 -extensions v3_req
      4. 验证实体证书
         1. openssl verify -CAfile ca.pem server.pem
      5. 导出server.p12格式证书
         1. openssl pkcs12 -export -clcerts -in server.pem -inkey server.key -password pass:mqtt -out server.p12
            1. 密码：mqtt
         2. 生成ca.jks文件
            1. keytool -importkeystore -srckeystore server.p12 -srcstoretype pkcs12 -destkeystore server.jks -deststoretype jks
               1. 前两次密码：123456 最后一次密码：mqtt
         3. 查看秘钥库详细信息
            1. keytool -keystore server.jks -list -v
      6. 生成server.jks文件
         1. keytool -import -v -keypass jo-mqtt -storepass 123456 -alias server -file server.pem -keystore server.jks
            1. 是否信任此证书? [否]:  是
         2. 检查别名配置
            1. keytool -keystore server.jks -list -alias server
               1. 结果：server, 2022-8-12, trustedCertEntry,
   3. 客户端证书
      1. 生成client私钥
         1. openssl genrsa -out client.key 2048
      2. 生成client证书申请文件
         1. openssl req -new -key client.key -out client.csr
            1. common name 输入client-mqtt.com 其他地方都输入BJ 密码输入忽略 直接回车即可
      3. 根证书来签发实体证书 
         1. openssl x509 -req -in client.csr -CA ca.pem -CAkey ca.key -CAcreateserial -out client.pem -days 3650 -sha256 -extensions v3_req
      4. 验证实体证书 
         1. openssl verify -CAfile ca.pem client.pem
      5. 导出client.p12格式证书
         1. openssl pkcs12 -export -clcerts -in client.pem -inkey client.key -password pass:mqtt -out client.p12
            1. 密码：mqtt
         2. 生成ca.jks文件
            1. keytool -importkeystore -srckeystore client.p12 -srcstoretype pkcs12 -destkeystore client.jks -deststoretype jks
               1. 前两次密码：123456 最后一次密码：mqtt
         3. 查看秘钥库详细信息
            1. keytool -keystore client.jks -list -v
      6. 生成client.jks文件
         1. keytool -import -v -keypass jo-mqtt -storepass 123456 -alias client -file client.pem -keystore client.jks
            1. 是否信任此证书? [否]:  是
         2. 检查别名配置
            1. keytool -keystore client.jks -list -alias client

