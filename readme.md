<h1>Let's build a SOLID SHACK and scale it</h1>

This is an attempt to promote SOLID SHACK architecture. Let's build a SOLID SHACK and scale it to enterprise level.

<h4>SOLID refers to design principles https://en.wikipedia.org/wiki/SOLID_(object-oriented_design) </h4>
<h4>SHACK (Scala/Spark, H-Hadoop, A-All things Apache, C-Cassandra, K-Kafka)</h4>
<br />
<b>This is Part II of the series, please check <a href='https://github.com/sgireddy/SHACKSparkBasics'> Part I </a> for implementation plan and our fictitious scenario "optimizing promo efficiency for ISellInstoreAndOnline.com". </b> 
<br />
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
        
9. Create Kafka topic
        
        kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic promo-efficiency
10. Play with Kafka from console <br>
    To produce messages:
    
            kafka-console-producer.sh --broker-list localhost:9092 --topic promo-efficiency
            type what ever you want and press ENTER to send
    Kafka provides two ways to to Consume messages (will provide more details in the coding section) <br />
    I. Receiver approach using ZooKeeper (i.e. ZooKeeper maintains message offsets etc. Easy to use & setup but not efficient & We don't enjoy full control.)
    
            kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning
    II. Direct approach where we provide bootstrap server IP (comma separated list of servers in cluster mode, no need to provide all servers as all Kafka needs is to reach out to one node to get cluster details) 
    
            kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning

11. Create Cassandra Key-Space and Table

        	CREATE KEYSPACE promo
          	WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
          	
    <b>NOTE: Cassandra Cluster is essentially a hash map where data is partitioned (& replicated) on different nodes based on the hash key, the first column in the Primary Key will be used as hash key to partition the data, so its essential to select an appropriate key to get optimal performance & evenly distributed system. </b>
          	    
            create table promo.activities ( 
            time_stamp bigint,
            product_id  bigint,
            user_id bigint,
            referrer    text,
            retail_price bigint,
            product_discount_pct bigint,
            cart_discount_pct bigint,
            action_code bigint,
            margin_pct bigint,
            primary key (product_id, time_stamp)
            )
            with clustering order by (time_stamp desc);
            
12. Clone this repo 
        
        git clone https://github.com/sgireddy/SHACKSparkKafkaCassandra
13. Open project using IntelliJ IDEA (I used CE ) & enjoy coding (will explain critical code in the next section)
 
14. Either you need to start & stop ZooKeeper & Kafka each time or use David's instructions (link provided under references) to setup as services 
        
        kafka-server-stop.sh
        zookeeper-server-stop.sh
        
    
<h4>Critical Code Explained</h4>

<a href='https://spark.apache.org/docs/1.6.1/streaming-kafka-integration.html'> Spark Streaming + Kafka Integration Guide </a> 
 and the <a href='https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/streaming/DirectKafkaWordCount.scala'> 
 direct streaming example </a> is pretty simple and easy to understand (in my opinion as simple as MSMQ)

I am more interested in achieving reliable, resilient & fault tolerance with checkpointing (see spark streaming programming guide for more details) using our getStreamingContext method.
I fumbled many times on checkpointing, kudos to Ahmed Alkilani for explaining this in detail in his course (I strongly suggest his course even if you are an experienced Spark developer, its very well worth the time and money).

Let's take a look at the code first in the order of its execution:

1. SparkConsumer.scala is our entry point, here is how we instantiate our streaming context (ssc) by invoking getStreamingContext helper function from utils config.Contexts. 
Please note, we are passing our streaming handler (ProductActivityByReferrerETL) as an argument.  


            val ssc = config.Contexts
              .getStreamingContext(jobs.ProductActivityByReferrerETL.promoEfficiencyJob, spark, batchDuration)
          
2. Here is our getStreamingContext function: 
See the signature for creatingFunc <i> () => streamingApp(sc, batchDuration) </i> it takes nothing and returns our handler back. 
Now see pattern matching, the streaming context will look into checkpoints and get us an active streaming context if there exists one.   
          
          def getStreamingContext(streamingApp: (SparkContext, Duration) => StreamingContext,
                                  sc: SparkContext,
                                  batchDuration: Duration) = {
            val creatingFunc = () => streamingApp(sc, batchDuration)
            val ssc = sc.getCheckpointDir match {
              case Some(checkpointDir) => StreamingContext.getActiveOrCreate(checkpointDir, creatingFunc, sc.hadoopConfiguration, createOnError = true)
              case None => StreamingContext.getActiveOrCreate(creatingFunc)
            }
            sc.getCheckpointDir.foreach( cp => sc.setCheckpointDir(cp))
            ssc
          }

Now look at our handler job, completely unaware of recovery logic and yet we can take advantage of checkpointing RDD's (to hdfs)
in case if ware doing other aggregations or state management (our next topic) before saving to cassandra (usually saving to cassandra is the last step in the process) 

            def promoEfficiencyJob(sc: SparkContext, duration: Duration): StreamingContext = {
              val ssc = new StreamingContext(sc, duration)
              val topic = KafkaSettings.kafkaTopic
              val kafkaDirectParams = KafkaSettings.kafkaDirectParams
              val kafkaDirectStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
                ssc, kafkaDirectParams, Set(topic)
              ).map(_._2)
          
              val userActivityStream = kafkaDirectStream.transform( input =>
                for {
                  line <- input
                  activity <- utils.tryParse[Activity](line)
                } yield activity
              )
              .saveToCassandra("promo", "activities")
          
              /*** Do Analytics on userActivityStream, maintain state, save snapshots to Cassandra as saving direct stream is impractical**/
              ssc
            }

Up next... Hadoop & State Management

<br />
<br />

<b>References:</b> <br/>
1. Spark Streaming + Kafka Integration Guide <br />
2. Spark Streaming Programming Guide <br /> 
3. Excellent Course on Lambda Architecture by Ahmed Alkilani <a href='https://app.pluralsight.com/library/courses/spark-kafka-cassandra-applying-lambda-architecture'> link </a> <br />
4. How to install Apache on CentOS 7 <a href='https://www.vultr.com/docs/how-to-install-apache-kafka-on-centos-7'>Link</a> <br />
5. David's System Admin Notes <a href='http://davidssysadminnotes.blogspot.com/2016/01/installing-spark-centos-7.html'>Link </a> <br />

