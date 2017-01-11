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
Comming soon ... in few hours...
<h4>Critical Code Explained</h4>
Comming soon ... in few hours...
<br />
<br />

<b>References:</b>
1. Spark Streaming + Kafka Integration Guide
2. Excellent Course on Lambda Architecture by Ahmed Alkilani <a href='https://app.pluralsight.com/library/courses/spark-kafka-cassandra-applying-lambda-architecture'> link </a>
3. How to install Apache on CentOS 7 <a href='https://www.vultr.com/docs/how-to-install-apache-kafka-on-centos-7'>Link</a>
4. David's System Admin Notes <a href='http://davidssysadminnotes.blogspot.com/2016/01/installing-spark-centos-7.html'>Link </a>