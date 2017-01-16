package jobs
import config.Settings._
import models.Activity
import _root_.kafka.serializer.StringDecoder
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import com.datastax.spark.connector.streaming._
import org.apache.spark.SparkContext
import org.apache.spark.sql.cassandra.CassandraSQLContext
import org.apache.spark.streaming._
import com.datastax.spark.connector._
import org.apache.spark
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}

/** Created by Shashi Gireddy on 1/4/17 */
object ProductActivityByReferrerETL {

  def promoEfficiencyJob(sc: SparkContext, duration: Duration): StreamingContext = {
    val ssc = new StreamingContext(sc, duration)
    var fromOffsets: Map[TopicAndPartition, Long] = Map.empty
    val cassandraContext = new CassandraSQLContext(sc)
    val offsetDF = cassandraContext.sql(
      """
        |select topic, partition, max(until_offset) as until_offset
        |from promo.offsets
        |group by topic, partition
      """.stripMargin)
    fromOffsets = offsetDF.rdd.collect().map( o => {
      println(o)
      (TopicAndPartition(o.getAs[String]("topic"), o.getAs[Int]("partition")), o.getAs[Long]("until_offset") + 1)
    }
    ).toMap

    val kafkaDirectStream = fromOffsets.isEmpty match {
      case true =>
        KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
          ssc, kafkaDirectParams, Set(kafkaTopic)
        )
      case false =>
        KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](
          ssc, kafkaDirectParams, fromOffsets, { mmd: MessageAndMetadata[String, String] => (mmd.key(), mmd.message()) }
        )
    }

    //Create Checkpoint
    kafkaDirectStream.checkpoint(Seconds(0))

    //Save Offsets to Cassandra
    kafkaDirectStream.foreachRDD( rdd => {
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      sc.makeRDD[OffsetRange](offsetRanges).saveToCassandra("promo", "offsets") //, SomeColumns("topic", "partition", "fromOffset", "untilOffset")
    })
    //Persist to Cassandra
    val userActivityStream = kafkaDirectStream.transform( input =>
      for {
        (k, v) <- input
        activity <- utils.tryParse[Activity](v)
      } yield activity
    )
    .saveToCassandra("promo", "activities")


    /*** Do Analytics on userActivityStream, maintain state, save snapshots to Cassandra as saving direct stream is impractical**/
    ssc
  }
}