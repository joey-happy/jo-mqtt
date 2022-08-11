1. 安装openssl
2. 参考：https://www.emqx.com/zh/blog/emqx-server-ssl-tls-secure-connection-configuration-guide https://cloud.tencent.com/developer/article/1548350
   1. 生成ca私钥 
      1. openssl genrsa -out ca.key 2048
   2. 生成ca根证书 
      1. openssl req -x509 -new -nodes -key ca.key -sha256 -days 3650 -out ca.pem
         1. common name 输入jo-mqtt.com 其他地方都输入BJ 
         2. 我们在生成ca根证书时，Common Name 最好是有效根域名(如 sztech.com ),  
            并且不能和后来服务器证书签署请求文件中填写的 Common Name 完全一样，否则会
            导致证书生成的时候出现
   3. 生成服务器私钥
      1. openssl genrsa -out server.key 2048
   4. 生成服务器证书申请文件
      1. openssl req -new -key server.key -out server.csr
         1. common name 输入test-mqtt.com 其他地方都输入BJ 密码输入忽略 直接回车即可
   5. 根证书来签发实体证书 openssl x509 -req -in server.csr -CA ca.pem -CAkey ca.key -CAcreateserial -out server.pem -days 3650 -sha256 -extensions v3_req
   6. 验证实体证书 openssl verify -CAfile ca.pem server.pem
