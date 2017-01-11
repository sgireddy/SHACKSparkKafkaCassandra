<h1>Let's build a SOLID SHACK and scale it</h1>

This is an attempt to promote SOLID SHACK architecture. Let's build a SOLID SHACK and scale it to enterprise level.

<h4>SOLID refers to design principles https://en.wikipedia.org/wiki/SOLID_(object-oriented_design) </h4>
<h4>SHACK (Scala/Spark, H-Hadoop, A-All things Apache, C-Cassandra, K-Kafka)</h4>
<br />
<b>This is Part II of the series, please check <a href='https://github.com/sgireddy/SHACKSparkBasics'> Part I </a> for implementation plan and our fictitious scenario "optimizing promo efficiency for ISellInstoreAndOnline.com". </b> 
<br />
 <b> Usage: </b> <br />
  Configure your system, start ZooKeeper & Kafka Services <br />
  Create Cassandra Key Store & activities table <br />
  Change application.conf under resources <br />
  Run "KafkaStreamGenerator" under generators package to generate & feed sample data to Kafka <br />
  Run "SparkConsumer" under consumer package to run spark job & persist to Cassandra <br />
<h3> Module Structure </h3>
Configure Spark, Kafka, and Cassandra on CentOS 7 <br />
Create Kafka Producer and feed our JSON data to Kafka cluster <br />
Consume using Spark Streams and persist to Cassandra <br />
<br />
<b>Next Steps</b> <br />
Configure Hadoop Cluster and persist batch data to hdfs <br />
Spark State Management <br />
Formally define Lambda Architecture <br />
Spark 2.0 and Structured Streaming <br />

Here is a simple Activity class, in this module we will stream it to kafka, receive from Spark and persist to Cassandra.

      case class Activity(
                         timestamp: Long,
                         productId: Int,
                         userId: Int,
                         referrer: String,
                         retailPrice: Int,
                         productDiscountPct: Int,
                         cartDiscountPct: Int,
                         actionCode: Int,
                         marginPct: Int
                         )

<h4> System Setup </h4>

1. Install latest Java from Oracle or OpenJDK. I used JDK 1.8.112 from Oracle. <br />
2. Download Spark

        wget http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz
        tar -xvzf spark-2.1.0-bin-hadoop2.7.tgz
        sudo mv spark-2.1.0-bin-hadoop2.7 /opt/spark
3. Download Kafka
        
        wget http://apache.mesi.com.ar/kafka/0.10.1.1/kafka_2.11-0.10.1.1.tgz
        tar -xvzf kafka_2.11-0.10.1.1.tgz
        sudo mv kafka_2.11-0.10.1.1 /opt/kafka
        
4. Download and Install Cassandra from datastax website: http://docs.datastax.com/en/cassandra/3.0/cassandra/install/installRHEL.html        
5. create a script sudo vi /etc/profile.d/spark-kafka-conf.sh (I prefer not updating /etc/profile directly, you may if you prefer), Please identify your JDK & JRE install paths and replace accordingly

        export JAVA_HOME JRE_HOME SPARK_HOME KAFKA_HOME
        
        JAVA_HOME=/usr/java/default
        JRE_HOME=/usr/java/default/jre
        SPARK_HOME=/opt/spark
        KAFKA_HOME=/opt/kafka
        PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$KAFKA_HOME/bin
        
6. Update Firewall Rules & reboot or invoke "source /etc/profile" to load updated configuration

        firewall-cmd --permanent --add-port=2181/tcp
        firewall-cmd --permanent --add-port=9092/tcp
        firewall-cmd --permanent --add-port=9160/tcp
        firewall-cmd --permanent --add-port=9042/tcp
        firewall-cmd --reload
7. start ZooKeeper server (comes with Kafka, you may install or use standalone instance)

        zookeeper-server-start.sh -daemon $KAFKA_HOME/config/zookeeper.properties
8. Start Kafka Server
        
        kafka-server-start.sh $KAFKA_HOME/config/server.properties
9. Either you need to start & stop ZooKeeper each time or use David's instructions below to setup as services 
        
        kafka-server-stop.sh
        zookeeper-server-stop.sh
        
    
<h4>Critical Code Explained</h4>
Comming soon ... in few hours...
<br />
<br />

<b>References:</b>
1. Spark Streaming + Kafka Integration Guide
2. Excellent Course on Lambda Architecture by Ahmed Alkilani <a href='https://app.pluralsight.com/library/courses/spark-kafka-cassandra-applying-lambda-architecture'> link </a>
3. How to install Apache on CentOS 7 <a href='https://www.vultr.com/docs/how-to-install-apache-kafka-on-centos-7'>Link</a>
4. David's System Admin Notes <a href='http://davidssysadminnotes.blogspot.com/2016/01/installing-spark-centos-7.html'>Link </a>

