sudo nohup sh bin/mqnamesrv &
sudo nohup sh bin/mqbroker -n 127.0.0.1:9876 -c conf/broker.conf autoCreateTopicEnable=true &
./mqadmin updateTopic -n 127.0.0.1:9876 -t stock -c DefaultCluster